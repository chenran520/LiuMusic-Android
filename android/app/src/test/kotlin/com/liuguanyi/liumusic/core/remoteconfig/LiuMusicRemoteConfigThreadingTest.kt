package com.liuguanyi.liumusic.core.remoteconfig

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LiuMusicRemoteConfigThreadingTest {
    @Test
    fun everyRefreshCallerIsForcedOntoIoDispatcher() {
        val source = File(
            "src/main/kotlin/com/liuguanyi/liumusic/core/remoteconfig/LiuMusicRemoteConfigClient.kt",
        ).readText()

        assertTrue(source.contains("withContext(Dispatchers.IO) { refreshOnIo(versionCode, force) }"))
    }
}
