package com.liuguanyi.liumusic.ui.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import com.liuguanyi.liumusic.playback.LiuMusicPlaybackModePreferences

enum class LiuMusicThemeMode { System, Light, Dark }
enum class LiuMusicSwipeFullAction { PlayNext, AddToQueue }
enum class LiuMusicLyricAnnotationDisplayMode { FocusedLine, AllLines }
enum class LiuMusicLyricsStyle { AppleMusic, Eva, TextPV }
enum class LiuMusicLyricsRenderingQuality { Low, Balanced, High }
enum class LiuMusicPlayerBackgroundMode { FlowingLight, AppleLyrics, BlurredArtwork }
enum class LiuMusicPlayerShell { AppleMusic, Classic }
enum class LiuMusicTextPVStyle {
    BlueBold, KineticSplit, BluePlane, CyberGrunge, Geometric, RainCity,
    CyberpunkHUD, EmotionCinema, HystericNight, SpiderWeb, StaggeredText,
    CalmVillain, GirlyClouds, SweetPink, FlyMeToTheMoon, KawaiiPixel,
    CrimeScene, Haruhikage,
    /** Compatibility values kept for installs that used the early Android preview. */
    Dynamic, Minimal, Cyber,
}

val LiuMusicTextPVStyle.referenceAnimationSpeed: Float
    get() = when (this) {
        LiuMusicTextPVStyle.StaggeredText -> 3.4f
        LiuMusicTextPVStyle.GirlyClouds -> 1.5f
        LiuMusicTextPVStyle.SweetPink, LiuMusicTextPVStyle.KawaiiPixel -> 1f
        LiuMusicTextPVStyle.FlyMeToTheMoon -> 3.7f
        LiuMusicTextPVStyle.CrimeScene -> 2.5f
        LiuMusicTextPVStyle.Haruhikage -> .8f
        else -> 2f
    }
enum class LiuMusicVolumeControlMode { System, Player }
enum class LiuMusicSecondaryLyricMode { Auto, Translation, Romanization, NextLine, Hidden }
enum class LiuMusicSystemLyricTitleMode { LyricFirst, SongFirst }
enum class LiuMusicScreenAwakeMode { Disabled, Player, Lyrics, HiddenLyricsInterface }
enum class LiuMusicLyricsGroupingMode { Word, Character }
enum class LiuMusicLyricsFontWeight(val composeWeight: FontWeight) {
    Light(FontWeight.Light), Regular(FontWeight.Normal), Medium(FontWeight.Medium),
    SemiBold(FontWeight.SemiBold), Bold(FontWeight.Bold), Heavy(FontWeight.Black),
}

