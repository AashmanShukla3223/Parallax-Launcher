package com.parallax.parallaxlauncher.ui.modes.razr

/**
 * Emergency telephone numbers, and the rule for letting one through the
 * no-SIM gate.
 *
 * ## Why this is a table and not just "112 and 911"
 *
 * The no-SIM screen refuses ordinary calls, because with no SIM the radio
 * cannot carry them. Emergency calls are the exception and must never be
 * refused, so the gate has to recognise them. Recognising only 112 and 911
 * would be wrong: Brazil routes 190, Chile 133, Iceland has 112 but no 911, and
 * India runs 100/101/108 for its individual services alongside 112. Someone
 * dialling 133 in Chile with no SIM is dialling the police, and must not be
 * stopped by our own UI.
 *
 * ## Where the data comes from
 *
 * The per-country numbers are from Wikipedia's
 * "List of emergency telephone numbers", cross-checked against the CEPT/EU 112
 * convention. Three sources are consulted at runtime, in this order:
 *
 *  1. [android.telephony.TelephonyManager.getEmergencyNumberList] on API 29+.
 *     This is the SIM and network's own list and is authoritative, so it wins.
 *  2. [byCountry] below, keyed on the SIM's ISO country code, falling back to
 *     the **network's**. That fallback is what keeps this table reachable in the
 *     one case it matters: with no SIM inserted `simCountryIso` is blank, but a
 *     handset sitting in coverage is still registered to a network and
 *     `networkCountryIso` reports that network's country. Verified on a SIM-less
 *     device registered to a Korean network — `gsm.sim.operator.iso-country`
 *     empty, `gsm.operator.iso-country` = `kr`.
 *  3. [GSM_UNIVERSAL], which is the fallback when the country is unknown.
 *
 * Treat the per-country rows as a starting point rather than gospel: emergency
 * numbers are national policy and they do change (Uzbekistan moved to 112,
 * Turkey consolidated onto 112, the UK still lists 119 for COVID testing which
 * is not an emergency number at all). The entries that actually matter for
 * safety are the short primary ones.
 *
 * ## Audit
 *
 * Every row was checked against Wikipedia's list plus a second source wherever a
 * row looked wrong. Corrected in that pass: Turkmenistan carried the obsolete
 * Soviet two-digit plan, which [normalise] collapsed to the bare digits 1-4;
 * Niue and Tokelau had international-format numbers transcribed as local short
 * codes; Thailand was missing 1669 (the ambulance number English-speaking
 * callers are actually given) and carried a 112 it does not have; Myanmar's
 * highway police number lost a digit; Malaysia, Tonga, Tuvalu, Vanuatu and six
 * French-plan territories were absent entirely.
 *
 * Still unverified and worth a further pass: Turkey's 110/119/122, Bangladesh
 * 991/998, Qatar 997/998, Saudi 996, UAE 112. These were left alone because no
 * national source could be confirmed for them.
 */
object RazrEmergencyNumbers {

    /**
     * Numbers carried in every GSM handset and honoured by every GSM network.
     *
     * 112 and 911 are the ITU-designated pair, and per the 3GPP/GSM spec both
     * are pre-programmed into the phone and route to whichever emergency desk
     * serves the current location. That is exactly why they work with no SIM,
     * with the keypad locked, and with no signal.
     */
    val GSM_UNIVERSAL = setOf("112", "911")

    /**
     * Widely accepted on GSM networks even with no SIM present, per the
     * standard's well-known emergency number handling.
     */
    val GSM_WIDELY_ACCEPTED = setOf("000", "110", "118", "119", "999")

