package com.liuguanyi.liumusic.ui.legal

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LiuMusicRemoteNoticeDeliveryTest {
    @Test
    fun appShowsOnlyConsentedVerifiedAnnouncementsAndRecordsDisplay() {
        val source = File("src/main/kotlin/com/liuguanyi/liumusic/ui/LiuMusicApp.kt").readText()

        assertTrue(source.contains("LiuMusicRemoteConfigConsent.enabled(context)"))
        assertTrue(source.contains("remoteConfigStatus.source == LiuMusicRemoteConfigSource.VerifiedRemote"))
        assertTrue(source.contains("LiuMusicRemoteNoticeStore.shouldShow(context, it)"))
        assertTrue(source.contains("LiuMusicRemoteNoticeStore.markShown(context, notice)"))
        assertTrue(source.contains("LiuMusicRemoteNoticeDialog("))
    }
}