/** Process-visible settings used by UI paths that need immediate recomposition. */
object LiuMusicSettingsRuntime {
    var performanceOverlayEnabled by mutableStateOf(false)
        internal set
    var themeMode by mutableStateOf(LiuMusicThemeMode.System)
        internal set
    var podcastsEnabled by mutableStateOf(true)
        internal set
    var listeningHistoryEnabled by mutableStateOf(true)
        internal set
    var downloadsEnabled by mutableStateOf(true)
        internal set
    var cloudMusicEnabled by mutableStateOf(true)
        internal set
    var podcastsHomePlacement by mutableStateOf(true)
        internal set
    var podcastsTabPlacement by mutableStateOf(false)
        internal set
    var podcastsLibraryPlacement by mutableStateOf(true)
        internal set
    var downloadsHomePlacement by mutableStateOf(false)
        internal set
    var downloadsTabPlacement by mutableStateOf(false)
        internal set
    var downloadsLibraryPlacement by mutableStateOf(true)
        internal set
    var cloudHomePlacement by mutableStateOf(false)
        internal set
    var cloudTabPlacement by mutableStateOf(false)
        internal set
    var cloudLibraryPlacement by mutableStateOf(true)
        internal set
    var flowingBackdropEnabled by mutableStateOf(true)
        internal set
    var flowingLightSpeed by mutableStateOf(1f)
        internal set
    var flowingLightSaturation by mutableStateOf(1f)
        internal set
    var flowingLightBrightness by mutableStateOf(1f)
        internal set
    var playerBackgroundMode by mutableStateOf(LiuMusicPlayerBackgroundMode.FlowingLight)
        internal set
    var playerShell by mutableStateOf(LiuMusicPlayerShell.AppleMusic)
        internal set
    var artworkMotionEnabled by mutableStateOf(true)
        internal set
    var reduceMotion by mutableStateOf(false)
        internal set
    // ⚠⚠⚠ 2026-10-06：默认值 **240 → 360**（用户拍板「默认动画速度和设置显示改回 360，
    //   之前是测试才默认 240ms」）。
    //   历史：2026-10-03 曾把默认从 360 改成 240（当时口径是「对齐 Apple Music 参考视频
    //   实测 ~200ms」）—— 现确认那是**测试期**的临时口径，正式默认回到 **360**。
    //   ⚠ 光改这一行**不够**：`SharedPreferences` 是持久化的，设备里存的旧值会盖住默认值
    //   ⇒ 必须配合 `playerTransitionDurationMs(context)` 里的**一次性 v2 迁移**
    //     （**无条件归一为 360**，只做一次），才能让「设置页显示」与「实际」**同时**立刻变成 360。
    var playerTransitionDurationMs by mutableStateOf(360)
        internal set
    var playerBackgroundIsolationEnabled by mutableStateOf(true)
        internal set
    var frostedGlassEnabled by mutableStateOf(false)
        internal set
    /**
     * 深色模式下**锁死暗色底栏**：关闭底栏按背后内容自适应翻成浅色的逻辑。
     *
     * ⚠ 只锁深色：浅色模式下自适应（暗背景→暗底栏）照常，不在此开关范围内。
     * 默认开启 —— 用户口径「新增机制，深色模式底栏不自适应变色到浅色底栏」。
     */
    var bottomBarLockDarkInDarkMode by mutableStateOf(true)
        internal set
    var keepScreenOn by mutableStateOf(false)
        internal set
    var screenAwakeMode by mutableStateOf(LiuMusicScreenAwakeMode.Disabled)
        internal set
    var immersivePlaybackEnabled by mutableStateOf(false)
        internal set
    var showLyricTranslation by mutableStateOf(true)
        internal set
    var automaticLyricSelectionEnabled by mutableStateOf(true)
        internal set
    var lyricStrongBindingEnabled by mutableStateOf(false)
        internal set
    var showLyricRomanization by mutableStateOf(true)
        internal set
    var lyricWordByWordEnabled by mutableStateOf(true)
        internal set
    var lyricPseudoTimingEnabled by mutableStateOf(false)
        internal set
    var lyricTapSeekEnabled by mutableStateOf(true)
        internal set
    var lyricLongPressShareEnabled by mutableStateOf(true)
        internal set
    var lyricInterludeCountdownEnabled by mutableStateOf(true)
        internal set
    var lyricAutoFollowEnabled by mutableStateOf(true)
        internal set
    var lyricReduceMotion by mutableStateOf(false)
        internal set
    var lyricAdvanceMs by mutableStateOf(0)
        internal set
    var lyricAdvanceAppliesToWordByWord by mutableStateOf(false)
        internal set
    var lyricRefreshRate by mutableStateOf(60)
        internal set
    var lyricBackgroundFrameRate by mutableStateOf(24)
        internal set
    var lyricRenderingQuality by mutableStateOf(LiuMusicLyricsRenderingQuality.High)
        internal set
    var lyricRomanizationDisplayMode by mutableStateOf(LiuMusicLyricAnnotationDisplayMode.FocusedLine)
        internal set
    var lyricTranslationDisplayMode by mutableStateOf(LiuMusicLyricAnnotationDisplayMode.AllLines)
        internal set
    var lyricFollowDelayMs by mutableStateOf(3_000)
        internal set
    var lyricFontScale by mutableStateOf(1f)
        internal set
    var lyricFocusPosition by mutableStateOf(.25f)
        internal set
    var lyricFontWeight by mutableStateOf(LiuMusicLyricsFontWeight.Heavy)
        internal set
    var lyricLiftMode by mutableStateOf(LiuMusicLyricsGroupingMode.Character)
        internal set
    var lyricLongToneDetectionMode by mutableStateOf(LiuMusicLyricsGroupingMode.Character)
        internal set
    var lyricGlowLongTonesOnly by mutableStateOf(true)
        internal set
    var lyricLongToneThresholdMs by mutableStateOf(950)
        internal set
    var lyricSpacingScale by mutableStateOf(1f)
        internal set
    var lyricBlurStrength by mutableStateOf(1f)
        internal set
    var lyricDistanceBlurScale by mutableStateOf(1.05f)
        internal set
    var lyricHiddenInterfaceBlurScale by mutableStateOf(.85f)
        internal set
    var lyricDimAmount by mutableStateOf(1f)
        internal set
    var lyricFocusScale by mutableStateOf(1.02f)
        internal set
    var lyricInactiveOpacity by mutableStateOf(.42f)
        internal set
    var lyricGlowStrength by mutableStateOf(1f)
        internal set
    var lyricGlowEnabled by mutableStateOf(true)
        internal set
    var lyricLongToneStrength by mutableStateOf(1f)
        internal set
    var lyricWordBounceEnabled by mutableStateOf(false)
        internal set
    var lyricHighlightGradientWidth by mutableStateOf(.7f)
        internal set
    var lyricHighlightGradientReduction by mutableStateOf(.65f)
        internal set
    var lyricRomanizationFontScale by mutableStateOf(.65f)
        internal set
    var lyricRomanizationOpacity by mutableStateOf(.9f)
        internal set
    var lyricTranslationFontScale by mutableStateOf(.65f)
        internal set
    var lyricTranslationOpacity by mutableStateOf(.9f)
        internal set
    var lyricInterfaceAutoHideDelayMs by mutableStateOf(5_000)
        internal set
    var lyricScrollHideThresholdDp by mutableStateOf(200)
        internal set
    var lyricCascadeDelayMs by mutableStateOf(21f)
        internal set
    var lyricCascadeDelayIncreaseMs by mutableStateOf(5f)
        internal set
    var lyricCascadeFollowingDelayMs by mutableStateOf(30f)
        internal set
    var lyricCascadeCatchUpRatio by mutableStateOf(.97f)
        internal set
    var lyricCascadeChaseSpeedGradient by mutableStateOf(.70f)
        internal set
    var lyricCascadeDurationMs by mutableStateOf(740f)
        internal set
    var lyricSnapThresholdMs by mutableStateOf(260f)
        internal set
    var lyricCascadeBounceEnabled by mutableStateOf(true)
        internal set
    var lyricCascadeBounce by mutableStateOf(.26f)
        internal set
    var lyricCascadeBounceGradient by mutableStateOf(.85f)
        internal set
    var lyricScaleBounceEnabled by mutableStateOf(true)
        internal set
    var lyricScaleBounce by mutableStateOf(.32f)
        internal set
    var lyricScaleBounceDurationMs by mutableStateOf(580)
        internal set
    var lyricFocusColorLeadMs by mutableStateOf(0)
        internal set
    var lyricsStyle by mutableStateOf(LiuMusicLyricsStyle.AppleMusic)
        internal set
    /** 歌词翻译语言：`system` 或具体语言标签（zh-Hans/zh-Hant/en/tr…）。 */
    var lyricLanguage by mutableStateOf("system")
        internal set
    var textPVStyle by mutableStateOf(LiuMusicTextPVStyle.BlueBold)
        internal set
    var textPVMotionIntensity by mutableStateOf(1f)
        internal set
    var textPVAnimationSpeed by mutableStateOf(2f)
        internal set
    var skylineEnabled by mutableStateOf(true)
        internal set
    var skylineShowSongInfo by mutableStateOf(true)
        internal set
    var skylineKeepsScreenAwake by mutableStateOf(true)
        internal set
    var skylineAmbientLines by mutableStateOf(2)
        internal set
    var skylineCurrentFontSize by mutableStateOf(54f)
        internal set
    var skylineCurrentMaximumScale by mutableStateOf(1.1f)
        internal set
    var skylineNextFontSize by mutableStateOf(24f)
        internal set
    var skylineCurrentSpacing by mutableStateOf(14f)
        internal set
    var skylineCurrentWidth by mutableStateOf(.64f)
        internal set
    var skylineNextOpacity by mutableStateOf(.48f)
        internal set
    var skylineAmbientFontSize by mutableStateOf(44f)
        internal set
    var skylineAmbientMaximumCharacters by mutableStateOf(4)
        internal set
    var skylineAmbientMaximumVisibleTexts by mutableStateOf(16)
        internal set
    var skylineAmbientOpacity by mutableStateOf(1f)
        internal set
    var skylineAmbientBlur by mutableStateOf(1f)
        internal set
    var skylineAmbientMaximumTilt by mutableStateOf(8f)
        internal set
    var skylineAmbientDrift by mutableStateOf(1f)
        internal set
    var systemLyricsEnabled by mutableStateOf(false)
        internal set
    var lyricNotificationsEnabled by mutableStateOf(false)
        internal set
    var systemLyricTitleMode by mutableStateOf(LiuMusicSystemLyricTitleMode.LyricFirst)
        internal set
    var lyricNotificationShowNextLine by mutableStateOf(false)
        internal set
    var lyricNotificationShowProgress by mutableStateOf(true)
        internal set
    var lyricNotificationShowArtwork by mutableStateOf(true)
        internal set
    var lyricNotificationBackgroundOnly by mutableStateOf(false)
        internal set
    var lyricNotificationDismissWhenPaused by mutableStateOf(true)
        internal set
    var lyricNotificationTitleTemplate by mutableStateOf("{lyric}")
        internal set
    var lyricNotificationSubtitleTemplate by mutableStateOf("{song} · {artist}")
        internal set
    var lyricNotificationFallback by mutableStateOf("{song} · {artist}")
        internal set
    var floatingLyricsEnabled by mutableStateOf(false)
        internal set
    var floatingSecondaryMode by mutableStateOf(LiuMusicSecondaryLyricMode.Auto)
        internal set
    var floatingFontSizeSp by mutableStateOf(18)
        internal set
    var floatingHighContrast by mutableStateOf(true)
        internal set
    /**
     * AutoSync: generate a real word-level timeline locally (ONNX) when a song
     * only has line-timed lyrics. Off by default — it is an experimental path
     * that spends CPU and battery on the current track.
     */
    var autoSyncWordLyricsEnabled by mutableStateOf(false)
        internal set
    var homeTabEnabled by mutableStateOf(true)
        internal set
    var homeQuickActionsEnabled by mutableStateOf(true)
        internal set
    var homePlaylistsEnabled by mutableStateOf(true)
        internal set
    var homeNewSongsEnabled by mutableStateOf(true)
        internal set
    var homeSectionOrder by mutableStateOf(listOf("QuickActions", "Playlists", "NewSongs"))
        internal set
    var exploreTabEnabled by mutableStateOf(true)
        internal set
    var libraryTabEnabled by mutableStateOf(true)
        internal set
    var rememberLastTab by mutableStateOf(true)
        internal set
    var disableAutomaticTabBarShrink by mutableStateOf(false)
        internal set
    var tabOrder by mutableStateOf(listOf("Home", "Explore", "Library", "Podcasts", "Downloads", "Cloud", "Settings"))
        internal set
    var defaultTab by mutableStateOf("Home")
        internal set
    var rememberLibraryPage by mutableStateOf(true)
        internal set
    var defaultLibraryPage by mutableStateOf("Songs")
        internal set
    var downloadLyricsEnabled by mutableStateOf(true)
        internal set
    var musicArea by mutableStateOf("全部")
        internal set
    var showPlaylistPlayCount by mutableStateOf(true)
        internal set
    var showHighQualityPlaylists by mutableStateOf(true)
        internal set
    var clipboardLinksEnabled by mutableStateOf(true)
        internal set
    var hapticFeedbackEnabled by mutableStateOf(true)
        internal set
    var swipeFullAction by mutableStateOf(LiuMusicSwipeFullAction.PlayNext)
        internal set
    var previousRestartsAfterFiveSeconds by mutableStateOf(true)
        internal set
    var startsHeartModeOnLaunch by mutableStateOf(false)
        internal set
    var volumeControlMode by mutableStateOf(LiuMusicVolumeControlMode.System)
        internal set
    var showPlayerQualityTip by mutableStateOf(true)
        internal set
    var systemFontEnabled by mutableStateOf(true)
        internal set
    var smartQueueEnabled by mutableStateOf(false)
        internal set
    var transitionUiEnabled by mutableStateOf(false)
        internal set

    private var initialized = false

    /** Reads only state required to draw the first root frame. The remaining settings load after setContent. */
    fun initializeCritical(context: Context) {
        val app = context.applicationContext
        themeMode = runCatching {
            LiuMusicThemeMode.valueOf(LiuMusicSettingsPreferences.string(app, "theme_mode", LiuMusicThemeMode.System.name))
        }.getOrDefault(LiuMusicThemeMode.System)
        homeTabEnabled = LiuMusicSettingsPreferences.boolean(app, "tab_home", true)
        exploreTabEnabled = LiuMusicSettingsPreferences.boolean(app, "tab_explore", true)
        libraryTabEnabled = LiuMusicSettingsPreferences.boolean(app, "tab_library", true)
        podcastsTabPlacement = LiuMusicSettingsPreferences.boolean(app, "placement_podcasts_tab", false)
        downloadsTabPlacement = LiuMusicSettingsPreferences.boolean(app, "placement_downloads_tab", false)
        cloudTabPlacement = LiuMusicSettingsPreferences.boolean(app, "placement_cloud_tab", false)
        rememberLastTab = LiuMusicSettingsPreferences.boolean(app, "general_remember_tab", true)
        disableAutomaticTabBarShrink = LiuMusicSettingsPreferences.boolean(app, "general_disable_auto_tabbar_shrink", false)
        tabOrder = LiuMusicSettingsPreferences.string(app, "tab_order", "Home,Explore,Library,Podcasts,Downloads,Cloud,Settings")
            .split(',').filter { it in setOf("Home", "Explore", "Library", "Podcasts", "Downloads", "Cloud", "Settings") }.distinct()
            .let { order -> (order + listOf("Home", "Explore", "Library", "Podcasts", "Downloads", "Cloud", "Settings")).distinct() }
        defaultTab = LiuMusicSettingsPreferences.string(app, "general_default_tab", "Home")
        rememberLibraryPage = LiuMusicSettingsPreferences.boolean(app, "library_remember_page", true)
        defaultLibraryPage = LiuMusicSettingsPreferences.string(app, "library_default_page", "Songs")
        clipboardLinksEnabled = LiuMusicSettingsPreferences.boolean(app, "general_clipboard_links", true)
        systemFontEnabled = LiuMusicSettingsPreferences.boolean(app, "system_font", true)
    }

