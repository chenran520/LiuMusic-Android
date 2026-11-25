package com.liuguanyi.liumusic.ui.provider

import androidx.compose.runtime.Composable
import com.liuguanyi.liumusic.core.music.model.MusicSource
import com.liuguanyi.liumusic.ui.search.SearchScreen

/**
 * Compatibility entry point kept while LiuMusicApp still references the old name.
 * The provider-specific search UI has been removed: all services render the
 * already-migrated LiuMusic SearchScreen and only swap the backing data source.
 */
@Composable
fun ProviderSearchScreen(source: MusicSource) = SearchScreen(source)
