package com.liuguanyi.liumusic.playback

/**
 * Shared flag used to let playback/service layers know the UI player
 * expand/collapse transition is active, so they can defer non-essential work.
 */
object LiuMusicPlayerTransitionState {
    @Volatile
    var isActive: Boolean = false
}
