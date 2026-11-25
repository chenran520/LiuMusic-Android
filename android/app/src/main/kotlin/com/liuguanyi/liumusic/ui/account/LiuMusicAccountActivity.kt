package com.liuguanyi.liumusic.ui.account

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.liuguanyi.liumusic.R
import com.liuguanyi.liumusic.core.account.NeteaseSessionStore
import com.liuguanyi.liumusic.core.library.NeteaseLibraryClient
import com.liuguanyi.liumusic.core.library.NeteasePlaylistSummary
import com.liuguanyi.liumusic.core.network.LiuMusicAccountDetail
import com.liuguanyi.liumusic.core.network.LiuMusicUserPlayRecord
import com.liuguanyi.liumusic.core.network.LiuMusicUserPlayRecordPeriod
import com.liuguanyi.liumusic.core.network.NeteaseAccountDetailsClient
import com.liuguanyi.liumusic.core.network.NeteaseSocialExtrasClient
import com.liuguanyi.liumusic.playback.PlaybackCommands
import com.liuguanyi.liumusic.ui.LiuMusicBottomContentClearance
import com.liuguanyi.liumusic.ui.finishLiuMusicPage
import com.liuguanyi.liumusic.ui.prepareLiuMusicPageWindow
import com.liuguanyi.liumusic.ui.startLiuMusicPage
import com.liuguanyi.liumusic.ui.glass.LiuMusicGlassButton
import com.liuguanyi.liumusic.ui.glass.LiuMusicGlassButtonStyle
import com.liuguanyi.liumusic.ui.glass.LiuMusicGlassCard
import com.liuguanyi.liumusic.ui.glass.LiuMusicGlassSegmentedControl
import com.liuguanyi.liumusic.ui.glass.LiuMusicPinnedListPage
import com.liuguanyi.liumusic.ui.glass.LiuMusicShapes
import com.liuguanyi.liumusic.ui.library.LiuMusicUnifiedPlaylistDetailScreen
import com.liuguanyi.liumusic.ui.theme.LiuMusicTheme
import com.liuguanyi.liumusic.ui.LiuMusicPageHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LiuMusicAccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        if (userId <= 0L) {
            finish()
            return
        }
        enableEdgeToEdge()
        prepareLiuMusicPageWindow()
        setContent {
            LiuMusicPageHost {
                LiuMusicTheme {
                    AccountHomeScreen(userId, ::finishLiuMusicPage)
                }
            }
        }
    }

    companion object {
        private const val EXTRA_USER_ID = "user_id"

        fun launch(context: Context, userId: Long) {
            if (userId <= 0L) return
            context.startLiuMusicPage(
                Intent(context, LiuMusicAccountActivity::class.java)
                    .putExtra(EXTRA_USER_ID, userId)
                    .apply { if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
            )
        }
    }
}

