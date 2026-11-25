package com.liuguanyi.liumusic.ui.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.liuguanyi.liumusic.R
import com.liuguanyi.liumusic.core.account.NeteaseSessionStore
import com.liuguanyi.liumusic.core.audio.MusicQualityPreferences
import com.liuguanyi.liumusic.core.download.LiuMusicDownloadPlaylistRef
import com.liuguanyi.liumusic.core.download.LiuMusicDownloadStore
import com.liuguanyi.liumusic.core.library.NeteaseLibraryClient
import com.liuguanyi.liumusic.core.library.NeteasePlaylistSummary
import com.liuguanyi.liumusic.core.model.SearchSong
import com.liuguanyi.liumusic.core.music.model.MusicSource
import com.liuguanyi.liumusic.core.network.LiuMusicCommentRepliesPage
import com.liuguanyi.liumusic.core.network.LiuMusicListenTogetherRoom
import com.liuguanyi.liumusic.core.network.LiuMusicMusicComment
import com.liuguanyi.liumusic.core.network.LiuMusicSearchKind
import com.liuguanyi.liumusic.core.network.LiuMusicUserPlayRecord
import com.liuguanyi.liumusic.core.network.LiuMusicUserPlayRecordPeriod
import com.liuguanyi.liumusic.ui.song.LiuMusicSongWikiActivity
import com.liuguanyi.liumusic.core.network.LiuMusicWikiSection
import com.liuguanyi.liumusic.core.network.NeteaseMusicOperationsClient
import com.liuguanyi.liumusic.core.network.NeteaseSearchClient
import com.liuguanyi.liumusic.core.network.NeteaseSocialExtrasClient
import com.liuguanyi.liumusic.playback.LiuMusicListenTogetherCoordinator
import com.liuguanyi.liumusic.playback.PlaybackCommands
import com.liuguanyi.liumusic.ui.glass.LiuMusicActionIcon
import com.liuguanyi.liumusic.ui.glass.LiuMusicIosGroupedList
import com.liuguanyi.liumusic.ui.glass.LiuMusicIosListRow
import com.liuguanyi.liumusic.ui.glass.LiuMusicSymbol
import com.liuguanyi.liumusic.ui.glass.LiuMusicSymbolIcon
import com.liuguanyi.liumusic.ui.animation.liuMusicPanelEnter
import com.liuguanyi.liumusic.ui.animation.liuMusicPanelExit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class SongActionPage {
    Main,
    Sleep,
    AddToPlaylist,
    CreatePlaylist,
    Comments,
    CommentReplies,
    ShareContacts,
    ListeningRank,
    Wiki,
    ListenTogether,
}

private data class LiuMusicMoreAction(
    val title: String,
    val symbol: LiuMusicSymbol,
    val onClick: () -> Unit,
)