    /**
     * Per-country numbers, keyed by ISO 3166-1 alpha-2 as reported by
     * `TelephonyManager.simCountryIso` / `networkCountryIso`.
     *
     * Includes the primary emergency numbers plus the per-service codes that a
     * caller might reasonably dial for police, ambulance or fire. Single and
     * double digit codes are included because several countries genuinely use
     * them (France 17/18/15, Chile 131/132/133), but note that GSM networks
     * often refuse one or two digit codes from a handset, so treat those as
     * best-effort rather than guaranteed.
     */
    val byCountry: Map<String, Set<String>> = mapOf(
        // ---- Africa ------------------------------------------------------
        "DZ" to setOf("1548", "14", "1055", "112", "17", "18"),
        "AO" to setOf("113", "112", "116", "115"),
        "BJ" to setOf("117", "112", "118"),
        "BI" to setOf("117", "112", "118"),
        "BW" to setOf("999", "997", "998", "112"),
        "BF" to setOf("17", "112", "18"),
        "CM" to setOf("112", "117", "119", "118"),
        "CV" to setOf("132", "130", "131"),
        "CF" to setOf("117", "118", "1220"),
        "TD" to setOf("17", "18"),
        "KM" to setOf("17", "18"),
        "CG" to setOf("117", "118"),
        "CD" to setOf("112", "118"),
        "DJ" to setOf("17", "18", "19"),
        "EG" to setOf("112", "122", "123", "180"),
        "GQ" to setOf("114", "115", "112", "116"),
        "ER" to setOf("113", "114", "116", "112", "115"),
        "SZ" to setOf("999", "977", "933"),
        "ET" to setOf("911", "991", "907", "939", "945"),
        "GA" to setOf("1730", "1300", "18"),
        "GM" to setOf("117", "116", "118", "112"),
        "GH" to setOf("112", "191", "193", "192"),
        "GN" to setOf("117", "18", "19", "118", "122"),
        "GW" to setOf("112", "117", "119", "118"),
        "CI" to setOf("110", "111", "170", "185", "180"),
        "LR" to setOf("911"),
        "KE" to setOf("112", "999", "911"),
        "LY" to setOf("1515", "193"),
        "LS" to setOf("123", "121", "122"),
        "MG" to setOf("117", "124", "118"),
        "MW" to setOf("997", "998", "999", "990"),
        "ML" to setOf("17", "15", "18", "112"),
        "MR" to setOf("117", "101", "118", "116", "119"),
        "MU" to setOf("112", "114", "115", "999", "995"),
        "MA" to setOf("19", "15", "112", "177"),
        "MZ" to setOf("119", "117", "198"),
        "NA" to setOf("10111"),
        "NE" to setOf("17", "15", "18"),
        "NG" to setOf("112"),
        "RW" to setOf("112", "912", "113"),
        "ST" to setOf("112"),
        "SN" to setOf("17", "18", "1515"),
        "SC" to setOf("112", "999", "133", "151"),
        "SL" to setOf("019", "999"),
        "SO" to setOf("888", "999", "555", "777"),
        "ZA" to setOf("10111", "10177", "107", "112"),
        "SD" to setOf("999"),
        "SS" to setOf("999"),
        "TZ" to setOf("112", "114", "115", "999", "199"),
        "TG" to setOf("117", "118", "8200"),
        "TN" to setOf("197", "190", "198", "193"),
        "UG" to setOf("112", "911", "999"),
        "ZM" to setOf("999", "911", "992", "993", "112"),
        "ZW" to setOf("999", "995", "994", "993", "112"),

        // ---- Caribbean ---------------------------------------------------
        "AG" to setOf("911", "999"),
        "AI" to setOf("911", "999"),
        "AW" to setOf("911"),
        "BS" to setOf("911", "919", "112"),
        "BB" to setOf("211", "511", "311"),
        "BM" to setOf("911"),
        "VG" to setOf("911", "999", "311"),
        "BQ" to setOf("911"),
        "KY" to setOf("911"),
        "CU" to setOf("106", "104", "105"),
        "CW" to setOf("911", "910", "912"),
        "DM" to setOf("999"),
        "DO" to setOf("911", "112"),
        "GD" to setOf("911"),
        "GP" to setOf("112", "17", "15", "18"),
        "HT" to setOf("114", "116", "115", "122"),
        "JM" to setOf("119", "110"),
        "MQ" to setOf("112", "17", "15", "18"),
        "MS" to setOf("911", "999"),
        "PR" to setOf("911"),
        "KN" to setOf("911"),
        "LC" to setOf("911", "999"),
        "MF" to setOf("17", "15", "18"),
        "VC" to setOf("999", "911", "112"),
        "SX" to setOf("911", "912", "919"),
        "TT" to setOf("999", "911", "811", "990"),
        "TC" to setOf("911"),
        "VI" to setOf("911"),

        // ---- Central America ---------------------------------------------
        "BZ" to setOf("911"),
        "CR" to setOf("911", "112"),
        "SV" to setOf("911", "132", "913"),
        "GT" to setOf("110", "122", "123", "1554", "119"),
        "HN" to setOf("911", "195", "198"),
        "NI" to setOf("118", "128", "115", "911"),
        "PA" to setOf("911", "104", "103"),

        // ---- North America -----------------------------------------------
        "CA" to setOf("911", "988", "112"),
        "MX" to setOf("911", "089", "112"),
        "US" to setOf("911", "988", "112"),

        // ---- South America -----------------------------------------------
        "AR" to setOf("911", "101", "107", "100", "103", "105", "106", "112"),
        "BO" to setOf("911", "110", "118", "119", "114", "120"),
        "BR" to setOf("190", "192", "193", "191", "194", "197", "198", "199", "153", "100", "128", "112", "911", "188"),
        "CL" to setOf("133", "131", "132", "911", "112"),
        "CO" to setOf("112", "125", "119", "123", "165"),
        "EC" to setOf("911", "101", "131", "102", "112", "103"),
        "GF" to setOf("112", "17", "15", "18"),
        "GY" to setOf("911", "913", "912"),
        "PY" to setOf("911", "912", "141", "132", "131"),
        "PE" to setOf("911", "105", "106", "116", "115", "100"),
        "SR" to setOf("115", "113", "110"),
        "UY" to setOf("911", "109", "105", "104"),
        "VE" to setOf("911", "171"),

        // ---- Asia --------------------------------------------------------
        "AF" to setOf("119", "112"),
        "BH" to setOf("999", "112", "199", "994"),
        "BD" to setOf("999", "112"),
        "BT" to setOf("113", "112", "110"),
        "BN" to setOf("993", "991", "995", "998"),
        "KH" to setOf("117", "119", "118"),
        "CN" to setOf("110", "120", "119", "122", "112", "911"),
        "TL" to setOf("112"),
        "HK" to setOf("999", "992", "911", "112"),
        "IN" to setOf("112", "100", "101", "108", "102", "1098", "1363", "181", "104"),
        "ID" to setOf("112", "110", "118", "119", "113", "115", "129", "123"),
        "IR" to setOf("110", "115", "125", "123", "112", "911"),
        "IQ" to setOf("112", "911", "104", "122", "115"),
        "IL" to setOf("100", "101", "102", "112"),
        "JP" to setOf("110", "119", "118", "112", "911"),
        "JO" to setOf("911", "112"),
        "KZ" to setOf("112", "102", "103", "101", "104"),
        "KR" to setOf("112", "119", "110", "111", "113"),
        "KW" to setOf("112"),
        "KG" to setOf("112"),
        "LA" to setOf("191", "195", "190"),
        "LB" to setOf("999", "112", "140", "175", "160", "125"),
        "MO" to setOf("999", "110", "112", "993"),
        "MY" to setOf("999", "112"),
        "MV" to setOf("911", "119", "100", "118", "191", "115"),
        "MN" to setOf("105", "102", "103", "101"),
        "MM" to setOf("999", "199", "192", "191", "1880"),
        "NP" to setOf("100", "102", "101", "103", "112"),
        "OM" to setOf("9999", "112"),
        "PK" to setOf("15", "115", "1122", "16", "1915", "112"),
        "PS" to setOf("100", "101", "102"),
        "PH" to setOf("911", "112", "163", "1343"),
        "QA" to setOf("999", "112"),
        "SA" to setOf("911", "999", "997", "998", "993"),
        "SG" to setOf("999", "995", "112", "911"),
        "LK" to setOf("119", "118", "110"),
        "SY" to setOf("112", "110", "113", "115"),
        "TW" to setOf("110", "119", "112", "113", "118"),
        "TJ" to setOf("112", "102", "103", "101", "104"),
        "TH" to setOf("191", "1669", "199"),
        "TR" to setOf("112", "911", "153", "114", "187"),
        "TM" to setOf("112", "101", "102", "103"),
        "AE" to setOf("999", "998", "997", "996", "901"),
        "UZ" to setOf("112", "102", "103", "101", "1050", "104", "1055"),
        "VN" to setOf("113", "115", "114", "111", "112"),
        "YE" to setOf("194", "191"),

        // ---- Europe ------------------------------------------------------
        "AL" to setOf("129", "127", "128", "126", "125", "112"),
        "AD" to setOf("110", "116", "118", "112"),
        "AM" to setOf("112", "911", "102", "103", "101", "104", "108"),
        "AT" to setOf("112", "133", "144", "122", "128", "140"),
        "AZ" to setOf("112", "102", "103", "101", "104", "902", "199"),
        "BY" to setOf("102", "103", "101", "104"),
        "BE" to setOf("101", "112", "1722"),
        "BA" to setOf("112", "122", "124", "123", "121"),
        "BG" to setOf("112", "166", "150", "160"),
        "HR" to setOf("112", "192", "194", "193", "195"),
        "CY" to setOf("112", "199", "1441"),
        "CZ" to setOf("112", "158", "155", "150", "156"),
        "DK" to setOf("112", "114"),
        "EE" to setOf("112"),
        "FI" to setOf("112"),
        "FO" to setOf("112"),
        "FR" to setOf("17", "15", "18", "112", "114", "119", "116000", "196"),
        "GE" to setOf("112"),
        "DE" to setOf("110", "112", "19222"),
        "GI" to setOf("199", "112", "999", "190"),
        "GR" to setOf("100", "112", "166", "199", "108"),
        "GL" to setOf("112"),
        "GG" to setOf("112", "999"),
        "HU" to setOf("112", "107", "104", "105", "1817"),
        "IS" to setOf("112", "911"),
        "IE" to setOf("112", "999", "901"),
        "IM" to setOf("112", "999"),
        "JE" to setOf("112", "999"),
        "IT" to setOf("112", "113", "115", "118", "114", "1522"),
        "XK" to setOf("192", "194", "193"),
        "LV" to setOf("112", "110", "113", "114"),
        "LT" to setOf("112", "022", "033", "011"),
        "LI" to setOf("117", "144", "118"),
        "LU" to setOf("112", "113"),
        "MT" to setOf("112"),
        "MD" to setOf("112"),
        "MC" to setOf("17", "112", "18"),
        "ME" to setOf("122", "112", "124", "123", "129"),
        "NL" to setOf("112", "911"),
        "MK" to setOf("192", "112", "194", "193"),
        "NO" to setOf("112", "113", "110", "120"),
        "PL" to setOf("997", "112", "999", "998", "911"),
        "PT" to setOf("112", "117", "144", "911"),
        "RO" to setOf("112", "911"),
        "RU" to setOf("102", "112", "103", "101", "104"),
        "SM" to setOf("112", "113", "118", "115"),
        "RS" to setOf("192", "112", "194", "193"),
        "SK" to setOf("112", "158", "155", "150", "159"),
        "SI" to setOf("113", "112"),
        "ES" to setOf("112", "091", "062", "092", "080", "085", "061"),
        "SE" to setOf("112", "911"),
        "CH" to setOf("117", "144", "118", "112", "145", "143"),
        "UA" to setOf("102", "103", "101", "112", "104"),
        "GB" to setOf("999", "112", "911"),
        "VA" to setOf("112"),

        // ---- Oceania -----------------------------------------------------
        "AS" to setOf("911"),
        "AU" to setOf("000", "112", "132500", "106"),
        "CK" to setOf("999", "911"),
        "FJ" to setOf("911", "917", "910"),
        "PF" to setOf("112", "17", "15", "18"),
        "GU" to setOf("911"),
        "KI" to setOf("100", "999", "192", "194", "195", "193"),
        "MH" to setOf("911"),
        "FM" to setOf("911"),
        "NR" to setOf("110", "111", "112"),
        "NC" to setOf("112", "17", "15", "18"),
        "NZ" to setOf("111", "112", "911"),
        "NU" to setOf("999"),
        "MP" to setOf("911"),
        "PW" to setOf("911"),
        "PG" to setOf("112", "111", "110"),
        "WS" to setOf("999", "995", "996", "994"),
        "SB" to setOf("911", "999", "111", "988"),
        "TK" to setOf("999"),
        "TO" to setOf("911", "922", "933", "999"),
        "TV" to setOf("911", "999", "000"),
        "VU" to setOf("111", "112", "113"),

        // ---- Territories with their own dialling plan --------------------
        "SH" to setOf("999", "112"),
        "AC" to setOf("999", "112"),
        "TA" to setOf("999", "911"),
        "FK" to setOf("112", "999"),
        "IO" to setOf("999", "112"),
        "AX" to setOf("112"),
        "BL" to setOf("112", "17", "18"),
        "PM" to setOf("112", "17", "15", "18"),
        "RE" to setOf("112", "17", "15", "18"),
        "WF" to setOf("15", "17", "18", "112"),
        "YT" to setOf("112", "17", "15", "18"),
        "CX" to setOf("000"),
        "CC" to setOf("000"),
        "AQ" to setOf("911"),
    )

