package com.liuguanyi.liumusic.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

class LiuMusicGitHubRoutingTest {
    private val original = LiuMusicGitHubRouting.UpdateManifestUrl

    @Test
    fun githubApiCanUseConfiguredRoutes() {
        val api = "https://api.github.com/repos/chenran520/LiuMusic-Android/releases"
        assertEquals(api, LiuMusicGitHubRouting.routedUrlFor(LiuMusicGitHubSource.GitHubDoh, api))
        assertEquals("https://ghfast.top/$api", LiuMusicGitHubRouting.routedUrlFor(LiuMusicGitHubSource.GhFast, api))
    }

    @Test
    fun concreteSourcesUseExpectedTrustedRoutes() {
        assertEquals(original, LiuMusicGitHubRouting.routedUrlFor(LiuMusicGitHubSource.GitHubDoh, original))
        assertEquals("https://ghfast.top/$original", LiuMusicGitHubRouting.routedUrlFor(LiuMusicGitHubSource.GhFast, original))
        assertEquals("https://ghproxy.net/$original", LiuMusicGitHubRouting.routedUrlFor(LiuMusicGitHubSource.GhProxy, original))
        assertEquals("https://gh-proxy.org/$original", LiuMusicGitHubRouting.routedUrlFor(LiuMusicGitHubSource.GhProxyOrg, original))
    }

    @Test
    fun oldJsDelivrSelectionMigratesToAuto() {
        assertEquals(LiuMusicGitHubSource.Auto, LiuMusicGitHubRouting.parseSource(null, "JsDelivr"))
        assertEquals(LiuMusicGitHubSource.GitHubDoh, LiuMusicGitHubRouting.parseSource(null, "GitHub"))
    }
}
