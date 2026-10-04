package com.parallax.parallaxlauncher.ui.modes.razr

/**
 * SIM operator names as a 2G handset actually displays them.
 *
 * ## Why this exists
 *
 * A GSM/2G phone does not show the consumer brand. It shows the *network*
 * operator name carried in the SIM's PLMN, and for a lot of today's brands that
 * name is the company the brand was spun out of, rebranded, or bought. So a
 * subscriber on Jio would have seen "Reliance Communications" on a 2G phone,
 * and a Hutch-era SIM shows "Hutch" rather than "Vi".
 *
 * Getting this right is the difference between a launcher that looks like it is
 * running the handset it depicts and one that looks like a modern phone with a
 * retro skin.
 *
 * ## Provenance — please read before editing
 *
 * [Provenance.USER] entries were specified by hand and are authoritative.
 * [Provenance.RESEARCHED] entries come from documented corporate lineage and
 * the operators' own published network naming. They are marked separately
 * precisely so they can be reviewed: MCC/MNC operator names are per-country and
 * change, so treat this list as a starting point rather than gospel.
 */

/**
 * Where a given row's 2G network name came from.
 */
enum class Provenance {
    /** Supplied directly; treat as authoritative. */
    USER,

    /** Derived from documented corporate lineage / published network naming. */
    RESEARCHED,
}

/**
 * One operator's identity.
 *
 * @param brand      the modern consumer-facing brand name, as printed on the SIM
 *                   and in current settings. This is what a 4G/5G handset shows.
 * @param network2G  the operator name a 2G/GSM handset puts on the status strip.
 * @param country    ISO 3166-1 alpha-2 where known.
 * @param mcc        Mobile Country Code, where known. 404 is India.
 * @param provenance see [Provenance].
 */
data class RazrOperator(
    val brand: String,
    val network2G: String,
    val country: String,
    val mcc: String?,
    val provenance: Provenance,
) {
    /** Longest display name the 176px panel can legibly carry. */
    val compact2G: String
        get() = if (network2G.length <= 12) network2G else network2G.take(11) + "…"
}

/**
 * The database of SIM providers and their 2G-era network names.
 *
 * Matching is deliberately loose: SIMs report wildly inconsistent operator
 * strings ("Airtel", "AIRTEL", "Bharti Airtel", "airtel"), so [resolveFor2G]
 * matches case-insensitively on any significant word of the reported name
 * rather than requiring an exact hit.
 */
object RazrOperators {

    /**
     * India. These four were specified directly and are the reason this file
     * exists; note that "Reliance Communications" and "Hutch" are the genuine
     * pre-2016-era 2G network names behind Jio and Vi respectively.
     */
    val india: List<RazrOperator> = listOf(
        RazrOperator(
            brand = "Jio",
            network2G = "Reliance Communications",
            country = "IN",
            mcc = "404",
            provenance = Provenance.USER,
        ),
        RazrOperator(
            brand = "Airtel",
            network2G = "AirTel",
            country = "IN",
            mcc = "404",
            provenance = Provenance.USER,
        ),
        RazrOperator(
            brand = "Vi",
            network2G = "Hutch",
            country = "IN",
            mcc = "404",
            provenance = Provenance.USER,
        ),
        RazrOperator(
            brand = "BSNL",
            network2G = "BSNL",
            country = "IN",
            mcc = "404",
            provenance = Provenance.USER,
        ),
        // MTNL was a DoT-owned GSM operator in Delhi and is the remaining
        // Indian network that was genuinely 2G-era; kept for completeness.
        RazrOperator(
            brand = "MTNL",
            network2G = "MTNL",
            country = "IN",
            mcc = "404",
            provenance = Provenance.RESEARCHED,
        ),
    )

