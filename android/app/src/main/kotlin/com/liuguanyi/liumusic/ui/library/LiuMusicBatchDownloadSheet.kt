package com.liuguanyi.liumusic.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liuguanyi.liumusic.R
import com.liuguanyi.liumusic.core.audio.MusicQuality
import com.liuguanyi.liumusic.core.audio.MusicQualityPreferences
import com.liuguanyi.liumusic.core.download.LiuMusicDownloadPlaylistRef
import com.liuguanyi.liumusic.core.download.LiuMusicDownloadStore
import com.liuguanyi.liumusic.core.model.SearchSong
import com.liuguanyi.liumusic.ui.glass.LiuMusicGlassButton
import com.liuguanyi.liumusic.ui.glass.LiuMusicGlassButtonStyle
import com.liuguanyi.liumusic.ui.glass.LiuMusicGlassSheet
import com.liuguanyi.liumusic.ui.glass.LiuMusicIosGroupedList
import com.liuguanyi.liumusic.ui.glass.LiuMusicIosListRow
import com.liuguanyi.liumusic.ui.glass.LiuMusicSymbol
import com.liuguanyi.liumusic.ui.glass.LiuMusicSymbolIcon

/** Multi-selection and explicit quality gate shared by playlists, albums and rankings. */
@Composable
fun LiuMusicBatchDownloadSheet(
    songs: List<SearchSong>,
    sourcePlaylist: LiuMusicDownloadPlaylistRef? = null,
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    val context = LocalContext.current.applicationContext
    val store = remember(context) { LiuMusicDownloadStore.get(context) }
    var selectedIds by remember(songs) { mutableStateOf(songs.map(SearchSong::id).toSet()) }
    var quality by remember { mutableStateOf(MusicQualityPreferences.read(context)) }
    val selectedSongs = remember(songs, selectedIds) { songs.filter { it.id in selectedIds } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp),
        dragHandle = {
            Box(Modifier.fillMaxWidth().height(18.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(width = 58.dp, height = 4.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .24f), RoundedCornerShape(99.dp)),
                )
            }
        },
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.88f).padding(horizontal = 18.dp, vertical = 18.dp)) {
            Text(stringResource(R.string.download_options), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f), fontSize = 13.sp)
            Text(stringResource(R.string.download_batch), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 3.dp))
            Text(
                stringResource(R.string.download_batch_hint),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .52f),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    Text(stringResource(R.string.player_quality_title), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f), fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                    LiuMusicIosGroupedList(surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        MusicQuality.entries.forEachIndexed { index, option ->
                            LiuMusicIosListRow(
                                title = option.title,
                                leading = { LiuMusicSymbolIcon(LiuMusicSymbol.MusicNote, Modifier.size(21.dp), MaterialTheme.colorScheme.onSurface, iconSize = 20.sp) },
                                trailing = { if (quality == option) Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                                showTopSeparator = index > 0,
                                onClick = { quality = option },
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.download_song_progress, selectedIds.size, songs.size), fontWeight = FontWeight.SemiBold)
                        Text(
                            if (selectedIds.size == songs.size) stringResource(R.string.library_deselect_all) else stringResource(R.string.library_select_all),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                selectedIds = if (selectedIds.size == songs.size) emptySet() else songs.map(SearchSong::id).toSet()
                            },
                        )
                    }
                }
                items(songs, key = SearchSong::id) { song ->
                    val selected = song.id in selectedIds
                    LiuMusicIosListRow(
                        title = song.name,
                        subtitle = song.artists,
                        leading = { LiuMusicSymbolIcon(LiuMusicSymbol.Download, Modifier.size(21.dp), MaterialTheme.colorScheme.onSurface, iconSize = 20.sp) },
                        trailing = { Text(if (selected) "✓" else "○", color = MaterialTheme.colorScheme.primary) },
                        onClick = { selectedIds = if (selected) selectedIds - song.id else selectedIds + song.id },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LiuMusicGlassButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    style = LiuMusicGlassButtonStyle.Bordered,
                ) { Text(stringResource(R.string.action_cancel)) }
                LiuMusicGlassButton(
                    onClick = {
                        selectedSongs.forEach { store.start(it, quality, sourcePlaylist) }
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = selectedSongs.isNotEmpty(),
                    style = LiuMusicGlassButtonStyle.BorderedProminent,
                ) { Text(stringResource(R.string.download_count, selectedSongs.size)) }
            }
        }
    }
}
