# Orbita Aurora — Architecture Verification Review

## Result

The proposed architecture is internally coherent for a GeckoView-based Android browser and is suitable for incremental implementation.

## Verified decisions

1. **GeckoView lifecycle**: `GeckoRuntime` is process-scoped and created once; individual `GeckoSession` instances represent browser tabs/sessions. This matches Mozilla's embedding model.
2. **Layer separation**: Core, Browser, Profile, Session, Download, Privacy, Settings, and UI have separate ownership boundaries.
3. **Profile integrity**: profiles are configuration presets, not claims of physical-device identity. Unsupported hardware identifiers are excluded.
4. **Viewport vs physical display**: viewport/resolution settings are treated as web-content configuration; they do not claim to alter Android's real display.
5. **Session cleanup**: cleanup distinguishes app-managed data from data that may remain outside direct application control.
6. **Dependency discipline**: only the official GeckoView artifact is required for the first milestone; no third-party browser library is introduced.
7. **Incremental build contract**: engine and basic navigation precede advanced fingerprint/profile logic.

## Build-tool decision

Current Android tooling supports AGP 9.4.0 with Gradle 9.6.0, and JDK 17 is the supported Java baseline. The project therefore targets AGP 9.4.0, Gradle 9.6+, Java 17, compileSdk 37.

## GeckoView decision

The project pins a stable GeckoView release rather than using a dynamic version:

`org.mozilla.geckoview:geckoview-stable:156.0.20260823094236`

## First milestone scope

The first runnable milestone intentionally implements:

- process-wide GeckoRuntime creation
- one GeckoSession attached to GeckoView
- HTTPS page loading
- address/search normalization
- back/forward/reload
- URL/location updates

The advanced layers remain isolated and are not falsely presented as complete before their actual GeckoView APIs are integrated.
