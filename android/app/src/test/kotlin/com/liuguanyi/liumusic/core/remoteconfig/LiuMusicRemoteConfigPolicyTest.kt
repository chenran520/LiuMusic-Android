package com.liuguanyi.liumusic.core.remoteconfig

import com.liuguanyi.liumusic.core.music.model.MusicSource
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LiuMusicRemoteConfigPolicyTest {
    private val remote = LiuMusicRemoteConfigDefaults.Config.copy(
        configVersion = 4,
        disabledCapabilities = setOf("qq_playback", "cross_provider_fallback"),
    )
    private val verified = LiuMusicRemoteConfigStatus(
        source = LiuMusicRemoteConfigSource.VerifiedRemote,
        config = remote,
    )

    @Test
    fun refusalAlwaysUsesBuiltInDefaults() {
        assertSame(
            LiuMusicRemoteConfigDefaults.Config,
            LiuMusicRemoteConfigPolicy.effectiveConfig(consentEnabled = false, status = verified),
        )
    }

    @Test
    fun consentAppliesOnlyVerifiedApplicableRemoteConfig() {
        assertSame(remote, LiuMusicRemoteConfigPolicy.effectiveConfig(consentEnabled = true, status = verified))
        assertSame(
            LiuMusicRemoteConfigDefaults.Config,
            LiuMusicRemoteConfigPolicy.effectiveConfig(
                consentEnabled = true,
                status = verified.copy(source = LiuMusicRemoteConfigSource.VersionInapplicable),
            ),
        )
    }

    @Test
    fun playbackKillSwitchAffectsOnlyDeclaredProvider() {
        assertFalse(LiuMusicRemoteConfigPolicy.providerPlaybackEnabled(remote, MusicSource.QQMusic))
        assertTrue(LiuMusicRemoteConfigPolicy.providerPlaybackEnabled(remote, MusicSource.Kugou))
        assertTrue(LiuMusicRemoteConfigPolicy.providerPlaybackEnabled(remote, MusicSource.Netease))
    }
}