    fun initialize(context: Context, force: Boolean = false) {
        if (initialized && !force) return
        initialized = true
        val app = context.applicationContext
        performanceOverlayEnabled = LiuMusicSettingsPreferences.boolean(app, "developer_performance_overlay", false)
        themeMode = runCatching {
            LiuMusicThemeMode.valueOf(LiuMusicSettingsPreferences.string(app, "theme_mode", LiuMusicThemeMode.System.name))
        }.getOrDefault(LiuMusicThemeMode.System)
        podcastsEnabled = LiuMusicSettingsPreferences.boolean(app, "feature_podcasts", true)
        listeningHistoryEnabled = LiuMusicSettingsPreferences.boolean(app, "feature_history", true)
        downloadsEnabled = LiuMusicSettingsPreferences.boolean(app, "feature_downloads", true)
        cloudMusicEnabled = LiuMusicSettingsPreferences.boolean(app, "feature_cloud_music", true)
        podcastsHomePlacement = LiuMusicSettingsPreferences.boolean(app, "placement_podcasts_home", true)
        podcastsTabPlacement = LiuMusicSettingsPreferences.boolean(app, "placement_podcasts_tab", false)
        podcastsLibraryPlacement = LiuMusicSettingsPreferences.boolean(app, "placement_podcasts_library", true)
        downloadsHomePlacement = LiuMusicSettingsPreferences.boolean(app, "placement_downloads_home", false)
        downloadsTabPlacement = LiuMusicSettingsPreferences.boolean(app, "placement_downloads_tab", false)
        downloadsLibraryPlacement = LiuMusicSettingsPreferences.boolean(app, "placement_downloads_library", true)
        cloudHomePlacement = LiuMusicSettingsPreferences.boolean(app, "placement_cloud_home", false)
        cloudTabPlacement = LiuMusicSettingsPreferences.boolean(app, "placement_cloud_tab", false)
        cloudLibraryPlacement = LiuMusicSettingsPreferences.boolean(app, "placement_cloud_library", true)
        val legacyFlowingBackdropEnabled = LiuMusicSettingsPreferences.boolean(app, "player_flowing_backdrop", true)
        playerBackgroundMode = LiuMusicSettingsPreferences.string(app, "player_background_mode", "")
            .takeIf { it.isNotBlank() }
            ?.let { value -> runCatching { LiuMusicPlayerBackgroundMode.valueOf(value) }.getOrNull() }
            ?: if (legacyFlowingBackdropEnabled) {
                LiuMusicPlayerBackgroundMode.FlowingLight
            } else {
                LiuMusicPlayerBackgroundMode.BlurredArtwork
            }
        flowingBackdropEnabled = playerBackgroundMode != LiuMusicPlayerBackgroundMode.BlurredArtwork
        flowingLightSpeed = LiuMusicSettingsPreferences.float(app, "player_flowing_speed", 1f).coerceIn(.25f, 2f)
        flowingLightSaturation = LiuMusicSettingsPreferences.float(app, "player_flowing_saturation", 1f).coerceIn(0f, 2f)
        flowingLightBrightness = LiuMusicSettingsPreferences.float(app, "player_flowing_brightness", 1f).coerceIn(.4f, 1.6f)
        playerShell = runCatching {
            LiuMusicPlayerShell.valueOf(LiuMusicSettingsPreferences.string(app, "player_shell", LiuMusicPlayerShell.AppleMusic.name))
        }.getOrDefault(LiuMusicPlayerShell.AppleMusic)
        artworkMotionEnabled = LiuMusicSettingsPreferences.boolean(app, "player_artwork_motion", true)
        reduceMotion = LiuMusicSettingsPreferences.boolean(app, "reduce_motion", false)
        // ⚠⚠ 2026-10-06：**正式默认 = 360**（240 只是 2026-10-03~10-06 的测试期口径）。
        //   `575 → 360` 那行是**旧档位**归一，与默认档无关，保留原样。
        // ⚠⚠⚠ 默认值迁移（v1 的 360→240、v2 的 240→360）**都不在这里做** ——
        //   本函数只被 `LiuMusicSettingsRuntime` 读，而设置页走
        //   `LiuMusicSettingsPreferences.playerTransitionDurationMs(...)`，
        //   两条路径不同源 ⇒ 迁移放这里会导致「显示 / 实际」不一致。
        //   统一走 [playerTransitionDurationMs]（读 + 迁移 + 写回同一处）。
        // ⚠⚠⚠ 必须带 `LiuMusicSettingsPreferences.` 前缀：本函数在 `LiuMusicSettingsRuntime`
        //   内部，而 `LiuMusicSettingsRuntime` 也有同名属性 `playerTransitionDurationMs`
        //   ⇒ 裸写会被解析成**属性自赋值**，编译报 `Unresolved reference`。
        playerTransitionDurationMs =
            LiuMusicSettingsPreferences.playerTransitionDurationMs(app)
        playerBackgroundIsolationEnabled = LiuMusicSettingsPreferences.boolean(app, "player_background_isolation", true)
        frostedGlassEnabled = LiuMusicSettingsPreferences.boolean(app, "player_frosted_glass", false)
        bottomBarLockDarkInDarkMode = LiuMusicSettingsPreferences.boolean(app, "bottom_bar_lock_dark_in_dark_mode", true)
        keepScreenOn = LiuMusicSettingsPreferences.boolean(app, "player_keep_screen_on", false)
        screenAwakeMode = runCatching {
            LiuMusicScreenAwakeMode.valueOf(
                LiuMusicSettingsPreferences.string(
                    app,
                    "player_screen_awake_mode",
                    if (keepScreenOn) LiuMusicScreenAwakeMode.Player.name else LiuMusicScreenAwakeMode.Disabled.name,
                ),
            )
        }.getOrDefault(LiuMusicScreenAwakeMode.Disabled)
        immersivePlaybackEnabled = LiuMusicSettingsPreferences.boolean(app, "immersive_playback", false)
        showPlayerQualityTip = LiuMusicSettingsPreferences.boolean(app, "player_show_quality_tip", true)
        systemFontEnabled = LiuMusicSettingsPreferences.boolean(app, "system_font", true)
        smartQueueEnabled = LiuMusicPlaybackModePreferences.smartQueue(app)
        transitionUiEnabled = LiuMusicSettingsPreferences.boolean(app, "transition_ui_enabled", false)
        showLyricTranslation = LiuMusicSettingsPreferences.boolean(app, "lyrics_translation", true)
        automaticLyricSelectionEnabled = LiuMusicSettingsPreferences.boolean(app, "lyrics_auto_select", true)
        lyricStrongBindingEnabled = LiuMusicSettingsPreferences.boolean(app, "experimental_lyric_strong_binding", false)
        showLyricRomanization = LiuMusicSettingsPreferences.boolean(app, "lyrics_romanization", false)
        lyricWordByWordEnabled = LiuMusicSettingsPreferences.boolean(app, "lyrics_word_by_word", true)
        lyricPseudoTimingEnabled = LiuMusicSettingsPreferences.boolean(app, "lyrics_pseudo_timing", false)
        lyricTapSeekEnabled = LiuMusicSettingsPreferences.boolean(app, "lyrics_tap_seek", true)
        lyricLongPressShareEnabled = LiuMusicSettingsPreferences.boolean(app, "lyrics_long_press_share", true)
        lyricInterludeCountdownEnabled = LiuMusicSettingsPreferences.boolean(app, "lyrics_interlude_countdown", true)
        lyricAutoFollowEnabled = LiuMusicSettingsPreferences.boolean(app, "lyrics_auto_follow", true)
        lyricReduceMotion = LiuMusicSettingsPreferences.boolean(app, "lyrics_reduce_motion", false)
        lyricAdvanceMs = LiuMusicSettingsPreferences.int(app, "lyrics_advance_ms", 0).coerceIn(-5_000, 5_000)
        lyricAdvanceAppliesToWordByWord = LiuMusicSettingsPreferences.boolean(app, "lyrics_advance_word_by_word", false)
        lyricLanguage = LiuMusicSettingsPreferences.string(app, "lyric_language", "system")
        lyricRefreshRate = LiuMusicSettingsPreferences.int(app, "lyrics_refresh_rate", 60)
            .takeIf { it in setOf(30, 60, 90, 120) } ?: 60
        lyricBackgroundFrameRate = LiuMusicSettingsPreferences.int(app, "lyrics_background_frame_rate", 24)
            .takeIf { it in setOf(15, 24, 30, 45, 60) } ?: 24
        lyricRenderingQuality = runCatching {
            LiuMusicLyricsRenderingQuality.valueOf(
                LiuMusicSettingsPreferences.string(
                    app,
                    "lyrics_rendering_quality",
                    LiuMusicLyricsRenderingQuality.High.name,
                ),
            )
        }.getOrDefault(LiuMusicLyricsRenderingQuality.High)
        lyricRomanizationDisplayMode = annotationMode(app, "lyrics_romanization_display_mode", LiuMusicLyricAnnotationDisplayMode.FocusedLine)
        lyricTranslationDisplayMode = annotationMode(app, "lyrics_translation_display_mode", LiuMusicLyricAnnotationDisplayMode.AllLines)
        lyricFollowDelayMs = LiuMusicSettingsPreferences.int(app, "lyrics_follow_delay_ms", 3_000).coerceIn(1_000, 8_000)
        lyricFontScale = LiuMusicSettingsPreferences.float(app, "lyrics_font_scale", 1f).coerceIn(.8f, 1.25f)
        lyricFocusPosition = LiuMusicSettingsPreferences.float(app, "lyrics_focus_position", .25f).coerceIn(.05f, .8f)
        lyricFontWeight = runCatching {
            LiuMusicLyricsFontWeight.valueOf(LiuMusicSettingsPreferences.string(app, "lyrics_font_weight", LiuMusicLyricsFontWeight.Heavy.name))
        }.getOrDefault(LiuMusicLyricsFontWeight.Heavy)
        lyricLiftMode = runCatching {
            LiuMusicLyricsGroupingMode.valueOf(LiuMusicSettingsPreferences.string(app, "lyrics_lift_mode", LiuMusicLyricsGroupingMode.Character.name))
        }.getOrDefault(LiuMusicLyricsGroupingMode.Character)
        lyricLongToneDetectionMode = runCatching {
            LiuMusicLyricsGroupingMode.valueOf(LiuMusicSettingsPreferences.string(app, "lyrics_long_tone_detection", LiuMusicLyricsGroupingMode.Character.name))
        }.getOrDefault(LiuMusicLyricsGroupingMode.Character)
        lyricGlowLongTonesOnly = LiuMusicSettingsPreferences.boolean(app, "lyrics_glow_long_tones_only", true)
        lyricLongToneThresholdMs = LiuMusicSettingsPreferences.int(app, "lyrics_long_tone_threshold_ms", 950).coerceIn(300, 1_500)
        lyricSpacingScale = LiuMusicSettingsPreferences.float(app, "lyrics_spacing_scale", 1f).coerceIn(.7f, 1.5f)
        lyricBlurStrength = LiuMusicSettingsPreferences.float(app, "lyrics_blur_strength", 1f).coerceIn(0f, 1.5f)
        lyricDistanceBlurScale = LiuMusicSettingsPreferences.float(app, "lyrics_distance_blur_scale", 1.05f).coerceIn(0f, 1.5f)
        lyricHiddenInterfaceBlurScale = LiuMusicSettingsPreferences.float(app, "lyrics_hidden_blur_scale", .85f).coerceIn(0f, 1.5f)
        lyricDimAmount = LiuMusicSettingsPreferences.float(app, "lyrics_dim_amount", 1f).coerceIn(0f, 1f)
        lyricFocusScale = LiuMusicSettingsPreferences.float(app, "lyrics_focus_scale", 1.02f).coerceIn(1f, 1.08f)
        lyricInactiveOpacity = LiuMusicSettingsPreferences.float(app, "lyrics_inactive_opacity", .42f).coerceIn(.15f, .65f)
        lyricGlowStrength = LiuMusicSettingsPreferences.float(app, "lyrics_glow_strength", 1f).coerceIn(0f, 1.5f)
        lyricGlowEnabled = LiuMusicSettingsPreferences.boolean(app, "lyrics_glow_enabled", true)
        lyricLongToneStrength = LiuMusicSettingsPreferences.float(app, "lyrics_long_tone_strength", 1f).coerceIn(0f, 1.5f)
        lyricWordBounceEnabled = LiuMusicSettingsPreferences.boolean(app, "lyrics_word_bounce_enabled", false)
        lyricHighlightGradientWidth = LiuMusicSettingsPreferences.float(app, "lyrics_highlight_gradient_width", .7f).coerceIn(.4f, 3f)
        lyricHighlightGradientReduction = LiuMusicSettingsPreferences.float(app, "lyrics_highlight_gradient_reduction", .65f).coerceIn(0f, 1f)
        lyricRomanizationFontScale = LiuMusicSettingsPreferences.float(app, "lyrics_romanization_font_scale", .65f).coerceIn(.5f, .8f)
        lyricRomanizationOpacity = LiuMusicSettingsPreferences.float(app, "lyrics_romanization_opacity", .9f).coerceIn(.4f, .9f)
        lyricTranslationFontScale = LiuMusicSettingsPreferences.float(app, "lyrics_translation_font_scale", .65f).coerceIn(.5f, .8f)
        lyricTranslationOpacity = LiuMusicSettingsPreferences.float(app, "lyrics_translation_opacity", .9f).coerceIn(.4f, .9f)
        lyricInterfaceAutoHideDelayMs = LiuMusicSettingsPreferences.int(app, "lyrics_interface_auto_hide_ms", 5_000).coerceIn(3_000, 15_000)
        lyricScrollHideThresholdDp = LiuMusicSettingsPreferences.int(app, "lyrics_scroll_hide_threshold_dp", 200).coerceIn(40, 240)
        lyricCascadeDelayMs = LiuMusicSettingsPreferences.float(app, "lyrics_cascade_delay_ms", 21f).coerceIn(0f, 100f)
        lyricCascadeDelayIncreaseMs = LiuMusicSettingsPreferences.float(app, "lyrics_cascade_delay_increase_ms", 5f).coerceIn(0f, 100f)
        lyricCascadeFollowingDelayMs = LiuMusicSettingsPreferences.float(app, "lyrics_cascade_following_delay_ms", 30f).coerceIn(0f, 200f)
        lyricCascadeCatchUpRatio = LiuMusicSettingsPreferences.float(app, "lyrics_cascade_catch_up_ratio", .97f).coerceIn(.5f, 1f)
        lyricCascadeChaseSpeedGradient = LiuMusicSettingsPreferences.float(app, "lyrics_cascade_chase_gradient", .70f).coerceIn(0f, 1f)
        lyricCascadeDurationMs = LiuMusicSettingsPreferences.float(app, "lyrics_cascade_duration_ms", 740f).coerceIn(200f, 1_200f)
        lyricSnapThresholdMs = LiuMusicSettingsPreferences.float(app, "lyrics_snap_threshold_ms", 260f).coerceIn(50f, 500f)
        lyricCascadeBounceEnabled = LiuMusicSettingsPreferences.boolean(app, "lyrics_cascade_bounce_enabled", true)
        lyricCascadeBounce = LiuMusicSettingsPreferences.float(app, "lyrics_cascade_bounce", .26f).coerceIn(0f, .8f)
        lyricCascadeBounceGradient = LiuMusicSettingsPreferences.float(app, "lyrics_cascade_bounce_gradient", .85f).coerceIn(0f, 1f)
        lyricScaleBounceEnabled = LiuMusicSettingsPreferences.boolean(app, "lyrics_scale_bounce_enabled", true)
        lyricScaleBounce = LiuMusicSettingsPreferences.float(app, "lyrics_scale_bounce", .32f).coerceIn(0f, .5f)
        lyricScaleBounceDurationMs = LiuMusicSettingsPreferences.int(app, "lyrics_scale_bounce_duration_ms", 580).coerceIn(150, 800)
        lyricFocusColorLeadMs = LiuMusicSettingsPreferences.int(app, "lyrics_focus_color_lead_ms", 0).coerceIn(-300, 300)
        lyricsStyle = runCatching {
            LiuMusicLyricsStyle.valueOf(LiuMusicSettingsPreferences.string(app, "lyrics_style", LiuMusicLyricsStyle.AppleMusic.name))
        }.getOrDefault(LiuMusicLyricsStyle.AppleMusic)
        textPVStyle = runCatching {
            LiuMusicTextPVStyle.valueOf(LiuMusicSettingsPreferences.string(app, "lyrics_text_pv_style", LiuMusicTextPVStyle.BlueBold.name))
        }.getOrDefault(LiuMusicTextPVStyle.BlueBold)
        textPVMotionIntensity = LiuMusicSettingsPreferences.float(app, "lyrics_text_pv_motion_intensity", 1f)
            .coerceIn(0f, 2f)
        textPVAnimationSpeed = LiuMusicSettingsPreferences.float(
            app,
            "lyrics_text_pv_animation_speed",
            textPVStyle.referenceAnimationSpeed,
        ).coerceIn(0f, 4f)
        skylineEnabled = LiuMusicSettingsPreferences.boolean(app, "lyrics_skyline_enabled", true)
        skylineShowSongInfo = LiuMusicSettingsPreferences.boolean(app, "lyrics_skyline_song_info", true)
        skylineKeepsScreenAwake = LiuMusicSettingsPreferences.boolean(app, "lyrics_skyline_keep_awake", true)
        skylineAmbientLines = LiuMusicSettingsPreferences.int(app, "lyrics_skyline_ambient_lines", 2).coerceIn(0, 4)
        skylineCurrentFontSize = LiuMusicSettingsPreferences.float(app, "lyrics_skyline_current_font_size", 54f).coerceIn(36f, 84f)
        skylineCurrentMaximumScale = LiuMusicSettingsPreferences.float(app, "lyrics_skyline_current_max_scale", 1.1f).coerceIn(1f, 1.2f)
        skylineNextFontSize = LiuMusicSettingsPreferences.float(app, "lyrics_skyline_next_font_size", 24f).coerceIn(14f, 44f)
        skylineCurrentSpacing = LiuMusicSettingsPreferences.float(app, "lyrics_skyline_current_spacing", 14f).coerceIn(4f, 36f)
        skylineCurrentWidth = LiuMusicSettingsPreferences.float(app, "lyrics_skyline_current_width", .64f).coerceIn(.4f, .82f)
        skylineNextOpacity = LiuMusicSettingsPreferences.float(app, "lyrics_skyline_next_opacity", .48f).coerceIn(.2f, .8f)
        skylineAmbientFontSize = LiuMusicSettingsPreferences.float(app, "lyrics_skyline_ambient_font_size", 44f).coerceIn(24f, 72f)
        skylineAmbientMaximumCharacters = LiuMusicSettingsPreferences.int(app, "lyrics_skyline_ambient_max_characters", 4).coerceIn(1, 4)
        skylineAmbientMaximumVisibleTexts = LiuMusicSettingsPreferences.int(app, "lyrics_skyline_ambient_max_visible", 16).coerceIn(4, 24)
        skylineAmbientOpacity = LiuMusicSettingsPreferences.float(app, "lyrics_skyline_ambient_opacity", 1f).coerceIn(.4f, 1.8f)
        skylineAmbientBlur = LiuMusicSettingsPreferences.float(app, "lyrics_skyline_ambient_blur", 1f).coerceIn(0f, 2f)
        skylineAmbientMaximumTilt = LiuMusicSettingsPreferences.float(app, "lyrics_skyline_ambient_max_tilt", 8f).coerceIn(0f, 20f)
        skylineAmbientDrift = LiuMusicSettingsPreferences.float(app, "lyrics_skyline_ambient_drift", 1f).coerceIn(0f, 2f)
        systemLyricsEnabled = LiuMusicSettingsPreferences.boolean(app, "system_lyrics_enabled", false)
        lyricNotificationsEnabled = LiuMusicSettingsPreferences.boolean(app, "lyrics_notifications_enabled", false)
        systemLyricTitleMode = runCatching {
            LiuMusicSystemLyricTitleMode.valueOf(
                LiuMusicSettingsPreferences.string(app, "system_lyrics_title_mode", LiuMusicSystemLyricTitleMode.LyricFirst.name),
            )
        }.getOrDefault(LiuMusicSystemLyricTitleMode.LyricFirst)
        lyricNotificationShowNextLine = LiuMusicSettingsPreferences.boolean(app, "lyrics_notification_next_line", false)
        lyricNotificationShowProgress = LiuMusicSettingsPreferences.boolean(app, "lyrics_notification_progress", true)
        lyricNotificationShowArtwork = LiuMusicSettingsPreferences.boolean(app, "lyrics_notification_artwork", true)
        lyricNotificationBackgroundOnly = LiuMusicSettingsPreferences.boolean(app, "lyrics_notification_background_only", false)
        lyricNotificationDismissWhenPaused = LiuMusicSettingsPreferences.boolean(app, "lyrics_notification_dismiss_paused", true)
        lyricNotificationTitleTemplate = LiuMusicSettingsPreferences.string(app, "lyrics_notification_title_template", "{lyric}")
        lyricNotificationSubtitleTemplate = LiuMusicSettingsPreferences.string(app, "lyrics_notification_subtitle_template", "{song} · {artist}")
        lyricNotificationFallback = LiuMusicSettingsPreferences.string(app, "lyrics_notification_fallback", "{song} · {artist}")
        floatingLyricsEnabled = LiuMusicSettingsPreferences.boolean(app, "floating_lyrics_enabled", false)
        floatingSecondaryMode = runCatching {
            LiuMusicSecondaryLyricMode.valueOf(
                LiuMusicSettingsPreferences.string(app, "floating_lyrics_secondary_mode", LiuMusicSecondaryLyricMode.Auto.name),
            )
        }.getOrDefault(LiuMusicSecondaryLyricMode.Auto)
        floatingFontSizeSp = LiuMusicSettingsPreferences.int(app, "floating_lyrics_font_size", 18).coerceIn(14, 28)
        floatingHighContrast = LiuMusicSettingsPreferences.boolean(app, "floating_lyrics_high_contrast", true)
        autoSyncWordLyricsEnabled = LiuMusicSettingsPreferences.boolean(app, "autosync_word_lyrics_enabled", false)
        homeTabEnabled = LiuMusicSettingsPreferences.boolean(app, "tab_home", true)
        homeQuickActionsEnabled = LiuMusicSettingsPreferences.boolean(app, "home_quick_actions", true)
        homePlaylistsEnabled = LiuMusicSettingsPreferences.boolean(app, "home_playlists", true)
        homeNewSongsEnabled = LiuMusicSettingsPreferences.boolean(app, "home_new_songs", true)
        homeSectionOrder = LiuMusicSettingsPreferences.string(app, "home_section_order", "QuickActions,Playlists,NewSongs")
            .split(',').filter { it in setOf("QuickActions", "Playlists", "NewSongs") }.distinct()
            .let { order -> (order + listOf("QuickActions", "Playlists", "NewSongs")).distinct() }
        exploreTabEnabled = LiuMusicSettingsPreferences.boolean(app, "tab_explore", true)
        libraryTabEnabled = LiuMusicSettingsPreferences.boolean(app, "tab_library", true)
        rememberLastTab = LiuMusicSettingsPreferences.boolean(app, "general_remember_tab", true)
        disableAutomaticTabBarShrink = LiuMusicSettingsPreferences.boolean(app, "general_disable_auto_tabbar_shrink", false)
        tabOrder = LiuMusicSettingsPreferences.string(app, "tab_order", "Home,Explore,Library,Podcasts,Downloads,Cloud,Settings")
            .split(',').filter { it in setOf("Home", "Explore", "Library", "Podcasts", "Downloads", "Cloud", "Settings") }.distinct()
            .let { order -> (order + listOf("Home", "Explore", "Library", "Podcasts", "Downloads", "Cloud", "Settings")).distinct() }
        defaultTab = LiuMusicSettingsPreferences.string(app, "general_default_tab", "Home")
        rememberLibraryPage = LiuMusicSettingsPreferences.boolean(app, "library_remember_page", true)
        defaultLibraryPage = LiuMusicSettingsPreferences.string(app, "library_default_page", "Songs")
        downloadLyricsEnabled = LiuMusicSettingsPreferences.boolean(app, "download_lyrics", true)
        musicArea = LiuMusicSettingsPreferences.string(app, "music_area", "全部")
        showPlaylistPlayCount = LiuMusicSettingsPreferences.boolean(app, "content_playlist_play_count", true)
        showHighQualityPlaylists = LiuMusicSettingsPreferences.boolean(app, "content_high_quality_playlist", true)
        clipboardLinksEnabled = LiuMusicSettingsPreferences.boolean(app, "general_clipboard_links", true)
        hapticFeedbackEnabled = LiuMusicSettingsPreferences.boolean(app, "general_haptic_feedback", true)
        swipeFullAction = runCatching {
            LiuMusicSwipeFullAction.valueOf(
                LiuMusicSettingsPreferences.string(app, "general_swipe_full_action", LiuMusicSwipeFullAction.PlayNext.name),
            )
        }.getOrDefault(LiuMusicSwipeFullAction.PlayNext)
        previousRestartsAfterFiveSeconds = LiuMusicSettingsPreferences.boolean(app, "playback_previous_restarts", true)
        startsHeartModeOnLaunch = LiuMusicSettingsPreferences.boolean(app, "playback_heart_mode_on_launch", false)
        volumeControlMode = runCatching {
            LiuMusicVolumeControlMode.valueOf(LiuMusicSettingsPreferences.string(app, "playback_volume_mode", LiuMusicVolumeControlMode.System.name))
        }.getOrDefault(LiuMusicVolumeControlMode.System)
    }

