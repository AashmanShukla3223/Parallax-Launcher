package com.parallax.parallaxlauncher.ui.media

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** One item in a media library. */
data class MediaItem(
    val id: Long,
    val uri: Uri,
    val title: String,
    val subtitle: String,
    val mime: String,
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val dateAddedSec: Long = 0L,
) {
    /** Rough "2:05" or "3:41" label. Music and video only. */
    fun durationLabel(): String? {
        if (durationMs <= 0L) return null
        val total = durationMs / 1000
        val m = total / 60
        val s = total % 60
        return "%d:%02d".format(m, s)
    }
}

/** The tabs the media browser offers. */
enum class MediaKind(val label: String) {
    ALL("ALL"),
    AUDIO("MUSIC"),
    VIDEO("MOVIES"),
    PHOTOS("PHOTOS"),
    GIFS("GIFS"),
}

/**
 * Reads the device's indexed media.
 *
 * Deliberately MediaStore only. This does not walk the filesystem and does not
 * request all-files access, so it cannot see documents, downloads or anything
 * else Android has not indexed -- which is the trade the app made on purpose.
 *
 * Every query runs on Dispatchers.IO and every cursor is closed in a finally,
 * because these are user-facing screens and a leaked cursor here shows up as the
 * whole app stalling later.
 */
class MediaRepository(private val context: Context) {

    suspend fun load(kind: MediaKind, limit: Int = 400): List<MediaItem> =
        withContext(Dispatchers.IO) {
            val out = ArrayList<MediaItem>(limit)
            kindsFor(kind).forEach { (collection, selection, kindLabel) ->
                runCatching {
                    query(collection, selection, limit, kindLabel, out)
                }
            }
            // Newest first, so a recents-first browser is the default without a
            // separate code path per collection.
            out.sortedByDescending { it.dateAddedSec }.take(limit)
        }

    // EXTERNAL_CONTENT_URI is a Uri, not a String. Declaring the field as String
    // looks harmless until the compiler rejects every call site.
    private data class Spec(
        val collection: Uri,
        val selection: String?,
        val kindLabel: String,
    )

    private fun kindsFor(kind: MediaKind): List<Spec> {
        val audio = Spec(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, "AUDIO")
        val video = Spec(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, "VIDEO")
        val images = Spec(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Images.Media.MIME_TYPE} LIKE ?", "IMAGE")
        val gifs = Spec(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "${MediaStore.Images.Media.MIME_TYPE} = ?",
            "GIF",
        )
        return when (kind) {
            MediaKind.ALL -> listOf(audio, video, images)
            MediaKind.AUDIO -> listOf(audio)
            MediaKind.VIDEO -> listOf(video)
            MediaKind.PHOTOS -> listOf(images)
            // Kept as its own tab: a GIF is still an image, so folding it into
            // PHOTOS would bury it among screenshots and camera rolls.
            MediaKind.GIFS -> listOf(gifs)
        }
    }

    private fun query(
        collection: Uri,
        selection: String?,
        limit: Int,
        kindLabel: String,
        out: MutableList<MediaItem>,
    ) {
        val isImage = kindLabel == "IMAGE" || kindLabel == "GIF"

        // Images have no DURATION column at all, so asking for one by name makes
        // the query throw. Build the projection per kind and look columns up by
        // name rather than by position.
        //
        // The shared column names live on MediaColumns: MediaStore.Media's copies
        // are gone from the SDK and referencing them does not resolve.
        val projection = if (kindLabel == "AUDIO") {
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED,
            )
        } else if (kindLabel == "VIDEO") {
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.ARTIST,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
            )
        } else {
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED,
            )
        }

        val args = when (kindLabel) {
            "GIF" -> arrayOf("image/gif")
            "IMAGE" -> arrayOf("image/%")
            else -> null
        }

        // Raw column names. MediaStore.Media's constants are gone from the SDK and
        // MediaColumns does not resolve either, and these names are the part of
        // MediaStore that is genuinely stable and public. The per-kind
        // projections above keep the typed constants, which do resolve.
        val cursor: Cursor? = context.contentResolver.query(
            collection, projection, selection, args,
            "date_added DESC LIMIT $limit",
        )
        cursor?.use { c ->
            fun col(name: String) = c.getColumnIndex(name)
            val idCol = col("_id")
            val titleCol = col("title")
            val subCol = c.getColumnIndex(if (isImage) "display_name" else "artist")
            val mimeCol = col("mime_type")
            // duration only exists on the audio and video tables.
            val durCol = if (isImage) -1 else col("duration")
            val sizeCol = col("_size")
            val dateCol = col("date_added")

            if (idCol < 0 || titleCol < 0) return

            while (c.moveToNext() && out.size < limit) {
                val id = c.getLong(idCol)
                val rawTitle = c.getString(titleCol)?.trim().orEmpty()
                val sub = if (subCol >= 0) c.getString(subCol)?.trim().orEmpty() else ""
                val mime = if (mimeCol >= 0) {
                    c.getString(mimeCol)?.trim().orEmpty().ifEmpty { "*/*" }
                } else "*/*"
                val duration = if (durCol >= 0) c.getLong(durCol) else 0L
                val size = if (sizeCol >= 0) c.getLong(sizeCol) else 0L

                out += MediaItem(
                    id = id,
                    uri = ContentUris.withAppendedId(collection, id),
                    // MediaStore hands back a literal "<unknown>" often enough
                    // to matter, and it would otherwise show as the filename.
                    title = rawTitle.takeUnless {
                        it.isEmpty() || it.equals("<unknown>", true)
                    } ?: "Untitled",
                    subtitle = sub.takeUnless {
                        it.isEmpty() || it.equals("<unknown>", true)
                    } ?: kindLabel.lowercase().replaceFirstChar { it.uppercase() },
                    mime = mime,
                    durationMs = duration,
                    sizeBytes = size,
                    dateAddedSec = if (dateCol >= 0) c.getLong(dateCol) else 0L,
                )
            }
        }
    }

    companion object {
        /**
         * The three runtime permissions media browsing needs, by API level.
         *
         * API 33 split READ_EXTERNAL_STORAGE into three granular permissions and
         * the old one is no longer granted at all, so asking for it on a modern
         * device silently returns denied.
         */
        fun permissionsFor(sdk: Int): Array<String> =
            if (sdk >= 33) {
                arrayOf(
                    "android.permission.READ_MEDIA_IMAGES",
                    "android.permission.READ_MEDIA_VIDEO",
                    "android.permission.READ_MEDIA_AUDIO",
                )
            } else {
                arrayOf("android.permission.READ_EXTERNAL_STORAGE")
            }
    }
}