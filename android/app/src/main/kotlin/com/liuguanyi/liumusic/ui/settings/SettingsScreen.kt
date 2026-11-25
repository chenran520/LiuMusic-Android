package com.liuguanyi.liumusic.ui.settings

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.StatFs
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liuguanyi.liumusic.ui.animation.LiuMusicPageSpec
import com.liuguanyi.liumusic.ui.LiuMusicInterruptiblePage
import com.liuguanyi.liumusic.ui.liuMusicPageRecede
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import coil3.compose.AsyncImage
import com.liuguanyi.liumusic.R
import com.liuguanyi.liumusic.BuildConfig
import com.liuguanyi.liumusic.core.account.NeteaseSessionStore
import com.liuguanyi.liumusic.core.diagnostics.LiuMusicLogExporter
import com.liuguanyi.liumusic.core.diagnostics.LiuMusicLogDeviceInfo
import com.liuguanyi.liumusic.core.provider.bilibili.BilibiliPlaybackAssociationStore
import com.liuguanyi.liumusic.core.music.provider.PlaybackAccountSlot
import com.liuguanyi.liumusic.core.music.provider.PlaybackAccountStore
import com.liuguanyi.liumusic.core.music.model.MusicSource
import com.liuguanyi.liumusic.core.music.model.MusicPlaylistSummary
import com.liuguanyi.liumusic.core.music.model.MusicResourceId
import com.liuguanyi.liumusic.core.music.provider.LiuMusicMusicProviders
import com.liuguanyi.liumusic.core.music.provider.UserLibraryCapability
import com.liuguanyi.liumusic.core.library.NeteaseLibraryClient
import com.liuguanyi.liumusic.core.provider.qqmusic.QQMusicSessionStore
import com.liuguanyi.liumusic.core.provider.kugou.KugouSessionStore
import com.liuguanyi.liumusic.ui.account.QQMusicLoginScreen
import com.liuguanyi.liumusic.ui.account.KugouLoginScreen
import com.liuguanyi.liumusic.ui.account.LiuMusicNeteaseLoginActivity
import com.liuguanyi.liumusic.playback.ProviderPlaybackQualityRuntime
import com.liuguanyi.liumusic.playback.CrossProviderPlaybackPreferences
import com.liuguanyi.liumusic.core.audio.MusicQuality
import com.liuguanyi.liumusic.core.audio.MusicQualityPreferences
import com.liuguanyi.liumusic.core.download.LiuMusicDownloadStore
import com.liuguanyi.liumusic.core.network.LiuMusicMessageContact
import com.liuguanyi.liumusic.core.network.LiuMusicPrivateMessage
import com.liuguanyi.liumusic.core.network.NeteaseMusicOperationsClient
import com.liuguanyi.liumusic.core.network.NeteaseSearchClient
import com.liuguanyi.liumusic.core.network.LiuMusicHttpClient
import com.liuguanyi.liumusic.core.network.LiuMusicGitHubRouting
import com.liuguanyi.liumusic.core.network.LiuMusicGitHubSource
import com.liuguanyi.liumusic.core.network.parseNeteaseListenTogetherInvitation
import com.liuguanyi.liumusic.core.recommendation.LocalAnalysisStage
import com.liuguanyi.liumusic.core.recommendation.LocalRecommendationEngine
import com.liuguanyi.liumusic.core.recommendation.LocalRecommendationStore
import com.liuguanyi.liumusic.core.recognition.SongRecognitionClient
import com.liuguanyi.liumusic.core.recognition.SongRecognitionResult
import com.liuguanyi.liumusic.core.update.LiuMusicRelease
import com.liuguanyi.liumusic.core.update.LiuMusicDevCommit
import com.liuguanyi.liumusic.core.update.LiuMusicUpdateClient
import com.liuguanyi.liumusic.playback.PlaybackCommands
import com.liuguanyi.liumusic.playback.LiuMusicAutoMixFadeCurve
import com.liuguanyi.liumusic.playback.LiuMusicAutoMixFallback
import com.liuguanyi.liumusic.playback.LiuMusicAutoMixMode
import com.liuguanyi.liumusic.playback.LiuMusicAutoMixSettings
import com.liuguanyi.liumusic.playback.LiuMusicEqualizerController
import com.liuguanyi.liumusic.playback.LiuMusicPlaybackModePreferences
import com.liuguanyi.liumusic.playback.LiuMusicAudioAnalysisPreferences
import com.liuguanyi.liumusic.playback.LiuMusicPlaybackService
import com.liuguanyi.liumusic.playback.LiuMusicMediaCache
import com.liuguanyi.liumusic.playback.LiuMusicListenTogetherCoordinator
import com.liuguanyi.liumusic.platform.floating.LiuMusicFloatingLyricsService
import com.liuguanyi.liumusic.platform.xiaomi.HyperOsFocusBridge
import com.liuguanyi.liumusic.ui.LiuMusicBottomContentClearance
import com.liuguanyi.liumusic.ui.LiuMusicPinkCat
import com.liuguanyi.liumusic.ui.hotStringResource
import com.liuguanyi.liumusic.ui.hotText
import com.liuguanyi.liumusic.ui.glass.LiuMusicActionIcon
import com.liuguanyi.liumusic.ui.glass.LiuMusicGlassTextField
import com.liuguanyi.liumusic.ui.glass.LiuMusicGlassDialog
import com.liuguanyi.liumusic.ui.glass.LiuMusicGlassButton
import com.liuguanyi.liumusic.ui.glass.LiuMusicGlassButtonStyle
import com.liuguanyi.liumusic.ui.glass.LiuMusicGlassToggle
import com.liuguanyi.liumusic.ui.glass.LiuMusicLiquidSlider
import com.liuguanyi.liumusic.ui.glass.LiuMusicSettingsDropdown
import com.liuguanyi.liumusic.ui.glass.LiuMusicShapes
import com.liuguanyi.liumusic.ui.glass.LiuMusicTypography
import com.liuguanyi.liumusic.ui.glass.LiuMusicIosGroupedList
import com.liuguanyi.liumusic.ui.glass.LiuMusicIosListRow
import com.liuguanyi.liumusic.ui.glass.LiuMusicIosTopBar
import com.liuguanyi.liumusic.ui.glass.LiuMusicPinnedListPage
import com.liuguanyi.liumusic.ui.layout.rememberLiuMusicStableStatusBarHeight
import com.liuguanyi.liumusic.ui.glass.LiuMusicSymbol
import com.liuguanyi.liumusic.ui.glass.LiuMusicSymbolIcon
import com.liuguanyi.liumusic.ui.glass.LiuMusicSymbolVariant
import com.liuguanyi.liumusic.ui.glass.LiuMusicSystemColors
import com.liuguanyi.liumusic.ui.glass.liuMusicContentSurface
import com.liuguanyi.liumusic.ui.glass.liuMusicLiquidButton
import com.liuguanyi.liumusic.ui.legal.LIUMUSIC_LEGAL_VERSION
import com.liuguanyi.liumusic.ui.legal.LiuMusicLegalDocument
import com.liuguanyi.liumusic.ui.legal.LiuMusicLegalDocumentDialog
import com.liuguanyi.liumusic.ui.legal.LiuMusicLegalLinks
import com.liuguanyi.liumusic.core.remoteconfig.LiuMusicRemoteConfigRuntime
import com.liuguanyi.liumusic.core.remoteconfig.LiuMusicRemoteConfigSource
import com.liuguanyi.liumusic.core.remoteconfig.LiuMusicRemoteConfigConsent
import com.liuguanyi.liumusic.ui.legal.LiuMusicCloudControlConsentDialog
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException

// ⚠ 必须 public：`LiuMusicSettingsDetailActivity.launch(context, route, source)` 是 public 的
//   Activity 入口，签名里出现 internal 类型会直接编译不过（public 函数暴露 internal 类型）。
enum class SettingsRoute(val title: String) {
    Playback("播放"),
    PlayerAppearance("外观"),
    Lyrics("歌词"),
    SystemPlayback("系统歌词显示"),
    SkylineLyrics("全屏天际歌词"),
    FloatingLyrics("悬浮窗歌词"),
    ContentFeatures("功能模块"),
    Blocklist("屏蔽管理"),
    Recognition("听歌识曲"),
    Messages("私信与站内分享"),
    ListenTogether("一起听"),
    Content("内容"),
    Storage("存储管理"),
    TabLayout("页面与标签栏"),
    General("通用"),
    RemoteConfig("远程兼容性配置"),
    About("关于 LiuMusic"),
    Legal("隐私政策与免责声明"),
    Privacy("隐私与本地算法"),
    Experimental("测试功能尝鲜"),
}

private data class SettingsItem(
    val route: SettingsRoute,
    val subtitle: String,
    val symbol: String,
    val keywords: String,
)

private data class SettingsSection(val title: String, val items: List<SettingsItem>)

private val SettingsSections = listOf(
    SettingsSection("应用", listOf(
        SettingsItem(SettingsRoute.General, "主题、启动行为与链接处理", "⚙", "主题 浅色 深色 跟随系统 默认页面 剪贴板"),
        SettingsItem(SettingsRoute.PlayerAppearance, "背景、封面动画与屏幕常亮", "✦", "模糊 色彩 饱和度 封面 自动锁屏"),
        SettingsItem(SettingsRoute.Content, "地区、歌单信息和发现内容", "▦", "华语 欧美 韩国 日本 播放量 内容"),
        SettingsItem(SettingsRoute.Playback, "音质、播放行为与自动混音", "♫", "高品质 无损 上一首 页面记忆 心动模式 交叉淡化 播放"),
        SettingsItem(SettingsRoute.Lyrics, "翻译、罗马音、逐字与歌词交互", "❞", "Apple Music EVA 文字PV 字体 YRC 翻译 罗马音 歌词"),
        SettingsItem(SettingsRoute.Storage, "空间统计、下载与缓存清理", "▰", "下载 存储 缓存 清理 数据库"),
    )),
    SettingsSection("扩展", listOf(
        SettingsItem(SettingsRoute.ContentFeatures, "播客、云盘、最近播放等模块", "☷", "播客 广播 云盘 最近播放 下载"),
        SettingsItem(SettingsRoute.Recognition, "麦克风音频指纹与持续识别", "⌁", "听歌识曲 麦克风 指纹 Shazam 持续识别"),
        SettingsItem(SettingsRoute.Messages, "联系人、会话历史与文字私信", "✉", "私信 联系人 会话 分享 网易云"),
        SettingsItem(SettingsRoute.ListenTogether, "创建、加入和管理网易云一起听房间", "◎", "一起听 房间 邀请 同步"),
        SettingsItem(SettingsRoute.TabLayout, "首页、标签栏与音乐库页面", "▥", "首页 标签栏 排序 推荐 歌单 历史"),
        SettingsItem(SettingsRoute.SystemPlayback, "通知、锁屏和系统媒体信息", "▣", "控制中心 通知 锁屏 Media3"),
        SettingsItem(SettingsRoute.FloatingLyrics, "Android 悬浮歌词能力与权限", "▤", "画中画 悬浮窗 其他应用"),
    )),
    SettingsSection("关于", listOf(
        SettingsItem(SettingsRoute.RemoteConfig, "签名配置状态、平台熔断声明与本地缓存", "⌁", "远程 配置 云控 签名 熔断 兼容 GitHub"),
        SettingsItem(SettingsRoute.About, "版本、项目主页与开源信息", "ⓘ", "GitHub 更新 开源 许可"),
        SettingsItem(SettingsRoute.Legal, "查看隐私政策、免责声明与同意版本", "▤", "隐私 政策 免责声明 法律 条款 数据"),
        SettingsItem(SettingsRoute.Experimental, "预览尚未稳定的新功能", "✦", "实验 测试 歌词 强绑定"),
    )),
)

/**
 * 设置**主页**（列表 + 搜索）。
 *
 * ⚠⚠ 2026-10-02：详情页**不再**在这里以覆盖层形式推出，改成启动
 * [LiuMusicSettingsDetailActivity]（真实 Activity）⇒ 整页推入/退回的转场由平台负责。
 * 原先那套（`LiuMusicInterruptiblePage` + `AnimatedContent` 弹簧位移 + `liuMusicPageRecede`
 * 让列表页退让 + 自研 `PredictiveBackHandler` + 延后一帧可见）**整块删掉**，原因见
 * `ui/LiuMusicPageTransitions.kt` 里 `LiuMusicPageHost` 的说明：应用内重放系统已经做得更好的事，
 * 结果是转场被组合/绘制帧牵连，快速连续打断必然顿挫。
 *
 * ⚠ 深链（原来靠 `initialRouteRequest` 传进来的那些）现在同样直接启动
 * [LiuMusicSettingsDetailActivity]，见 `LiuMusicApp` 里 `settingsRouteRequest` 的两个调用点。
 */
@Composable
fun SettingsScreen(
    session: NeteaseSessionStore,
    source: MusicSource = MusicSource.Netease,
    onLogin: () -> Unit,
    onOpenAccount: (() -> Unit)? = null,
    onOpenServices: (() -> Unit)? = null,
    onOpenMessages: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    // 设置主页的滚动位置：详情页现在是另一个 Activity，本组合在它盖住期间仍然存活，
    // 退回时列表位置自然保持（`rememberScrollState` 由 `rememberSaveable` 兜底）。
    val rootScrollState = rememberScrollState()

    LaunchedEffect(Unit) { LiuMusicSettingsPreferences.initialize(context) }
    LaunchedEffect(Unit) {
        if (LocalRecommendationStore.isAlgorithmEnabled(context) && LocalRecommendationStore.hasPersonalizationConsent(context)) {
            LocalRecommendationEngine.start(context)
        }
    }
    LaunchedEffect(session.cookie) {
        if (session.isLoggedIn) session.refreshProfile()
    }

    val normalized = search.trim().lowercase()
    val visibleSections = SettingsSections.mapNotNull { section ->
        val filtered = section.items.filter { item ->
            normalized.isBlank() || listOf(item.route.title, item.subtitle, item.keywords)
                .joinToString(" ").lowercase().contains(normalized)
        }
        filtered.takeIf { it.isNotEmpty() }?.let { SettingsSection(section.title, it) }
    }

    // ⚠ 2026-09-29：状态栏避让必须放在 `verticalScroll` **之后**（即滚动内容的内边距里）。
    //   原来 `.statusBarsPadding()` 打在 `verticalScroll` 之前 ⇒ 滚动视口被压到状态栏下缘
    //   (41dp)，滚上去的内容一律在 y=41dp 被裁掉、**永远进不了状态栏/挖孔区域**。
    //   改成内边距后静止态位置不变（18+41），滚动时头部从状态栏底下穿出。
    val statusBarTop = rememberLiuMusicStableStatusBarHeight()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rootScrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp + statusBarTop, bottom = LiuMusicBottomContentClearance),
    ) {
        LiuMusicIosTopBar(
            title = stringResource(R.string.tab_settings),
            modifier = Modifier.padding(horizontal = 0.dp),
            contentPadding = PaddingValues(horizontal = 0.dp),
        )
        Spacer(Modifier.height(14.dp))
        SettingsSearchField(value = search, onValueChange = { search = it })
        Spacer(Modifier.height(20.dp))

        if (normalized.isBlank() || stringResource(R.string.settings_account_keywords).contains(normalized)) {
            SettingsAccountCard(
                session = session,
                onLogin = onLogin,
                onOpenAccount = onOpenAccount,
                onOpenServices = onOpenServices,
            )
            Spacer(Modifier.height(24.dp))
        }

        visibleSections.forEach { section ->
            Text(
                section.title,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 8.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            SettingsSectionCard(section) { selected ->
                if (selected == SettingsRoute.Messages && onOpenMessages != null) {
                    onOpenMessages()
                } else {
                    // 详情页是**独立 Activity** ⇒ 整页推入转场由平台负责（可连续打断、
                    // 带系统屏幕圆角、支持预测式返回）。见 [LiuMusicSettingsDetailActivity]。
                    LiuMusicSettingsDetailActivity.launch(context, selected, source)
                }
            }
            Spacer(Modifier.height(22.dp))
        }

        if (visibleSections.isEmpty() && normalized.isNotBlank()) {
            Text(
                stringResource(R.string.settings_search_empty),
                modifier = Modifier.padding(vertical = 36.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
            )
        }

        if (normalized.isBlank()) {
            SettingsResetCard()
            Spacer(Modifier.height(18.dp))
            if (session.isLoggedIn) {
                SettingsDangerButton(stringResource(R.string.settings_sign_out)) { session.clear() }
            }
        }
    }
}

@Composable
private fun SettingsSearchField(value: String, onValueChange: (String) -> Unit) {
    LiuMusicGlassTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        leadingContent = {
            LiuMusicSymbolIcon(
                symbol = LiuMusicSymbol.Search,
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f),
            )
        },
        placeholder = { Text(stringResource(R.string.settings_search_placeholder), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .42f), fontSize = 16.sp) },
        textStyle = androidx.compose.ui.text.TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            lineHeight = 21.sp,
        ),
    )
}

