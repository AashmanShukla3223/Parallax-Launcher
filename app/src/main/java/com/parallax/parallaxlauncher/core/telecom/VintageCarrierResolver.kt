package com.parallax.parallaxlauncher.core.telecom

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale

object VintageCarrierResolver {
    fun resolve(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val raw = (tm?.simOperatorName?.takeIf { it.isNotBlank() }
            ?: tm?.networkOperatorName?.takeIf { it.isNotBlank() }
            ?: "Reliance").lowercase(Locale.ROOT)

        return when {
            // Jio / Reliance
            raw.contains("jio") || raw.contains("reliance") -> "Reliance Communications"
            // Vi / Vodafone / Idea -> Hutch
            raw.contains("vi") || raw.contains("vodafone") || raw.contains("idea") -> "HUTCH"
            // Airtel
            raw.contains("airtel") || raw.contains("bharti") -> "AirTel 2G"
            // BSNL
            raw.contains("bsnl") || raw.contains("cellone") -> "CellOne"
            // US Carriers
            raw.contains("at&t") || raw.contains("att") || raw.contains("cingular") -> "Cingular"
            raw.contains("verizon") -> "Verizon Wireless"
            raw.contains("t-mobile") || raw.contains("tmobile") || raw.contains("voicestream") -> "VoiceStream"
            raw.contains("sprint") -> "Sprint PCS"
            // UK / EU
            raw.contains("o2") -> "BT Cellnet"
            raw.contains("orange") || raw.contains("ee") -> "Orange"
            else -> "Reliance Communications"
        }
    }
}
