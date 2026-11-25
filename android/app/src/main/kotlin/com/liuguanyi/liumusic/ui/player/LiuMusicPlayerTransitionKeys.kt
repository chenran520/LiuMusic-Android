package com.liuguanyi.liumusic.ui.player

internal data class LiuMusicPlayerArtworkKey(val mediaId: String?)

internal data object LiuMusicPlayerShellKey

internal fun sharedPlayerArtworkKey(mediaId: String?): LiuMusicPlayerArtworkKey = LiuMusicPlayerArtworkKey(mediaId)

internal fun sharedPlayerShellKey(): LiuMusicPlayerShellKey = LiuMusicPlayerShellKey