@Composable
private fun SettingsAccountCard(
    session: NeteaseSessionStore,
    onLogin: () -> Unit,
    onOpenAccount: (() -> Unit)?,
    onOpenServices: (() -> Unit)?,
) {
    val accent = com.liuguanyi.liumusic.ui.glass.LiuMusicSystemColors.Red
    Text(
        stringResource(R.string.accessibility_account),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.48f),
    )
    LiuMusicIosGroupedList(surfaceColor = MaterialTheme.colorScheme.surface) {
        when {
            session.isLoggedIn && session.profile != null -> {
                val profile = session.profile!!
                LiuMusicIosListRow(
                    title = profile.nickname,
                    leading = { AsyncImage(model = profile.avatarUrl, contentDescription = null, modifier = Modifier.size(30.dp).clip(CircleShape)) },
                    detail = stringResource(R.string.provider_logged_in),
                    chevronTint = accent,
                     onClick = onOpenAccount ?: onLogin,
                    showTopSeparator = false,
                )
            }
            session.isLoggedIn && session.isRefreshing -> LiuMusicIosListRow(
                title = stringResource(R.string.settings_account_loading),
                leading = { CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = accent) },
                onClick = onOpenServices,
                showTopSeparator = false,
            )
            else -> LiuMusicIosListRow(
                title = stringResource(R.string.app_login_netease),
                leading = { LiuMusicSymbolIcon(LiuMusicSymbol.Person, Modifier.size(30.dp), accent, LiuMusicSymbolVariant.Fill) },
                chevronTint = accent,
                 onClick = onOpenAccount ?: onLogin,
                showTopSeparator = false,
            )
        }
        if (onOpenServices != null) {
            LiuMusicIosListRow(
                title = stringResource(R.string.tab_services),
                subtitle = stringResource(R.string.settings_services_subtitle),
                leading = { LiuMusicSymbolIcon(LiuMusicSymbol.MusicNote, Modifier.size(30.dp), accent) },
                chevronTint = accent,
                onClick = onOpenServices,
                showTopSeparator = true,
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(section: SettingsSection, onOpen: (SettingsRoute) -> Unit) {
    val accent = com.liuguanyi.liumusic.ui.glass.LiuMusicSystemColors.Red
    LiuMusicIosGroupedList(surfaceColor = MaterialTheme.colorScheme.surface) {
        section.items.forEach { item ->
            LiuMusicIosListRow(
                title = item.route.title,
                leading = {
                    Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                        LiuMusicActionIcon(item.symbol, Modifier.size(22.dp), accent)
                    }
                },
                chevronTint = accent,
                onClick = { onOpen(item.route) },
                showTopSeparator = item != section.items.first(),
            )
        }
    }
}

@Composable
internal fun SettingsDetailScreen(route: SettingsRoute, source: MusicSource, session: NeteaseSessionStore, onBack: () -> Unit) {
    val context = LocalContext.current
    LiuMusicPinnedListPage(
        title = route.title,
        onNavigateBack = onBack,
        bottomPadding = LiuMusicBottomContentClearance,
    ) {
        item(key = "settings-detail:${route.name}") {
            Column {
                when (route) {
                    SettingsRoute.Playback -> PlaybackSettings(context)
                    SettingsRoute.PlayerAppearance -> PlayerAppearanceSettings(context)
                    SettingsRoute.Lyrics -> LyricsSettings(context)
                    SettingsRoute.SystemPlayback -> SystemPlaybackSettings(context)
                    SettingsRoute.FloatingLyrics -> FloatingLyricsSettings(context)
                    SettingsRoute.ContentFeatures -> ContentFeatureSettings(context)
                    SettingsRoute.Recognition -> LiuMusicRecognitionPanel(context)
                    SettingsRoute.Messages -> MessagesSettings(context)
                    SettingsRoute.ListenTogether -> ListenTogetherSettings(context)
                    SettingsRoute.Content -> ContentSettings(context)
                    SettingsRoute.Storage -> StorageSettings(context)
                    SettingsRoute.TabLayout -> TabLayoutSettings(context)
                    SettingsRoute.General -> GeneralSettings(context)
                    SettingsRoute.RemoteConfig -> RemoteConfigSettings()
                    SettingsRoute.About -> AboutSettings(context)
                    SettingsRoute.Legal -> LegalSettings(context)
                    SettingsRoute.Privacy -> PrivacySettings(context)
                    SettingsRoute.Experimental -> ExperimentalSettings(context, source, session)
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun LegalSettings(context: android.content.Context) {
    var selectedDocument by remember { mutableStateOf<LiuMusicLegalDocument?>(null) }
    var cloudControlEnabled by remember { mutableStateOf(LiuMusicRemoteConfigConsent.enabled(context)) }
    var showCloudControlConsent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val consentVersion = remember {
        LiuMusicSettingsPreferences.string(context, "legal_consent_version")
    }

    SettingsGlassGroup {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.settings_legal_documents), fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = "当前文本版本：$LIUMUSIC_LEGAL_VERSION",
                modifier = Modifier.padding(top = 7.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
            Text(
                text = if (consentVersion.isBlank()) {
                    stringResource(R.string.settings_legal_no_consent)
                } else {
                    "已同意版本：$consentVersion"
                },
                modifier = Modifier.padding(top = 3.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .52f),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    LiuMusicIosGroupedList(surfaceColor = MaterialTheme.colorScheme.surface) {
        LiuMusicIosListRow(
            title = stringResource(R.string.legal_privacy),
            leading = {
                LiuMusicSymbolIcon(
                    LiuMusicSymbol.Info,
                    Modifier.size(24.dp),
                    LiuMusicSystemColors.Blue,
                )
            },
            onClick = { selectedDocument = LiuMusicLegalDocument.PrivacyPolicy },
            showTopSeparator = false,
        )
        LiuMusicIosListRow(
            title = stringResource(R.string.settings_legal_disclaimer),
            leading = {
                LiuMusicSymbolIcon(
                    LiuMusicSymbol.Book,
                    Modifier.size(24.dp),
                    LiuMusicSystemColors.Blue,
                )
            },
            onClick = { selectedDocument = LiuMusicLegalDocument.Disclaimer },
            showTopSeparator = true,
        )
        LiuMusicIosListRow(
            title = stringResource(R.string.legal_cloud),
            leading = {
                LiuMusicSymbolIcon(
                    LiuMusicSymbol.Info,
                    Modifier.size(24.dp),
                    LiuMusicSystemColors.Blue,
                )
            },
            onClick = { selectedDocument = LiuMusicLegalDocument.CloudControlPrivacy },
            showTopSeparator = true,
        )
        LiuMusicIosListRow(
            title = stringResource(R.string.settings_legal_third_party),
            leading = {
                LiuMusicSymbolIcon(
                    LiuMusicSymbol.Book,
                    Modifier.size(24.dp),
                    LiuMusicSystemColors.Blue,
                )
            },
            onClick = { selectedDocument = LiuMusicLegalDocument.ThirdPartyMusicSources },
            showTopSeparator = true,
        )
    }
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        SettingsExternalToggleRow(
            title = stringResource(R.string.settings_legal_remote_toggle),
            value = cloudControlEnabled,
            note = stringResource(R.string.settings_legal_remote_note),
            grouped = true,
        ) { requested ->
            if (requested) {
                showCloudControlConsent = true
            } else {
                LiuMusicRemoteConfigConsent.reject(context)
                cloudControlEnabled = false
                scope.launch { LiuMusicRemoteConfigRuntime.clearCache(context) }
            }
        }
    }
    Text(
        stringResource(R.string.settings_legal_remote_footer),
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
    )

    selectedDocument?.let { document ->
        LiuMusicLegalDocumentDialog(
            document = document,
            onDismiss = { selectedDocument = null },
        )
    }
    if (showCloudControlConsent) {
        LiuMusicCloudControlConsentDialog(
            onReject = { showCloudControlConsent = false },
            onAccept = {
                LiuMusicRemoteConfigConsent.accept(context)
                LiuMusicRemoteConfigRuntime.initializeAndRefresh(context, BuildConfig.VERSION_CODE, force = true)
                cloudControlEnabled = true
                showCloudControlConsent = false
            },
        )
    }
}

@Composable
private fun PrivacySettings(context: android.content.Context) {
    var algorithmEnabled by remember { mutableStateOf(LocalRecommendationStore.isAlgorithmEnabled(context)) }
    var consent by remember { mutableStateOf(LocalRecommendationStore.hasPersonalizationConsent(context)) }
    var showPolicy by remember { mutableStateOf(false) }
    var showClearPersonalization by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(10) }
    val progress by LocalRecommendationStore.progress

    LaunchedEffect(showPolicy) {
        if (!showPolicy) return@LaunchedEffect
        secondsLeft = 10
        while (secondsLeft > 0) { kotlinx.coroutines.delay(1_000L); secondsLeft-- }
    }

    SettingsGlassGroup {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.settings_privacy_data_title), fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.settings_privacy_data_body), modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f), fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        SettingsExternalToggleRow(stringResource(R.string.settings_privacy_algorithm), algorithmEnabled, stringResource(R.string.settings_privacy_algorithm_note)) {
            algorithmEnabled = it
            LocalRecommendationStore.setAlgorithmEnabled(context, it)
            if (it && consent) LocalRecommendationEngine.start(context) else if (!it) LocalRecommendationEngine.stop()
        }
    }
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        Text(stringResource(R.string.settings_privacy_personalization), modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text(if (consent) stringResource(R.string.settings_privacy_consented) else stringResource(R.string.settings_privacy_not_consented), modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 10.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f))
        SettingsActionButton(if (consent) stringResource(R.string.settings_privacy_withdraw) else stringResource(R.string.settings_privacy_read_policy)) {
            if (consent) {
                consent = false
                LocalRecommendationStore.clearPersonalization(context)
                LocalRecommendationEngine.stop()
            } else showPolicy = true
        }
        SettingsActionButton(stringResource(R.string.settings_privacy_clear_all)) { showClearPersonalization = true }
    }
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.settings_privacy_status), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(
                if (progress.isFullAnalysis) "后台全量分析：${progress.stage} · ${progress.processed}/${progress.total}"
                else "快速更新：${progress.stage}",
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
            )
            Text("模型后端：${progress.modelBackend}", modifier = Modifier.padding(top = 4.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .52f))
            progress.message?.let { Text(it, modifier = Modifier.padding(top = 5.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .52f)) }
        }
    }
    SettingsActionButton(stringResource(R.string.settings_privacy_start_full)) {
        if (algorithmEnabled && consent) LocalRecommendationEngine.startFullAnalysis(context)
    }
    if (showPolicy) {
        LiuMusicGlassDialog(visible = true, onDismiss = { if (secondsLeft == 0) showPolicy = false }) {
            Text(stringResource(R.string.settings_privacy_policy_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.settings_privacy_policy_body), modifier = Modifier.padding(top = 10.dp), fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(16.dp))
            SettingsActionButton(if (secondsLeft > 0) "请阅读后等待 ${secondsLeft} 秒" else stringResource(R.string.legal_agree_enable)) {
                if (secondsLeft == 0) {
                    consent = true; showPolicy = false
                    LocalRecommendationStore.setPersonalizationConsent(context, true)
                    LocalRecommendationStore.setConsentAt(context, System.currentTimeMillis())
                    if (algorithmEnabled) LocalRecommendationEngine.start(context)
                }
            }
        }
    }
    if (showClearPersonalization) {
        LiuMusicGlassDialog(visible = true, onDismiss = { showClearPersonalization = false }) {
            Text(stringResource(R.string.settings_privacy_clear_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.settings_privacy_clear_body), modifier = Modifier.padding(top = 10.dp), fontSize = 13.sp, lineHeight = 19.sp)
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsActionButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) { showClearPersonalization = false }
                SettingsDangerButton(stringResource(R.string.settings_privacy_clear_confirm), Modifier.weight(1f)) {
                    LocalRecommendationEngine.stop()
                    LocalRecommendationStore.clearPersonalization(context)
                    consent = false
                    showClearPersonalization = false
                }
            }
        }
    }
}

@Composable
private fun SystemPlaybackSettings(context: android.content.Context) {
    var systemLyrics by remember { mutableStateOf(LiuMusicSettingsRuntime.systemLyricsEnabled) }
    var notifications by remember { mutableStateOf(LiuMusicSettingsRuntime.lyricNotificationsEnabled) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notifications = granted
        LiuMusicSettingsPreferences.setBoolean(context, "lyrics_notifications_enabled", granted)
    }

    SettingsGlassGroup {
        SettingsExternalToggleRow(
            title = stringResource(R.string.settings_system_lyrics),
            value = systemLyrics,
            note = stringResource(R.string.settings_system_lyrics_note),
            grouped = true,
        ) {
            systemLyrics = it
            LiuMusicSettingsPreferences.setBoolean(context, "system_lyrics_enabled", it)
        }
        val protocol = remember { HyperOsFocusBridge.protocol(context) }
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_system_title_format),
            selected = LiuMusicSettingsRuntime.systemLyricTitleMode,
            items = listOf(
                LiuMusicSystemLyricTitleMode.LyricFirst to stringResource(R.string.settings_system_title_lyric),
                LiuMusicSystemLyricTitleMode.SongFirst to stringResource(R.string.settings_system_title_song),
            ),
            onSelected = { LiuMusicSettingsPreferences.setString(context, "system_lyrics_title_mode", it.name) },
            grouped = true,
        )
        SettingsExternalToggleRow(
            title = stringResource(R.string.settings_lyric_notification),
            value = notifications,
            note = stringResource(R.string.settings_lyric_notification_note),
            grouped = true,
        ) { enabled ->
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                notifications = enabled
                LiuMusicSettingsPreferences.setBoolean(context, "lyrics_notifications_enabled", enabled)
            }
        }
        val hyperIsland = remember { mutableStateOf(LiuMusicSettingsPreferences.boolean(context, "hyperos_super_island_enabled", false)) }
        SettingsExternalToggleRow(
            title = stringResource(R.string.settings_hyperos_island),
            value = hyperIsland.value,
            note = stringResource(R.string.settings_hyperos_island_note),
            grouped = true,
        ) { enabled ->
            hyperIsland.value = enabled
            LiuMusicSettingsPreferences.setBoolean(context, "hyperos_super_island_enabled", enabled)
        }
    }
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        SettingsToggleRow(context, stringResource(R.string.settings_notify_next_line), "lyrics_notification_next_line", false, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_notify_progress), "lyrics_notification_progress", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_notify_artwork), "lyrics_notification_artwork", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_notify_background_only), "lyrics_notification_background_only", false, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_notify_dismiss_paused), "lyrics_notification_dismiss_paused", true, grouped = true)
    }
    NotificationTemplateField(context, stringResource(R.string.settings_notify_title_template), "lyrics_notification_title_template", "{lyric}")
    NotificationTemplateField(context, stringResource(R.string.settings_notify_subtitle_template), "lyrics_notification_subtitle_template", "{song} · {artist}")
    NotificationTemplateField(context, stringResource(R.string.settings_notify_fallback), "lyrics_notification_fallback", "{song} · {artist}")
    Text(
        stringResource(R.string.settings_notify_variables),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .46f),
        fontSize = 11.sp,
        modifier = Modifier.padding(bottom = 10.dp),
    )
    SettingsActionButton(stringResource(R.string.settings_notify_test)) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel("liumusic_lyrics", context.getString(R.string.settings_notify_channel_lyrics), NotificationManager.IMPORTANCE_LOW))
        }
        manager.notify(
            10_043,
            NotificationCompat.Builder(context, "liumusic_lyrics")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("这是测试歌词")
                .setContentText("LiuMusic · 通知模板预览")
                .setStyle(NotificationCompat.BigTextStyle().bigText("这是测试歌词\n下一句歌词预览"))
                .setSilent(true)
                .build(),
        )
    }
}

