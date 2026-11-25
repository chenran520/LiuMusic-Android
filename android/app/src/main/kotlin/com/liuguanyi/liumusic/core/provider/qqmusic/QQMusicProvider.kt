package com.liuguanyi.liumusic.core.provider.qqmusic

import com.liuguanyi.liumusic.core.lyrics.LyricsDocument
import com.liuguanyi.liumusic.core.music.model.AudioQualityTier
import com.liuguanyi.liumusic.core.music.model.MusicAccountSummary
import com.liuguanyi.liumusic.core.music.model.MusicAlbumDetail
import com.liuguanyi.liumusic.core.music.model.MusicAlbumSummary
import com.liuguanyi.liumusic.core.music.model.MusicArtistDetail
import com.liuguanyi.liumusic.core.music.model.MusicArtistSummary
import com.liuguanyi.liumusic.core.music.model.MusicHomeFeed
import com.liuguanyi.liumusic.core.music.model.MusicPage
import com.liuguanyi.liumusic.core.music.model.MusicPlaylistDetail
import com.liuguanyi.liumusic.core.music.model.MusicPlaylistSummary
import com.liuguanyi.liumusic.core.music.model.MusicRankingSummary
import com.liuguanyi.liumusic.core.music.model.MusicSource
import com.liuguanyi.liumusic.core.music.model.MusicTrack
import com.liuguanyi.liumusic.core.music.model.PlaybackResolution
import com.liuguanyi.liumusic.core.music.provider.AlbumCapability
import com.liuguanyi.liumusic.core.music.provider.ArtistCapability
import com.liuguanyi.liumusic.core.music.provider.CatalogSearchCapability
import com.liuguanyi.liumusic.core.music.provider.FavoriteCapability
import com.liuguanyi.liumusic.core.music.provider.HomeFeedCapability
import com.liuguanyi.liumusic.core.music.provider.LyricsCapability
import com.liuguanyi.liumusic.core.music.provider.LocalAggregationCapability
import com.liuguanyi.liumusic.core.music.provider.MusicCapability
import com.liuguanyi.liumusic.core.music.provider.MusicProvider
import com.liuguanyi.liumusic.core.music.provider.PlaybackCapability
import com.liuguanyi.liumusic.core.music.provider.PlaylistCapability
import com.liuguanyi.liumusic.core.music.provider.RankingCapability
import com.liuguanyi.liumusic.core.music.provider.SearchCapability
import com.liuguanyi.liumusic.core.music.provider.UserLibraryCapability
import okhttp3.OkHttpClient

