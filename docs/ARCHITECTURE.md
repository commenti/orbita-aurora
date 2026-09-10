# Orbita Aurora Browser — Architecture Contract

Package name: `com.orbita.aurora`  
Project codename: Orbita Aurora  
Platform: Android  
Primary browser engine: Mozilla GeckoView

## Purpose

Build a normal, usable Android web browser with tabs, navigation, copy/paste, downloads, device profiles, configurable viewport/resolution profiles, and temporary browser sessions.

## Important design boundary

A profile may make browser configuration consistent with a selected device class, but the project must **not** claim that a website will see a literally identical physical device. Browser engines, OS APIs, network characteristics, Client Hints, JavaScript APIs, and other signals can differ.

The system prefers internally consistent, predefined profiles over arbitrary random fingerprint generation.

## Architecture layers

- **Core** — small platform-independent models/interfaces and shared utilities.
- **Browser** — GeckoView integration, runtime lifecycle, browser sessions/tabs, page navigation, browser delegates.
- **Profile** — device-profile JSON loading, validation, profile selection, UA configuration, viewport/resolution configuration, profile persistence.
- **Session** — temporary-session lifecycle, session timer, session destruction, session-scoped browser data coordination.
- **Download** — download requests, Android storage integration, download status, download UI state.
- **Privacy** — browser-data clearing and session cleanup coordination. No promise of forensic deletion from the entire Android OS.
- **Settings** — advanced settings UI and actions for profile, resolution/viewport, session, and privacy controls.
- **UI** — main browser screen, address/search bar, tabs, settings screens, downloads screens, dialogs, reusable UI components.

## Data/configuration

`app/src/main/assets/device_profiles.json` is the source of predefined device profiles.

Profiles may contain only data that is technically meaningful and verifiable, including:

- id
- display name
- device class
- OS family/version label
- browser family/version label
- user-agent string where applicable
- client-hints metadata where the chosen engine supports it
- viewport width/height
- supported resolution presets
- device pixel ratio where applicable
- orientation defaults

Unsupported hardware identifiers must not be invented.

## Session model

A session is temporary browser state owned by the app. Session destruction coordinates:

1. closing/detaching session tabs
2. clearing session-scoped site data where supported
3. clearing app-managed cache/history for the session
4. deleting the session record
5. updating UI state

The implementation must distinguish app-managed data from data the Android OS or browser engine may retain outside the app's direct control.

## Resolution model

Resolution and viewport are distinct:

- **Physical display resolution** — the Android device's real screen.
- **Browser viewport** — the content area presented to web content.
- **Device pixel ratio** — CSS-pixel to device-pixel relationship.

The app may emulate/control web-content viewport characteristics only where GeckoView APIs and Android UI constraints permit. It must not pretend Android has physically changed into another monitor or phone.

Desktop and mobile presets are represented as profile data rather than hard-coded throughout UI code.

## Dependency rule

Lock the architecture and package structure before adding implementation code.
Prefer the smallest dependency set possible.
Use official Mozilla/Android APIs first.
Do not introduce a third-party library unless the agent explains why it is necessary.

## Build strategy

Use a compile-checked, incremental workflow:

`Foundation -> Browser engine -> Basic UI -> Profile -> Resolution/viewport -> Downloads -> Session -> Privacy cleanup -> Advanced settings -> polish`

After each phase, compile and fix only issues caused by that phase.

## File ownership rule

One logical responsibility per file. Avoid giant files.
Do not create extra files unless architecture requires them.

## First implementation target

The first runnable milestone is a minimal GeckoView browser that can open a URL and display a page. Advanced fingerprint/profile logic is not implemented before engine and basic navigation work.