    private fun annotationMode(
        context: Context,
        key: String,
        default: LiuMusicLyricAnnotationDisplayMode,
    ): LiuMusicLyricAnnotationDisplayMode = runCatching {
        LiuMusicLyricAnnotationDisplayMode.valueOf(
            LiuMusicSettingsPreferences.string(context, key, default.name),
        )
    }.getOrDefault(default)
}

object LiuMusicSettingsPreferences {
    private const val NAME = "liumusic_app_settings"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun initialize(context: Context) = LiuMusicSettingsRuntime.initialize(context)

    fun initializeCritical(context: Context) = LiuMusicSettingsRuntime.initializeCritical(context)

    fun boolean(context: Context, key: String, default: Boolean = false): Boolean =
        prefs(context).getBoolean(key, default)

    fun string(context: Context, key: String, default: String = ""): String =
        prefs(context).getString(key, default) ?: default

    fun int(context: Context, key: String, default: Int = 0): Int =
        prefs(context).getInt(key, default)

    /**
     * 读取 `player_transition_duration_ms`，并做**一次性**的旧默认迁移。
     *
     * ### ⚠⚠ 迁移史（两次**方向相反**，别搞混）
     * · **v1**（2026-10-03，**已废弃**）：默认 `360 → 240`，一次性标记键
     *   `..._migrated_v1`。当时口径是「对齐 Apple Music 参考 ~200ms」。
     * · **v2**（2026-10-06，**当前**）：确认 **240 只是测试期口径**，正式默认回到 **360**
     *   → 需要把老设备里残留的 **240 纠正回 360**，一次性标记键 `..._migrated_v2`。
     *   ⚠⚠ **必须用新键**：老设备的 v1 标记已是 `true`，复用 v1 键会让纠正**永不执行**
     *     （正是「改了默认值但设备上没变」的经典坑）。
     *
     * ⚠⚠⚠ 为什么迁移必须落在**这个被两边共用的读取函数**上：
     *   设置页那项走 `selectedProvider = { playerTransitionDurationMs(context) }`
     *   （见 `SettingsScreen` 的「播放器展开/收回时长」），而运行时
     *   `LiuMusicSettingsRuntime.load()` 也调本函数 ⇒ 只有在这里迁移，
     *   「**显示 == 实际**」才成立。只在 `load()` 里迁 ⇒ 设置页仍显示旧值
     *   （历史报障原文：「**不要实际变，设置显示没变**」）。
     *
     * ⚠ v2 迁移是**无条件归一为 360**（不是「只把 240 改成 360」）：用户要的是
     *   「默认值**和设置显示**都回到 360」，而持久化值会盖住默认值，设备上还可能残留
     *   测试期跟着调的其它档 ⇒ 只有无条件归一才能让设置页**立刻**显示 360。
     *   **只做一次**，之后 `240/300/360/460/600` 五档全部自由可调、正常持久化。
     */
    fun playerTransitionDurationMs(context: Context): Int {
        val store = prefs(context)
        val migrated = store.getBoolean(KEY_PLAYER_TRANSITION_DURATION_MIGRATED_V2, false)
        if (!migrated) {
            // ⚠⚠ 2026-10-06（用户口径：「默认动画速度和**设置显示**改回 360，
            //   之前是**测试才**默认 240ms」）：
            //   这是**测试期口径的纠正**，所以这里**无条件**归一到 360，
            //   而不是「只把 240 改成 360」——
            //   · 用户要的是「**设置显示**也立刻是 360」，只改默认值 / 只挑 240 改
            //     都盖不住持久化值（设备上可能残留测试期跟着调的 300/460/600）；
            //   · **只做一次**（v2 标记），此后 240/300/360/460/600 五档全部自由可调、
            //     正常持久化，用户选什么就是什么。
            //   ⚠ 无存储值的新装设备走同一个分支 ⇒ 同样得到 360（正式默认）。
            store.edit()
                .putInt(KEY_PLAYER_TRANSITION_DURATION_MS, 360)
                .putBoolean(KEY_PLAYER_TRANSITION_DURATION_MIGRATED_V2, true)
                .apply()
            return 360
        }
        return store.getInt(KEY_PLAYER_TRANSITION_DURATION_MS, 360).coerceIn(200, 1_200)
    }