@Composable
private fun ExperimentalSettings(context: android.content.Context, source: MusicSource, session: NeteaseSessionStore) {
    var playbackEnabled by remember { mutableStateOf(PlaybackAccountStore.isEnabled(context)) }
    var showQQLogin by remember { mutableStateOf(false) }
    var showKugouLogin by remember { mutableStateOf(false) }
    var showClearPlaybackConfirmation by remember { mutableStateOf(false) }
    var showClearLyricBindingsConfirmation by remember { mutableStateOf(false) }
    var showClearBilibiliAssociationsConfirmation by remember { mutableStateOf(false) }
    var bilibiliLyricAlignment by remember(source) {
        mutableStateOf(LiuMusicSettingsPreferences.boolean(context, "bilibili_lyric_audio_alignment", false))
    }
    var refreshRevision by remember { mutableIntStateOf(0) }
    var autoSyncEnabled by remember(source) {
        mutableStateOf(LiuMusicSettingsRuntime.autoSyncWordLyricsEnabled)
    }
    SettingsGlassGroup {
        SettingsExternalToggleRow(
            title = hotStringResource(R.string.settings_autosync_word),
            value = autoSyncEnabled,
            note = hotStringResource(R.string.settings_autosync_word_note),
            grouped = true,
        ) { enabled ->
            autoSyncEnabled = enabled
            LiuMusicSettingsPreferences.setBoolean(context, "autosync_word_lyrics_enabled", enabled)
        }
    }
    if (autoSyncEnabled) {
        SettingsGlassGroup { AutoSyncModelPanel(context) }
        Spacer(Modifier.height(10.dp))
    }

    val neteaseLoginLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) refreshRevision++
    }

    if (source == MusicSource.Bilibili) {
        SettingsGlassGroup {
            SettingsExternalToggleRow(
                title = stringResource(R.string.settings_bilibili_align),
                value = bilibiliLyricAlignment,
                grouped = true,
            ) { enabled ->
                bilibiliLyricAlignment = enabled
                LiuMusicSettingsPreferences.setBoolean(context, "bilibili_lyric_audio_alignment", enabled)
            }
        }
        SettingsInfoCard(stringResource(R.string.settings_bilibili_align_warning))
        Spacer(Modifier.height(10.dp))
        SettingsActionButton(stringResource(R.string.settings_bilibili_clear)) { showClearBilibiliAssociationsConfirmation = true }
        Spacer(Modifier.height(10.dp))
    }

    if (showClearBilibiliAssociationsConfirmation) {
        LiuMusicGlassDialog(visible = true, onDismiss = { showClearBilibiliAssociationsConfirmation = false }) {
            Text(stringResource(R.string.settings_bilibili_clear_title), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.settings_bilibili_clear_body), Modifier.padding(top = 8.dp))
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsActionButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) { showClearBilibiliAssociationsConfirmation = false }
                SettingsActionButton(stringResource(R.string.provider_clear), Modifier.weight(1f)) {
                    BilibiliPlaybackAssociationStore.clear(context)
                    showClearBilibiliAssociationsConfirmation = false
                }
            }
        }
    }

    if (showQQLogin) {
        Dialog(
            onDismissRequest = { showQQLogin = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            QQMusicLoginScreen(
                onDismiss = { showQQLogin = false },
                onLoggedIn = { showQQLogin = false; refreshRevision++ },
                targetSlot = PlaybackAccountSlot.Playback,
            )
        }
    }
    if (showKugouLogin) {
        Dialog(
            onDismissRequest = { showKugouLogin = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            KugouLoginScreen(
                onDismiss = { showKugouLogin = false },
                onLoggedIn = { showKugouLogin = false; refreshRevision++ },
                targetSlot = PlaybackAccountSlot.Playback,
            )
        }
    }

    SettingsGlassGroup {
        SettingsExternalToggleRow(
            title = stringResource(R.string.settings_playback_second_account),
            value = playbackEnabled,
            grouped = true,
        ) { enabled ->
            playbackEnabled = enabled
            PlaybackAccountStore.setEnabled(context, enabled)
            if (!enabled) ProviderPlaybackQualityRuntime.clear()
        }
    }
    if (playbackEnabled) {
        Spacer(Modifier.height(10.dp))
        SettingsGlassGroup {
            SettingsInfoCard(stringResource(R.string.settings_playback_second_account_note))
        }
        Spacer(Modifier.height(10.dp))

        val neteaseCookie = remember(refreshRevision) { NeteaseSessionStore.readPlaybackCookie(context) }
        val qqLoggedIn = remember(refreshRevision) { QQMusicSessionStore.read(context, playback = true).isLoggedIn }
        val kugouLoggedIn = remember(refreshRevision) { KugouSessionStore.read(context, playback = true).isLoggedIn }

        SettingsGlassGroup {
            LiuMusicIosListRow(
                title = stringResource(R.string.account_netease),
                subtitle = if (NeteaseSessionStore.containsMusicU(neteaseCookie)) stringResource(R.string.provider_logged_in) else stringResource(R.string.provider_not_signed_in),
                leading = { LiuMusicSymbolIcon(LiuMusicSymbol.MusicNote, Modifier.size(24.dp), MaterialTheme.colorScheme.primary) },
                onClick = { neteaseLoginLauncher.launch(LiuMusicNeteaseLoginActivity.createIntent(context, PlaybackAccountSlot.Playback)) },
                showTopSeparator = false,
            )
            LiuMusicIosListRow(
                title = stringResource(R.string.settings_qq_music),
                subtitle = if (qqLoggedIn) stringResource(R.string.provider_logged_in) else stringResource(R.string.provider_not_signed_in),
                leading = { LiuMusicSymbolIcon(LiuMusicSymbol.MusicNote, Modifier.size(24.dp), MaterialTheme.colorScheme.primary) },
                onClick = { showQQLogin = true },
            )
            LiuMusicIosListRow(
                title = stringResource(R.string.settings_kugou),
                subtitle = if (kugouLoggedIn) stringResource(R.string.provider_logged_in) else stringResource(R.string.provider_not_signed_in),
                leading = { LiuMusicSymbolIcon(LiuMusicSymbol.MusicNote, Modifier.size(24.dp), MaterialTheme.colorScheme.primary) },
                onClick = { showKugouLogin = true },
            )
            LiuMusicIosListRow(
                title = stringResource(R.string.settings_apple_music),
                subtitle = stringResource(R.string.settings_apple_second_account),
                leading = { LiuMusicSymbolIcon(LiuMusicSymbol.MusicNote, Modifier.size(24.dp), MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)) },
                onClick = null,
            )
        }
        Spacer(Modifier.height(10.dp))
        SettingsDangerButton(stringResource(R.string.settings_clear_second_accounts)) { showClearPlaybackConfirmation = true }
        LiuMusicGlassDialog(
            visible = showClearPlaybackConfirmation,
            onDismiss = { showClearPlaybackConfirmation = false },
        ) {
            Text(stringResource(R.string.settings_clear_second_accounts_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_clear_second_accounts_body),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SettingsActionButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) { showClearPlaybackConfirmation = false }
                SettingsActionButton(stringResource(R.string.provider_clear), Modifier.weight(1f)) {
                    PlaybackAccountStore.clear(context)
                    ProviderPlaybackQualityRuntime.clear()
                    refreshRevision++
                    showClearPlaybackConfirmation = false
                }
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    if (!LiuMusicSettingsRuntime.automaticLyricSelectionEnabled) {
        SettingsInfoCard(stringResource(R.string.settings_auto_lyrics_required))
        return
    }
    var enabled by remember {
        mutableStateOf(LiuMusicSettingsPreferences.boolean(context, "experimental_lyric_strong_binding", false))
    }
    SettingsGlassGroup {
        SettingsExternalToggleRow(
            title = stringResource(R.string.settings_lyric_binding),
            value = enabled,
            grouped = true,
        ) { value ->
            enabled = value
            LiuMusicSettingsPreferences.setBoolean(context, "experimental_lyric_strong_binding", value)
            if (!value) com.liuguanyi.liumusic.core.lyrics.LyricBindingStore.clear(context)
        }
    }
    Spacer(Modifier.height(10.dp))
    SettingsDangerButton(stringResource(R.string.settings_clear_lyric_bindings)) {
        showClearLyricBindingsConfirmation = true
    }
    LiuMusicGlassDialog(
        visible = showClearLyricBindingsConfirmation,
        onDismiss = { showClearLyricBindingsConfirmation = false },
    ) {
        Text(stringResource(R.string.settings_clear_lyric_bindings_title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.settings_clear_lyric_bindings_body),
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SettingsActionButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) {
                showClearLyricBindingsConfirmation = false
            }
            SettingsDangerButton(stringResource(R.string.provider_clear), Modifier.weight(1f)) {
                com.liuguanyi.liumusic.core.lyrics.LyricBindingStore.clear(context)
                showClearLyricBindingsConfirmation = false
            }
        }
    }
}

@Composable
private fun NotificationTemplateField(
    context: android.content.Context,
    title: String,
    key: String,
    default: String,
) {
    var value by remember(key) { mutableStateOf(LiuMusicSettingsPreferences.string(context, key, default)) }
    Text(title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .48f), modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))
    LiuMusicGlassTextField(
        value = value,
        onValueChange = {
            value = it.take(80)
            LiuMusicSettingsPreferences.setString(context, key, value)
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp),
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun FloatingLyricsSettings(context: android.content.Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        Text(
            text = stringResource(R.string.settings_floating_requires_android_8),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
        )
        return
    }
    var enabled by remember { mutableStateOf(LiuMusicSettingsRuntime.floatingLyricsEnabled) }
    var permissionGranted by remember { mutableStateOf(AndroidSettings.canDrawOverlays(context)) }

    fun startFloatingLyrics() {
        LiuMusicSettingsPreferences.setBoolean(context, "floating_lyrics_enabled", true)
        enabled = true
        ContextCompat.startForegroundService(context, Intent(context, LiuMusicFloatingLyricsService::class.java))
    }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        permissionGranted = AndroidSettings.canDrawOverlays(context)
        if (permissionGranted) startFloatingLyrics()
    }

    SettingsGlassGroup {
        SettingsExternalToggleRow(
            title = stringResource(R.string.settings_floating_show),
            value = enabled,
            note = stringResource(R.string.settings_floating_show_note),
            grouped = true,
        ) { shouldEnable ->
            if (!shouldEnable) {
                enabled = false
                LiuMusicSettingsPreferences.setBoolean(context, "floating_lyrics_enabled", false)
                context.stopService(Intent(context, LiuMusicFloatingLyricsService::class.java))
            } else if (AndroidSettings.canDrawOverlays(context)) {
                permissionGranted = true
                startFloatingLyrics()
            } else {
                overlayPermissionLauncher.launch(
                    Intent(
                        AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}"),
                    ),
                )
            }
        }
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_floating_secondary),
            selected = LiuMusicSettingsRuntime.floatingSecondaryMode,
            items = listOf(
                LiuMusicSecondaryLyricMode.Auto to stringResource(R.string.settings_floating_secondary_auto),
                LiuMusicSecondaryLyricMode.Translation to stringResource(R.string.settings_floating_translation),
                LiuMusicSecondaryLyricMode.Romanization to stringResource(R.string.settings_floating_romanization),
                LiuMusicSecondaryLyricMode.NextLine to stringResource(R.string.settings_floating_next_line),
                LiuMusicSecondaryLyricMode.Hidden to stringResource(R.string.settings_floating_hidden),
            ),
            onSelected = { LiuMusicSettingsPreferences.setString(context, "floating_lyrics_secondary_mode", it.name) },
            grouped = true,
        )
        LyricsChoiceSetting(
            context,
            stringResource(R.string.settings_floating_font_size),
            "floating_lyrics_font_size",
            18,
            listOf(14, 16, 18, 20, 24, 28),
            grouped = true,
        ) { "$it sp" }
        SettingsToggleRow(context, stringResource(R.string.settings_floating_contrast), "floating_lyrics_high_contrast", true, stringResource(R.string.settings_floating_contrast_note), grouped = true)
    }
}

@Composable
private fun PlaybackSettings(context: android.content.Context) {
    var quality by remember { mutableStateOf(MusicQualityPreferences.read(context)) }
    var volumeMode by remember { mutableStateOf(LiuMusicSettingsRuntime.volumeControlMode) }
    SettingsGlassGroup {
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_playback_quality),
            selected = quality,
            items = MusicQuality.entries.map { it to it.title },
            onSelected = { quality = it; PlaybackCommands.changeQuality(context, it) },
            grouped = true,
        )
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_volume_slider),
            selected = volumeMode,
            items = listOf(
                LiuMusicVolumeControlMode.System to stringResource(R.string.settings_volume_system),
                LiuMusicVolumeControlMode.Player to stringResource(R.string.settings_volume_player),
            ),
            onSelected = { volumeMode = it; LiuMusicSettingsPreferences.setString(context, "playback_volume_mode", it.name) },
            grouped = true,
        )
        SettingsToggleRow(
            context,
            stringResource(R.string.settings_allow_other_apps),
            "playback_allow_other_apps",
            false,
            stringResource(R.string.settings_allow_other_apps_note),
            grouped = true,
        )
        SettingsToggleRow(context, stringResource(R.string.settings_remember_player_page), "playback_remember_page", true, grouped = true)
        SettingsToggleRow(
            context,
            stringResource(R.string.settings_save_queue_on_exit),
            "playback_save_queue",
            true,
            "重新打开软件时恢复退出前的播放列表、当前歌曲和进度（暂停状态，不会自动播放）。",
            grouped = true,
        )
        SettingsToggleRow(context, stringResource(R.string.settings_heart_mode_on_launch), "playback_heart_mode_on_launch", false, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_previous_restarts), "playback_previous_restarts", true, grouped = true)
        SettingsToggleRow(
            context,
            stringResource(R.string.settings_show_quality_tip),
            "player_show_quality_tip",
            true,
            stringResource(R.string.settings_show_quality_tip_note),
            grouped = true,
        )
        LyricsChoiceSetting(
            context,
            stringResource(R.string.settings_player_transition_duration),
            // ⚠⚠ 2026-10-03：这一项不能直接用 `LiuMusicSettingsPreferences.int(context, key, default)`
            //   —— 那是裸 `getInt`，**不做旧默认迁移**，而运行时的迁移在
            //   `playerTransitionDurationMs()` 里 ⇒ 两条路径不同源。
            //   正好会复现用户报障的「**不要实际变，设置显示没变**」（显示 360 / 实际 240）。
            //   ⇒ 走专用读取函数：读 + 迁移 + 写回同一处，保证「显示 == 实际」。
            key = LiuMusicSettingsPreferences.KEY_PLAYER_TRANSITION_DURATION_MS,
            selectedProvider = { LiuMusicSettingsPreferences.playerTransitionDurationMs(context) },
            values = listOf(240, 300, 360, 460, 600),
            grouped = true,
        ) { "${it}ms" }
    }
    Spacer(Modifier.height(10.dp))
    EqualizerSettings(context)
    Spacer(Modifier.height(10.dp))
    AutoMixSettings(context)
    Spacer(Modifier.height(10.dp))
    AnalysisCacheSettings(context)
}

@Composable
private fun EqualizerSettings(context: android.content.Context) {
    var enabled by remember { mutableStateOf(LiuMusicSettingsPreferences.boolean(context, "equalizer_enabled", false)) }
    var preset by remember { mutableStateOf(LiuMusicSettingsPreferences.string(context, "equalizer_preset", "Flat")) }
    var preamp by remember { mutableStateOf(LiuMusicSettingsPreferences.number(context, "equalizer_preamp_db", 0f)) }
    SettingsExternalToggleRow(stringResource(R.string.settings_equalizer), enabled, stringResource(R.string.settings_equalizer_note)) {
        enabled = it
        LiuMusicSettingsPreferences.setBoolean(context, "equalizer_enabled", it)
    }
    if (!enabled) return
    Text(stringResource(R.string.settings_eq_presets), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .48f))
    Spacer(Modifier.height(8.dp))
    SettingsGlassGroup {
        LiuMusicEqualizerController.PRESETS.keys.forEach { value ->
            val label = mapOf(
                "Flat" to stringResource(R.string.settings_eq_flat), "Bass" to stringResource(R.string.settings_eq_bass), "Vocal" to stringResource(R.string.settings_eq_vocal), "Treble" to stringResource(R.string.settings_eq_treble),
                "Electronic" to "电子", "Rock" to "摇滚", "Classical" to "古典", "Custom" to stringResource(R.string.settings_eq_custom),
            ).getValue(value)
            SettingsChoiceRow(label, preset == value) {
                preset = value
                LiuMusicSettingsPreferences.setString(context, "equalizer_preset", value)
            }
        }
    }
    if (preset == "Custom") {
        Spacer(Modifier.height(12.dp))
        listOf("31 Hz", "62 Hz", "125 Hz", "250 Hz", "500 Hz", "1 kHz", "2 kHz", "4 kHz", "8 kHz", "16 kHz")
            .forEachIndexed { index, label ->
                var gain by remember(index) {
                    mutableStateOf(LiuMusicSettingsPreferences.number(context, "equalizer_custom_band_$index", 0f))
                }
                SettingsFloatSlider(label, gain, -12f..12f, 47, { value ->
                    val rounded = kotlin.math.round(value * 2f) / 2f
                    if (rounded > 0f) "+%.1f dB".format(rounded) else "%.1f dB".format(rounded)
                }) { value ->
                    gain = kotlin.math.round(value * 2f) / 2f
                    LiuMusicSettingsPreferences.setFloat(context, "equalizer_custom_band_$index", gain)
                }
        }
    }
    Spacer(Modifier.height(12.dp))
    Text(stringResource(R.string.settings_eq_preamp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .48f))
    Spacer(Modifier.height(8.dp))
    SettingsFloatSlider(stringResource(R.string.settings_eq_input_gain), preamp, -12f..12f, 47, { value ->
        val rounded = kotlin.math.round(value * 2f) / 2f
        if (rounded > 0f) "+%.1f dB".format(rounded) else "%.1f dB".format(rounded)
    }) { value ->
        preamp = kotlin.math.round(value * 2f) / 2f
        LiuMusicSettingsPreferences.setFloat(context, "equalizer_preamp_db", preamp)
    }
}