    /**
     * Reduces a dialled string to bare digits, dropping a national trunk
     * prefix so `0911` still matches `911`.
     */
    private fun normalise(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        val trimmed = digits.trimStart('0')
        return trimmed.ifEmpty { digits }
    }

    /**
     * Decides whether [raw] is an emergency number and therefore must be
     * allowed to place even with no SIM.
     *
     * @param raw the number as dialled.
     * @param countryIso the SIM/network country, ISO 3166-1 alpha-2. Blank or
     *        unrecognised is fine: the universal set still applies, which is
     *        what matters most, since 112 and 911 work everywhere.
     * @param platformNumbers the value of
     *        `TelephonyManager.getEmergencyNumberList()` on API 29+, which is
     *        the SIM's own list and therefore authoritative. Pass an empty set
     *        when it is unavailable.
     */
    fun isEmergency(raw: String, countryIso: String?, platformNumbers: Set<String> = emptySet()): Boolean {
        val digits = normalise(raw)
        if (digits.isEmpty()) return false

        // 1. The SIM and network's own list wins outright.
        if (platformNumbers.any { normalise(it) == digits }) return true

        // 2. Pre-programmed GSM numbers: always valid, no SIM required.
        if (digits in GSM_UNIVERSAL) return true

        // 3. Per-country table.
        val iso = countryIso?.trim()?.uppercase() ?: ""
        if (iso.isNotEmpty()) {
            val local = byCountry[iso]
            if (local != null && local.any { normalise(it) == digits }) return true
        }

        // 4. The wider well-known GSM list, for when the country is unknown.
        if (digits in GSM_WIDELY_ACCEPTED) return true

        return false
    }

    /** The country's primary emergency number, for display. Null if unknown. */
    fun primaryFor(countryIso: String?): String? {
        val iso = countryIso?.trim()?.uppercase() ?: return null
        return byCountry[iso]?.firstOrNull()
    }
}