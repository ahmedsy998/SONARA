package com.example.ui.util

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.LayoutDirection
import com.example.model.AppLanguage
import java.util.Locale

fun isArabicLanguage(language: AppLanguage): Boolean {
    return when (language) {
        AppLanguage.ARABIC -> true
        AppLanguage.ENGLISH -> false
        AppLanguage.SYSTEM -> Locale.getDefault().language == "ar"
    }
}

@Composable
fun isArabicLocale(language: AppLanguage): Boolean = isArabicLanguage(language)

fun layoutDirectionFor(isArabic: Boolean): LayoutDirection {
    return if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
}

fun tr(isArabic: Boolean, en: String, ar: String): String = if (isArabic) ar else en

fun Context.localizedFor(language: AppLanguage): Context {
    val targetLocale = when (language) {
        AppLanguage.ARABIC -> Locale("ar")
        AppLanguage.ENGLISH -> Locale("en")
        AppLanguage.SYSTEM -> Locale.getDefault()
    }
    val config = Configuration(resources.configuration)
    config.setLocale(targetLocale)
    config.setLayoutDirection(targetLocale)
    return createConfigurationContext(config)
}

fun localizedForHelper(context: Context, language: AppLanguage): Context = context.localizedFor(language)

fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val hrs = totalSec / 3600L
    val mins = (totalSec % 3600L) / 60L
    val secs = totalSec % 60L
    return if (hrs > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(Locale.US, "%d:%02d", mins, secs)
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val mb = bytes / (1024.0 * 1024.0)
    return String.format(Locale.US, "%.1f MB", mb)
}