@Composable
private fun AutoMixSettings(context: android.content.Context) {
    var settings by remember { mutableStateOf(LiuMusicAutoMixSettings.read(context)) }
    fun refresh() { settings = LiuMusicAutoMixSettings.read(context) }
    var showSmartQueueIntro by remember { mutableStateOf(false) }

    val legacyAutoMix = remember { LiuMusicSettingsPreferences.boolean(context, "playback_auto_mix", false) }
    var autoMixEnabled by remember {
        mutableStateOf(LiuMusicPlaybackModePreferences.autoMix(context) || legacyAutoMix)
    }
    LaunchedEffect(legacyAutoMix) {
        if (legacyAutoMix) {
            LiuMusicPlaybackModePreferences.setAutoMix(context, true)
            LiuMusicSettingsPreferences.setBoolean(context, "playback_auto_mix", false)
        }
    }
    SettingsGlassGroup {
        SettingsExternalToggleRow(
            title = stringResource(R.string.settings_automix),
            value = autoMixEnabled,
            note = stringResource(R.string.settings_automix_note),
            grouped = true,
        ) {
            autoMixEnabled = it
            LiuMusicPlaybackModePreferences.setAutoMix(context, it)
        }
        SettingsExternalToggleRow(
            title = stringResource(R.string.settings_transition_animation),
            value = LiuMusicSettingsRuntime.transitionUiEnabled,
            grouped = true,
        ) { enabled ->
            LiuMusicSettingsPreferences.setBoolean(context, "transition_ui_enabled", enabled)
            LiuMusicSettingsRuntime.transitionUiEnabled = enabled
        }
    }
    if (autoMixEnabled) {
        Spacer(Modifier.height(10.dp))
        if (settings.mode == LiuMusicAutoMixMode.Smart) SettingsGlassGroup {
            SettingsExternalToggleRow(
                title = stringResource(R.string.settings_smart_queue),
                value = LiuMusicSettingsRuntime.smartQueueEnabled,
                grouped = true,
            ) { enabled ->
                if (enabled && !LiuMusicSettingsPreferences.boolean(context, "smart_queue_intro_shown", false)) {
                    showSmartQueueIntro = true
                } else {
                    LiuMusicPlaybackModePreferences.setSmartQueue(context, enabled)
                    LiuMusicSettingsRuntime.smartQueueEnabled = enabled
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        SettingsGlassGroup {
            LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_automix_mode),
            selected = settings.mode,
            items = listOf(LiuMusicAutoMixMode.Smart to stringResource(R.string.settings_automix_smart), LiuMusicAutoMixMode.Fixed to stringResource(R.string.settings_automix_fixed)),
            onSelected = { LiuMusicPlaybackModePreferences.setAutoMixString(context, "automix_mode", it.name); refresh() },
            grouped = true,
        )
        if (settings.mode == LiuMusicAutoMixMode.Smart) {
            SettingsExternalToggleRow(stringResource(R.string.settings_skip_quiet_opening), settings.skipQuietOpening, stringResource(R.string.settings_skip_quiet_opening_note), grouped = true) {
                LiuMusicPlaybackModePreferences.setAutoMixBoolean(context, "automix_skip_quiet_opening", it)
                refresh()
            }
            val limitOptions = listOf(
                -1 to "不限",
                20 to "前 20 首",
                40 to "前 40 首",
                60 to "前 60 首",
                100 to "前 100 首",
            )
            LiuMusicSettingsDropdown(
                title = "队列分析上限",
                selected = limitOptions.minByOrNull {
                    kotlin.math.abs(settings.smartQueueAnalysisLimit - it.first)
                }?.first ?: 60,
                items = limitOptions,
                onSelected = { LiuMusicPlaybackModePreferences.setAutoMixInt(context, "automix_smart_queue_analysis_limit", it); refresh() },
                grouped = true,
            )
            SettingsExternalToggleRow(stringResource(R.string.settings_analyze_streaming), settings.analyzeStreaming, stringResource(R.string.settings_analyze_streaming_note), grouped = true) {
                LiuMusicPlaybackModePreferences.setAutoMixBoolean(context, "automix_analyze_streaming", it)
                refresh()
            }
            LiuMusicSettingsDropdown(
                title = stringResource(R.string.settings_smart_transition_length),
                selected = settings.transitionBars,
                items = listOf(4, 8, 16).map { it to "$it 小节" },
                onSelected = { LiuMusicPlaybackModePreferences.setAutoMixInt(context, "automix_transition_bars", it); refresh() },
                grouped = true,
            )
            LiuMusicSettingsDropdown(
                title = stringResource(R.string.settings_tail_cut),
                selected = settings.tailCutBars,
                items = listOf(0 to stringResource(R.string.settings_tail_keep), 2 to "提前 2 小节", 4 to "提前 4 小节", 8 to "提前 8 小节"),
                onSelected = { LiuMusicPlaybackModePreferences.setAutoMixInt(context, "automix_tail_cut_bars", it); refresh() },
                grouped = true,
            )
            val confidenceOptions = listOf(.30f, .42f, .55f, .70f)
            LiuMusicSettingsDropdown(
                title = stringResource(R.string.settings_min_confidence),
                selected = confidenceOptions.minByOrNull { kotlin.math.abs(settings.minimumConfidence - it) } ?: .42f,
                items = confidenceOptions.map { it to "${(it * 100).toInt()}%" },
                onSelected = { LiuMusicPlaybackModePreferences.setAutoMixFloat(context, "automix_minimum_confidence", it); refresh() },
                grouped = true,
            )
        }
        val durationOptions = listOf(3_000L, 6_000L, 8_000L, 12_000L, 16_000L, 20_000L)
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_crossfade_duration),
            selected = durationOptions.minByOrNull { kotlin.math.abs(settings.fixedDurationMs - it) } ?: 6_000L,
            items = durationOptions.map { it to "${it / 1_000} 秒" },
            onSelected = { LiuMusicPlaybackModePreferences.setAutoMixLong(context, "automix_fixed_duration_ms", it); refresh() },
            grouped = true,
        )
        val preloadOptions = listOf(30_000L, 60_000L, 90_000L, 120_000L, 180_000L)
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_preload_lead),
            selected = preloadOptions.minByOrNull { kotlin.math.abs(settings.preloadLeadMs - it) } ?: 60_000L,
            items = preloadOptions.map { it to "${it / 1_000} 秒" },
            onSelected = { LiuMusicPlaybackModePreferences.setAutoMixLong(context, "automix_preload_lead_ms", it); refresh() },
            grouped = true,
        )
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_fade_curve),
            selected = settings.fadeCurve,
            items = listOf(
                LiuMusicAutoMixFadeCurve.EqualPower to stringResource(R.string.settings_fade_equal_power),
                LiuMusicAutoMixFadeCurve.Smooth to stringResource(R.string.settings_fade_smooth),
                LiuMusicAutoMixFadeCurve.Linear to stringResource(R.string.settings_fade_linear),
            ),
            onSelected = { LiuMusicPlaybackModePreferences.setAutoMixString(context, "automix_fade_curve", it.name); refresh() },
            grouped = true,
        )
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_analysis_fallback),
            selected = settings.fallback,
            items = listOf(
                LiuMusicAutoMixFallback.Crossfade to stringResource(R.string.settings_fallback_selected_duration),
                LiuMusicAutoMixFallback.ShortCrossfade to stringResource(R.string.settings_fallback_short_crossfade),
                LiuMusicAutoMixFallback.Normal to stringResource(R.string.settings_fallback_normal),
            ),
            onSelected = { LiuMusicPlaybackModePreferences.setAutoMixString(context, "automix_fallback", it.name); refresh() },
            grouped = true,
        )
        SettingsExternalToggleRow(stringResource(R.string.settings_tempo_matching), settings.tempoMatching, stringResource(R.string.settings_tempo_matching_note), grouped = true) {
            LiuMusicPlaybackModePreferences.setAutoMixBoolean(context, "automix_tempo_matching", it)
            refresh()
        }
        if (settings.tempoMatching) {
            val adjustmentOptions = listOf(.02f, .05f, .08f, .10f)
            LiuMusicSettingsDropdown(
                title = stringResource(R.string.settings_max_tempo_adjustment),
                selected = adjustmentOptions.minByOrNull { kotlin.math.abs(settings.maxTempoAdjustment - it) } ?: .05f,
                items = adjustmentOptions.map { it to "${(it * 100).toInt()}%" },
                onSelected = { LiuMusicPlaybackModePreferences.setAutoMixFloat(context, "automix_max_tempo_adjustment", it); refresh() },
                grouped = true,
            )
        }
    }
    }
    if (showSmartQueueIntro) {
        LiuMusicGlassDialog(visible = true, onDismiss = { showSmartQueueIntro = false }) {
            Text(stringResource(R.string.settings_smart_queue), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.settings_smart_queue_intro))
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.settings_smart_queue_rules), fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.settings_smart_queue_rule_closest))
            Text(stringResource(R.string.settings_smart_queue_rule_unique))
            Text(stringResource(R.string.settings_smart_queue_rule_background))
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsActionButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) { showSmartQueueIntro = false }
                SettingsActionButton(stringResource(R.string.settings_action_enable), Modifier.weight(1f)) {
                    LiuMusicPlaybackModePreferences.setSmartQueue(context, true)
                    LiuMusicSettingsRuntime.smartQueueEnabled = true
                    LiuMusicSettingsPreferences.setBoolean(context, "smart_queue_intro_shown", true)
                    showSmartQueueIntro = false
                }
            }
        }
    }
}

@Composable
private fun AnalysisCacheSettings(context: android.content.Context) {
    var showClearConfirmation by remember { mutableStateOf(false) }
    SettingsGlassGroup {
        SettingsActionButton(stringResource(R.string.settings_clear_analysis_cache)) { showClearConfirmation = true }
    }
    if (showClearConfirmation) {
        LiuMusicGlassDialog(visible = true, onDismiss = { showClearConfirmation = false }) {
            Text(stringResource(R.string.settings_clear_analysis_cache_confirm))
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsActionButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) { showClearConfirmation = false }
                SettingsActionButton(stringResource(R.string.settings_privacy_clear_confirm), Modifier.weight(1f)) {
                    LiuMusicAudioAnalysisPreferences.clearAll(context)
                    showClearConfirmation = false
                }
            }
        }
    }
}

@Composable
private fun PlayerAppearanceSettings(context: android.content.Context) {
    LyricsStringChoiceSetting(
        context,
        stringResource(R.string.settings_player_shell),
        "player_shell",
        LiuMusicSettingsRuntime.playerShell.name,
        com.liuguanyi.liumusic.ui.settings.LiuMusicPlayerShell.entries.map { it.name },
    ) {
        when (com.liuguanyi.liumusic.ui.settings.LiuMusicPlayerShell.valueOf(it)) {
            com.liuguanyi.liumusic.ui.settings.LiuMusicPlayerShell.AppleMusic -> "Apple Music"
            com.liuguanyi.liumusic.ui.settings.LiuMusicPlayerShell.Classic -> "经典播放器（手机 / 平板 / 横屏）"
        }
    }
    Spacer(Modifier.height(10.dp))
    SettingsToggleRow(
        context,
        stringResource(R.string.settings_frosted_glass),
        "player_frosted_glass",
        false,
        stringResource(R.string.settings_frosted_glass_note),
    )
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_player_background),
            selected = LiuMusicSettingsRuntime.playerBackgroundMode,
            items = listOf(
                LiuMusicPlayerBackgroundMode.FlowingLight to stringResource(R.string.settings_bg_flowing_light),
                LiuMusicPlayerBackgroundMode.AppleLyrics to stringResource(R.string.settings_bg_apple_lyrics),
                LiuMusicPlayerBackgroundMode.BlurredArtwork to stringResource(R.string.settings_bg_blurred_artwork),
            ),
            onSelected = {
                LiuMusicSettingsPreferences.setString(context, "player_background_mode", it.name)
                LiuMusicSettingsRuntime.flowingBackdropEnabled = it != LiuMusicPlayerBackgroundMode.BlurredArtwork
            },
            grouped = true,
        )
        // 流光背景专属调参：只在「取色流动光影」模式下出现（上游 0.6.2 d9b0714，
        // 标签按本地惯例写死中文，不走 stringResource）。
        if (LiuMusicSettingsRuntime.playerBackgroundMode == LiuMusicPlayerBackgroundMode.FlowingLight) {
            PreferenceFloatSlider(
                context,
                stringResource(R.string.settings_flowing_speed),
                "player_flowing_speed",
                1f,
                .25f..2f,
                6,
            ) { "%.2fx".format(it) }
            PreferenceFloatSlider(
                context,
                stringResource(R.string.settings_flowing_saturation),
                "player_flowing_saturation",
                1f,
                0f..2f,
                19,
            ) { "${(it * 100).toInt()}%" }
            PreferenceFloatSlider(
                context,
                stringResource(R.string.settings_flowing_brightness),
                "player_flowing_brightness",
                1f,
                .4f..1.6f,
                11,
            ) { "${(it * 100).toInt()}%" }
        }
        SettingsToggleRow(context, stringResource(R.string.settings_flowing_backdrop), "player_flowing_backdrop", true, stringResource(R.string.settings_flowing_backdrop_note), grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_background_isolation), "player_background_isolation", true, stringResource(R.string.settings_background_isolation_note), grouped = true)
        LyricsChoiceSetting(context, stringResource(R.string.settings_background_fps), "lyrics_background_frame_rate", 24, listOf(15, 24, 30, 45, 60), grouped = true) { value ->
            when (value) {
                15 -> "15 FPS · 省电"
                24 -> "24 FPS · 省电"
                30 -> "30 FPS · 均衡"
                else -> "$value FPS · 推荐"
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        SettingsToggleRow(context, stringResource(R.string.settings_reduce_motion), "reduce_motion", false, stringResource(R.string.settings_reduce_motion_note), grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_artwork_motion), "player_artwork_motion", true, grouped = true)
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_screen_awake_scope),
            selected = LiuMusicSettingsRuntime.screenAwakeMode,
            items = listOf(
                LiuMusicScreenAwakeMode.Disabled to stringResource(R.string.settings_skyline_off),
                LiuMusicScreenAwakeMode.Player to stringResource(R.string.settings_awake_player),
                LiuMusicScreenAwakeMode.Lyrics to stringResource(R.string.settings_awake_lyrics),
                LiuMusicScreenAwakeMode.HiddenLyricsInterface to stringResource(R.string.settings_awake_hidden_lyrics),
            ),
            onSelected = { LiuMusicSettingsPreferences.setString(context, "player_screen_awake_mode", it.name) },
            grouped = true,
        )
    }
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        SettingsToggleRow(
            context,
            stringResource(R.string.settings_immersive_playback),
            "immersive_playback",
            false,
            "播放器全屏时自动隐藏顶部系统状态栏",
            grouped = true,
        )
    }
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        SettingsToggleRow(
            context,
            "深色模式保持暗色底栏",
            "bottom_bar_lock_dark_in_dark_mode",
            true,
            "深色模式下底栏不再根据背后内容自适应翻成浅色；浅色模式不受影响。",
            grouped = true,
        )
    }
}