@Composable
fun LiuMusicSongActionsOverlay(
    song: SearchSong,
    queue: List<SearchSong>,
    visible: Boolean,
    onDismiss: () -> Unit,
    playbackState: LiuMusicPlaybackUiState? = null,
    onNavigateSearch: ((String, LiuMusicSearchKind) -> Unit)? = null,
    sourcePlaylist: LiuMusicDownloadPlaylistRef? = null,
    sourceOwnedPlaylistId: Long? = null,
    onSourcePlaylistChanged: (() -> Unit)? = null,
    // ★ 「天际歌词」入口（2026-10-07）。只有平板双栏传得进来，见
    //   [LiuMusicNowPlayingActionsSheet] 同名声明的注释；为 null 时本行不渲染。
    onShowSkyline: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val app = context.applicationContext
    val scope = rememberCoroutineScope()
    val cookieProvider = remember(app) { { NeteaseSessionStore.readCookie(app) } }
    val library = remember(app) { NeteaseLibraryClient(cookieProvider = cookieProvider) }
    val ops = remember(app) { NeteaseMusicOperationsClient(cookieProvider = cookieProvider) }
    val social = remember(app) { NeteaseSocialExtrasClient(cookieProvider = cookieProvider) }
    val account = remember(app) { NeteaseSearchClient(cookieProvider = cookieProvider) }
    val downloads = remember(app) { LiuMusicDownloadStore.get(app) }

    var page by remember(song.id, visible) { mutableStateOf(SongActionPage.Main) }
    var busy by remember(song.id, visible) { mutableStateOf(false) }
    var message by remember(song.id, visible) { mutableStateOf<String?>(null) }
    var liked by remember(song.id, visible) { mutableStateOf<Boolean?>(null) }
    var writablePlaylists by remember(song.id, visible) { mutableStateOf<List<NeteasePlaylistSummary>>(emptyList()) }
    var newPlaylistName by remember(song.id, visible) { mutableStateOf("") }
    var newPlaylistPrivate by remember(song.id, visible) { mutableStateOf(false) }
    var hotComments by remember(song.id, visible) { mutableStateOf<List<LiuMusicMusicComment>>(emptyList()) }
    var comments by remember(song.id, visible) { mutableStateOf<List<LiuMusicMusicComment>>(emptyList()) }
    var commentsPage by remember(song.id, visible) { mutableStateOf<com.liuguanyi.liumusic.core.network.LiuMusicCommentsPage?>(null) }
    var selectedComment by remember(song.id, visible) { mutableStateOf<LiuMusicMusicComment?>(null) }
    var repliesPage by remember(song.id, visible) { mutableStateOf<LiuMusicCommentRepliesPage?>(null) }
    var shareContacts by remember(song.id, visible) { mutableStateOf<List<com.liuguanyi.liumusic.core.network.LiuMusicMessageContact>>(emptyList()) }
    var playRecordPeriod by remember(song.id, visible) { mutableStateOf(LiuMusicUserPlayRecordPeriod.Week) }
    var playRecords by remember(song.id, visible) { mutableStateOf<List<LiuMusicUserPlayRecord>>(emptyList()) }
    var wiki by remember(song.id, visible) { mutableStateOf<List<LiuMusicWikiSection>>(emptyList()) }
    val togetherState by LiuMusicListenTogetherCoordinator.state(app).collectAsState()
    val listenRoom = togetherState.room
    var invitationText by remember(visible) { mutableStateOf("") }

    LaunchedEffect(visible, song.id) {
        if (!visible) return@LaunchedEffect
        runCatching {
            val profile = account.accountProfile()
            val ids = withContext(Dispatchers.IO) { library.likedSongIdsBlocking(profile.userId) }
            liked = song.id in ids
        }
    }
    LaunchedEffect(visible, playbackState != null) { if (!visible) return@LaunchedEffect; if (playbackState != null) LiuMusicListenTogetherCoordinator.ensureStarted(app) }

    suspend fun loadOwnedPlaylists() {
        busy = true
        message = null
        runCatching {
            val profile = account.accountProfile()
            withContext(Dispatchers.IO) { library.userPlaylistsBlocking(profile.userId) }
                .filter { it.creatorUserId == profile.userId }
        }.onSuccess { writablePlaylists = it }
            .onFailure { message = it.message ?: context.getString(R.string.player_playlists_failed) }
        busy = false
    }

    suspend fun loadComments(append: Boolean = false) { busy = true; message = null; val current = commentsPage; val offset = if (append) current?.nextOffset ?: comments.size else 0; val before = if (append) current?.beforeTime ?: 0L else 0L; runCatching { social.songComments(song.id, offset, before) }.onSuccess { loaded -> if (append) comments = (comments + loaded.comments).distinctBy(LiuMusicMusicComment::id) else { hotComments = loaded.hotComments; comments = loaded.comments }; commentsPage = loaded.copy(comments = comments, nextOffset = comments.size) }.onFailure { message = it.message ?: context.getString(R.string.player_comments_failed) }; busy = false }
    suspend fun loadShareContacts() { busy = true; message = null; runCatching { val profile = account.accountProfile(); ops.messageContacts(profile.userId) }.onSuccess { shareContacts = it }.onFailure { message = it.message ?: context.getString(R.string.player_contacts_failed) }; busy = false }

    suspend fun loadReplies(parent: LiuMusicMusicComment, append: Boolean) {
        busy = true
        message = null
        val current = repliesPage
        val requestTime = if (append) current?.nextTime ?: -1L else -1L
        runCatching { social.songCommentReplies(song.id, parent.id, requestTime) }
            .onSuccess { loaded ->
                repliesPage = if (!append || current == null) {
                    loaded
                } else {
                    loaded.copy(
                        ownerComment = loaded.ownerComment ?: current.ownerComment,
                        replies = (current.replies + loaded.replies).distinctBy(LiuMusicMusicComment::id),
                        totalCount = maxOf(current.totalCount, loaded.totalCount),
                    )
                }
            }
            .onFailure { message = it.message ?: context.getString(R.string.player_replies_failed) }
        busy = false
    }

    suspend fun loadPlayRecords(period: LiuMusicUserPlayRecordPeriod) {
        busy = true
        message = null
        playRecordPeriod = period
        runCatching {
            val profile = account.accountProfile()
            social.userPlayRecords(profile.userId, period)
        }.onSuccess { playRecords = it }
            .onFailure { message = it.message ?: context.getString(R.string.player_rank_failed) }
        busy = false
    }

    suspend fun loadWiki() {
        busy = true
        message = null
        runCatching { ops.songWiki(song.id) }
            .onSuccess { wiki = it }
            .onFailure { message = it.message ?: context.getString(R.string.player_wiki_failed) }
        busy = false
    }

    BackHandler(enabled = visible) {
        page = when (page) {
            SongActionPage.CommentReplies -> SongActionPage.Comments
            SongActionPage.Main -> {
                onDismiss()
                SongActionPage.Main
            }
            else -> SongActionPage.Main
        }
    }

    if (visible) ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp),
        dragHandle = {
            Box(
                Modifier.fillMaxWidth().height(18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.size(width = 58.dp, height = 4.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .24f), RoundedCornerShape(99.dp)),
                )
            }
        },
    ) {
        AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        liuMusicPanelEnter() togetherWith liuMusicPanelExit()
                    },
            modifier = Modifier.fillMaxWidth(),
            label = "song-action-page",
        ) { target ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                    ) {
                        ActionHeader(
                            song,
                            when (target) {
                                SongActionPage.Main -> stringResource(R.string.player_action_title)
                                SongActionPage.Sleep -> stringResource(R.string.player_sleep_timer)
                                SongActionPage.AddToPlaylist -> stringResource(R.string.player_add_to_playlist)
                                SongActionPage.CreatePlaylist -> stringResource(R.string.player_create_playlist)
                                SongActionPage.Comments -> stringResource(R.string.player_comments)
                                SongActionPage.CommentReplies -> stringResource(R.string.player_comment_replies)
                                SongActionPage.ShareContacts -> stringResource(R.string.player_share_netease_friends)
                                SongActionPage.ListeningRank -> stringResource(R.string.player_listening_rank)
                                SongActionPage.Wiki -> stringResource(R.string.player_wiki)
                                SongActionPage.ListenTogether -> stringResource(R.string.player_listen_together)
                            },
                        )
                        message?.let {
                            Text(
                                it,
                                color = Color(0xFFFF8A90),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }

                        when (target) {
                            SongActionPage.Main -> {
                                Text(stringResource(R.string.player_more_actions), Modifier.padding(bottom = 10.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                // ★ 平板双栏专属：⋯ 里补一条进「天际歌词」的通道（2026-10-07）。
                                //   放**第一条** —— 它切的是"整屏呈现方式"，与下面那一串歌曲操作不是
                                //   一类，混在中间会找不到。`onShowSkyline == null`（手机 / 竖屏）时
                                //   `listOfNotNull` 直接滤掉 ⇒ 那两端的菜单逐像素不变。
                                val actions = listOfNotNull(
                                    onShowSkyline?.let { openSkyline ->
                                        LiuMusicMoreAction(stringResource(R.string.player_skyline_lyrics), LiuMusicSymbol.Lyrics) {
                                            openSkyline()
                                            onDismiss()
                                        }
                                    },
                                    LiuMusicMoreAction(stringResource(R.string.player_sleep_timer), LiuMusicSymbol.Moon) { page = SongActionPage.Sleep },
                                    LiuMusicMoreAction(stringResource(R.string.player_play_next), LiuMusicSymbol.Next) {
                                        if (playbackState == null) PlaybackCommands.playNext(context, song)
                                        onDismiss()
                                    },
                                    LiuMusicMoreAction(stringResource(R.string.player_add_to_queue), LiuMusicSymbol.AddToPlaylist) {
                                        if (playbackState != null) playbackState.addCurrentToQueue() else PlaybackCommands.addToQueue(context, song)
                                        onDismiss()
                                    },
                                    LiuMusicMoreAction(if (downloads.contains(song.id)) stringResource(R.string.player_delete_download) else stringResource(R.string.player_download_song), if (downloads.contains(song.id)) LiuMusicSymbol.Trash else LiuMusicSymbol.Download) {
                                        if (downloads.contains(song.id)) downloads.remove(song.id) else downloads.start(song, MusicQualityPreferences.read(app), sourcePlaylist)
                                    },
                                    LiuMusicMoreAction(stringResource(R.string.player_add_to_playlist), LiuMusicSymbol.AddToPlaylist) {
                                        page = SongActionPage.AddToPlaylist
                                        scope.launch { loadOwnedPlaylists() }
                                    },
                                    LiuMusicMoreAction(if (liked == true) stringResource(R.string.player_unlike) else stringResource(R.string.player_like), LiuMusicSymbol.Heart) {
                                        val desired = liked != true
                                        busy = true
                                        scope.launch {
                                            runCatching { ops.setSongLiked(song.id, desired) }
                                                .onSuccess { liked = desired }
                                                .onFailure { message = it.message }
                                            busy = false
                                        }
                                    },
                                    LiuMusicMoreAction(stringResource(R.string.player_system_share), LiuMusicSymbol.Share) { shareSong(context, song); onDismiss() },
                                    LiuMusicMoreAction(stringResource(R.string.player_share_netease_friends), LiuMusicSymbol.Mail) { page = SongActionPage.ShareContacts; scope.launch { loadShareContacts() } },
                                    LiuMusicMoreAction(stringResource(R.string.player_share_timeline), LiuMusicSymbol.Message) {
                                        if (!busy) {
                                            busy = true
                                            scope.launch {
                                                runCatching { social.shareSongToTimeline(song.id) }
                                                    .onSuccess { message = context.getString(R.string.player_shared_timeline) }
                                                    .onFailure { message = it.message ?: context.getString(R.string.player_share_timeline_failed) }
                                                busy = false
                                            }
                                        }
                                    },
                                    LiuMusicMoreAction(stringResource(R.string.player_view_comments), LiuMusicSymbol.Comment) { page = SongActionPage.Comments; scope.launch { loadComments(false) } },
                                    LiuMusicMoreAction(stringResource(R.string.player_listening_rank), LiuMusicSymbol.Clock) { page = SongActionPage.ListeningRank; scope.launch { loadPlayRecords(LiuMusicUserPlayRecordPeriod.Week) } },
                                    LiuMusicMoreAction(stringResource(R.string.player_wiki), LiuMusicSymbol.Book) {
                                        LiuMusicSongWikiActivity.launch(context, song)
                                        onDismiss()
                                    },
                                )
                                sourceOwnedPlaylistId?.let { playlistId ->
                                    ActionItem(stringResource(R.string.player_remove_from_playlist), "−") {
                                        if (!busy) {
                                            busy = true
                                            scope.launch {
                                                runCatching { ops.removeSongFromPlaylist(song.id, playlistId) }
                                                    .onSuccess { onSourcePlaylistChanged?.invoke(); onDismiss() }
                                                    .onFailure { message = it.message ?: context.getString(R.string.player_remove_song_failed) }
                                                busy = false
                                            }
                                        }
                                    }
                                }
                                if (playbackState != null) {
                                    ActionItem(stringResource(R.string.player_listen_together), "◎") { page = SongActionPage.ListenTogether }
                                }
                                if (song.album.isNotBlank() && onNavigateSearch != null) {
                                    ActionItem(stringResource(R.string.player_go_album, song.album), "▣") { onDismiss(); onNavigateSearch(song.album, LiuMusicSearchKind.Albums) }
                                }
                                if (song.artists.isNotBlank() && onNavigateSearch != null) {
                                    ActionItem(stringResource(R.string.player_go_artist, song.artists), "♬") { onDismiss(); onNavigateSearch(song.artists.substringBefore(" / "), LiuMusicSearchKind.Artists) }
                                }
                                LiuMusicIosGroupedList(surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                                    actions.forEachIndexed { index, action ->
                                        LiuMusicIosListRow(
                                            title = action.title,
                                            leading = { LiuMusicSymbolIcon(action.symbol, Modifier.size(22.dp), MaterialTheme.colorScheme.onSurface, iconSize = 21.sp) },
                                            showTopSeparator = index > 0,
                                            onClick = action.onClick,
                                        )
                                    }
                                }
                            }

                            SongActionPage.Sleep -> {
                                val state = playbackState
                                if (state != null) {
                                    listOf(15, 30, 45, 60).forEach { minutes ->
                                        ActionItem(stringResource(R.string.player_sleep_minutes, minutes), "◷") { state.setSleepTimer(minutes); onDismiss() }
                                    }
                                    if (state.sleepTimerEndRealtimeMs > 0L) {
                                        ActionItem(stringResource(R.string.player_cancel_sleep), "×") { state.cancelSleepTimer(); onDismiss() }
                                    }
                                }
                                ActionItem(stringResource(R.string.player_back), "‹") { page = SongActionPage.Main }
                            }

                            SongActionPage.AddToPlaylist -> {
                                if (busy) LoadingRow(stringResource(R.string.player_loading_playlists))
                                ActionItem(stringResource(R.string.player_new_playlist), "+") {
                                    message = null
                                    page = SongActionPage.CreatePlaylist
                                }
                                writablePlaylists.forEach { playlist ->
                                    Row(
                                        Modifier.fillMaxWidth().height(58.dp).clickable(enabled = !busy) {
                                            busy = true
                                            scope.launch {
                                                runCatching { ops.addSongToPlaylist(song.id, playlist.id) }
                                                    .onSuccess { onDismiss() }
                                                    .onFailure { message = it.message }
                                                busy = false
                                            }
                                        },
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        AsyncImage(
                                            playlist.coverUrl,
                                            null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(46.dp).padding(2.dp),
                                        )
                                        Spacer(Modifier.size(10.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(playlist.name, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(stringResource(R.string.player_track_count, playlist.trackCount), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f), fontSize = 12.sp)
                                        }
                                    }
                                }
                                if (!busy && writablePlaylists.isEmpty() && message == null) {
                                    Text(stringResource(R.string.player_no_writable_playlists), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f), modifier = Modifier.padding(12.dp))
                                }
                                ActionItem(stringResource(R.string.player_back), "‹") { page = SongActionPage.Main }
                            }

                            SongActionPage.CreatePlaylist -> {
                                Text(
                                    stringResource(R.string.player_create_playlist),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .08f), RoundedCornerShape(14.dp))
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    if (newPlaylistName.isBlank()) {
                                        Text(stringResource(R.string.player_playlist_name), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .42f), fontSize = 16.sp)
                                    }
                                    BasicTextField(
                                        value = newPlaylistName,
                                        onValueChange = { newPlaylistName = it.take(40) },
                                        enabled = !busy,
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                                ActionItem(
                                    if (newPlaylistPrivate) stringResource(R.string.player_playlist_private) else stringResource(R.string.player_playlist_public),
                                    if (newPlaylistPrivate) "▣" else "◎",
                                ) { newPlaylistPrivate = !newPlaylistPrivate }
                                ActionItem(stringResource(R.string.player_create_and_add), "✓") {
                                    if (!busy && newPlaylistName.isNotBlank()) {
                                        busy = true
                                        message = null
                                        scope.launch {
                                            runCatching {
                                                val created = ops.createPlaylist(newPlaylistName, newPlaylistPrivate)
                                                ops.addSongToPlaylist(song.id, created.id)
                                                created
                                            }.onSuccess {
                                                onSourcePlaylistChanged?.invoke()
                                                onDismiss()
                                            }.onFailure { message = it.message ?: context.getString(R.string.player_playlist_create_failed) }
                                            busy = false
                                        }
                                    }
                                }
                                ActionItem(stringResource(R.string.player_back_to_playlists), "‹") { page = SongActionPage.AddToPlaylist }
                            }

                            SongActionPage.Comments -> {
                                if (busy && comments.isEmpty()) LoadingRow(stringResource(R.string.player_loading_comments))
                                if (hotComments.isNotEmpty()) Text(stringResource(R.string.player_hot_comments), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f), fontSize = 12.sp)
                                LazyColumn(Modifier.fillMaxWidth().height(360.dp)) {
                                    items(hotComments, key = { "hot-${it.id}" }) { c -> Column(Modifier.fillMaxWidth().clickable { selectedComment = c; repliesPage = null; page = SongActionPage.CommentReplies; scope.launch { loadReplies(c, false) } }.padding(vertical = 9.dp)) { CommentRow(c) } }
                                    if (comments.isNotEmpty()) item { Text(stringResource(R.string.player_latest_comments, commentsPage?.totalCount ?: comments.size), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f), fontSize = 12.sp) }
                                    items(comments, key = { "latest-${it.id}" }) { c -> Column(Modifier.fillMaxWidth().clickable { selectedComment = c; repliesPage = null; page = SongActionPage.CommentReplies; scope.launch { loadReplies(c, false) } }.padding(vertical = 9.dp)) { CommentRow(c) } }
                                }
                                if (commentsPage?.hasMore == true) ActionItem(stringResource(R.string.player_load_more_comments), "+") { if (!busy) scope.launch { loadComments(true) } }
                                ActionItem(stringResource(R.string.player_back), "‹") { page = SongActionPage.Main }
                            }
                            SongActionPage.ShareContacts -> {
                                if (busy && shareContacts.isEmpty()) LoadingRow(stringResource(R.string.player_loading_contacts))
                                LazyColumn(Modifier.fillMaxWidth().height(350.dp)) { items(shareContacts, key = { "share-${it.id}" }) { contact -> Row(Modifier.fillMaxWidth().clickable(enabled = !busy) { busy = true; scope.launch { runCatching { social.sendSongToUser(song.id, contact.id) }.onSuccess { message = context.getString(R.string.player_sent_to, contact.name); page = SongActionPage.Main }.onFailure { message = it.message ?: context.getString(R.string.player_send_failed) }; busy = false } }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { AsyncImage(contact.avatarUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.size(44.dp)); Column(Modifier.weight(1f).padding(start = 10.dp)) { Text(contact.name, color = Color.White); if (contact.signature.isNotBlank()) Text(contact.signature, color = Color.White.copy(alpha = .45f), fontSize = 11.sp, maxLines = 1) } } } }
                                ActionItem(stringResource(R.string.player_back), "‹") { page = SongActionPage.Main }
                            }
                            SongActionPage.CommentReplies -> {
                                val parent = selectedComment
                                val replies = repliesPage
                                if (parent != null) {
                                        Text(stringResource(R.string.player_original_comment), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f), fontSize = 12.sp)
                                    CommentRow(replies?.ownerComment ?: parent)
                                }
                                if (busy && replies == null) LoadingRow(stringResource(R.string.player_loading_replies))
                                LazyColumn(Modifier.fillMaxWidth().height(280.dp)) {
                                    items(replies?.replies.orEmpty(), key = { it.id }) { reply -> CommentRow(reply) }
                                }
                                replies?.let {
                                    Text(
                                        if (it.totalCount > 0) stringResource(R.string.player_all_replies, it.totalCount) else stringResource(R.string.player_no_replies),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    )
                                    if (it.hasMore && parent != null) {
                                        ActionItem(stringResource(R.string.player_load_more_replies), "+") {
                                            if (!busy) scope.launch { loadReplies(parent, append = true) }
                                        }
                                    }
                                }
                                ActionItem(stringResource(R.string.player_back_to_comments), "‹") { page = SongActionPage.Comments }
                            }

                            SongActionPage.ListeningRank -> {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    RankPeriodButton(
                                        title = stringResource(R.string.player_rank_week),
                                        selected = playRecordPeriod == LiuMusicUserPlayRecordPeriod.Week,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        if (!busy) scope.launch { loadPlayRecords(LiuMusicUserPlayRecordPeriod.Week) }
                                    }
                                    RankPeriodButton(
                                        title = stringResource(R.string.player_rank_all_time),
                                        selected = playRecordPeriod == LiuMusicUserPlayRecordPeriod.AllTime,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        if (!busy) scope.launch { loadPlayRecords(LiuMusicUserPlayRecordPeriod.AllTime) }
                                    }
                                }
                                if (busy) LoadingRow(stringResource(R.string.player_loading_rank))
                                LazyColumn(Modifier.fillMaxWidth().height(330.dp)) {
                                    items(playRecords, key = { it.song.id }) { record ->
                                        Row(
                                            Modifier.fillMaxWidth().clickable {
                                                val songs = playRecords.map(LiuMusicUserPlayRecord::song)
                                                PlaybackCommands.playQueue(context, songs, record.song.id)
                                                onDismiss()
                                            }.padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            AsyncImage(
                                                record.song.artworkUrl,
                                                null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.size(44.dp).padding(2.dp),
                                            )
                                            Spacer(Modifier.size(10.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(record.song.name, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(record.song.artists, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            Text(stringResource(R.string.player_play_count, record.playCount), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f), fontSize = 12.sp)
                                        }
                                    }
                                }
                                if (!busy && playRecords.isEmpty() && message == null) {
                                    Text(stringResource(R.string.player_rank_empty), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f), modifier = Modifier.padding(12.dp))
                                }
                                ActionItem(stringResource(R.string.player_back), "‹") { page = SongActionPage.Main }
                            }

                            SongActionPage.Wiki -> {
                                if (busy) LoadingRow(stringResource(R.string.player_loading_wiki))
                                LazyColumn(Modifier.fillMaxWidth().height(360.dp)) {
                                    items(wiki, key = { it.title + it.lines.hashCode() }) { section ->
                                        Column(Modifier.fillMaxWidth().padding(vertical = 9.dp)) {
                                            Text(section.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            section.lines.forEach { line ->
                                                Text(line, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .70f), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                                            }
                                        }
                                    }
                                }
                                if (!busy && wiki.isEmpty() && message == null) {
                                    Text(stringResource(R.string.player_wiki_empty), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f), modifier = Modifier.padding(12.dp))
                                }
                                ActionItem(stringResource(R.string.player_back), "‹") { page = SongActionPage.Main }
                            }

                            SongActionPage.ListenTogether -> {
                                val room = listenRoom
                                if (room == null) {
                                    Text(
                                        stringResource(R.string.player_listen_together_hint),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(6.dp, 6.dp, 6.dp, 12.dp),
                                    )
                                    ActionItem(stringResource(R.string.player_listen_together_start), "◎") {
                                        if (!busy) {
                                            busy = true
                                            message = null
                                            scope.launch {
                                                runCatching {
                                                    val created = ops.createListenTogetherRoom()
                                                    val profile = account.accountProfile()
                                                    val ids = queue.map(SearchSong::id).filter { it > 0L }.ifEmpty { listOf(song.id) }
                                                    ops.reportListenTogetherPlaylist(created.id, profile.userId, ids, 1)
                                                    created
                                                }.onSuccess { LiuMusicListenTogetherCoordinator.adoptRoom(app, it) }.onFailure { message = it.message ?: context.getString(R.string.player_room_create_failed) }
                                                busy = false
                                            }
                                        }
                                    }
                                    BasicTextField(
                                        value = invitationText,
                                        onValueChange = { invitationText = it },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                                        decorationBox = { inner ->
                                            Box(
                                                Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp)
                                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .08f), RoundedCornerShape(14.dp))
                                                    .padding(horizontal = 12.dp, vertical = 11.dp),
                                            ) {
                                                if (invitationText.isBlank()) {
                                                    Text(stringResource(R.string.player_invite_placeholder), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .38f), fontSize = 14.sp)
                                                }
                                                inner()
                                            }
                                        },
                                    )
                                    ActionItem(stringResource(R.string.player_join_room), "→") {
                                        val parsed = parseListenTogetherInvitation(invitationText)
                                        if (parsed == null) message = context.getString(R.string.player_invite_invalid)
                                        else if (!busy) {
                                            busy = true
                                            message = null
                                            scope.launch {
                                                runCatching { ops.joinListenTogetherRoom(parsed.first, parsed.second) }
                                                    .onSuccess { LiuMusicListenTogetherCoordinator.adoptRoom(app, it) }
                                                    .onFailure { message = it.message ?: context.getString(R.string.player_join_room_failed) }
                                                busy = false
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        stringResource(R.string.player_room_members, room.id, room.users.size.coerceAtLeast(1)),
                                         color = MaterialTheme.colorScheme.onSurface.copy(alpha = .72f),
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(6.dp),
                                    )
                                    room.users.forEach { member ->
                                        Text("• ${member.name}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp))
                                    }
                                    Text(
                                        when (togetherState.phase) { LiuMusicListenTogetherCoordinator.Phase.Reconnecting -> stringResource(R.string.player_reconnecting, togetherState.consecutiveFailures); LiuMusicListenTogetherCoordinator.Phase.Connected -> stringResource(R.string.player_sync_enabled); LiuMusicListenTogetherCoordinator.Phase.Idle -> stringResource(R.string.player_restoring_room) },
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    )
                                    ActionItem(stringResource(R.string.player_share_room), "↗") { shareListenTogether(context, song.id, room) }
                                    ActionItem(stringResource(R.string.player_end_room), "×") {
                                        if (!busy) {
                                            busy = true
                                            scope.launch {
                                                runCatching { ops.endListenTogetherRoom(room.id) }
                                                    .onSuccess { LiuMusicListenTogetherCoordinator.clearRoom(app) }
                                                    .onFailure { message = it.message ?: context.getString(R.string.player_end_room_failed) }
                                                busy = false
                                            }
                                        }
                                    }
                                }
                                if (busy) LoadingRow(stringResource(R.string.player_connecting_together))
                                ActionItem(stringResource(R.string.player_back), "‹") { page = SongActionPage.Main }
                            }
                        }
        }
    }

}

}