    const val KEY_PLAYER_TRANSITION_DURATION_MS = "player_transition_duration_ms"

    /**
     * v2 一次性标记：「测试期默认 240 → 正式默认 360」的纠正。
     *
     * ⚠ 用**独立键**（不复用 v1 的 `..._migrated_v1`）—— 老设备 v1 标记已为 true，
     *   复用会导致纠正永不执行。
     * ⚠ 也不能用「值等于 240 就算没选过」的判据单独做：那会让**用户手动选的 240**
     *   每次读取都被拉回 360（该档位不可持久化）。所以照样只在首读时做一次。
     */
    private const val KEY_PLAYER_TRANSITION_DURATION_MIGRATED_V2 =
        "player_transition_duration_ms__migrated_v2"

    fun float(context: Context, key: String, default: Float = 0f): Float =
        prefs(context).getFloat(key, default)

    fun number(context: Context, key: String, default: Float = 0f): Float =
        when (val value = prefs(context).all[key]) {
            is Float -> value
            is Int -> value.toFloat()
            is Long -> value.toFloat()
            is Double -> value.toFloat()
            else -> default
        }

    fun long(context: Context, key: String, default: Long = 0L): Long =
        prefs(context).getLong(key, default)

    fun setLong(context: Context, key: String, value: Long) {
        prefs(context).edit().putLong(key, value).apply()
    }