@Composable
private fun LyricsSettings(context: android.content.Context) {
    var lyricsStyle by remember { mutableStateOf(LiuMusicSettingsRuntime.lyricsStyle) }
    // Group 1: 歌词样式-歌词渲染质量-逐字歌词-普通LRC-点击跳转-长按分享-间奏倒计时-自动跟随-手动滚动恢复-减弱动画
    SettingsGlassGroup {
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_lyrics_style),
            selected = lyricsStyle,
            items = listOf(
                LiuMusicLyricsStyle.AppleMusic to stringResource(R.string.settings_apple_music),
                LiuMusicLyricsStyle.Eva to stringResource(R.string.settings_lyrics_style_eva),
                LiuMusicLyricsStyle.TextPV to stringResource(R.string.settings_lyrics_style_text_pv),
            ),
            onSelected = { lyricsStyle = it; LiuMusicSettingsPreferences.setString(context, "lyrics_style", it.name) },
            grouped = true,
        )
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_lyrics_rendering),
            selected = LiuMusicSettingsRuntime.lyricRenderingQuality,
            items = listOf(
                LiuMusicLyricsRenderingQuality.Low to stringResource(R.string.settings_lyrics_quality_low),
                LiuMusicLyricsRenderingQuality.Balanced to stringResource(R.string.settings_lyrics_quality_balanced),
                LiuMusicLyricsRenderingQuality.High to stringResource(R.string.settings_lyrics_quality_high),
            ),
            onSelected = { LiuMusicSettingsPreferences.setString(context, "lyrics_rendering_quality", it.name) },
            grouped = true,
        )
        var lyricLanguage by remember { mutableStateOf(LiuMusicSettingsRuntime.lyricLanguage) }
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_lyrics_language),
            selected = lyricLanguage,
            items = listOf(
                "system" to stringResource(R.string.settings_lyrics_language_system),
                "zh-Hans" to stringResource(R.string.settings_lyrics_language_zh_hans),
                "zh-Hant" to stringResource(R.string.settings_lyrics_language_zh_hant),
                "en" to stringResource(R.string.settings_lyrics_language_en),
                "tr" to stringResource(R.string.settings_lyrics_language_tr),
            ),
            onSelected = {
                lyricLanguage = it
                LiuMusicSettingsPreferences.setString(context, "lyric_language", it)
            },
            grouped = true,
        )
        SettingsToggleRow(context, stringResource(R.string.settings_lyrics_auto_select), "lyrics_auto_select", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_lyrics_word_by_word), "lyrics_word_by_word", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_lyrics_pseudo_timing), "lyrics_pseudo_timing", true, stringResource(R.string.settings_lyrics_pseudo_timing_note), grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_lyrics_tap_seek), "lyrics_tap_seek", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_lyrics_long_press_share), "lyrics_long_press_share", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_lyrics_interlude), "lyrics_interlude_countdown", true, stringResource(R.string.settings_lyrics_interlude_note), grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_lyrics_auto_follow), "lyrics_auto_follow", true, grouped = true)
        LyricsChoiceSetting(context, stringResource(R.string.settings_lyrics_follow_delay), "lyrics_follow_delay_ms", 3_000, listOf(1_500, 3_000, 5_000, 8_000), grouped = true) { "${it / 1_000f} 秒" }
        SettingsToggleRow(context, stringResource(R.string.settings_lyrics_reduce_motion), "lyrics_reduce_motion", false, stringResource(R.string.settings_lyrics_reduce_motion_note), grouped = true)
    }
    if (lyricsStyle == LiuMusicLyricsStyle.TextPV) {
        var pvStyle by remember { mutableStateOf(LiuMusicSettingsRuntime.textPVStyle) }
        var pvMotionIntensity by remember { mutableStateOf(LiuMusicSettingsRuntime.textPVMotionIntensity) }
        var pvAnimationSpeed by remember { mutableStateOf(LiuMusicSettingsRuntime.textPVAnimationSpeed) }
        Spacer(Modifier.height(10.dp))
        SettingsGlassGroup {
            Text(stringResource(R.string.settings_text_pv_style), modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .48f))
            listOf(
                LiuMusicTextPVStyle.BlueBold to stringResource(R.string.settings_pv_blue_bold),
                LiuMusicTextPVStyle.KineticSplit to stringResource(R.string.settings_pv_kinetic_split),
                LiuMusicTextPVStyle.BluePlane to stringResource(R.string.settings_pv_blue_plane),
                LiuMusicTextPVStyle.CyberGrunge to stringResource(R.string.settings_pv_cyber_grunge),
                LiuMusicTextPVStyle.Geometric to stringResource(R.string.settings_pv_geometric),
                LiuMusicTextPVStyle.RainCity to stringResource(R.string.settings_pv_rain_city),
                LiuMusicTextPVStyle.CyberpunkHUD to stringResource(R.string.settings_pv_cyberpunk_hud),
                LiuMusicTextPVStyle.EmotionCinema to stringResource(R.string.settings_pv_emotion_cinema),
                LiuMusicTextPVStyle.HystericNight to stringResource(R.string.settings_pv_hysteric_night),
                LiuMusicTextPVStyle.SpiderWeb to stringResource(R.string.settings_pv_spider_web),
                LiuMusicTextPVStyle.StaggeredText to stringResource(R.string.settings_pv_staggered),
                LiuMusicTextPVStyle.CalmVillain to stringResource(R.string.settings_pv_calm_villain),
                LiuMusicTextPVStyle.GirlyClouds to stringResource(R.string.settings_pv_girly_clouds),
                LiuMusicTextPVStyle.SweetPink to stringResource(R.string.settings_pv_sweet_pink),
                LiuMusicTextPVStyle.FlyMeToTheMoon to "Fly Me to the Moon",
                LiuMusicTextPVStyle.KawaiiPixel to stringResource(R.string.settings_pv_kawaii_pixel),
                LiuMusicTextPVStyle.CrimeScene to stringResource(R.string.settings_pv_crime_scene),
                LiuMusicTextPVStyle.Haruhikage to stringResource(R.string.settings_pv_haruhikage),
            ).forEach { (style, title) ->
                SettingsChoiceRow(title, pvStyle == style) {
                    LiuMusicSettingsPreferences.setString(context, "lyrics_text_pv_style", style.name)
                    pvStyle = style
                    pvAnimationSpeed = style.referenceAnimationSpeed
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        SettingsFloatSlider(stringResource(R.string.settings_pv_motion_intensity), pvMotionIntensity, 0f..2f, 19) {
            pvMotionIntensity = it; LiuMusicSettingsPreferences.setFloat(context, "lyrics_text_pv_motion_intensity", it)
        }
        SettingsFloatSlider(stringResource(R.string.settings_pv_animation_speed), pvAnimationSpeed, 0f..4f, 39) {
            pvAnimationSpeed = it; LiuMusicSettingsPreferences.setFloat(context, "lyrics_text_pv_animation_speed", it)
        }
        Spacer(Modifier.height(10.dp))
        SettingsActionButton(stringResource(R.string.settings_pv_reset)) {
            LiuMusicSettingsPreferences.setString(context, "lyrics_text_pv_style", LiuMusicTextPVStyle.BlueBold.name)
            LiuMusicSettingsPreferences.setFloat(context, "lyrics_text_pv_motion_intensity", 1f)
            LiuMusicSettingsPreferences.setFloat(context, "lyrics_text_pv_animation_speed", 2f)
            pvStyle = LiuMusicTextPVStyle.BlueBold; pvMotionIntensity = 1f; pvAnimationSpeed = 2f
        }
    }

    // Group 2: 显示翻译-翻译歌词大小-翻译歌词亮度
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        SettingsToggleRow(context, stringResource(R.string.settings_show_translation), "lyrics_translation", true, grouped = true)
        LyricsFloatChoiceSetting(context, stringResource(R.string.settings_translation_size), "lyrics_translation_font_scale", .65f, listOf(.5f, .55f, .6f, .65f, .7f, .75f, .8f), grouped = true) { "${(it * 100).toInt()}%" }
        LyricsFloatChoiceSetting(context, stringResource(R.string.settings_translation_brightness), "lyrics_translation_opacity", .9f, listOf(.4f, .5f, .6f, .7f, .8f, .9f), grouped = true) { "${(it * 100).toInt()}%" }
        LyricsStringChoiceSetting(
            context, stringResource(R.string.settings_translation_scope), "lyrics_translation_display_mode",
            LiuMusicLyricAnnotationDisplayMode.AllLines.name, LiuMusicLyricAnnotationDisplayMode.entries.map { it.name }, grouped = true,
        ) { if (it == LiuMusicLyricAnnotationDisplayMode.FocusedLine.name) "仅当前播放行" else "全部歌词行" }
        SettingsToggleRow(context, stringResource(R.string.settings_show_romanization), "lyrics_romanization", false, grouped = true)
        LyricsStringChoiceSetting(
            context, stringResource(R.string.settings_romanization_scope), "lyrics_romanization_display_mode",
            LiuMusicLyricAnnotationDisplayMode.FocusedLine.name, LiuMusicLyricAnnotationDisplayMode.entries.map { it.name }, grouped = true,
        ) { if (it == LiuMusicLyricAnnotationDisplayMode.FocusedLine.name) "仅当前播放行" else "全部歌词行" }
        LyricsFloatChoiceSetting(context, stringResource(R.string.settings_romanization_size), "lyrics_romanization_font_scale", .65f, listOf(.5f, .55f, .6f, .65f, .7f, .75f, .8f), grouped = true) { "${(it * 100).toInt()}%" }
        LyricsFloatChoiceSetting(context, stringResource(R.string.settings_romanization_brightness), "lyrics_romanization_opacity", .9f, listOf(.4f, .5f, .6f, .7f, .8f, .9f), grouped = true) { "${(it * 100).toInt()}%" }
    }

    // Group 3: 歌词提前量-提前量同时应用于逐字高亮-歌词刷新率-歌词字号-歌词字重-抬升方式
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        LyricsChoiceSetting(context, stringResource(R.string.settings_lyrics_advance), "lyrics_advance_ms", 0, listOf(-1_000, -500, -200, 0, 200, 500, 1_000, 2_000, 5_000), grouped = true) { value ->
            if (value == 0) "同步" else if (value > 0) "提前 ${value}ms" else "延后 ${-value}ms"
        }
        SettingsToggleRow(context, stringResource(R.string.settings_lyrics_advance_words), "lyrics_advance_word_by_word", false, grouped = true)
        LyricsChoiceSetting(context, stringResource(R.string.settings_lyrics_refresh_rate), "lyrics_refresh_rate", 60, listOf(30, 60, 90, 120), grouped = true) { "$it FPS" }
        LyricsFloatChoiceSetting(context, stringResource(R.string.settings_lyrics_font_size), "lyrics_font_scale", 1f, listOf(.85f, 1f, 1.12f, 1.25f), grouped = true) { "${(it * 100).toInt()}%" }
        LyricsStringChoiceSetting(
            context, stringResource(R.string.settings_lyrics_font_weight), "lyrics_font_weight", LiuMusicLyricsFontWeight.Heavy.name,
            LiuMusicLyricsFontWeight.entries.map { it.name }, grouped = true,
        ) { value ->
            when (LiuMusicLyricsFontWeight.valueOf(value)) {
                LiuMusicLyricsFontWeight.Light -> "细体"
                LiuMusicLyricsFontWeight.Regular -> "常规"
                LiuMusicLyricsFontWeight.Medium -> "中等"
                LiuMusicLyricsFontWeight.SemiBold -> "半粗体"
                LiuMusicLyricsFontWeight.Bold -> "粗体"
                LiuMusicLyricsFontWeight.Heavy -> "特粗体"
            }
        }
        LyricsStringChoiceSetting(
            context, stringResource(R.string.settings_lyrics_lift), "lyrics_lift_mode", LiuMusicLyricsGroupingMode.Character.name,
            LiuMusicLyricsGroupingMode.entries.map { it.name }, grouped = true,
        ) { if (it == LiuMusicLyricsGroupingMode.Word.name) "按词抬升" else "按字抬升" }
    }

    // Group 4: 长音识别方式-逐字歌词光效-仅长音显示光晕-逐字光晕-长音延展-长音判定时长-行间距-远近模糊
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        LyricsStringChoiceSetting(
            context, stringResource(R.string.settings_long_tone_detection), "lyrics_long_tone_detection", LiuMusicLyricsGroupingMode.Character.name,
            LiuMusicLyricsGroupingMode.entries.map { it.name }, grouped = true,
        ) { if (it == LiuMusicLyricsGroupingMode.Word.name) "按词识别" else "按字识别" }
        SettingsToggleRow(context, stringResource(R.string.settings_lyrics_glow), "lyrics_glow_enabled", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_glow_long_tones_only), "lyrics_glow_long_tones_only", true, grouped = true)
        LyricsFloatChoiceSetting(context, "逐字光晕", "lyrics_glow_strength", 1f, listOf(0f, .6f, 1f, 1.4f), grouped = true) { if (it == 0f) "关闭" else "${(it * 100).toInt()}%" }
        LyricsFloatChoiceSetting(context, "长音延展", "lyrics_long_tone_strength", 1f, listOf(0f, .6f, 1f, 1.4f), grouped = true) { if (it == 0f) "关闭" else "${(it * 100).toInt()}%" }
        LyricsChoiceSetting(context, stringResource(R.string.settings_long_tone_threshold), "lyrics_long_tone_threshold_ms", 950, listOf(300, 500, 700, 950, 1_200, 1_500), grouped = true) { "${it / 1000f} 秒" }
        LyricsFloatChoiceSetting(context, stringResource(R.string.settings_line_spacing), "lyrics_spacing_scale", 1f, listOf(.8f, 1f, 1.2f, 1.4f), grouped = true) { "${(it * 100).toInt()}%" }
        LyricsFloatChoiceSetting(context, "远近模糊", "lyrics_blur_strength", 1f, listOf(0f, .5f, .8f, 1f), grouped = true) { if (it == 0f) "关闭" else "${(it * 100).toInt()}%" }
    }

    // Group 5: 当前行放大-未播放文字亮度-控制栏自动隐藏-滚动隐藏UI阈值
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        LyricsFloatChoiceSetting(context, stringResource(R.string.settings_focus_scale), "lyrics_focus_scale", 1.02f, listOf(1f, 1.02f, 1.04f, 1.08f), grouped = true) { "${(it * 100).toInt()}%" }
        LyricsFloatChoiceSetting(context, stringResource(R.string.settings_inactive_opacity), "lyrics_inactive_opacity", .42f, listOf(.3f, .42f, .5f, .6f), grouped = true) { "${(it * 100).toInt()}%" }
        LyricsChoiceSetting(context, stringResource(R.string.settings_controls_auto_hide), "lyrics_interface_auto_hide_ms", 5_000, (3..15).map { it * 1_000 }, grouped = true) { "${it / 1_000} 秒" }
        LyricsChoiceSetting(context, stringResource(R.string.settings_scroll_hide_threshold), "lyrics_scroll_hide_threshold_dp", 200, listOf(40, 80, 120, 160, 200, 240), grouped = true) { "$it dp" }
    }

    // Group 6: 启用位移回弹-启用升格回弹-升格回弹时长-焦点回弹时长
    Spacer(Modifier.height(10.dp))
    SettingsGlassGroup {
        SettingsToggleRow(context, stringResource(R.string.settings_cascade_bounce_enabled), "lyrics_cascade_bounce_enabled", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_scale_bounce_enabled), "lyrics_scale_bounce_enabled", true, grouped = true)
        LyricsChoiceSetting(context, stringResource(R.string.settings_scale_bounce_duration), "lyrics_scale_bounce_duration_ms", 580, listOf(150, 250, 350, 450, 580, 700, 800), grouped = true) { "${it}ms" }
        LyricsChoiceSetting(context, "焦点回弹时长", "lyrics_focus_color_lead_ms", 0, listOf(-300, -200, -100, -50, 0, 50, 100, 200, 300), grouped = true) { if (it == 0) "同步" else "${it}ms" }
    }

    // Group 7: 最大回弹弹性-回弹强度梯度-升格回弹弹性
    Spacer(Modifier.height(10.dp))
    PreferenceFloatSlider(context, stringResource(R.string.settings_max_cascade_bounce), "lyrics_cascade_bounce", .26f, 0f..8f / 10f, 79) { "${(it * 100).toInt()}%" }
    PreferenceFloatSlider(context, stringResource(R.string.settings_bounce_gradient), "lyrics_cascade_bounce_gradient", .85f, 0f..1f, 99) { "${(it * 100).toInt()}%" }
    PreferenceFloatSlider(context, stringResource(R.string.settings_scale_bounce_amount), "lyrics_scale_bounce", .32f, 0f..5f / 10f, 49) { "${(it * 100).toInt()}%" }

    // Group 8: 高光渐变宽度-渐变削减程度
    Spacer(Modifier.height(10.dp))
    PreferenceFloatSlider(context, stringResource(R.string.settings_highlight_width), "lyrics_highlight_gradient_width", .7f, .4f..3f, 25) { "%.1f 字宽".format(it) }
    PreferenceFloatSlider(context, stringResource(R.string.settings_gradient_reduction), "lyrics_highlight_gradient_reduction", .65f, 0f..1f, 19) { "${(it * 100).toInt()}%" }

    // Group 9: 焦点垂直位置-默认逐句模糊加强-隐藏UI逐句模糊加强-非焦点歌词变暗
    Spacer(Modifier.height(10.dp))
    PreferenceFloatSlider(context, stringResource(R.string.settings_focus_position), "lyrics_focus_position", .25f, .05f..8f / 10f, 74) { "距顶部 ${(it * 100).toInt()}%" }
    PreferenceFloatSlider(context, stringResource(R.string.settings_distance_blur), "lyrics_distance_blur_scale", 1.05f, 0f..1.5f, 29) { "${(it * 100).toInt()}%" }
    PreferenceFloatSlider(context, stringResource(R.string.settings_hidden_blur), "lyrics_hidden_blur_scale", .85f, 0f..1.5f, 29) { "${(it * 100).toInt()}%" }
    PreferenceFloatSlider(context, stringResource(R.string.settings_dim_amount), "lyrics_dim_amount", 1f, 0f..1f, 49) { "${(it * 100).toInt()}%" }

    // Group 10: 基础拖尾延迟-逐句拖尾增量-后续歌词启动延迟-拖尾追赶节奏-追赶速度梯度-位移收束时长
    Spacer(Modifier.height(10.dp))
    PreferenceFloatSlider(context, stringResource(R.string.settings_cascade_delay), "lyrics_cascade_delay_ms", 21f, 0f..100f, 99) { "${it.toInt()} ms" }
    PreferenceFloatSlider(context, stringResource(R.string.settings_cascade_delay_increase), "lyrics_cascade_delay_increase_ms", 5f, 0f..100f, 99) { "${it.toInt()} ms/句" }
    PreferenceFloatSlider(context, stringResource(R.string.settings_following_delay), "lyrics_cascade_following_delay_ms", 30f, 0f..200f, 199) { "${it.toInt()} ms" }
    PreferenceFloatSlider(context, stringResource(R.string.settings_catch_up_ratio), "lyrics_cascade_catch_up_ratio", .97f, .5f..1f, 49) { "${(it * 100).toInt()}%" }
    PreferenceFloatSlider(context, stringResource(R.string.settings_chase_gradient), "lyrics_cascade_chase_gradient", .70f, 0f..1f, 99) { "${(it * 100).toInt()}%" }
    PreferenceFloatSlider(context, stringResource(R.string.settings_cascade_duration), "lyrics_cascade_duration_ms", 740f, 200f..1_200f, 99) { "%.2f 秒".format(it / 1_000f) }

    // Group 11: 瞬移阈值
    Spacer(Modifier.height(10.dp))
    PreferenceFloatSlider(context, stringResource(R.string.settings_snap_threshold), "lyrics_snap_threshold_ms", 260f, 50f..500f, 89) { "${it.toInt()} ms" }
}

@Composable
private fun LyricsStringChoiceSetting(
    context: android.content.Context,
    title: String,
    key: String,
    default: String,
    values: List<String>,
    grouped: Boolean = false,
    label: (String) -> String,
) {
    val groupRowIndex = LocalSettingsGroupRowIndex.current
    val showSep = if (grouped) groupRowIndex.intValue++.let { it > 0 } else false
    var selected by remember(key) { mutableStateOf(LiuMusicSettingsPreferences.string(context, key, default)) }
    LiuMusicSettingsDropdown(
        title = title,
        selected = selected,
        items = values.map { it to label(it) },
        onSelected = {
            selected = it
            LiuMusicSettingsPreferences.setString(context, key, it)
        },
        grouped = grouped,
        showTopSeparator = showSep,
    )
    if (!grouped) Spacer(Modifier.height(10.dp))
}

@Composable
private fun LyricsChoiceSetting(
    context: android.content.Context,
    title: String,
    key: String,
    default: Int,
    values: List<Int>,
    grouped: Boolean = false,
    label: (Int) -> String,
) = LyricsChoiceSetting(
    context = context,
    title = title,
    key = key,
    selectedProvider = { LiuMusicSettingsPreferences.int(context, key, default) },
    values = values,
    grouped = grouped,
    label = label,
)

/**
 * `selectedProvider` 重载（2026-10-03）。
 *
 * ⚠ 为什么要它：原先本函数内部固定用 `LiuMusicSettingsPreferences.int(context, key, default)`
 *   读取选中项，而 `int()` 是**裸 `getInt`、不做任何迁移**。
 *   当某项setting 的默认值需要跨版本迁移时（`player_transition_duration_ms`
 *   默认经历过 `360 → 240`（v1，已废弃）与 `240 → 360`（v2，当前）两轮），
 *   运行时的迁移在别处、设置页这条路径不迁移 ⇒
 *   **「实际时长已变、设置显示没变」**（用户报障原话）。
 *   ⇒ 让调用方注入 `selectedProvider`，把「读到的值」与「显示的值」**锁成同一个来源**。
 */
@Composable
private fun LyricsChoiceSetting(
    context: android.content.Context,
    title: String,
    key: String,
    selectedProvider: () -> Int,
    values: List<Int>,
    grouped: Boolean = false,
    label: (Int) -> String,
) {
    val groupRowIndex = LocalSettingsGroupRowIndex.current
    val effectiveShowTopSeparator = if (grouped) groupRowIndex.intValue++.let { it > 0 } else false
    // ⚠ 不用 remember 缓存：`selectedProvider` 可能每次读取都做一次迁移+写回
    //   （那正是我们要的：进设置页即完成迁移，显示立刻是新值）。
    var selected by remember(key) { mutableStateOf(selectedProvider()) }
    LiuMusicSettingsDropdown(
        title = title,
        selected = selected,
        items = values.map { it to label(it) },
        onSelected = {
            selected = it
            LiuMusicSettingsPreferences.setInt(context, key, it)
        },
        grouped = grouped,
        showTopSeparator = effectiveShowTopSeparator,
    )
    if (!grouped) Spacer(Modifier.height(10.dp))
}

@Composable
private fun LyricsFloatChoiceSetting(
    context: android.content.Context,
    title: String,
    key: String,
    default: Float,
    values: List<Float>,
    grouped: Boolean = false,
    label: (Float) -> String,
) {
    val groupRowIndex = LocalSettingsGroupRowIndex.current
    val effectiveShowTopSeparator = if (grouped) groupRowIndex.intValue++.let { it > 0 } else false
    var selected by remember(key) { mutableStateOf(LiuMusicSettingsPreferences.float(context, key, default)) }
    val selectedValue = values.minByOrNull { kotlin.math.abs(selected - it) } ?: default
    LiuMusicSettingsDropdown(
        title = title,
        selected = selectedValue,
        items = values.map { it to label(it) },
        onSelected = {
            selected = it
            LiuMusicSettingsPreferences.setFloat(context, key, it)
        },
        grouped = grouped,
        showTopSeparator = effectiveShowTopSeparator,
    )
    if (!grouped) Spacer(Modifier.height(10.dp))
}

@Composable
private fun SettingsFloatSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    label: (Float) -> String = { "${(it * 100).toInt()}%" },
    onValueChange: (Float) -> Unit,
) {
    // Show the value the finger is currently on while dragging; the setting is
    // still only committed once the drag ends.
    var draggingValue by remember { mutableStateOf<Float?>(null) }
    val shownValue = draggingValue ?: value
    Row(
        // Match LiuMusicIosListRow's 16dp gutter so the label lines up with the
        // neighbouring setting titles.
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .48f))
        Text(label(shownValue), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f))
    }
    LiuMusicLiquidSlider(
        value = value,
        onValueChange = onValueChange,
        onTransientValueChange = { draggingValue = it },
        onValueChangeFinished = { draggingValue = null },
        valueRange = range,
        stepSize = if (steps > 0) (range.endInclusive - range.start) / (steps + 1) else 0f,
        visibilityThreshold = 0.001f,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
    )
}

@Composable
private fun PreferenceFloatSlider(
    context: android.content.Context,
    title: String,
    key: String,
    default: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    label: (Float) -> String,
) {
    var value by remember(key) { mutableStateOf(LiuMusicSettingsPreferences.float(context, key, default)) }
    SettingsFloatSlider(title, value, range, steps, label) {
        value = it
        LiuMusicSettingsPreferences.setFloat(context, key, it)
    }
}

