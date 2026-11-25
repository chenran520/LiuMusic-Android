package com.liuguanyi.liumusic.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liuguanyi.liumusic.ui.glass.LiuMusicGlassIconButton
import com.liuguanyi.liumusic.ui.glass.LiuMusicSymbol
import com.liuguanyi.liumusic.ui.layout.LiuMusicWindowWidthClass
import com.liuguanyi.liumusic.ui.layout.rememberLiuMusicWindowInfo

/** Classic shell preserves its visual language for every player page and window class. */
@Composable
internal fun LiuMusicClassicNowPlayingScene(
    state: LiuMusicPlaybackUiState,
    page: LiuMusicNowPlayingPage,
    onPageChanged: (LiuMusicNowPlayingPage) -> Unit,
    onDismiss: () -> Unit,
    onShowActions: () -> Unit,
    onShowQuality: () -> Unit,
) {
    val window = rememberLiuMusicWindowInfo()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = window.isLandscape || window.widthClass != LiuMusicWindowWidthClass.Compact
        val tabletPortrait = window.widthClass != LiuMusicWindowWidthClass.Compact && !window.isLandscape
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        Column(
            Modifier
                .fillMaxSize()
                .widthIn(max = window.maxContentWidth)
                .padding(horizontal = window.gutter),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LiuMusicGlassIconButton(LiuMusicSymbol.Xmark, onDismiss, contentDescription = "收起播放器")
                Text("正在播放", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                LiuMusicGlassIconButton(LiuMusicSymbol.Ellipsis, onShowActions, contentDescription = "播放操作")
            }
            if (page == LiuMusicNowPlayingPage.Artwork && wide) {
                Row(
                    Modifier.fillMaxSize().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(38.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Artwork(
                        state.artworkUrl,
                        Modifier.weight(.46f).fillMaxWidth().clip(RoundedCornerShape(18.dp)),
                    )
                    Column(Modifier.weight(.54f).widthIn(max = 560.dp), verticalArrangement = Arrangement.Center) {
                        ClassicMetadata(state)
                        Spacer(Modifier.height(20.dp))
                        LiuMusicNowPlayingCoreControls(state, page, onPageChanged, onShowQuality)
                    }
                }
            } else if (page == LiuMusicNowPlayingPage.Artwork) {
                Box(Modifier.weight(1f).fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                    Artwork(
                        state.artworkUrl,
                        Modifier.size(if (tabletPortrait) 420.dp else minOf(availableWidth - 38.dp, availableHeight * .43f)).clip(RoundedCornerShape(18.dp)),
                    )
                }
                ClassicMetadata(state)
                Spacer(Modifier.height(8.dp))
                LiuMusicNowPlayingCoreControls(state, page, onPageChanged, onShowQuality)
            } else if (wide) {
                Row(
                    Modifier.fillMaxSize().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Artwork(
                        state.artworkUrl,
                        Modifier
                            .weight(.40f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp)),
                    )
                    ClassicAlternatePage(
                        state = state,
                        page = page,
                        onPageChanged = onPageChanged,
                        onShowQuality = onShowQuality,
                        modifier = Modifier.weight(.60f).fillMaxSize(),
                    )
                }
            } else {
                ClassicMetadata(state)
                ClassicAlternatePage(
                    state = state,
                    page = page,
                    onPageChanged = onPageChanged,
                    onShowQuality = onShowQuality,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun ClassicAlternatePage(
    state: LiuMusicPlaybackUiState,
    page: LiuMusicNowPlayingPage,
    onPageChanged: (LiuMusicNowPlayingPage) -> Unit,
    onShowQuality: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier) {
        ClassicMetadata(state)
        Spacer(Modifier.height(12.dp))
        when (page) {
            LiuMusicNowPlayingPage.Lyrics -> LiuMusicIOSLyricsPanel(
                state = state,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                allowAutomaticSkyline = false,
            )
            LiuMusicNowPlayingPage.Queue -> LiuMusicQueuePanel(
                state = state,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                showSongHeader = false,
                interactive = true,
            )
            LiuMusicNowPlayingPage.Artwork -> Unit
        }
        Spacer(Modifier.height(8.dp))
        LiuMusicNowPlayingCoreControls(
            state = state,
            page = page,
            onPageSelected = onPageChanged,
            onShowQuality = onShowQuality,
        )
    }
}

@Composable
private fun ClassicMetadata(state: LiuMusicPlaybackUiState) {
    Column(Modifier.fillMaxWidth()) {
        Text(state.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 23.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(state.artist, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .56f), fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