    fun setBoolean(context: Context, key: String, value: Boolean) {
        prefs(context).edit().putBoolean(key, value).apply()
        if (key == "lyrics_auto_select" && !value) {
            prefs(context).edit().putBoolean("experimental_lyric_strong_binding", false).apply()
            LiuMusicSettingsRuntime.lyricStrongBindingEnabled = false
            com.liuguanyi.liumusic.core.lyrics.LyricBindingStore.clear(context)
        }
        if (key == "experimental_lyric_strong_binding" && !value) {
            com.liuguanyi.liumusic.core.lyrics.LyricBindingStore.clear(context)
        }
        when (key) {
            "developer_performance_overlay" -> LiuMusicSettingsRuntime.performanceOverlayEnabled = value
            "feature_podcasts" -> LiuMusicSettingsRuntime.podcastsEnabled = value
            "feature_history" -> LiuMusicSettingsRuntime.listeningHistoryEnabled = value
            "feature_downloads" -> LiuMusicSettingsRuntime.downloadsEnabled = value
            "feature_cloud_music" -> LiuMusicSettingsRuntime.cloudMusicEnabled = value
            "placement_podcasts_home" -> LiuMusicSettingsRuntime.podcastsHomePlacement = value
            "placement_podcasts_tab" -> LiuMusicSettingsRuntime.podcastsTabPlacement = value
            "placement_podcasts_library" -> LiuMusicSettingsRuntime.podcastsLibraryPlacement = value
            "placement_downloads_home" -> LiuMusicSettingsRuntime.downloadsHomePlacement = value
            "placement_downloads_tab" -> LiuMusicSettingsRuntime.downloadsTabPlacement = value
            "placement_downloads_library" -> LiuMusicSettingsRuntime.downloadsLibraryPlacement = value
            "placement_cloud_home" -> LiuMusicSettingsRuntime.cloudHomePlacement = value
            "placement_cloud_tab" -> LiuMusicSettingsRuntime.cloudTabPlacement = value
            "placement_cloud_library" -> LiuMusicSettingsRuntime.cloudLibraryPlacement = value
            "player_flowing_backdrop" -> LiuMusicSettingsRuntime.flowingBackdropEnabled = value
            "player_artwork_motion" -> LiuMusicSettingsRuntime.artworkMotionEnabled = value
            "reduce_motion" -> LiuMusicSettingsRuntime.reduceMotion = value
            "player_background_isolation" -> LiuMusicSettingsRuntime.playerBackgroundIsolationEnabled = value
            "player_frosted_glass" -> LiuMusicSettingsRuntime.frostedGlassEnabled = value
            "bottom_bar_lock_dark_in_dark_mode" -> LiuMusicSettingsRuntime.bottomBarLockDarkInDarkMode = value
            "player_keep_screen_on" -> LiuMusicSettingsRuntime.keepScreenOn = value
            "lyrics_translation" -> LiuMusicSettingsRuntime.showLyricTranslation = value
            "lyrics_auto_select" -> LiuMusicSettingsRuntime.automaticLyricSelectionEnabled = value
            "experimental_lyric_strong_binding" -> LiuMusicSettingsRuntime.lyricStrongBindingEnabled = value
            "lyrics_romanization" -> LiuMusicSettingsRuntime.showLyricRomanization = value
            "lyrics_word_by_word" -> LiuMusicSettingsRuntime.lyricWordByWordEnabled = value
            "lyrics_pseudo_timing" -> LiuMusicSettingsRuntime.lyricPseudoTimingEnabled = value
            "lyrics_tap_seek" -> LiuMusicSettingsRuntime.lyricTapSeekEnabled = value
            "lyrics_long_press_share" -> LiuMusicSettingsRuntime.lyricLongPressShareEnabled = value
            "lyrics_interlude_countdown" -> LiuMusicSettingsRuntime.lyricInterludeCountdownEnabled = value
            "lyrics_auto_follow" -> LiuMusicSettingsRuntime.lyricAutoFollowEnabled = value
            "lyrics_reduce_motion" -> LiuMusicSettingsRuntime.lyricReduceMotion = value
            "lyrics_glow_long_tones_only" -> LiuMusicSettingsRuntime.lyricGlowLongTonesOnly = value
            "lyrics_glow_enabled" -> LiuMusicSettingsRuntime.lyricGlowEnabled = value
            "lyrics_word_bounce_enabled" -> LiuMusicSettingsRuntime.lyricWordBounceEnabled = value
            "lyrics_cascade_bounce_enabled" -> LiuMusicSettingsRuntime.lyricCascadeBounceEnabled = value
            "lyrics_scale_bounce_enabled" -> LiuMusicSettingsRuntime.lyricScaleBounceEnabled = value
            "lyrics_advance_word_by_word" -> LiuMusicSettingsRuntime.lyricAdvanceAppliesToWordByWord = value
            "lyrics_skyline_enabled" -> LiuMusicSettingsRuntime.skylineEnabled = value
            "lyrics_skyline_song_info" -> LiuMusicSettingsRuntime.skylineShowSongInfo = value
            "lyrics_skyline_keep_awake" -> LiuMusicSettingsRuntime.skylineKeepsScreenAwake = value
            "system_lyrics_enabled" -> LiuMusicSettingsRuntime.systemLyricsEnabled = value
            "lyrics_notifications_enabled" -> LiuMusicSettingsRuntime.lyricNotificationsEnabled = value
            "lyrics_notification_next_line" -> LiuMusicSettingsRuntime.lyricNotificationShowNextLine = value
            "lyrics_notification_progress" -> LiuMusicSettingsRuntime.lyricNotificationShowProgress = value
            "lyrics_notification_artwork" -> LiuMusicSettingsRuntime.lyricNotificationShowArtwork = value
            "lyrics_notification_background_only" -> LiuMusicSettingsRuntime.lyricNotificationBackgroundOnly = value
            "lyrics_notification_dismiss_paused" -> LiuMusicSettingsRuntime.lyricNotificationDismissWhenPaused = value
            "floating_lyrics_enabled" -> LiuMusicSettingsRuntime.floatingLyricsEnabled = value
            "floating_lyrics_high_contrast" -> LiuMusicSettingsRuntime.floatingHighContrast = value
            "autosync_word_lyrics_enabled" -> LiuMusicSettingsRuntime.autoSyncWordLyricsEnabled = value
            "tab_home" -> LiuMusicSettingsRuntime.homeTabEnabled = value
            "home_quick_actions" -> LiuMusicSettingsRuntime.homeQuickActionsEnabled = value
            "home_playlists" -> LiuMusicSettingsRuntime.homePlaylistsEnabled = value
            "home_new_songs" -> LiuMusicSettingsRuntime.homeNewSongsEnabled = value
            "tab_explore" -> LiuMusicSettingsRuntime.exploreTabEnabled = value
            "tab_library" -> LiuMusicSettingsRuntime.libraryTabEnabled = value
            "general_remember_tab" -> LiuMusicSettingsRuntime.rememberLastTab = value
            "general_disable_auto_tabbar_shrink" -> LiuMusicSettingsRuntime.disableAutomaticTabBarShrink = value
            "library_remember_page" -> LiuMusicSettingsRuntime.rememberLibraryPage = value
            "download_lyrics" -> LiuMusicSettingsRuntime.downloadLyricsEnabled = value
            "content_playlist_play_count" -> LiuMusicSettingsRuntime.showPlaylistPlayCount = value
            "content_high_quality_playlist" -> LiuMusicSettingsRuntime.showHighQualityPlaylists = value
            "general_clipboard_links" -> LiuMusicSettingsRuntime.clipboardLinksEnabled = value
            "general_haptic_feedback" -> LiuMusicSettingsRuntime.hapticFeedbackEnabled = value
            "playback_previous_restarts" -> LiuMusicSettingsRuntime.previousRestartsAfterFiveSeconds = value
            "playback_heart_mode_on_launch" -> LiuMusicSettingsRuntime.startsHeartModeOnLaunch = value
            "immersive_playback" -> LiuMusicSettingsRuntime.immersivePlaybackEnabled = value
            "player_show_quality_tip" -> LiuMusicSettingsRuntime.showPlayerQualityTip = value
            "system_font" -> LiuMusicSettingsRuntime.systemFontEnabled = value
        }
    }