    /**
     * Elsewhere. Rows are included where the 2G network name differs from the
     * modern brand, which is the whole point of the table, plus the majors a
     * 2G handset would plausibly have registered on.
     */
    val international: List<RazrOperator> = listOf(
        // United Kingdom
        RazrOperator("Vodafone UK", "Vodafone", "GB", "234", Provenance.RESEARCHED),
        RazrOperator("O2 UK", "O2", "GB", "234", Provenance.RESEARCHED),
        RazrOperator("Three UK", "Three", "GB", "234", Provenance.RESEARCHED),
        RazrOperator("EE", "EE", "GB", "234", Provenance.RESEARCHED),
        RazrOperator("BT Mobile", "BT Mobile", "GB", "234", Provenance.RESEARCHED),

        // Germany / Austria / Switzerland
        RazrOperator("Telekom DE", "Telekom", "DE", "262", Provenance.RESEARCHED),
        RazrOperator("Vodafone DE", "Vodafone", "DE", "262", Provenance.RESEARCHED),
        RazrOperator("O2 DE", "O2", "DE", "262", Provenance.RESEARCHED),
        RazrOperator("T-Mobile DE", "T-Mobile", "DE", "262", Provenance.RESEARCHED),
        RazrOperator("A1 Telekom Austria", "A1", "AT", "232", Provenance.RESEARCHED),
        RazrOperator("Swisscom", "Swisscom", "CH", "228", Provenance.RESEARCHED),
        RazrOperator("Sunrise", "Sunrise", "CH", "228", Provenance.RESEARCHED),

        // Nordics
        RazrOperator("Telia", "Telia", "SE", "240", Provenance.RESEARCHED),
        RazrOperator("Tele2", "Tele2", "SE", "240", Provenance.RESEARCHED),
        RazrOperator("Telenor", "Telenor", "NO", "242", Provenance.RESEARCHED),
        RazrOperator("Elisa", "Elisa", "FI", "244", Provenance.RESEARCHED),
        RazrOperator("TDC / YouSee", "TDC", "DK", "208", Provenance.RESEARCHED),
        RazrOperator("Telia FI", "Telia", "FI", "244", Provenance.RESEARCHED),

        // North America
        RazrOperator("T-Mobile US", "T-Mobile", "US", "310", Provenance.RESEARCHED),
        RazrOperator("AT&T", "AT&T", "US", "310", Provenance.RESEARCHED),
        RazrOperator("Verizon", "Verizon", "US", "311", Provenance.RESEARCHED),
        RazrOperator("Rogers", "Rogers", "CA", "302", Provenance.RESEARCHED),
        RazrOperator("Bell", "Bell", "CA", "302", Provenance.RESEARCHED),
        RazrOperator("Telus", "TELUS", "CA", "302", Provenance.RESEARCHED),

        // Western Europe
        RazrOperator("Orange FR", "Orange", "FR", "208", Provenance.RESEARCHED),
        RazrOperator("SFR", "SFR", "FR", "208", Provenance.RESEARCHED),
        RazrOperator("Bouygues", "Bouygues", "FR", "208", Provenance.RESEARCHED),
        RazrOperator("TIM", "TIM", "IT", "222", Provenance.RESEARCHED),
        RazrOperator("WindTre", "Wind Tre", "IT", "222", Provenance.RESEARCHED),
        RazrOperator("Vodafone IT", "Vodafone", "IT", "222", Provenance.RESEARCHED),
        RazrOperator("Movistar", "Movistar", "ES", "214", Provenance.RESEARCHED),
        RazrOperator("Orange ES", "Orange", "ES", "214", Provenance.RESEARCHED),
        RazrOperator("Vodafone ES", "Vodafone", "ES", "214", Provenance.RESEARCHED),
        RazrOperator("KPN", "KPN", "NL", "204", Provenance.RESEARCHED),
        RazrOperator("Vodafone NL", "Vodafone", "NL", "204", Provenance.RESEARCHED),
        RazrOperator("T-Mobile NL", "T-Mobile", "NL", "204", Provenance.RESEARCHED),
        RazrOperator("Proximus", "Proximus", "BE", "206", Provenance.RESEARCHED),
        RazrOperator("Orange BE", "Orange", "BE", "206", Provenance.RESEARCHED),

        // Middle East / Africa / Turkey
        RazrOperator("Turkcell", "Turkcell", "TR", "286", Provenance.RESEARCHED),
        RazrOperator("Vodafone TR", "Vodafone", "TR", "286", Provenance.RESEARCHED),
        RazrOperator("Turk Telekom", "Turk Telekom", "TR", "286", Provenance.RESEARCHED),
        RazrOperator("Etisalat UAE", "Etisalat", "AE", "424", Provenance.RESEARCHED),
        RazrOperator("Zain", "Zain", "KW", "419", Provenance.RESEARCHED),
        RazrOperator("STC", "STC", "SA", "420", Provenance.RESEARCHED),
        RazrOperator("Mobily", "Mobily", "SA", "420", Provenance.RESEARCHED),
        RazrOperator("MTN South Africa", "MTN", "ZA", "655", Provenance.RESEARCHED),
        RazrOperator("Vodacom", "Vodacom", "ZA", "655", Provenance.RESEARCHED),
        RazrOperator("Cell C", "Cell C", "ZA", "655", Provenance.RESEARCHED),
        RazrOperator("MTN Nigeria", "MTN", "NG", "621", Provenance.RESEARCHED),
        RazrOperator("Airtel Nigeria", "Airtel", "NG", "621", Provenance.RESEARCHED),
        RazrOperator("Glo", "Glo", "NG", "621", Provenance.RESEARCHED),
        RazrOperator("Safaricom", "Safaricom", "KE", "639", Provenance.RESEARCHED),
        RazrOperator("Airtel Kenya", "Airtel", "KE", "639", Provenance.RESEARCHED),
        RazrOperator("Telkom Kenya", "Telkom", "KE", "639", Provenance.RESEARCHED),
        RazrOperator("Econet", "Econet", "ZW", "613", Provenance.RESEARCHED),

        // Asia-Pacific
        RazrOperator("Telstra", "Telstra", "AU", "505", Provenance.RESEARCHED),
        RazrOperator("Optus", "Optus", "AU", "505", Provenance.RESEARCHED),
        RazrOperator("Vodafone AU", "Vodafone", "AU", "505", Provenance.RESEARCHED),
        RazrOperator("Spark", "Spark", "NZ", "530", Provenance.RESEARCHED),
        RazrOperator("Telkomsel", "Telkomsel", "ID", "510", Provenance.RESEARCHED),
        RazrOperator("Indosat", "Indosat", "ID", "510", Provenance.RESEARCHED),
        RazrOperator("XL Axiata", "XL", "ID", "510", Provenance.RESEARCHED),
        RazrOperator("Smartfren", "Smartfren", "ID", "510", Provenance.RESEARCHED),
        RazrOperator("Globe", "Globe", "PH", "515", Provenance.RESEARCHED),
        RazrOperator("Smart", "Smart", "PH", "515", Provenance.RESEARCHED),
        RazrOperator("AIS", "AIS", "TH", "520", Provenance.RESEARCHED),
        RazrOperator("TrueMove", "True", "TH", "520", Provenance.RESEARCHED),
        RazrOperator("Viettel", "Viettel", "VN", "452", Provenance.RESEARCHED),
        RazrOperator("Vinaphone", "Vinaphone", "VN", "452", Provenance.RESEARCHED),
        RazrOperator("Grameenphone", "Grameenphone", "BD", "470", Provenance.RESEARCHED),
        RazrOperator("Banglalink", "Banglalink", "BD", "470", Provenance.RESEARCHED),
        RazrOperator("Jazz", "Jazz", "PK", "410", Provenance.RESEARCHED),
        RazrOperator("Zong", "Zong", "PK", "410", Provenance.RESEARCHED),
        RazrOperator("Telenor PK", "Telenor", "PK", "410", Provenance.RESEARCHED),
        RazrOperator("Ufone", "Ufone", "PK", "410", Provenance.RESEARCHED),
        RazrOperator("DiGi", "DiGi", "MY", "502", Provenance.RESEARCHED),
        RazrOperator("Maxis", "Maxis", "MY", "502", Provenance.RESEARCHED),
        RazrOperator("Celcom", "Celcom", "MY", "502", Provenance.RESEARCHED),
        RazrOperator("Singtel", "Singtel", "SG", "525", Provenance.RESEARCHED),
        RazrOperator("M1", "M1", "SG", "525", Provenance.RESEARCHED),
        RazrOperator("China Mobile", "China Mobile", "CN", "460", Provenance.RESEARCHED),
        RazrOperator("China Unicom", "China Unicom", "CN", "460", Provenance.RESEARCHED),
        RazrOperator("China Telecom", "China Telecom", "CN", "460", Provenance.RESEARCHED),
        RazrOperator("SK Telecom", "SKT", "KR", "450", Provenance.RESEARCHED),
        RazrOperator("KT", "KT", "KR", "450", Provenance.RESEARCHED),

        // Americas
        RazrOperator("Claro BR", "Claro", "BR", "724", Provenance.RESEARCHED),
        RazrOperator("Vivo BR", "Vivo", "BR", "724", Provenance.RESEARCHED),
        RazrOperator("TIM Brasil", "TIM", "BR", "724", Provenance.RESEARCHED),
        RazrOperator("Oi", "Oi", "BR", "724", Provenance.RESEARCHED),
        RazrOperator("Telcel", "Telcel", "MX", "334", Provenance.RESEARCHED),
        RazrOperator("Movistar MX", "Movistar", "MX", "334", Provenance.RESEARCHED),
        RazrOperator("AT&T MX", "AT&T", "MX", "334", Provenance.RESEARCHED),
        RazrOperator("Claro AR", "Claro", "AR", "722", Provenance.RESEARCHED),
        RazrOperator("Personal AR", "Personal", "AR", "722", Provenance.RESEARCHED),
        RazrOperator("Entel", "Entel", "CL", "730", Provenance.RESEARCHED),
        RazrOperator("Movistar Chile", "Movistar", "CL", "730", Provenance.RESEARCHED),
        RazrOperator("T-Mobile PL", "Era", "PL", "260", Provenance.RESEARCHED),

        // Eastern Europe / Russia
        RazrOperator("MTS", "MTS", "RU", "250", Provenance.RESEARCHED),
        RazrOperator("Beeline", "Beeline", "RU", "250", Provenance.RESEARCHED),
        RazrOperator("MegaFon", "MegaFon", "RU", "250", Provenance.RESEARCHED),
        RazrOperator("Tele2 RU", "Tele2", "RU", "250", Provenance.RESEARCHED),
        RazrOperator("Kyivstar", "Kyivstar", "UA", "255", Provenance.RESEARCHED),
        RazrOperator("Vodafone UA", "Vodafone", "UA", "255", Provenance.RESEARCHED),
        RazrOperator("Orange RO", "Orange", "RO", "226", Provenance.RESEARCHED),
        RazrOperator("Telekom RO", "Telekom", "RO", "226", Provenance.RESEARCHED),
    )