@Composable
private fun AccountHomeScreen(userId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext
    val cookie = remember(app) { { NeteaseSessionStore.readCookie(app) } }
    val details = remember(app) { NeteaseAccountDetailsClient(cookie) }
    val social = remember(app) { NeteaseSocialExtrasClient(cookie) }
    val library = remember(app) { NeteaseLibraryClient(cookie) }
    val scope = rememberCoroutineScope()
    var profile by remember(userId) { mutableStateOf<LiuMusicAccountDetail?>(null) }
    var playlists by remember(userId) { mutableStateOf<List<NeteasePlaylistSummary>>(emptyList()) }
    var period by remember(userId) { mutableStateOf(LiuMusicUserPlayRecordPeriod.Week) }
    var records by remember(userId) { mutableStateOf<List<LiuMusicUserPlayRecord>>(emptyList()) }
    var loading by remember(userId) { mutableStateOf(true) }
    var error by remember(userId) { mutableStateOf<String?>(null) }
    var selectedPlaylist by remember(userId) { mutableStateOf<NeteasePlaylistSummary?>(null) }

    suspend fun loadRank(requested: LiuMusicUserPlayRecordPeriod) {
        period = requested
        loading = true
        error = null
        runCatching { social.userPlayRecords(userId, requested) }
            .onSuccess { records = it }
            .onFailure { error = it.message ?: context.getString(R.string.player_rank_failed) }
        loading = false
    }

    suspend fun refresh() {
        loading = true
        error = null
        runCatching {
            details.userDetail(userId) to withContext(Dispatchers.IO) { library.userPlaylistsBlocking(userId) }
        }.onSuccess { (loadedProfile, loadedPlaylists) ->
            profile = loadedProfile
            playlists = loadedPlaylists
            runCatching { social.userPlayRecords(userId, period) }
                .onSuccess { records = it }
                .onFailure { error = it.message ?: context.getString(R.string.player_rank_failed) }
        }.onFailure { error = it.message ?: context.getString(R.string.account_profile_failed) }
        loading = false
    }

    LaunchedEffect(userId) { refresh() }

    selectedPlaylist?.let { playlist ->
        LiuMusicUnifiedPlaylistDetailScreen(playlist = playlist, onBack = { selectedPlaylist = null })
        return
    }

    LiuMusicPinnedListPage(
        title = stringResource(R.string.account_profile_title),
        onNavigateBack = onBack,
        bottomPadding = LiuMusicBottomContentClearance,
    ) {
        profile?.let { value ->
            item(key = "account-hero") {
                AccountHero(value, playlists.size)
            }
        }

        if (loading && profile == null) {
            item(key = "loading") {
                Box(Modifier.fillMaxWidth().padding(64.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }

        error?.let { message ->
            item(key = "error") {
                LiuMusicGlassCard(Modifier.fillMaxWidth()) {
                    Text(message, color = MaterialTheme.colorScheme.error)
                    LiuMusicGlassButton(
                        onClick = { scope.launch { refresh() } },
                        modifier = Modifier.fillMaxWidth(),
                        style = LiuMusicGlassButtonStyle.BorderedProminent,
                        shape = LiuMusicShapes.capsule,
                    ) { Text(stringResource(R.string.account_retry)) }
                }
            }
        }

        if (profile != null) {
            item(key = "rank-title") {
                Text(stringResource(R.string.account_listening_rank), fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp, start = 4.dp))
            }
            item(key = "rank-period") {
                LiuMusicGlassSegmentedControl(
                    items = listOf(stringResource(R.string.player_rank_week), stringResource(R.string.player_rank_all_time)),
                    selectedIndex = if (period == LiuMusicUserPlayRecordPeriod.Week) 0 else 1,
                    onSelected = { index ->
                        val requested = if (index == 0) LiuMusicUserPlayRecordPeriod.Week else LiuMusicUserPlayRecordPeriod.AllTime
                        if (requested != period) scope.launch { loadRank(requested) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            records.take(100).forEachIndexed { index, record ->
                item(key = "rank-${period.name}-${record.song.id}") {
                    AccountRankRow(index, record) {
                        PlaybackCommands.playQueue(context, records.map(LiuMusicUserPlayRecord::song), record.song.id)
                    }
                }
            }
        }

        if (playlists.isNotEmpty()) {
            item(key = "playlist-title") {
                Text(stringResource(R.string.account_playlists_count, playlists.size), fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp, start = 4.dp))
            }
            playlists.forEach { playlist ->
                item(key = "playlist-${playlist.id}") {
                    AccountPlaylistRow(playlist) { selectedPlaylist = playlist }
                }
            }
        }
    }
}

@Composable
private fun AccountHero(profile: LiuMusicAccountDetail, playlistCount: Int) {
    LiuMusicGlassCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(132.dp).clip(CircleShape),
            )
            Text(profile.nickname, fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            profile.signature?.takeIf(String::isNotBlank)?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            Text(stringResource(R.string.account_level_listens, profile.level, profile.listenSongs), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.account_user_id, profile.userId), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .72f))
            Row(Modifier.fillMaxWidth().padding(top = 5.dp)) {
                AccountMetric(profile.follows, stringResource(R.string.account_following), Modifier.weight(1f))
                AccountMetric(profile.followers, stringResource(R.string.account_followers), Modifier.weight(1f))
                AccountMetric(profile.playlistCount.takeIf { it > 0 } ?: playlistCount, stringResource(R.string.account_playlists), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AccountMetric(value: Int, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AccountRankRow(index: Int, record: LiuMusicUserPlayRecord, onClick: () -> Unit) {
    LiuMusicGlassCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${index + 1}", modifier = Modifier.size(30.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            AsyncImage(record.song.artworkUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)))
            Column(Modifier.weight(1f).padding(start = 11.dp)) {
                Text(record.song.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(record.song.artists, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(stringResource(R.string.player_play_count, record.playCount), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun AccountPlaylistRow(playlist: NeteasePlaylistSummary, onClick: () -> Unit) {
    LiuMusicGlassCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(playlist.coverUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.size(54.dp).clip(RoundedCornerShape(11.dp)))
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(playlist.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(stringResource(R.string.player_track_count, playlist.trackCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
