# LiuMusic Multi-Provider UI Architecture Contract

[中文](../README.zh-Hans.md)

This document is an architectural invariant for the Android port.

## Core rule

**LiuMusic owns presentation. Music providers own data and capabilities.**

NetEase, QQ Music and Kugou must render through the same canonical LiuMusic Compose presentation wherever the product semantics are shared. A provider must not create a second Home, Explore, Search, Library, playlist-detail, player, settings, animation, or background implementation just because its API is different.

Provider differences are allowed only in:

- authentication/session storage;
- API clients and protocol models;
- provider-neutral model mapping;
- capability availability;
- provider-native actions whose semantics genuinely do not exist on other services.

## Presentation invariants

The following belong to canonical LiuMusic UI and must never be selected by `MusicSource`:

- layout and spacing;
- navigation transitions;
- `SharedTransitionLayout` / shared-element artwork transitions;
- `AnimatedContent` timing and motion;
- Liquid Glass modifiers and backdrop sampling;
- artwork-driven / Flowing Light backgrounds;
- MiniPlayer and Now Playing presentation;
- Settings presentation and route hierarchy;
- typography and canonical root-tab names.

A provider may make an action or section unavailable through a capability gate. It must not replace the surrounding screen with a provider-specific renderer.

## Settings invariants

**Switching music source changes the backing provider, not LiuMusic settings.**

The canonical `SettingsScreen` is shared by NetEase, QQ Music and Kugou. Playback, lyrics, player appearance, animation/background behavior, system lyrics, Skyline lyrics, floating lyrics, tab layout, general UI behavior and other LiuMusic-local preferences use the same global `LiuMusicSettingsPreferences` / runtime state regardless of the selected provider.

Rules:

- changing `MusicSource` must not replace the Settings renderer;
- changing `MusicSource` must not navigate away from the current Settings page/route;
- global LiuMusic preference keys must not be provider-namespaced merely because a new provider is added;
- player/lyrics/appearance settings should affect provider playback whenever the underlying renderer/playback behavior is provider-neutral;
- provider-local state is limited to authentication/session/device identity, rights/API requirements and genuinely provider-native operations;
- settings for a feature that has no equivalent capability on a provider may remain visible as canonical LiuMusic configuration, but the unavailable provider action/section must not be faked;
- source/account selection may be presented as a compact control layered onto canonical Settings, but it must not become a second provider settings app.

This keeps an iOS → Android settings migration one-time: a new canonical LiuMusic setting is implemented once and all compatible providers inherit it automatically.

## Data flow

```text
Provider API / local session
        ↓
Provider implementation + Capability interfaces
        ↓
Provider-neutral domain models
        ↓
LiuMusic presentation state / compatibility bridge
        ↓
Canonical LiuMusic Compose renderer
```

`LiuMusicLegacyUiBridge` is the compatibility seam while older LiuMusic presentation code still consumes `SearchSong`, `NeteasePlaylistSummary`, and related UI-era models. Provider-native ids must remain attached to their backing domain objects; synthetic bridge ids are UI keys only and must never be sent to a provider API.

## iOS → Android feature migration rule

When a new LiuMusic iOS feature is migrated:

1. Port the visual/interaction behavior into the existing canonical Android LiuMusic screen/component.
2. Keep animation, background, gesture and layout code provider-agnostic.
3. If the feature needs data not present in the common domain, add or extend a provider-neutral model/capability.
4. Implement that capability in each provider that genuinely supports the same semantics.
5. Gate only the unavailable action/section. Do not fork the screen.
6. Preserve existing NetEase behavior unless the migrated feature intentionally changes canonical LiuMusic behavior for every compatible source.
7. If the feature adds a LiuMusic-local setting, add it once to canonical Settings and reuse the same preference/runtime value for every compatible provider.

This means a future visual or settings improvement from LiuMusic iOS should normally be implemented once and automatically appear for QQ Music and Kugou when their capabilities can supply the required state.

## Forbidden patterns

Avoid introducing code shaped like this inside canonical presentation:

```kotlin
when (source) {
    MusicSource.Netease -> NeteaseLibraryUi()
    MusicSource.QQMusic -> QQMusicLibraryUi()
    MusicSource.Kugou -> KugouLibraryUi()
}
```

Also avoid provider checks around animation/background selection such as:

```kotlin
if (source == MusicSource.QQMusic) QQBackground() else LiuMusicFlowingLightBackdrop(...)
```

And do not namespace global LiuMusic settings by provider:

```kotlin
// Wrong for a canonical UI/player preference.
"lyrics_style_${source.storageValue}"
```

The preferred pattern is one renderer consuming state plus capability-driven optional actions/sections, with one canonical settings state controlling compatible behavior across providers.

## Regression checks

Before merging a provider/UI migration:

- canonical root navigation remains identical across providers;
- switching provider while Settings is open keeps the Settings route active;
- the same canonical `SettingsScreen` is used for all providers;
- global LiuMusic settings are not reset or provider-namespaced on source changes;
- provider-native ids survive mapping through the UI bridge;
- NetEase-only actions are not called with synthetic provider ids;
- unsupported actions are hidden/disabled rather than faked;
- playlist artwork shared-element transitions still work;
- Flowing Light / artwork background stays the same renderer;
- Liquid Glass modifiers and backdrop ownership are unchanged;
- MiniPlayer / Now Playing behavior remains provider-independent;
- unit tests and debug APK build pass.
