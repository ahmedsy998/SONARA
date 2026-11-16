package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DynamicArtColors(
    val vibrant: Color,
    val darkVibrant: Color,
    val muted: Color,
    val lightVibrant: Color
)

object AlbumArtPaletteExtractor {

    suspend fun extractColors(context: Context, artUri: Uri?): DynamicArtColors? =
        withContext(Dispatchers.IO) {
            if (artUri == null) return@withContext null
            runCatching {
                val bitmap: Bitmap? = context.contentResolver.openInputStream(artUri)?.use { stream ->
                    val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                    BitmapFactory.decodeStream(stream, null, opts)
                }
                if (bitmap == null) return@withContext null
                val palette = Palette.from(bitmap).generate()
                bitmap.recycle()

                val vibrantInt = palette.getVibrantColor(palette.getDominantColor(0xFF38BDF8.toInt()))
                val darkVibrantInt = palette.getDarkVibrantColor(palette.getDarkMutedColor(0xFF0F172A.toInt()))
                val mutedInt = palette.getMutedColor(0xFF1E293B.toInt())
                val lightVibrantInt = palette.getLightVibrantColor(vibrantInt)

                DynamicArtColors(
                    vibrant = Color(vibrantInt),
                    darkVibrant = Color(darkVibrantInt),
                    muted = Color(mutedInt),
                    lightVibrant = Color(lightVibrantInt)
                )
            }.getOrNull()
        }
}