@Composable
private fun ContentFeatureSettings(context: android.content.Context) {
    SettingsGlassGroup {
        SettingsToggleRow(context, "播客", "feature_podcasts", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_feature_history), "feature_history", true, grouped = true)
        SettingsToggleRow(context, "下载", "feature_downloads", true, stringResource(R.string.settings_feature_downloads_note), grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_feature_cloud), "feature_cloud_music", true, stringResource(R.string.settings_feature_cloud_note), grouped = true)
    }
}

@Composable
private fun MessagesSettings(context: android.content.Context) {
    val ops = remember(context) {
        NeteaseMusicOperationsClient(cookieProvider = { NeteaseSessionStore.readCookie(context) })
    }
    val account = remember(context) {
        NeteaseSearchClient(cookieProvider = { NeteaseSessionStore.readCookie(context) })
    }
    val scope = rememberCoroutineScope()
    var contacts by remember { mutableStateOf<List<LiuMusicMessageContact>>(emptyList()) }
    var selected by remember { mutableStateOf<LiuMusicMessageContact?>(null) }
    var messages by remember { mutableStateOf<List<LiuMusicPrivateMessage>>(emptyList()) }
    var currentUserId by remember { mutableStateOf(0L) }
    var draft by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableStateOf(0) }

    LaunchedEffect(selected?.id, reload) {
        busy = true
        error = null
        runCatching {
            val profile = account.accountProfile()
            currentUserId = profile.userId
            val contact = selected
            if (contact == null) {
                val recent = ops.privateMessageConversations(profile.userId)
                val follows = ops.messageContacts(profile.userId)
                contacts = (recent + follows).filter { it.id != profile.userId }.distinctBy(LiuMusicMessageContact::id)
            } else {
                messages = ops.privateMessageHistory(contact.id)
            }
        }.onFailure { error = it.message ?: "私信读取失败" }
        busy = false
    }

    if (selected == null) {
        Text(stringResource(R.string.settings_messages_contacts), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .48f))
        Spacer(Modifier.height(8.dp))
        if (busy) Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.size(10.dp))
            Text(stringResource(R.string.settings_messages_loading), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f))
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
        LazyColumn(Modifier.fillMaxWidth().height(440.dp)) {
            items(contacts.take(100), key = LiuMusicMessageContact::id) { contact ->
                Row(
                    Modifier.fillMaxWidth().clickable { selected = contact }.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(contact.avatarUrl, null, Modifier.size(42.dp).clip(CircleShape))
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(contact.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        if (contact.signature.isNotBlank()) Text(contact.signature, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .45f))
                    }
                    LiuMusicActionIcon("›", Modifier.size(18.dp), MaterialTheme.colorScheme.onSurface.copy(alpha = .3f))
                }
            }
        }
        return
    }

    val contact = selected!!
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        SettingsRoundButton("‹") { selected = null; messages = emptyList(); draft = "" }
        Spacer(Modifier.size(12.dp))
        AsyncImage(contact.avatarUrl, null, Modifier.size(38.dp).clip(CircleShape))
        Spacer(Modifier.size(10.dp))
        Text(contact.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text(stringResource(R.string.settings_messages_refresh), Modifier.clickable { reload++ }.padding(8.dp), color = MaterialTheme.colorScheme.primary)
    }
    Spacer(Modifier.height(14.dp))
    if (busy && messages.isEmpty()) CircularProgressIndicator(Modifier.size(24.dp))
    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
    LazyColumn(Modifier.fillMaxWidth().height(420.dp)) {
        items(messages.takeLast(60), key = LiuMusicPrivateMessage::id) { message ->
            val outgoing = message.fromUserId == currentUserId
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                if (outgoing) Spacer(Modifier.weight(.2f))
                Text(
                    message.text,
                    modifier = Modifier.background(
                        if (outgoing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = .08f),
                        RoundedCornerShape(16.dp),
                    ).padding(horizontal = 12.dp, vertical = 9.dp).weight(.8f, fill = false),
                    color = if (outgoing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                )
                if (!outgoing) Spacer(Modifier.weight(.2f))
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.onBackground.copy(alpha = .06f), RoundedCornerShape(22.dp)).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = draft,
            onValueChange = { draft = it },
            textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            decorationBox = { inner ->
                Box {
                    if (draft.isBlank()) Text(stringResource(R.string.settings_messages_compose), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .38f))
                    inner()
                }
            },
        )
        Text(
            if (busy) "…" else stringResource(R.string.settings_messages_send),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(enabled = !busy && draft.isNotBlank()) {
                val text = draft.trim()
                busy = true
                scope.launch {
                    runCatching { ops.sendPrivateText(text, contact.id) }
                        .onSuccess { draft = ""; messages = ops.privateMessageHistory(contact.id) }
                        .onFailure { error = it.message ?: "发送失败" }
                    busy = false
                }
            }.padding(horizontal = 10.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun ContentSettings(context: android.content.Context) {
    var area by remember { mutableStateOf(LiuMusicSettingsRuntime.musicArea) }
    var crossProviderFallback by remember {
        mutableStateOf(CrossProviderPlaybackPreferences.enabled(context))
    }
    var showCrossProviderFallbackNotice by remember { mutableStateOf(false) }
    SettingsGlassGroup {
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_content_area),
            selected = area,
            items = listOf("全部" to "全部", "华语" to "华语", "欧美" to "欧美", "日本" to "日本", "韩国" to "韩国"),
            onSelected = { area = it; LiuMusicSettingsPreferences.setString(context, "music_area", it) },
            grouped = true,
        )
        SettingsToggleRow(context, stringResource(R.string.settings_content_hq_playlists), "content_high_quality_playlist", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_content_play_count), "content_playlist_play_count", true, grouped = true)
        SettingsExternalToggleRow(
            title = stringResource(R.string.settings_content_cross_provider),
            value = crossProviderFallback,
            grouped = true,
        ) { enabled ->
            if (enabled) {
                showCrossProviderFallbackNotice = true
            } else {
                crossProviderFallback = false
                CrossProviderPlaybackPreferences.setEnabled(context, false)
            }
        }
    }
    if (crossProviderFallback) {
        Spacer(Modifier.height(10.dp))
        SettingsInfoCard(
            stringResource(R.string.settings_content_cross_provider_active),
        )
    }
    if (showCrossProviderFallbackNotice) {
        LiuMusicGlassDialog(
            visible = true,
            onDismiss = { showCrossProviderFallbackNotice = false },
        ) {
            Text(stringResource(R.string.settings_content_cross_provider_title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.settings_content_cross_provider_body),
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .72f),
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
            Text(
                text = stringResource(R.string.settings_content_cross_provider_match),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .72f),
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
            Text(
                text = stringResource(R.string.settings_content_cross_provider_terms),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .56f),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
            LiuMusicLegalLinks(modifier = Modifier.padding(top = 6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SettingsActionButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) {
                    showCrossProviderFallbackNotice = false
                }
                SettingsActionButton(stringResource(R.string.settings_content_cross_provider_enable), Modifier.weight(1f)) {
                    crossProviderFallback = true
                    CrossProviderPlaybackPreferences.setEnabled(context, true)
                    showCrossProviderFallbackNotice = false
                }
            }
        }
    }
}

@Composable
private fun ListenTogetherSettings(context: android.content.Context) {
    val app = context.applicationContext
    val state by LiuMusicListenTogetherCoordinator.state(app).collectAsState()
    val room = state.room
    val scope = rememberCoroutineScope()
    val ops = remember(app) {
        NeteaseMusicOperationsClient(cookieProvider = { NeteaseSessionStore.readCookie(app) })
    }
    var invitation by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    SettingsInfoCard(stringResource(R.string.settings_listen_info))
    Spacer(Modifier.height(12.dp))
    if (room == null) {
        SettingsActionButton(stringResource(R.string.settings_listen_start)) {
            if (!busy) {
                busy = true
                message = null
                scope.launch {
                    runCatching { ops.createListenTogetherRoom() }
                        .onSuccess { LiuMusicListenTogetherCoordinator.adoptRoom(app, it) }
                        .onFailure { message = it.message ?: "创建房间失败" }
                    busy = false
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        LiuMusicGlassTextField(
            value = invitation,
            onValueChange = { invitation = it },
            placeholder = { Text(stringResource(R.string.settings_listen_paste), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .4f)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        SettingsActionButton(stringResource(R.string.settings_listen_join)) {
            val parsed = parseNeteaseListenTogetherInvitation(invitation)
            if (parsed == null) {
                message = "邀请链接缺少房间或邀请人信息"
            } else if (!busy) {
                busy = true
                message = null
                scope.launch {
                    runCatching { ops.joinListenTogetherRoom(parsed.roomId, parsed.inviterId) }
                        .onSuccess { LiuMusicListenTogetherCoordinator.adoptRoom(app, it) }
                        .onFailure { message = it.message ?: "加入房间失败" }
                    busy = false
                }
            }
        }
    } else {
        SettingsGlassGroup {
            LiuMusicIosListRow(stringResource(R.string.settings_listen_room), detail = room.id, showTopSeparator = false)
            LiuMusicIosListRow(stringResource(R.string.settings_listen_members), detail = "${room.users.size.coerceAtLeast(1)} 人")
            room.users.forEach { user -> LiuMusicIosListRow(user.name) }
            LiuMusicIosListRow(
                stringResource(R.string.settings_listen_connection),
                detail = when (state.phase) {
                    LiuMusicListenTogetherCoordinator.Phase.Connected -> stringResource(R.string.settings_listen_synced)
                    LiuMusicListenTogetherCoordinator.Phase.Reconnecting -> stringResource(R.string.settings_listen_reconnecting)
                    LiuMusicListenTogetherCoordinator.Phase.Idle -> stringResource(R.string.settings_listen_resuming)
                },
            )
        }
        Spacer(Modifier.height(10.dp))
        SettingsActionButton(stringResource(R.string.settings_listen_share)) {
            val inviter = room.users.firstOrNull()?.id ?: room.creatorId
            val url = "https://music.163.com/listen-together/share/?roomId=${room.id}&inviterId=$inviter"
            context.startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, url),
                    "分享一起听邀请",
                ),
            )
        }
        Spacer(Modifier.height(10.dp))
        SettingsDangerButton(stringResource(R.string.settings_listen_end)) {
            if (!busy) {
                busy = true
                scope.launch {
                    runCatching { ops.endListenTogetherRoom(room.id) }
                        .onSuccess { LiuMusicListenTogetherCoordinator.clearRoom(app) }
                        .onFailure { message = it.message ?: "退出房间失败" }
                    busy = false
                }
            }
        }
    }
    if (busy) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.Center) {
            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
        }
    }
    (message ?: state.lastError)?.let {
        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun StorageSettings(context: android.content.Context) {
    var usage by remember { mutableStateOf(LiuMusicStorageUsage()) }
    var loading by remember { mutableStateOf(true) }
    var maintenanceMessage by remember { mutableStateOf<String?>(null) }
    var confirmation by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val downloads = remember(context) { LiuMusicDownloadStore.get(context) }
    suspend fun refresh() {
        loading = true
        usage = withContext(Dispatchers.IO) {
            val stats = StatFs(context.filesDir.path)
            LiuMusicStorageUsage(
                downloads = downloads.totalByteCount,
                networkCache = listOf("liumusic_http", "image_cache", "coil3_disk_cache")
                    .sumOf { context.cacheDir.resolve(it).treeByteCount() },
                playbackCache = context.cacheDir.resolve("liumusic_media").treeByteCount(),
                temporary = context.cacheDir.resolve("automix_analysis").treeByteCount(),
                localData = listOf(
                    context.filesDir.resolve("automix_analysis_index.json"),
                    context.filesDir.resolve("automix_analysis"),
                    context.filesDir.resolve("netease_library_cache"),
                ).sumOf { it.treeByteCount() },
                deviceTotal = stats.totalBytes,
                deviceAvailable = stats.availableBytes,
            )
        }
        loading = false
    }
    LaunchedEffect(Unit) { refresh() }

    var autoCache by remember { mutableStateOf(LiuMusicSettingsPreferences.boolean(context, "downloads_auto_cache", false)) }

    val deviceUsed = (usage.deviceTotal - usage.deviceAvailable).coerceAtLeast(0L)
    val deviceFraction = if (usage.deviceTotal > 0L) deviceUsed.toFloat() / usage.deviceTotal else 0f
    Box(Modifier.fillMaxWidth().liuMusicContentSurface(LiuMusicShapes.largeCard).padding(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            LiuMusicSymbolIcon(LiuMusicSymbol.Storage, Modifier.size(30.dp), MaterialTheme.colorScheme.onSurface)
            Text(stringResource(R.string.settings_storage_managed), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f))
            Text(if (loading) stringResource(R.string.settings_storage_calculating) else formatBytes(usage.managed), fontSize = 34.sp, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                progress = { deviceFraction.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            )
            Text(
                "设备已使用 ${formatBytes(deviceUsed)} · 可用 ${formatBytes(usage.deviceAvailable)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .50f),
            )
            Text("可重新生成的缓存：${formatBytes(usage.reclaimable)}", fontSize = 12.sp)
        }
    }
    Spacer(Modifier.height(18.dp))
    Text(stringResource(R.string.settings_storage_items), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .48f))
    Spacer(Modifier.height(8.dp))
    SettingsGlassGroup {
        StorageUsageRow(LiuMusicSymbol.Download, stringResource(R.string.settings_storage_downloads), usage.downloads, false)
        StorageUsageRow(LiuMusicSymbol.Apps, stringResource(R.string.settings_storage_network), usage.networkCache, true)
        StorageUsageRow(LiuMusicSymbol.AutoMix, stringResource(R.string.settings_storage_playback), usage.playbackCache, true)
        StorageUsageRow(LiuMusicSymbol.RadioWaves, stringResource(R.string.settings_storage_temporary), usage.temporary, true)
        StorageUsageRow(LiuMusicSymbol.Storage, stringResource(R.string.settings_storage_local), usage.localData, true)
    }
    Spacer(Modifier.height(18.dp))
    Text(stringResource(R.string.settings_storage_cleanup), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .48f))
    Spacer(Modifier.height(8.dp))
    SettingsGlassGroup {
        StorageActionRow(LiuMusicSymbol.Trash, stringResource(R.string.settings_storage_clear_all), false) { confirmation = "all_cache" }
        StorageActionRow(LiuMusicSymbol.Apps, stringResource(R.string.settings_storage_clear_network), true) { confirmation = "network_cache" }
        StorageActionRow(LiuMusicSymbol.AutoMix, stringResource(R.string.settings_storage_clear_playback), true) { confirmation = "playback_cache" }
    }
    Spacer(Modifier.height(18.dp))

    SettingsGlassGroup {
        SettingsToggleRow(context, stringResource(R.string.settings_storage_download_lyrics), "download_lyrics", true, stringResource(R.string.settings_storage_download_lyrics_note), grouped = true)
        SettingsExternalToggleRow(stringResource(R.string.settings_storage_auto_cache), autoCache, stringResource(R.string.settings_storage_auto_cache_note), grouped = true) {
            autoCache = it
            LiuMusicSettingsPreferences.setBoolean(context, "downloads_auto_cache", it)
        }
    }
    if (autoCache) {
        LyricsChoiceSetting(context, stringResource(R.string.settings_storage_threshold), "downloads_auto_cache_threshold", 3, listOf(2, 3, 5, 8, 10)) { "$it 次" }
        var cacheQuality by remember { mutableStateOf(LiuMusicSettingsPreferences.string(context, "downloads_auto_cache_quality", MusicQuality.Standard.name)) }
        Text(stringResource(R.string.settings_storage_auto_quality), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .48f))
        Spacer(Modifier.height(8.dp))
        SettingsGlassGroup {
            MusicQuality.entries.forEach { value ->
                SettingsChoiceRow(value.title, cacheQuality == value.name) {
                    cacheQuality = value.name
                    LiuMusicSettingsPreferences.setString(context, "downloads_auto_cache_quality", value.name)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        SettingsActionButton(stringResource(R.string.settings_storage_reset_stats)) { downloads.resetAutomaticCacheHistory() }
    }
    Spacer(Modifier.height(10.dp))

    if (downloads.activeDownloads.isNotEmpty()) {
        Text(stringResource(R.string.settings_storage_downloading), modifier = Modifier.padding(top=10.dp,bottom=8.dp), fontWeight=FontWeight.SemiBold)
        SettingsGlassGroup {
            downloads.activeDownloads.values.forEach { active ->
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(active.song.name, maxLines=1, overflow=TextOverflow.Ellipsis)
                        Text(active.fractionCompleted?.let { "${(it*100).toInt()}% · ${active.quality.title}" } ?: active.quality.title, color=MaterialTheme.colorScheme.onSurface.copy(alpha=.5f), fontSize=11.sp)
                    }
                    Text(stringResource(R.string.action_cancel), color=MaterialTheme.colorScheme.error, modifier=Modifier.clickable { downloads.cancel(active.song.id) }.padding(8.dp))
                }
            }
        }
    }

    if (downloads.downloads.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        SettingsDangerButton(stringResource(R.string.settings_storage_delete_all)) { downloads.removeAll() }
    }
    downloads.errorMessage?.let { Text(it, color=MaterialTheme.colorScheme.error, fontSize=12.sp, modifier=Modifier.padding(top=10.dp)) }
    maintenanceMessage?.let { Text(it, color=MaterialTheme.colorScheme.primary, fontSize=12.sp, modifier=Modifier.padding(top=10.dp)) }
    Spacer(Modifier.height(14.dp))
    SettingsActionButton(stringResource(R.string.settings_storage_repair)) {
        downloads.repairStorage { result ->
            maintenanceMessage = result.fold(
                onSuccess = {
                    "已移除 ${it.missingRecordsRemoved} 条缺失记录、${it.orphanFilesRemoved} 个孤立文件，回收 ${formatBytes(it.recoveredBytes)}；索引已压缩。"
                },
                onFailure = { it.message ?: "修复失败" },
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    SettingsActionButton(stringResource(R.string.settings_storage_clear_temp)) {
        confirmation = "temporary"
    }
    confirmation?.let { action ->
        val (title, message) = when (action) {
            "all_cache" -> stringResource(R.string.settings_storage_confirm_all_title) to stringResource(R.string.settings_storage_confirm_all_body)
            "network_cache" -> stringResource(R.string.settings_storage_confirm_network_title) to stringResource(R.string.settings_storage_confirm_network_body)
            "playback_cache" -> stringResource(R.string.settings_storage_confirm_playback_title) to stringResource(R.string.settings_storage_confirm_playback_body)
            else -> stringResource(R.string.settings_storage_confirm_temp_title) to stringResource(R.string.settings_storage_confirm_temp_body)
        }
        LiuMusicGlassDialog(visible = true, onDismiss = { confirmation = null }) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(message, Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f))
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsActionButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) { confirmation = null }
                SettingsActionButton(stringResource(R.string.settings_storage_clear), Modifier.weight(1f)) {
                    confirmation = null
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            when (action) {
                                "all_cache" -> { LiuMusicHttpClient.clearCache(); LiuMusicMediaCache.clear(context) }
                                "network_cache" -> LiuMusicHttpClient.clearCache()
                                "playback_cache" -> LiuMusicMediaCache.clear(context)
                                else -> context.cacheDir.resolve("automix_analysis").deleteRecursively()
                            }
                        }
                        maintenanceMessage = "清理完成"
                        refresh()
                    }
                }
            }
        }
    }
}

