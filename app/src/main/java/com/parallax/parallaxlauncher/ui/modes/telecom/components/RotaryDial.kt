package com.parallax.parallaxlauncher.ui.modes.telecom.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.parallax.parallaxlauncher.core.haptics.HapticEngine
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private const val STOPPER_ANGLE = 140f
private val DIGIT_LIST = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')

@Composable
fun RotaryDial(
    modifier: Modifier = Modifier,
    haptics: HapticEngine,
    onDigitDialed: (Char) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val rotationAnim = remember { Animatable(0f) }
    var currentHoleIndex by remember { mutableIntStateOf(-1) }
    var startDragAngle by remember { mutableFloatStateOf(0f) }
    var baseWheelAngle by remember { mutableFloatStateOf(0f) }

    fun restingAngleFor(digitIndex: Int): Float {
        // Digit 1 is at 100°, each subsequent digit is stepped counter-clockwise by 28°
        return 100f - digitIndex * 28f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.width * 0.40f
                            val holeRadius = size.width * 0.08f
                            val vec = offset - center
                            val dist = hypot(vec.x, vec.y)

                            // Check if touch landed near the hole ring
                            if (dist in (radius - holeRadius * 1.5f)..(radius + holeRadius * 1.5f)) {
                                val touchAngle = (Math.toDegrees(atan2(vec.y.toDouble(), vec.x.toDouble())).toFloat() + 360f) % 360f
                                for (i in DIGIT_LIST.indices) {
                                    val hAngle = (restingAngleFor(i) + 360f) % 360f
                                    val diff = kotlin.math.abs(touchAngle - hAngle)
                                    val circularDiff = kotlin.math.min(diff, 360f - diff)
                                    if (circularDiff < 16f) {
                                        currentHoleIndex = i
                                        startDragAngle = touchAngle
                                        baseWheelAngle = rotationAnim.value
                                        haptics.click()
                                        break
                                    }
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            if (currentHoleIndex != -1) {
                                change.consume()
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val vec = change.position - center
                                val touchAngle = (Math.toDegrees(atan2(vec.y.toDouble(), vec.x.toDouble())).toFloat() + 360f) % 360f

                                var delta = touchAngle - startDragAngle
                                if (delta < -180f) delta += 360f
                                if (delta > 180f) delta -= 360f

                                val rest = restingAngleFor(currentHoleIndex)
                                // Maximum clockwise rotation is distance from hole resting angle to stopper
                                val maxClockwise = ((STOPPER_ANGLE - rest) + 360f) % 360f
                                val targetRotation = (baseWheelAngle + delta).coerceIn(0f, maxClockwise)

                                scope.launch {
                                    rotationAnim.snapTo(targetRotation)
                                }
                            }
                        },
                        onDragEnd = {
                            if (currentHoleIndex != -1) {
                                val dialedDigit = DIGIT_LIST[currentHoleIndex]
                                val rest = restingAngleFor(currentHoleIndex)
                                val maxClockwise = ((STOPPER_ANGLE - rest) + 360f) % 360f
                                val currentRot = rotationAnim.value

                                // If pulled at least 70% toward the stopper, consider it a full dial
                                val reachedStopper = currentRot >= maxClockwise * 0.72f

                                scope.launch {
                                    val returnTime = ((currentRot / 360f) * 450).toInt().coerceAtLeast(180)
                                    rotationAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(returnTime)
                                    )
                                    if (reachedStopper) {
                                        haptics.thud()
                                        onDigitDialed(dialedDigit)
                                    }
                                }
                                currentHoleIndex = -1
                            }
                        },
                        onDragCancel = {
                            currentHoleIndex = -1
                            scope.launch { rotationAnim.animateTo(0f) }
                        }
                    )
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width * 0.46f
            val holeRingRadius = size.width * 0.35f
            val holeRadius = size.width * 0.065f

            // 1. Outer Bakelite Bezel / Housing
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2E2E32), Color(0xFF141416), Color(0xFF09090A)),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFF4A4B50),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 4f)
            )

            // 2. Inner Dial Base / Number Plate (Stationary numbers)
            val innerBaseRadius = size.width * 0.42f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFF7F4EB), Color(0xFFE2DDD1)),
                    center = center,
                    radius = innerBaseRadius
                ),
                radius = innerBaseRadius,
                center = center
            )

            // Draw Stationary Numbers (1..9, 0) and small letters underneath
            for (i in DIGIT_LIST.indices) {
                val angleDeg = restingAngleFor(i)
                val rad = Math.toRadians(angleDeg.toDouble())
                val numX = center.x + (holeRingRadius * cos(rad)).toFloat()
                val numY = center.y + (holeRingRadius * sin(rad)).toFloat()

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = size.width * 0.055f
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                    }
                    drawText(DIGIT_LIST[i].toString(), numX, numY + (paint.textSize * 0.35f), paint)
                }
            }

            // 3. Rotating Finger Wheel (Draw rotated by current rotationAnim)
            rotate(degrees = rotationAnim.value, pivot = center) {
                // Transparent acrylic face with specular ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x33FFFFFF), Color(0x11FFFFFF), Color(0x44000000)),
                        center = center,
                        radius = innerBaseRadius
                    ),
                    radius = innerBaseRadius,
                    center = center
                )

                // Cutout Finger Holes with bevel
                for (i in DIGIT_LIST.indices) {
                    val angleDeg = restingAngleFor(i)
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val hx = center.x + (holeRingRadius * cos(rad)).toFloat()
                    val hy = center.y + (holeRingRadius * sin(rad)).toFloat()

                    // Clear transparent hole with dark rim
                    drawCircle(
                        color = Color(0x22000000),
                        radius = holeRadius,
                        center = Offset(hx, hy)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x88FFFFFF), Color(0x33888888)),
                            center = Offset(hx, hy),
                            radius = holeRadius
                        ),
                        radius = holeRadius,
                        center = Offset(hx, hy),
                        style = Stroke(width = 3f)
                    )
                }

                // Chrome/Bakelite Center Wheel Cap
                val capRadius = size.width * 0.16f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFE8E8E8), Color(0xFF888A8E), Color(0xFF222325)),
                        center = center,
                        radius = capRadius
                    ),
                    radius = capRadius,
                    center = center
                )
                drawCircle(
                    color = Color(0xFFC49A45), // Brass accent ring
                    radius = capRadius * 0.75f,
                    center = center,
                    style = Stroke(width = 4f)
                )
            }

            // 4. Fixed Chrome Finger Stop (Stopper at ~140°)
            val stopRad = Math.toRadians(STOPPER_ANGLE.toDouble())
            val stopTip = Offset(
                center.x + (holeRingRadius * 1.05f * cos(stopRad)).toFloat(),
                center.y + (holeRingRadius * 1.05f * sin(stopRad)).toFloat()
            )
            val stopBase = Offset(
                center.x + (outerRadius * 0.98f * cos(stopRad)).toFloat(),
                center.y + (outerRadius * 0.98f * sin(stopRad)).toFloat()
            )

            drawLine(
                brush = Brush.linearGradient(listOf(Color(0xFFE0E0E0), Color(0xFF7E8085))),
                start = stopBase,
                end = stopTip,
                strokeWidth = 14f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color(0xFFC0A050), // Brass pin
                radius = 7f,
                center = stopTip
            )
        }
    }
}
