package com.liuguanyi.liumusic.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.liuguanyi.liumusic.core.remoteconfig.LiuMusicHotStrings

/**
 * UI text with a signed hot-update override.
 *
 * The app inlines a lot of copy, so [hotText] takes an explicit stable key and
 * the built-in default; [hotStringResource] does the same for a string resource
 * looked up by its entry name. Both fall back to the built-in text whenever the
 * remote config carries no override, so a build behaves identically offline.
 */

/** Override for inlined UI text. `key` must be stable across versions. */
@Composable
fun hotText(key: String, default: String, vararg args: Any): String {
    val overrides by LiuMusicHotStrings.overrides.collectAsState()
    val localeTag = currentLocaleTag(LocalContext.current)
    val text = LiuMusicHotStrings.override(localeTag, key, overrides) ?: default
    return formatArgs(text, args)
}

/** Override for a string resource, looked up by its resource entry name. */
@Composable
fun hotStringResource(@StringRes id: Int, vararg args: Any): String {
    val context = LocalContext.current
    val overrides by LiuMusicHotStrings.overrides.collectAsState()
    val default = stringResource(id, *args)
    val key = runCatching { context.resources.getResourceEntryName(id) }.getOrNull() ?: return default
    val text = LiuMusicHotStrings.override(currentLocaleTag(context), key, overrides) ?: return default
    return formatArgs(text, args)
}

private fun currentLocaleTag(context: Context): String {
    val locales = context.resources.configuration.locales
    val locale = if (locales.isEmpty) null else locales[0]
    return locale?.toLanguageTag()?.takeIf { it.isNotBlank() } ?: "default"
}

private fun formatArgs(text: String, args: Array<out Any>): String {
    if (args.isEmpty()) return text
    return runCatching { String.format(text, *args) }.getOrDefault(text)
}