private data class LiuMusicStorageUsage(
    val downloads: Long = 0L,
    val networkCache: Long = 0L,
    val playbackCache: Long = 0L,
    val temporary: Long = 0L,
    val localData: Long = 0L,
    val deviceTotal: Long = 0L,
    val deviceAvailable: Long = 0L,
) {
    val managed: Long get() = downloads + networkCache + playbackCache + temporary + localData
    val reclaimable: Long get() = networkCache + playbackCache + temporary
}

@Composable
private fun StorageUsageRow(symbol: LiuMusicSymbol, title: String, bytes: Long, showSeparator: Boolean) {
    LiuMusicIosListRow(
        title = title,
        detail = formatBytes(bytes),
        leading = { LiuMusicSymbolIcon(symbol, Modifier.size(22.dp), LiuMusicSystemColors.Red) },
        showTopSeparator = showSeparator,
    )
}

@Composable
private fun StorageActionRow(symbol: LiuMusicSymbol, title: String, showSeparator: Boolean, onClick: () -> Unit) {
    LiuMusicIosListRow(
        title = title,
        leading = { LiuMusicSymbolIcon(symbol, Modifier.size(22.dp), LiuMusicSystemColors.Red) },
        onClick = onClick,
        showTopSeparator = showSeparator,
    )
}

private fun java.io.File.treeByteCount(): Long = when {
    isFile -> length()
    isDirectory -> listFiles()?.sumOf { it.treeByteCount() } ?: 0L
    else -> 0L
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L * 1024L -> "%.2f GB".format(bytes / 1024.0 / 1024.0 / 1024.0)
    bytes >= 1024L * 1024L -> "%.1f MB".format(bytes / 1024.0 / 1024.0)
    bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}

@Composable
private fun TabLayoutSettings(context: android.content.Context) {
    SettingsGlassGroup {
        SettingsToggleRow(context, stringResource(R.string.settings_tabs_home), "tab_home", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_tabs_explore), "tab_explore", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_tabs_library), "tab_library", true, grouped = true)
    }
    listOf(
        Triple("播客", "podcasts", LiuMusicSettingsRuntime.podcastsEnabled),
        Triple("下载", "downloads", LiuMusicSettingsRuntime.downloadsEnabled),
        Triple("云盘", "cloud", LiuMusicSettingsRuntime.cloudMusicEnabled),
    ).forEach { (title, key, enabled) ->
        Spacer(Modifier.height(10.dp))
        SettingsGlassGroup {
            Text(title, modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .48f))
            SettingsToggleRow(context, stringResource(R.string.settings_tabs_home_shortcut), "placement_${key}_home", key == "podcasts", if (enabled) null else "请先在内容功能中启用$title。", grouped = true)
            SettingsToggleRow(context, stringResource(R.string.settings_tabs_library_page), "placement_${key}_library", true, grouped = true)
            SettingsToggleRow(context, stringResource(R.string.settings_tabs_own_tab), "placement_${key}_tab", false, grouped = true)
        }
    }
    Spacer(Modifier.height(10.dp))
    var order by remember { mutableStateOf(LiuMusicSettingsRuntime.tabOrder) }
    SettingsGlassGroup {
        Text(stringResource(R.string.settings_tabs_order), modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .48f))
        order.forEachIndexed { index, page ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(when (page) { "Home" -> stringResource(R.string.settings_tabs_home); "Explore" -> stringResource(R.string.settings_tabs_explore); "Library" -> stringResource(R.string.settings_tabs_library); "Podcasts" -> "播客"; "Downloads" -> "下载"; "Cloud" -> "云盘"; else -> stringResource(R.string.settings_tabs_settings) }, Modifier.weight(1f))
                LiuMusicActionIcon("↑", Modifier.size(18.dp).clickable(enabled = index > 0) {
                    order = order.toMutableList().apply { add(index - 1, removeAt(index)) }
                    LiuMusicSettingsPreferences.setString(context, "tab_order", order.joinToString(","))
                }.padding(10.dp), MaterialTheme.colorScheme.primary.copy(alpha = if (index > 0) 1f else .25f))
                LiuMusicActionIcon("↓", Modifier.size(18.dp).clickable(enabled = index < order.lastIndex) {
                    order = order.toMutableList().apply { add(index + 1, removeAt(index)) }
                    LiuMusicSettingsPreferences.setString(context, "tab_order", order.joinToString(","))
                }.padding(10.dp), MaterialTheme.colorScheme.primary.copy(alpha = if (index < order.lastIndex) 1f else .25f))
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    var homeOrder by remember { mutableStateOf(LiuMusicSettingsRuntime.homeSectionOrder) }
    SettingsGlassGroup {
        Text(stringResource(R.string.settings_tabs_home_sections), modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .48f))
        SettingsToggleRow(context, stringResource(R.string.settings_tabs_shortcuts), "home_quick_actions", true, grouped = true)
        SettingsToggleRow(context, "推荐歌单", "home_playlists", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_tabs_new_songs), "home_new_songs", true, grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_tabs_remember_library), "library_remember_page", true, grouped = true)
        homeOrder.forEachIndexed { index, section ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(when (section) { "QuickActions" -> stringResource(R.string.settings_tabs_shortcuts); "Playlists" -> "推荐歌单"; else -> stringResource(R.string.settings_tabs_new_songs) }, Modifier.weight(1f))
                LiuMusicActionIcon("↑", Modifier.size(18.dp).clickable(enabled = index > 0) {
                    homeOrder = homeOrder.toMutableList().apply { add(index - 1, removeAt(index)) }
                    LiuMusicSettingsPreferences.setString(context, "home_section_order", homeOrder.joinToString(","))
                }.padding(10.dp), MaterialTheme.colorScheme.primary.copy(alpha = if (index > 0) 1f else .25f))
                LiuMusicActionIcon("↓", Modifier.size(18.dp).clickable(enabled = index < homeOrder.lastIndex) {
                    homeOrder = homeOrder.toMutableList().apply { add(index + 1, removeAt(index)) }
                    LiuMusicSettingsPreferences.setString(context, "home_section_order", homeOrder.joinToString(","))
                }.padding(10.dp), MaterialTheme.colorScheme.primary.copy(alpha = if (index < homeOrder.lastIndex) 1f else .25f))
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    var libraryPage by remember { mutableStateOf(LiuMusicSettingsRuntime.defaultLibraryPage) }
    SettingsGlassGroup {
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_tabs_default_library),
            selected = libraryPage,
            items = listOf("Songs" to stringResource(R.string.settings_tabs_songs), "Playlists" to stringResource(R.string.settings_tabs_playlist_page), "Podcasts" to "播客", "Cloud" to "云盘", "History" to stringResource(R.string.settings_feature_history), "Downloads" to "下载")
                .filter { (value, _) ->
                    (value != "Podcasts" || LiuMusicSettingsRuntime.podcastsEnabled) &&
                        (value != "Podcasts" || LiuMusicSettingsRuntime.podcastsLibraryPlacement) &&
                        (value != "Cloud" || LiuMusicSettingsRuntime.cloudMusicEnabled) &&
                        (value != "Cloud" || LiuMusicSettingsRuntime.cloudLibraryPlacement) &&
                        (value != "History" || LiuMusicSettingsRuntime.listeningHistoryEnabled) &&
                        (value != "Downloads" || LiuMusicSettingsRuntime.downloadsEnabled) &&
                        (value != "Downloads" || LiuMusicSettingsRuntime.downloadsLibraryPlacement)
                },
            onSelected = { libraryPage = it; LiuMusicSettingsPreferences.setString(context, "library_default_page", it) },
            grouped = true,
        )
        Text(stringResource(R.string.settings_tabs_search_note), modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .46f))
    }
}

@Composable
private fun GeneralSettings(context: android.content.Context) {
    var theme by remember { mutableStateOf(LiuMusicSettingsRuntime.themeMode) }
    var swipeFullAction by remember { mutableStateOf(LiuMusicSettingsRuntime.swipeFullAction) }
    SettingsGlassGroup {
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_general_theme),
            selected = theme,
            items = listOf(
                LiuMusicThemeMode.System to stringResource(R.string.settings_general_theme_system),
                LiuMusicThemeMode.Light to stringResource(R.string.settings_general_theme_light),
                LiuMusicThemeMode.Dark to stringResource(R.string.settings_general_theme_dark),
            ),
            onSelected = { theme = it; LiuMusicSettingsPreferences.setString(context, "theme_mode", it.name) },
            grouped = true,
        )
        var defaultTab by remember { mutableStateOf(LiuMusicSettingsRuntime.defaultTab) }
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_general_launch),
            selected = defaultTab,
            items = listOf(
                "Home" to stringResource(R.string.settings_tabs_home),
                "Explore" to stringResource(R.string.settings_tabs_explore),
                "Library" to stringResource(R.string.settings_tabs_library),
                "Settings" to stringResource(R.string.settings_tabs_settings),
            ),
            onSelected = { defaultTab = it; LiuMusicSettingsPreferences.setString(context, "general_default_tab", it) },
            grouped = true,
        )
        SettingsToggleRow(context, stringResource(R.string.settings_general_remember_tab), "general_remember_tab", true, grouped = true)
        LiuMusicSettingsDropdown(
            title = stringResource(R.string.settings_general_swipe),
            selected = swipeFullAction,
            items = listOf(
                LiuMusicSwipeFullAction.PlayNext to stringResource(R.string.settings_general_play_next),
                LiuMusicSwipeFullAction.AddToQueue to stringResource(R.string.settings_general_add_queue),
            ),
            onSelected = {
                swipeFullAction = it
                LiuMusicSettingsPreferences.setString(context, "general_swipe_full_action", it.name)
            },
            grouped = true,
        )
        SettingsToggleRow(context, stringResource(R.string.settings_general_clipboard), "general_clipboard_links", true, stringResource(R.string.settings_general_clipboard_note), grouped = true)
        SettingsToggleRow(context, stringResource(R.string.settings_general_haptics), "general_haptic_feedback", true, grouped = true)
        SettingsToggleRow(
            context,
            stringResource(R.string.settings_general_tabbar),
            "general_disable_auto_tabbar_shrink",
            false,
            stringResource(R.string.settings_general_tabbar_note),
            grouped = true,
        )
    }
}