@Composable
private fun ActionHeader(song: SearchSong, title: String) {
    val foreground = MaterialTheme.colorScheme.onSurface
    Text(title, color = foreground.copy(alpha = .58f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Text(
        song.name,
        color = foreground,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 3.dp, bottom = 10.dp),
    )
}

@Composable
private fun ActionItem(title: String, symbol: String, onClick: () -> Unit) {
    val foreground = MaterialTheme.colorScheme.onSurface
    Row(
        Modifier.fillMaxWidth().height(46.dp).clickable(onClick = onClick).padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
            LiuMusicActionIcon(
                token = symbol,
                modifier = Modifier.size(20.dp),
                color = foreground.copy(alpha = 0.86f),
            )
        }
        Text(title, color = foreground, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun CommentRow(comment: LiuMusicMusicComment) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 6.dp)) {
        Text(comment.user, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(comment.content, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .9f), fontSize = 14.sp, modifier = Modifier.padding(top = 3.dp))
        if (comment.timeText.isNotBlank() || comment.likedCount > 0L) {
            Text(
                listOf(comment.timeText.takeIf(String::isNotBlank), "♡ ${comment.likedCount}").filterNotNull().joinToString(" · "),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .42f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun RankPeriodButton(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = if (selected) .18f else .07f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(title, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (selected) 1f else .6f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LoadingRow(text: String) {
    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
        Spacer(Modifier.size(10.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f))
    }
}

private fun shareSong(context: Context, song: SearchSong) {
    val url = "https://music.163.com/song?id=${song.id}"
    runCatching {
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "${song.name} - ${song.artists}\n$url"),
                context.getString(R.string.player_share_song),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

private fun parseListenTogetherInvitation(value: String): Pair<String, String>? =
    com.liuguanyi.liumusic.core.network.parseNeteaseListenTogetherInvitation(value)
        ?.let { it.roomId to it.inviterId }

private fun shareListenTogether(context: Context, songId: Long, room: LiuMusicListenTogetherRoom) {
    val inviter = room.creatorId.ifBlank { room.users.firstOrNull()?.id.orEmpty() }
    val url = "https://st.music.163.com/listen-together/share/?songId=$songId&roomId=${Uri.encode(room.id)}&inviterId=${Uri.encode(inviter)}"
    runCatching {
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, url),
                context.getString(R.string.player_share_together_invite),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
