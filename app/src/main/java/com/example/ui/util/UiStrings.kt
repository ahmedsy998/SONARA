package com.example.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.model.AppLanguage
import java.util.Locale

@Composable
fun isArabicLocale(language: AppLanguage): Boolean {
    return when (language) {
        AppLanguage.ARABIC -> true
        AppLanguage.ENGLISH -> false
        AppLanguage.SYSTEM -> Locale.getDefault().language == "ar"
    }
}

fun layoutDirectionFor(isArabic: Boolean): LayoutDirection {
    return if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
}

fun tr(isArabic: Boolean, en: String, ar: String): String = if (isArabic) ar else en

fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val hrs = totalSec / 3600L
    val mins = (totalSec % 3600L) / 60L
    val secs = totalSec % 60L
    return if (hrs > 0) {
        "%d:%02d:%02d".format(hrs, mins, secs)
    } else {
        "%d:%02d".format(mins, secs)
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val mb = bytes / (1024.0 * 1024.0)
    return "%.1f MB".format(mb)
}