class QQMusicProvider(
    sessionProvider: () -> QQMusicSession = { QQMusicSession("", "", "") },
    httpClient: OkHttpClient = com.liuguanyi.liumusic.core.network.LiuMusicHttpClient.shared,
) : MusicProvider,
    SearchCapability,
    CatalogSearchCapability,
    LyricsCapability,
    PlaybackCapability,
    FavoriteCapability,
    HomeFeedCapability,
    UserLibraryCapability,
    PlaylistCapability,
    RankingCapability,
    AlbumCapability,
    ArtistCapability,
    LocalAggregationCapability {
    override val source: MusicSource = MusicSource.QQMusic
    override val displayName: String = source.displayName
    override val capabilities: Set<MusicCapability> = setOf(
        MusicCapability.Search,
        MusicCapability.Playback,
        MusicCapability.Lyrics,
        MusicCapability.Library,
        MusicCapability.Playlists,
        MusicCapability.Albums,
        MusicCapability.Artists,
        MusicCapability.Favorites,
        MusicCapability.HomeRecommendations,
        MusicCapability.Rankings,
    )

    private val api = QQMusicApiClient(
        sessionProvider = sessionProvider,
        httpClient = httpClient,
    )
    private val richLyrics = QQMusicRichLyricsClient(
        sessionProvider = sessionProvider,
        httpClient = httpClient,
    )
    private val playback = QQMusicPlaybackVkeyClient(
        sessionProvider = sessionProvider,
        httpClient = httpClient,
    )
    private val favorites = QQMusicFavoriteClient(
        sessionProvider = sessionProvider,
        httpClient = httpClient,
    )
    private val playlists = QQMusicPlaylistClient(
        sessionProvider = sessionProvider,
        httpClient = httpClient,
    )
    private val rankings = QQMusicRankingClient(
        sessionProvider = sessionProvider,
        httpClient = httpClient,
    )
    private val catalog = QQMusicCatalogClient(
        sessionProvider = sessionProvider,
        httpClient = httpClient,
    )

    override suspend fun searchSongs(query: String, page: Int, pageSize: Int): MusicPage<MusicTrack> =
        catalog.searchTracks(query, page, pageSize)

    override suspend fun searchPlaylists(query: String, page: Int, pageSize: Int): MusicPage<MusicPlaylistSummary> =
        catalog.searchPlaylists(query, page, pageSize)

    override suspend fun searchAlbums(query: String, page: Int, pageSize: Int): MusicPage<MusicAlbumSummary> =
        catalog.searchAlbums(query, page, pageSize)

    override suspend fun searchArtists(query: String, page: Int, pageSize: Int): MusicPage<MusicArtistSummary> =
        catalog.searchArtists(query, page, pageSize)

    override suspend fun lyrics(track: MusicTrack): LyricsDocument {
        val rich = runCatching { richLyrics.lyrics(track) }.getOrNull()
            ?: return api.lyrics(track)
        val needsTranslation = rich.lines.none { !it.translation.isNullOrBlank() }
        val needsRomanization = rich.lines.none { !it.romanization.isNullOrBlank() }
        if (!needsTranslation && !needsRomanization) return rich

        // QQ occasionally serves genuine QRC timing while the rich translation
        // field is empty. The ordinary lyric endpoint can still contain the
        // translation. Keep the QRC syllable timeline and only fill missing
        // annotations from the line-timed response.
        val fallback = runCatching { api.lyrics(track) }.getOrNull() ?: return rich
        return mergeFallbackAnnotations(rich, fallback)
    }

    override suspend fun resolvePlayback(
        track: MusicTrack,
        quality: AudioQualityTier,
    ): PlaybackResolution = playback.resolve(track, quality)

    override suspend fun setFavorite(track: MusicTrack, favorite: Boolean) {
        favorites.setFavorite(track, favorite)
    }

    override suspend fun homeFeed(
        playlistLimit: Int,
        newSongLimit: Int,
        rankingLimit: Int,
    ): MusicHomeFeed = api.homeFeed(playlistLimit, newSongLimit, rankingLimit)

    override suspend fun accountSummary(): MusicAccountSummary? = api.accountSummary()

    override suspend fun userPlaylists(page: Int, pageSize: Int): MusicPage<MusicPlaylistSummary> =
        api.userPlaylists(page, pageSize)

    override suspend fun aggregationTracks(page: Int, pageSize: Int): MusicPage<MusicTrack> {
        val playlists = api.userPlaylists(page = 1, pageSize = 20).items
        val tracks = playlists.flatMap { playlist ->
            runCatching { this@QQMusicProvider.playlistDetail(playlist, page = 1, pageSize = pageSize).tracks }
                .getOrDefault(emptyList())
        }.distinctBy { it.id.value }
        return MusicPage(tracks, page, pageSize, tracks.size.toLong())
    }

    override suspend fun playlistDetail(
        playlist: MusicPlaylistSummary,
        page: Int,
        pageSize: Int,
    ): MusicPlaylistDetail = playlists.detail(playlist, page, pageSize)

    override suspend fun rankingTracks(
        ranking: MusicRankingSummary,
        page: Int,
        pageSize: Int,
    ): MusicPage<MusicTrack> = rankings.tracks(ranking, page, pageSize)

    override suspend fun albumDetail(
        album: MusicAlbumSummary,
        page: Int,
        pageSize: Int,
    ): MusicAlbumDetail = catalog.albumDetail(album, page, pageSize)

    override suspend fun artistDetail(
        artist: MusicArtistSummary,
        page: Int,
        pageSize: Int,
    ): MusicArtistDetail = catalog.artistDetail(artist, page, pageSize)

    private fun mergeFallbackAnnotations(
        rich: LyricsDocument,
        fallback: LyricsDocument,
    ): LyricsDocument {
        if (fallback.lines.isEmpty()) return rich
        return rich.copy(
            lines = rich.lines.map { line ->
                val annotation = fallback.lines
                    .minByOrNull { candidate -> kotlin.math.abs(candidate.timeMs - line.timeMs) }
                    ?.takeIf { candidate -> kotlin.math.abs(candidate.timeMs - line.timeMs) <= AnnotationToleranceMs }
                line.copy(
                    translation = line.translation
                        ?: annotation?.translation?.takeIf(String::isNotBlank),
                    romanization = line.romanization
                        ?: annotation?.romanization?.takeIf(String::isNotBlank),
                    romanizationSyllables = line.romanizationSyllables.ifEmpty {
                        annotation?.romanizationSyllables.orEmpty()
                    },
                )
            },
        )
    }

    private companion object {
        const val AnnotationToleranceMs = 1_500L
    }
}