@Composable
fun LiuMusicRecognitionPanel(context: android.content.Context) {
    val client = remember(context) { SongRecognitionClient(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var duration by remember {
        mutableStateOf(LiuMusicSettingsPreferences.string(context, "recognition_duration", "6").toIntOrNull() ?: 6)
    }
    var working by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("靠近声源后开始识别") }
    var error by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<List<SongRecognitionResult>>(emptyList()) }
    var recognitionJob by remember { mutableStateOf<Job?>(null) }
    DisposableEffect(client) {
        onDispose {
            recognitionJob?.cancel()
            client.close()
        }
    }

    fun startCapture() {
        recognitionJob?.cancel()
        recognitionJob = scope.launch {
            working = true
            error = null
            if (duration != 0) results = emptyList()
            try {
                if (duration == 0) {
                    status = "正在持续识别；点击停止结束"
                    while (isActive) {
                        val found = client.recognize(9)
                        if (found.isNotEmpty()) {
                            results = (found + results).distinctBy { it.song.id }.take(100)
                        }
                    }
                } else {
                    status = "正在聆听 ${duration} 秒…"
                    val found = client.recognize(duration)
                    results = found
                    status = if (found.isEmpty()) "没有识别到歌曲，请靠近声源后重试" else "识别完成"
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                status = if (results.isEmpty()) "识别已停止" else "已停止，保留识别结果"
            } catch (failure: Throwable) {
                error = failure.message ?: "听歌识曲失败"
                status = "无法完成识别"
            } finally {
                working = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) startCapture() else error = "没有麦克风权限；请在系统设置中允许 LiuMusic 使用麦克风。"
    }

    Spacer(Modifier.height(14.dp))
    SettingsGlassGroup {
        listOf(3 to stringResource(R.string.settings_recognition_seconds_fast), 6 to stringResource(R.string.settings_recognition_seconds_recommended), 9 to stringResource(R.string.settings_recognition_seconds_noisy)).forEach { (value, title) ->
            SettingsChoiceRow(title, duration == value) {
                if (!working) {
                    duration = value
                    LiuMusicSettingsPreferences.setString(context, "recognition_duration", value.toString())
                }
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    SettingsActionButton(if (working) stringResource(R.string.settings_recognition_stop) else stringResource(R.string.settings_recognition_start)) {
        if (working) {
            recognitionJob?.cancel()
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startCapture()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        if (working) {
            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            Spacer(Modifier.size(10.dp))
        }
        Text(status, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f), fontSize = 13.sp)
    }
    error?.let { message ->
        Text(message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
    }
    if (results.isNotEmpty()) {
        Text(stringResource(R.string.settings_recognition_results), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        SettingsGlassGroup {
            Column {
                results.forEach { result ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            PlaybackCommands.playQueue(
                                context = context,
                                songs = results.map { it.song },
                                selectedSongId = result.song.id,
                                startPositionMs = result.startTimeMs,
                            )
                        }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(result.song.artworkUrl, null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(9.dp)))
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(result.song.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                            Text(result.song.artists, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f))
                        }
                        if (result.startTimeMs > 0L) Text("${result.startTimeMs / 1000}s", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .42f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteConfigSettings() {
    val context = LocalContext.current.applicationContext
    val status by LiuMusicRemoteConfigRuntime.status.collectAsState()
    val scope = rememberCoroutineScope()
    val githubRouting = remember { LiuMusicGitHubRouting(context) }
    val consentEnabled = LiuMusicRemoteConfigConsent.enabled(context)
    val source = if (!consentEnabled) {
        stringResource(R.string.settings_remote_declined)
    } else when (status.source) {
        LiuMusicRemoteConfigSource.BuiltIn -> stringResource(R.string.settings_remote_builtin)
        LiuMusicRemoteConfigSource.VerifiedRemote -> stringResource(R.string.settings_remote_verified)
        LiuMusicRemoteConfigSource.VersionInapplicable -> stringResource(R.string.settings_remote_inapplicable)
    }
    SettingsGlassGroup {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(source, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.settings_remote_summary),
                modifier = Modifier.padding(top = 7.dp),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f),
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    SettingsGlassGroup {
        RemoteConfigStatusLine(stringResource(R.string.settings_remote_version), status.config.configVersion.toString())
        RemoteConfigStatusLine(
            stringResource(R.string.settings_remote_access),
            githubRouting.effectiveRoute()?.takeIf { githubRouting.selectedSource() == LiuMusicGitHubSource.Auto }
                ?.let { "${it.source.label}（${it.latencyMs}ms）" }
                ?: githubRouting.selectedSource().label,
        )
        RemoteConfigStatusLine(stringResource(R.string.settings_remote_key), status.keyId ?: stringResource(R.string.settings_remote_builtin_key))
        RemoteConfigStatusLine(stringResource(R.string.settings_remote_last_check), formatRemoteConfigTime(status.lastCheckedAtEpochMs))
        RemoteConfigStatusLine(stringResource(R.string.settings_remote_last_update), formatRemoteConfigTime(status.lastUpdatedAtEpochMs))
        RemoteConfigStatusLine(
            stringResource(R.string.settings_remote_breakers),
            status.config.disabledCapabilities.takeIf(Set<String>::isNotEmpty)?.joinToString("、") ?: "无",
        )
        RemoteConfigStatusLine(stringResource(R.string.settings_remote_fallback_order), status.config.fallback.order.joinToString(" → "))
        RemoteConfigStatusLine(stringResource(R.string.settings_remote_timeout), "${status.config.fallback.timeoutMs} ms")
        RemoteConfigStatusLine("热更新", hotUpdateSummary())
    }
    status.error?.let { error ->
        Spacer(Modifier.height(12.dp))
        SettingsInfoCard("最近检查失败：$error\n已继续使用上一次有效配置或内置默认值。")
    }
    Spacer(Modifier.height(12.dp))
    SettingsActionButton(
        when {
            !consentEnabled -> stringResource(R.string.settings_remote_enable_first)
            status.refreshing -> stringResource(R.string.settings_about_checking)
            else -> stringResource(R.string.settings_remote_check)
        },
    ) {
        if (consentEnabled && !status.refreshing) {
            scope.launch { LiuMusicRemoteConfigRuntime.refresh(force = true) }
        }
    }
    Spacer(Modifier.height(10.dp))
    SettingsDangerButton(stringResource(R.string.settings_remote_clear)) {
        scope.launch { LiuMusicRemoteConfigRuntime.clearCache(context) }
    }
}

/**
 * Compact view of what the signed hot-update channel currently contributes, so
 * a maintainer can confirm a payload landed without digging through logs.
 */
@Composable
private fun hotUpdateSummary(): String {
    val text by com.liuguanyi.liumusic.core.remoteconfig.LiuMusicHotStrings.overrides.collectAsState()
    val colors by com.liuguanyi.liumusic.core.remoteconfig.LiuMusicHotStyles.colors.collectAsState()
    val scripts by com.liuguanyi.liumusic.core.remoteconfig.LiuMusicHotScripts.scripts.collectAsState()
    val rules by com.liuguanyi.liumusic.core.remoteconfig.LiuMusicHotRules.rules.collectAsState()
    val neteaseStrategy = com.liuguanyi.liumusic.core.remoteconfig.LiuMusicHotStrategies.value("netease_playback")
    return buildString {
        append("文案 ").append(text.values.sumOf { it.size })
        append(" · 颜色 ").append(colors.size)
        append(" · 脚本 ").append(scripts.size)
        append(" · 后缀 ").append(rules.searchTitleStripSuffixes.size)
        append(" · 网易云 ").append(neteaseStrategy)
        val overridden = text.values.sumOf { it.size } + colors.size + scripts.size +
            rules.searchTitleStripSuffixes.size
        if (overridden == 0 && neteaseStrategy == "v1") append("（未覆盖）")
    }
}

@Composable
private fun RemoteConfigStatusLine(title: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f))
        Text(value, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End, fontSize = 13.sp)
    }
}

private fun formatRemoteConfigTime(value: Long): String = if (value <= 0L) {
    "尚未"
} else {
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(value))
}

@Composable
private fun AboutSettings(context: android.content.Context) {
    val githubRouting = remember { LiuMusicGitHubRouting(context) }
    val updateClient = remember { LiuMusicUpdateClient(context, routing = githubRouting) }
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var exportingLogs by remember { mutableStateOf(false) }
    var showLogExportInfo by remember { mutableStateOf(false) }
    var release by remember { mutableStateOf<LiuMusicRelease?>(null) }
    var updateStatus by remember { mutableStateOf<String?>(null) }
    var versionTapCount by remember { mutableIntStateOf(0) }
    var lastVersionTapAt by remember { mutableStateOf(0L) }
    var showCatEgg by remember { mutableStateOf(false) }
    var downloadSource by remember { mutableStateOf(githubRouting.selectedSource()) }
    val exportLogsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        if (uri == null) {
            exportingLogs = false
        } else {
            scope.launch {
                runCatching {
                    LiuMusicLogExporter.exportRecentLogs(context, uri)
                }.onSuccess { result ->
                    updateStatus = "已导出 LiuMusic 当前进程日志（${result.lineCount} 行）"
                }.onFailure { error ->
                    updateStatus = error.message ?: "日志导出失败"
                }
                exportingLogs = false
            }
        }
    }
    Box(
        modifier = Modifier.pointerInput(Unit) {
            var distance = 0f
            detectVerticalDragGestures(
                onDragStart = { distance = 0f },
                onVerticalDrag = { _, dragAmount ->
                    distance += dragAmount
                    if (distance > 180f) {
                        showCatEgg = true
                        distance = 0f
                    }
                },
            )
        },
    ) {
        SettingsGlassGroup {
            Column(Modifier.padding(18.dp)) {
            Text("LiuMusic Android", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "版本 ${BuildConfig.VERSION_NAME} · 柳乐 LiuMusic，由柳贯一独立开发。",
                modifier = Modifier
                    .padding(top = 7.dp)
                    .clickable {
                        val now = System.currentTimeMillis()
                        versionTapCount = if (now - lastVersionTapAt < 2_000L) versionTapCount + 1 else 1
                        lastVersionTapAt = now
                        if (versionTapCount >= 7) {
                            showCatEgg = true
                            versionTapCount = 0
                        }
                    },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha=.62f),
            )
            Text(hotText("about.maintainer", stringResource(R.string.settings_about_maintainer)), modifier = Modifier.padding(top=14.dp), fontWeight=FontWeight.SemiBold)
            Text(hotText("about.upstream", stringResource(R.string.settings_about_upstream)), modifier = Modifier.padding(top=5.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha=.58f))
            }
        }
    }
    if (showCatEgg) {
        LiuMusicGlassDialog(visible = true, onDismiss = { showCatEgg = false }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                LiuMusicPinkCat()
                Text(hotText("about.cat_title", stringResource(R.string.settings_about_cat_title)), style = MaterialTheme.typography.titleLarge)
                Text(hotText("about.cat_body", stringResource(R.string.settings_about_cat_body)), modifier = Modifier.padding(top = 7.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f))
                LiuMusicGlassButton(
                    onClick = { showCatEgg = false },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    style = LiuMusicGlassButtonStyle.BorderedProminent,
                ) { Text(stringResource(R.string.settings_about_cat_accept)) }
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    SettingsToggleRow(context, stringResource(R.string.settings_about_auto_update), "update_auto_check", true, stringResource(R.string.settings_about_auto_update_note))
    Spacer(Modifier.height(10.dp))
    LiuMusicSettingsDropdown(
        title = stringResource(R.string.settings_about_github_source),
        selected = downloadSource,
        items = listOf(
            LiuMusicGitHubSource.Auto to stringResource(R.string.settings_about_source_auto),
            LiuMusicGitHubSource.GitHubDoh to stringResource(R.string.settings_about_source_doh),
            LiuMusicGitHubSource.GhFast to stringResource(R.string.settings_about_source_ghfast),
            LiuMusicGitHubSource.GhProxy to stringResource(R.string.settings_about_source_ghproxy),
            LiuMusicGitHubSource.GhProxyOrg to stringResource(R.string.settings_about_source_ghproxy_org),
        ),
        onSelected = {
            downloadSource = it
            githubRouting.selectSource(it)
        },
    )
    Text(
        githubRouting.effectiveRoute()?.takeIf { downloadSource == LiuMusicGitHubSource.Auto }?.let {
            "自动测速当前选择 ${it.source.label}（${it.latencyMs}ms）；更新检查、APK 下载与已授权的签名配置共用此源。"
        } ?: stringResource(R.string.settings_about_source_auto_note),
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
    )
    SettingsActionButton(if (checking) stringResource(R.string.settings_about_checking) else stringResource(R.string.settings_about_check_update)) {
        if (!checking) scope.launch {
            checking = true
            runCatching { updateClient.latestStableRelease(forceSourceBenchmark = true) }
                .onSuccess {
                    release = it
                    updateStatus = if (updateClient.isNewer(it.version, BuildConfig.VERSION_NAME)) {
                        "发现新版本 ${it.version}：${it.name}"
                    } else {
                        "当前已是最新版本（${BuildConfig.VERSION_NAME}）"
                    }
                }
                .onFailure { updateStatus = it.message ?: "更新检查失败" }
            checking = false
        }
    }
    val currentCommit = BuildConfig.GIT_SHA.take(7)
    if (currentCommit.isNotBlank()) {
        Spacer(Modifier.height(10.dp))
        Spacer(Modifier.height(10.dp))
        Text(
            text = "当前构建 commit $currentCommit（开发版）",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .42f),
        )
    }
    var devCommit by remember { mutableStateOf<LiuMusicDevCommit?>(null) }
    SettingsActionButton(stringResource(R.string.settings_about_check_dev)) {
        if (!checking) scope.launch {
            checking = true
            runCatching { updateClient.latestDevCommit() }
                .onSuccess { latest ->
                    devCommit = latest
                    updateStatus = if (latest.sha == BuildConfig.GIT_SHA) {
                        "已经是最新提交 ${latest.sha.take(7)}"
                    } else {
                        "main 有新提交 ${latest.sha.take(7)}：${latest.message}"
                    }
                }
                .onFailure { updateStatus = it.message ?: "检查最新提交失败" }
            checking = false
        }
    }
    devCommit?.takeIf { it.sha != BuildConfig.GIT_SHA }?.let { latest ->
        Spacer(Modifier.height(10.dp))
        SettingsActionButton(stringResource(R.string.settings_about_download_dev)) {
            scope.launch {
                val target = runCatching { updateClient.devBuildUrl() }.getOrNull()
                    ?: "https://github.com/chenran520/LiuMusic-Android/actions/workflows/build.yml"
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target))) }
                    .onFailure { updateStatus = it.message ?: "无法打开下载页" }
            }
        }
        Text(
            text = "新提交 ${latest.sha.take(7)}（${latest.author}）：${latest.message}",
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f),
        )
    }
    updateStatus?.let { message ->
        Spacer(Modifier.height(10.dp))
        Text(message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f))
    }
    release?.takeIf { updateClient.isNewer(it.version, BuildConfig.VERSION_NAME) }?.let { available ->
        Spacer(Modifier.height(10.dp))
        var deltaPatch by remember(available.version) {
            mutableStateOf<com.liuguanyi.liumusic.core.remoteconfig.LiuMusicRemoteUpdatePatch?>(null)
        }
        LaunchedEffect(available.version) {
            deltaPatch = withContext(Dispatchers.IO) {
                val patch = com.liuguanyi.liumusic.core.remoteconfig.LiuMusicRemoteConfigRuntime.status.value
                    .config.updatePatches.firstOrNull { it.toVersion == available.version }
                    ?: return@withContext null
                val installed = com.liuguanyi.liumusic.core.update.LiuMusicApkDelta.installedApkSha256(context)
                    ?: return@withContext null
                patch.takeIf { it.fromApkSha256.equals(installed, ignoreCase = true) }
            }
        }
        deltaPatch?.let { patch ->
            SettingsActionButton(
                "增量更新 ${available.version}（约 %.0f MB）".format(patch.patchBytes / 1024.0 / 1024.0),
            ) {
                scope.launch {
                    updateStatus = "正在下载增量补丁…"
                    val patchFile = updateClient.downloadPatch(context, patch)
                    if (patchFile == null) {
                        updateStatus = "增量补丁下载失败，可用整包下载"
                        return@launch
                    }
                    updateStatus = "正在重建安装包…"
                    val apk = withContext(Dispatchers.IO) {
                        com.liuguanyi.liumusic.core.update.LiuMusicApkDelta.reconstruct(
                            context = context,
                            patch = patchFile,
                            expectedApkSha256 = patch.apkSha256,
                            versionName = available.version,
                        )
                    }
                    if (apk == null) {
                        updateStatus = "增量重建校验失败，请用整包下载"
                        return@launch
                    }
                    updateStatus = if (com.liuguanyi.liumusic.core.update.LiuMusicApkDelta.launchInstaller(context, apk)) {
                        "已打开系统安装器"
                    } else {
                        "无法打开安装器，请用整包下载"
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        SettingsActionButton(if (available.apkUrl != null) "下载 ${available.version} APK" else "打开 ${available.version} 发布页") {
            scope.launch {
                val target = runCatching { updateClient.downloadUrl(available) }.getOrNull() ?: available.pageUrl
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target))) }
                    .onFailure { updateStatus = it.message ?: "无法打开下载链接" }
            }
        }
        if (available.notes.isNotBlank()) {
            Text(available.notes.take(700), modifier = Modifier.padding(top = 10.dp), fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f))
        }
    }
    Spacer(Modifier.height(14.dp))
    Spacer(Modifier.height(10.dp))
    SettingsActionButton(stringResource(R.string.settings_about_restore_player)) {
        LiuMusicSettingsPreferences.resetRecommendedPlayerSettings(context)
        updateStatus = "已恢复推荐播放器、歌词与 AutoMix 配置"
    }
    Spacer(Modifier.height(14.dp))
    SettingsGlassGroup {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.settings_about_licenses_title), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "LiuMusic 主体：GNU GPLv3\n" +
                    "qier222/YesPlayMusic：网易云接口与播放器实现参考（MIT）\n" +
                    "jayfunc/BetterLyrics：逐字歌词渲染、光效与动效参考\n" +
                    "WXRIW/Lyricify-Lyrics-Helper：网易云 YRC 解析参考\n" +
                    "neteasecloudmusicapienhanced/api-enhanced：听歌识曲与音频指纹运行时\n" +
                    "DanteAlighieri13210914/pv-tool：文字 PV 原始实现（Non-Commercial License）\n" +
                     "mjhydri/BeatNet：自动混音节拍/重拍/速度分析（CC BY 4.0）\n" +
                     "NEORUAA/MeiloX：基于 Mei 的仿 Apple Music 网易云音乐客户端，提供 UI 参考\n" +
                     "liuguanyi/Square：Spotify 与 YouTube Music 双后端架构参考\n" +
                     "YouTube Music：基于 Square 架构，使用 vendored Metrolist InnerTube（GPL-3.0）与 NewPipeExtractor\n" +
                     "Spotify：参考 Square Spotify backend，使用 librespot-java（Apache-2.0）\n" +
                     "vivo OriginOS：自动尝试原子岛通知适配，不提供单独开关\n" +
                     "thlucas1/SpotifyWebApiPython：Spotify Web API 客户端，提供 Spotify API 参考\n" +
                    "bromothymolb/bilibili-api-zoku：Bilibili API 调用整合项目，提供 Bilibili API 参考\n" +
                    "Kyant0 AndroidLiquidGlass / Backdrop：Android 液态玻璃渲染基础",
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f),
                fontSize = 13.sp,
                lineHeight = 20.sp,
            )
        }
    }
    Spacer(Modifier.height(14.dp))
    SettingsActionButton(stringResource(R.string.settings_about_github)) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/chenran520/LiuMusic-Android"))) }
    }
    Spacer(Modifier.height(10.dp))
    SettingsActionButton(stringResource(R.string.settings_about_ios)) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("mailto:2676911574@qq.com"))) }
    }
    Spacer(Modifier.height(10.dp))
    SettingsActionButton(stringResource(R.string.settings_about_upstream_licenses)) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("mailto:2676911574@qq.com"))) }
    }
    Spacer(Modifier.height(10.dp))
    SettingsActionButton(if (exportingLogs) stringResource(R.string.settings_about_exporting) else stringResource(R.string.settings_about_export)) {
        if (!exportingLogs) showLogExportInfo = true
    }

    if (showLogExportInfo) {
        val deviceInfo: LiuMusicLogDeviceInfo = LiuMusicLogExporter.collectDeviceInfo(context)
        LiuMusicGlassDialog(
            visible = true,
            onDismiss = { showLogExportInfo = false },
        ) {
            Text(stringResource(R.string.settings_about_export), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_about_export_body),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .64f),
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .045f))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Android 版本：${deviceInfo.androidVersion}", fontSize = 13.sp)
                Text("手机型号：${deviceInfo.phoneModel}", fontSize = 13.sp)
                Text("系统版本：${deviceInfo.systemVersion}", fontSize = 13.sp)
                Text(
                    "已登录音乐源：${deviceInfo.loggedMusicSources.takeIf { it.isNotEmpty() }?.joinToString("、") ?: "无"}",
                    fontSize = 13.sp,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SettingsActionButton(stringResource(R.string.action_cancel), Modifier.weight(1f)) { showLogExportInfo = false }
                SettingsActionButton(stringResource(R.string.settings_about_choose_location), Modifier.weight(1f)) {
                    showLogExportInfo = false
                    exportingLogs = true
                    exportLogsLauncher.launch("LiuMusic-logs-${System.currentTimeMillis()}.txt")
                }
            }
        }
    }
}


private val LocalSettingsGroupedRows = staticCompositionLocalOf { false }
internal val LocalSettingsGroupRowIndex = staticCompositionLocalOf { mutableIntStateOf(0) }

@Composable
private fun SettingsToggleRow(
    context: android.content.Context,
    title: String,
    key: String,
    default: Boolean,
    note: String? = null,
    grouped: Boolean = false,
    showTopSeparator: Boolean = false,
) {
    var value by remember(key) { mutableStateOf(LiuMusicSettingsPreferences.boolean(context, key, default)) }
    val groupRowIndex = LocalSettingsGroupRowIndex.current
    val effectiveShowTopSeparator = when {
        showTopSeparator -> true
        grouped -> groupRowIndex.intValue++.let { it > 0 }
        else -> false
    }
    @Composable fun row() {
        LiuMusicIosListRow(
            title = title,
            trailing = {
                LiuMusicGlassToggle(checked = value, onCheckedChange = {
                    value = it
                    LiuMusicSettingsPreferences.setBoolean(context, key, it)
                })
            },
            showTopSeparator = effectiveShowTopSeparator,
        )
    }
    if (grouped || LocalSettingsGroupedRows.current) row() else {
        SettingsGlassGroup { row() }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun SettingsExternalToggleRow(
    title: String,
    value: Boolean,
    note: String? = null,
    grouped: Boolean = false,
    onValueChange: (Boolean) -> Unit,
) {
    val groupRowIndex = LocalSettingsGroupRowIndex.current
    val effectiveShowTopSeparator = if (grouped) groupRowIndex.intValue++.let { it > 0 } else false
    val row = @Composable {
        LiuMusicIosListRow(
            title = title,
            trailing = { LiuMusicGlassToggle(checked = value, onCheckedChange = onValueChange) },
            showTopSeparator = effectiveShowTopSeparator,
        )
    }
    if (grouped) {
        row()
    } else {
        SettingsGlassGroup { row() }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun SettingsChoiceRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, Modifier.weight(1f), fontSize = 16.sp)
        if (selected) Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SettingsGlassGroup(content: @Composable ColumnScope.() -> Unit) {
    val rowIndex = remember { mutableIntStateOf(0) }
    rowIndex.intValue = 0
    CompositionLocalProvider(LocalSettingsGroupRowIndex provides rowIndex) {
        LiuMusicIosGroupedList(surfaceColor = MaterialTheme.colorScheme.surface, content = content)
    }
}

@Composable
private fun SettingsInfoCard(value: String) {
    SettingsGlassGroup {
        Text(
            value,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .52f),
        )
    }
}

@Composable
private fun SettingsActionButton(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.fillMaxWidth().height(50.dp).liuMusicLiquidButton(
            shape = RoundedCornerShape(25.dp),
            surfaceColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .06f),
        ).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(title, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun SettingsRoundButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).liuMusicLiquidButton(shape = CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { LiuMusicActionIcon(text, Modifier.size(20.dp), MaterialTheme.colorScheme.onSurface) }
}

@Composable
private fun SettingsResetCard() {
    val context = LocalContext.current
    SettingsDangerButton(stringResource(R.string.settings_reset_player)) {
        LiuMusicSettingsPreferences.reset(context)
        LiuMusicPlaybackModePreferences.reset(context)
        PlaybackCommands.changeQuality(context, MusicQuality.Standard)
    }
}

@Composable
private fun SettingsDangerButton(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.fillMaxWidth().height(54.dp).liuMusicLiquidButton(
            shape = RoundedCornerShape(27.dp),
            tint = MaterialTheme.colorScheme.error.copy(alpha = .25f),
            surfaceColor = MaterialTheme.colorScheme.error.copy(alpha = .20f),
        ).clickable(onClick = onClick).padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart,
    ) { Text(title, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold) }
}