    fun setInt(context: Context, key: String, value: Int) {
        prefs(context).edit().putInt(key, value).apply()
        when (key) {
            "lyrics_advance_ms" -> LiuMusicSettingsRuntime.lyricAdvanceMs = value.coerceIn(-5_000, 5_000)
            "lyrics_follow_delay_ms" -> LiuMusicSettingsRuntime.lyricFollowDelayMs = value.coerceIn(1_000, 8_000)
            "lyrics_refresh_rate" -> LiuMusicSettingsRuntime.lyricRefreshRate =
                value.takeIf { it in setOf(30, 60, 90, 120) } ?: 60
            "lyrics_background_frame_rate" -> LiuMusicSettingsRuntime.lyricBackgroundFrameRate =
                value.takeIf { it in setOf(15, 24, 30, 45, 60) } ?: 24
            "player_transition_duration_ms" -> LiuMusicSettingsRuntime.playerTransitionDurationMs =
                value.coerceIn(200, 1_200)
            "lyrics_long_tone_threshold_ms" -> LiuMusicSettingsRuntime.lyricLongToneThresholdMs = value.coerceIn(300, 1_500)
            "lyrics_interface_auto_hide_ms" -> LiuMusicSettingsRuntime.lyricInterfaceAutoHideDelayMs = value.coerceIn(3_000, 15_000)
            "lyrics_scroll_hide_threshold_dp" -> LiuMusicSettingsRuntime.lyricScrollHideThresholdDp = value.coerceIn(40, 240)
            "lyrics_scale_bounce_duration_ms" -> LiuMusicSettingsRuntime.lyricScaleBounceDurationMs = value.coerceIn(150, 800)
            "lyrics_focus_color_lead_ms" -> LiuMusicSettingsRuntime.lyricFocusColorLeadMs = value.coerceIn(-300, 300)
            "lyrics_skyline_ambient_lines" -> LiuMusicSettingsRuntime.skylineAmbientLines = value.coerceIn(0, 4)
            "lyrics_skyline_ambient_max_characters" -> LiuMusicSettingsRuntime.skylineAmbientMaximumCharacters = value.coerceIn(1, 4)
            "lyrics_skyline_ambient_max_visible" -> LiuMusicSettingsRuntime.skylineAmbientMaximumVisibleTexts = value.coerceIn(4, 24)
            "floating_lyrics_font_size" -> LiuMusicSettingsRuntime.floatingFontSizeSp = value.coerceIn(14, 28)
        }
    }

    fun setFloat(context: Context, key: String, value: Float) {
        prefs(context).edit().putFloat(key, value).apply()
        when (key) {
            "lyrics_font_scale" -> LiuMusicSettingsRuntime.lyricFontScale = value.coerceIn(.8f, 1.25f)
            "lyrics_focus_position" -> LiuMusicSettingsRuntime.lyricFocusPosition = value.coerceIn(.05f, .8f)
            "lyrics_spacing_scale" -> LiuMusicSettingsRuntime.lyricSpacingScale = value.coerceIn(.7f, 1.5f)
            "lyrics_blur_strength" -> LiuMusicSettingsRuntime.lyricBlurStrength = value.coerceIn(0f, 1.5f)
            "lyrics_distance_blur_scale" -> LiuMusicSettingsRuntime.lyricDistanceBlurScale = value.coerceIn(0f, 1.5f)
            "lyrics_hidden_blur_scale" -> LiuMusicSettingsRuntime.lyricHiddenInterfaceBlurScale = value.coerceIn(0f, 1.5f)
            "lyrics_dim_amount" -> LiuMusicSettingsRuntime.lyricDimAmount = value.coerceIn(0f, 1f)
            "lyrics_focus_scale" -> LiuMusicSettingsRuntime.lyricFocusScale = value.coerceIn(1f, 1.08f)
            "lyrics_inactive_opacity" -> LiuMusicSettingsRuntime.lyricInactiveOpacity = value.coerceIn(.15f, .65f)
            "lyrics_glow_strength" -> LiuMusicSettingsRuntime.lyricGlowStrength = value.coerceIn(0f, 1.5f)
            "lyrics_long_tone_strength" -> LiuMusicSettingsRuntime.lyricLongToneStrength = value.coerceIn(0f, 1.5f)
            "lyrics_highlight_gradient_width" -> LiuMusicSettingsRuntime.lyricHighlightGradientWidth = value.coerceIn(.4f, 3f)
            "lyrics_highlight_gradient_reduction" -> LiuMusicSettingsRuntime.lyricHighlightGradientReduction = value.coerceIn(0f, 1f)
            "lyrics_romanization_font_scale" -> LiuMusicSettingsRuntime.lyricRomanizationFontScale = value.coerceIn(.5f, .8f)
            "lyrics_romanization_opacity" -> LiuMusicSettingsRuntime.lyricRomanizationOpacity = value.coerceIn(.4f, .9f)
            "lyrics_translation_font_scale" -> LiuMusicSettingsRuntime.lyricTranslationFontScale = value.coerceIn(.5f, .8f)
            "lyrics_translation_opacity" -> LiuMusicSettingsRuntime.lyricTranslationOpacity = value.coerceIn(.4f, .9f)
            "lyrics_cascade_delay_ms" -> LiuMusicSettingsRuntime.lyricCascadeDelayMs = value.coerceIn(0f, 100f)
            "lyrics_cascade_delay_increase_ms" -> LiuMusicSettingsRuntime.lyricCascadeDelayIncreaseMs = value.coerceIn(0f, 100f)
            "lyrics_cascade_following_delay_ms" -> LiuMusicSettingsRuntime.lyricCascadeFollowingDelayMs = value.coerceIn(0f, 200f)
            "lyrics_cascade_catch_up_ratio" -> LiuMusicSettingsRuntime.lyricCascadeCatchUpRatio = value.coerceIn(.5f, 1f)
            "lyrics_cascade_chase_gradient" -> LiuMusicSettingsRuntime.lyricCascadeChaseSpeedGradient = value.coerceIn(0f, 1f)
            "lyrics_cascade_duration_ms" -> LiuMusicSettingsRuntime.lyricCascadeDurationMs = value.coerceIn(200f, 1_200f)
            "lyrics_snap_threshold_ms" -> LiuMusicSettingsRuntime.lyricSnapThresholdMs = value.coerceIn(50f, 500f)
            "lyrics_cascade_bounce" -> LiuMusicSettingsRuntime.lyricCascadeBounce = value.coerceIn(0f, .8f)
            "lyrics_cascade_bounce_gradient" -> LiuMusicSettingsRuntime.lyricCascadeBounceGradient = value.coerceIn(0f, 1f)
            "lyrics_scale_bounce" -> LiuMusicSettingsRuntime.lyricScaleBounce = value.coerceIn(0f, .5f)
            "lyrics_skyline_current_font_size" -> LiuMusicSettingsRuntime.skylineCurrentFontSize = value.coerceIn(36f, 84f)
            "lyrics_skyline_current_max_scale" -> LiuMusicSettingsRuntime.skylineCurrentMaximumScale = value.coerceIn(1f, 1.2f)
            "lyrics_skyline_next_font_size" -> LiuMusicSettingsRuntime.skylineNextFontSize = value.coerceIn(14f, 44f)
            "lyrics_skyline_current_spacing" -> LiuMusicSettingsRuntime.skylineCurrentSpacing = value.coerceIn(4f, 36f)
            "lyrics_skyline_current_width" -> LiuMusicSettingsRuntime.skylineCurrentWidth = value.coerceIn(.4f, .82f)
            "lyrics_skyline_next_opacity" -> LiuMusicSettingsRuntime.skylineNextOpacity = value.coerceIn(.2f, .8f)
            "lyrics_skyline_ambient_font_size" -> LiuMusicSettingsRuntime.skylineAmbientFontSize = value.coerceIn(24f, 72f)
            "lyrics_skyline_ambient_opacity" -> LiuMusicSettingsRuntime.skylineAmbientOpacity = value.coerceIn(.4f, 1.8f)
            "lyrics_skyline_ambient_blur" -> LiuMusicSettingsRuntime.skylineAmbientBlur = value.coerceIn(0f, 2f)
            "lyrics_skyline_ambient_max_tilt" -> LiuMusicSettingsRuntime.skylineAmbientMaximumTilt = value.coerceIn(0f, 20f)
            "lyrics_skyline_ambient_drift" -> LiuMusicSettingsRuntime.skylineAmbientDrift = value.coerceIn(0f, 2f)
            "lyrics_text_pv_motion_intensity" -> LiuMusicSettingsRuntime.textPVMotionIntensity = value.coerceIn(0f, 2f)
            "lyrics_text_pv_animation_speed" -> LiuMusicSettingsRuntime.textPVAnimationSpeed = value.coerceIn(0f, 4f)
            "player_flowing_speed" -> LiuMusicSettingsRuntime.flowingLightSpeed = value.coerceIn(.25f, 2f)
            "player_flowing_saturation" -> LiuMusicSettingsRuntime.flowingLightSaturation = value.coerceIn(0f, 2f)
            "player_flowing_brightness" -> LiuMusicSettingsRuntime.flowingLightBrightness = value.coerceIn(.4f, 1.6f)
        }
    }

