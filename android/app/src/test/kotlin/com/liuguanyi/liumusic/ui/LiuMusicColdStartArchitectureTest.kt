package com.liuguanyi.liumusic.ui

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiuMusicColdStartArchitectureTest {
    @Test
    fun playbackAndOptionalIntegrationsWaitForFirstDraw() {
        val mainActivity = File("src/main/kotlin/com/liuguanyi/liumusic/MainActivity.kt").readText()
        val app = File("src/main/kotlin/com/liuguanyi/liumusic/ui/LiuMusicApp.kt").readText()
        val player = File("src/main/kotlin/com/liuguanyi/liumusic/ui/player/LiuMusicPlayerUi.kt").readText()

        assertTrue(mainActivity.contains("firstDraw.await()"))
        assertTrue(mainActivity.contains("playbackConnectionEnabled = true"))
        assertTrue(mainActivity.contains("LiuMusicRemoteConfigRuntime.initializeAndRefresh"))
        assertTrue(player.contains("if (!connectionEnabled) return@DisposableEffect onDispose {}"))
        assertTrue(app.contains("rememberLiuMusicPlaybackUiState(connectionEnabled = playbackConnectionEnabled)"))
        assertFalse(app.contains("ProviderPlaybackRuntime.initialize(context)"))
        val application = File("src/main/kotlin/com/liuguanyi/liumusic/LiuMusicApplication.kt").readText()
        assertFalse(application.contains("LiuMusicRemoteConfigRuntime"))
    }

    @Test
    fun homeRecommendationJsonIsNotParsedDuringComposition() {
        val home = File("src/main/kotlin/com/liuguanyi/liumusic/ui/discovery/LiuMusicDiscoveryScreens.kt").readText()

        assertFalse(home.contains("mutableStateOf(LocalRecommendationStore.readRecommendations(context))"))
        assertFalse(home.contains("mutableStateOf(LocalRecommendationStore.readCandidateTracks(context))"))
        assertTrue(home.contains("withContext(Dispatchers.IO)"))
    }
}