    /** Everything, India first so the hand-specified rows win any tie. */
    val all: List<RazrOperator> = india + international

    /** Used when the SIM cannot be read, or reports something unrecognised. */
    const val UNKNOWN_OPERATOR = "MOTOROLA"

    /**
     * Words that are too generic to match on, so "Orange" as a brand does not
     * get matched by a reported name that merely contains the word "mobile".
     */
    private val STOP_WORDS = setOf(
        "mobile", "telecom", "telecommunications", "communications", "cellular",
        "network", "the", "ltd", "limited", "inc", "gmbh", "s.a.", "bv",
    )

    /**
     * Resolves a SIM-reported operator name to the name a 2G handset would
     * display for it.
     *
     * Matching is intentionally forgiving because SIM operator strings vary
     * wildly ("Airtel", "AIRTEL", "Bharti Airtel", "Airtel India"). It tries,
     * in order: exact match on a normalised form, then exact match on a row's
     * 2G name, then any single significant word of the reported name.
     *
     * @param simOperatorName the operator string the SIM/System reports. May be
     *        blank, null, or the placeholder Android uses when there is no SIM.
     * @return the 2G network name, or [simOperatorName] trimmed and capped when
     *         it is not in the database, so an unknown carrier still shows
     *         something honest rather than being mislabelled as an Indian one.
     */
    fun resolveFor2G(simOperatorName: String?): String {
        val reported = simOperatorName?.trim().orEmpty()
        if (reported.isEmpty()) return UNKNOWN_OPERATOR

        // Android hands back empty or a placeholder when there is no SIM.
        if (reported.equals("Unknown", true) ||
            reported.equals("Carrier", true) ||
            reported.equals("Not available", true)
        ) {
            return UNKNOWN_OPERATOR
        }

        val norm = normalise(reported)

        all.firstOrNull { normalise(it.brand) == norm }?.let { return it.compact2G }
        all.firstOrNull { normalise(it.network2G) == norm }?.let { return it.compact2G }

        val words = norm.split(' ').filter { it.length >= 3 && it !in STOP_WORDS }
        for (w in words) {
            all.firstOrNull { normalise(it.brand).contains(w) }?.let { return it.compact2G }
            all.firstOrNull { normalise(it.network2G).contains(w) }?.let { return it.compact2G }
        }

        return reported.take(12)
    }

    /** Strips case and punctuation so "Bharti Airtel Ltd." matches "airtel". */
    private fun normalise(s: String): String =
        s.lowercase()
            .replace('.', ' ')
            .replace('-', ' ')
            .filter { it.isLetter() || it == ' ' }
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ")
}
