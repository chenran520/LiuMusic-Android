package com.liuguanyi.liumusic.core.provider.kuwo

import com.liuguanyi.liumusic.core.lyrics.LyricsDocument
import com.liuguanyi.liumusic.core.music.model.AudioQualityTier
import com.liuguanyi.liumusic.core.music.model.MusicPage
import com.liuguanyi.liumusic.core.music.model.MusicAccountSummary
import com.liuguanyi.liumusic.core.music.model.MusicPlaylistDetail
import com.liuguanyi.liumusic.core.music.model.MusicPlaylistSummary
import com.liuguanyi.liumusic.core.music.model.MusicSource
import com.liuguanyi.liumusic.core.music.model.MusicTrack
import com.liuguanyi.liumusic.core.music.model.PlaybackResolution
import com.liuguanyi.liumusic.core.music.provider.LyricsCapability
import com.liuguanyi.liumusic.core.music.provider.MusicCapability
import com.liuguanyi.liumusic.core.music.provider.MusicProvider
import com.liuguanyi.liumusic.core.music.provider.PlaybackCapability
import com.liuguanyi.liumusic.core.music.provider.PlaylistCapability
import com.liuguanyi.liumusic.core.music.provider.SearchCapability
import com.liuguanyi.liumusic.core.music.provider.UserLibraryCapability
import okhttp3.OkHttpClient

class KuwoProvider(
    private val sessionProvider: () -> KuwoSession = { KuwoSession("", "", "") },
    httpClient: OkHttpClient = com.liuguanyi.liumusic.core.network.LiuMusicHttpClient.shared,
) : MusicProvider,
    SearchCapability,
    PlaybackCapability,
    LyricsCapability,
    UserLibraryCapability,
    PlaylistCapability {
    override val source: MusicSource = MusicSource.Kuwo
    override val displayName: String = source.displayName
    override val capabilities: Set<MusicCapability> = setOf(
        MusicCapability.Search,
        MusicCapability.Playback,
        MusicCapability.Lyrics,
        MusicCapability.Library,
        MusicCapability.Playlists,
    )

    private val api = KuwoApiClient(httpClient)
    private val playlists = KuwoPlaylistClient(sessionProvider, httpClient)
    private val lyricsClient = KuwoLyricsClient(httpClient)

    override suspend fun searchSongs(
        query: String,
        page: Int,
        pageSize: Int,
    ): MusicPage<MusicTrack> = api.searchSongs(query, page, pageSize)

    override suspend fun resolvePlayback(
        track: MusicTrack,
        quality: AudioQualityTier,
    ): PlaybackResolution = api.resolvePlayback(track, quality)

    override suspend fun lyrics(track: MusicTrack): LyricsDocument = lyricsClient.lyrics(track)

    override suspend fun accountSummary(): MusicAccountSummary? = sessionProvider().takeIf(KuwoSession::isLoggedIn)?.let {
        MusicAccountSummary(source, it.userId, it.nickname.ifBlank { "酷我用户" }, subtitle = "手机号登录")
    }

    override suspend fun userPlaylists(page: Int, pageSize: Int): MusicPage<MusicPlaylistSummary> =
        playlists.userPlaylists(page, pageSize)

    override suspend fun playlistDetail(
        playlist: MusicPlaylistSummary,
        page: Int,
        pageSize: Int,
    ): MusicPlaylistDetail = playlists.detail(playlist, page, pageSize)
}
