package com.liuguanyi.liumusic.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.liuguanyi.liumusic.core.remoteconfig.LiuMusicHotStyles

/**
 * Applies signed colour overrides onto the built-in scheme.
 *
 * Only the roles [LiuMusicHotStyles] validates are read, so an unknown key can never
 * reach here, and every lookup falls back to the built-in colour.
 */
internal fun ColorScheme.withHotOverrides(overrides: Map<String, Int>, dark: Boolean): ColorScheme {
    if (overrides.isEmpty()) return this
    fun role(name: String, fallback: Color): Color =
        LiuMusicHotStyles.color(name, dark, overrides)?.let(::Color) ?: fallback
    return copy(
        primary = role("primary", primary),
        onPrimary = role("onPrimary", onPrimary),
        primaryContainer = role("primaryContainer", primaryContainer),
        onPrimaryContainer = role("onPrimaryContainer", onPrimaryContainer),
        secondary = role("secondary", secondary),
        tertiary = role("tertiary", tertiary),
        error = role("error", error),
        background = role("background", background),
        onBackground = role("onBackground", onBackground),
        surface = role("surface", surface),
        onSurface = role("onSurface", onSurface),
        surfaceVariant = role("surfaceVariant", surfaceVariant),
        onSurfaceVariant = role("onSurfaceVariant", onSurfaceVariant),
        outline = role("outline", outline),
    )
}
