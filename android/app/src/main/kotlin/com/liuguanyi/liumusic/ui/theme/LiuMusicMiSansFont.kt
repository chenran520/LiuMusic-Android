package com.liuguanyi.liumusic.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.liuguanyi.liumusic.R

val LiuMusicLanTingProFontFamily = FontFamily(
    // The XML family maps standard Android weights to this font's private
    // 150..700 axis coordinates. Compose can then use normal weight matching.
    Font(R.font.mi_lan_pro, weight = FontWeight.Normal),
)

val LocalLiuMusicFontFamily = compositionLocalOf<FontFamily> { LiuMusicLanTingProFontFamily }

@Composable
fun rememberLiuMusicFontFamily(context: Context): FontFamily {
    val useSystemFont = com.liuguanyi.liumusic.ui.settings.LiuMusicSettingsRuntime.systemFontEnabled
    return remember(context, useSystemFont) {
        if (useSystemFont) FontFamily.Default else LiuMusicLanTingProFontFamily
    }
}