    fun setString(context: Context, key: String, value: String) {
        prefs(context).edit().putString(key, value).apply()
        when (key) {
            "theme_mode" -> LiuMusicSettingsRuntime.themeMode = runCatching {
                LiuMusicThemeMode.valueOf(value)
            }.getOrDefault(LiuMusicThemeMode.System)
            "music_area" -> LiuMusicSettingsRuntime.musicArea = value
            "tab_order" -> LiuMusicSettingsRuntime.tabOrder = value.split(',')
                .filter { it in setOf("Home", "Explore", "Library", "Podcasts", "Downloads", "Cloud", "Settings") }.distinct()
                .let { order -> (order + listOf("Home", "Explore", "Library", "Podcasts", "Downloads", "Cloud", "Settings")).distinct() }
            "home_section_order" -> LiuMusicSettingsRuntime.homeSectionOrder = value.split(',')
                .filter { it in setOf("QuickActions", "Playlists", "NewSongs") }.distinct()
                .let { order -> (order + listOf("QuickActions", "Playlists", "NewSongs")).distinct() }
            "general_default_tab" -> LiuMusicSettingsRuntime.defaultTab = value
            "general_swipe_full_action" -> LiuMusicSettingsRuntime.swipeFullAction = runCatching {
                LiuMusicSwipeFullAction.valueOf(value)
            }.getOrDefault(LiuMusicSwipeFullAction.PlayNext)
            "library_default_page" -> LiuMusicSettingsRuntime.defaultLibraryPage = value
            "lyrics_romanization_display_mode" -> LiuMusicSettingsRuntime.lyricRomanizationDisplayMode = runCatching {
                LiuMusicLyricAnnotationDisplayMode.valueOf(value)
            }.getOrDefault(LiuMusicLyricAnnotationDisplayMode.FocusedLine)
            "lyrics_translation_display_mode" -> LiuMusicSettingsRuntime.lyricTranslationDisplayMode = runCatching {
                LiuMusicLyricAnnotationDisplayMode.valueOf(value)
            }.getOrDefault(LiuMusicLyricAnnotationDisplayMode.AllLines)
            "lyrics_rendering_quality" -> LiuMusicSettingsRuntime.lyricRenderingQuality = runCatching {
                LiuMusicLyricsRenderingQuality.valueOf(value)
            }.getOrDefault(LiuMusicLyricsRenderingQuality.High)
            "lyrics_style" -> LiuMusicSettingsRuntime.lyricsStyle = runCatching {
                LiuMusicLyricsStyle.valueOf(value)
            }.getOrDefault(LiuMusicLyricsStyle.AppleMusic)
            "lyric_language" -> LiuMusicSettingsRuntime.lyricLanguage = value
            "player_background_mode" -> {
                LiuMusicSettingsRuntime.playerBackgroundMode = runCatching {
                    LiuMusicPlayerBackgroundMode.valueOf(value)
                }.getOrDefault(LiuMusicPlayerBackgroundMode.FlowingLight)
                LiuMusicSettingsRuntime.flowingBackdropEnabled =
                    LiuMusicSettingsRuntime.playerBackgroundMode != LiuMusicPlayerBackgroundMode.BlurredArtwork
            }
            "player_shell" -> LiuMusicSettingsRuntime.playerShell = runCatching {
                LiuMusicPlayerShell.valueOf(value)
            }.getOrDefault(LiuMusicPlayerShell.AppleMusic)
            "lyrics_text_pv_style" -> {
                LiuMusicSettingsRuntime.textPVStyle = runCatching {
                    LiuMusicTextPVStyle.valueOf(value)
                }.getOrDefault(LiuMusicTextPVStyle.BlueBold)
                setFloat(
                    context,
                    "lyrics_text_pv_animation_speed",
                    LiuMusicSettingsRuntime.textPVStyle.referenceAnimationSpeed,
                )
            }
            "lyrics_font_weight" -> LiuMusicSettingsRuntime.lyricFontWeight = runCatching {
                LiuMusicLyricsFontWeight.valueOf(value)
            }.getOrDefault(LiuMusicLyricsFontWeight.Heavy)
            "lyrics_lift_mode" -> LiuMusicSettingsRuntime.lyricLiftMode = runCatching {
                LiuMusicLyricsGroupingMode.valueOf(value)
            }.getOrDefault(LiuMusicLyricsGroupingMode.Character)
            "lyrics_long_tone_detection" -> LiuMusicSettingsRuntime.lyricLongToneDetectionMode = runCatching {
                LiuMusicLyricsGroupingMode.valueOf(value)
            }.getOrDefault(LiuMusicLyricsGroupingMode.Character)
            "system_lyrics_title_mode" -> LiuMusicSettingsRuntime.systemLyricTitleMode = runCatching {
                LiuMusicSystemLyricTitleMode.valueOf(value)
            }.getOrDefault(LiuMusicSystemLyricTitleMode.LyricFirst)
            "lyrics_notification_title_template" -> LiuMusicSettingsRuntime.lyricNotificationTitleTemplate = value
            "lyrics_notification_subtitle_template" -> LiuMusicSettingsRuntime.lyricNotificationSubtitleTemplate = value
            "lyrics_notification_fallback" -> LiuMusicSettingsRuntime.lyricNotificationFallback = value
            "floating_lyrics_secondary_mode" -> LiuMusicSettingsRuntime.floatingSecondaryMode = runCatching {
                LiuMusicSecondaryLyricMode.valueOf(value)
            }.getOrDefault(LiuMusicSecondaryLyricMode.Auto)
            "playback_volume_mode" -> LiuMusicSettingsRuntime.volumeControlMode = runCatching {
                LiuMusicVolumeControlMode.valueOf(value)
            }.getOrDefault(LiuMusicVolumeControlMode.System)
            "player_screen_awake_mode" -> LiuMusicSettingsRuntime.screenAwakeMode = runCatching {
                LiuMusicScreenAwakeMode.valueOf(value)
            }.getOrDefault(LiuMusicScreenAwakeMode.Disabled)
        }
    }

    fun reset(context: Context) {
        prefs(context).edit().clear().apply()
        LiuMusicSettingsRuntime.initialize(context, force = true)
    }

    fun resetRecommendedPlayerSettings(context: Context) {
        prefs(context).edit()
            .remove("player_background_mode")
            .remove("player_shell")
            .remove("player_flowing_backdrop")
            .remove("player_artwork_motion")
            .remove("player_background_isolation")
            .remove("player_frosted_glass")
            .remove(KEY_PLAYER_TRANSITION_DURATION_MS)
            // ⚠ 2026-10-06：连**迁移标记**一起清掉，否则「恢复默认」后标记仍在
            //   ⇒ 迁移逻辑不再跑。清掉后下次读取走首读分支 ⇒ 回到**正式默认 360**。
            //   ⚠ 清的是 **v2** 标记（当前生效的那个）；v1 是已废弃键，不必再管。
            .remove(KEY_PLAYER_TRANSITION_DURATION_MIGRATED_V2)
            .remove("lyrics_style")
            .remove("lyrics_rendering_quality")
            .remove("lyrics_text_pv_style")
            .remove("lyrics_text_pv_motion_intensity")
            .remove("lyrics_text_pv_animation_speed")
            .apply()
        LiuMusicPlaybackModePreferences.reset(context)
        LiuMusicSettingsRuntime.initialize(context.applicationContext, force = true)
    }
}
