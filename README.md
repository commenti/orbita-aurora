# Orbita Aurora Browser

Android browser project using Mozilla GeckoView as the primary web engine.

Package: `com.orbita.aurora`

See `docs/ARCHITECTURE.md` for the architecture contract.
See `docs/AI_AGENT_PROMPT.txt` for the controlled one-file-at-a-time AI development workflow.

## Current milestone

Milestone 1 implements the smallest runnable GeckoView browser: a single tab/session, URL loading, address/search normalization, back, forward, and reload.

Advanced profile emulation, viewport controls, downloads, temporary sessions, privacy cleanup, and settings remain separately owned phases.

## Build

Requirements:

- Android Studio that supports AGP 9.4.x
- JDK 17+
- Android SDK Platform 37
- Network access to Google's Maven repository and Mozilla's Maven repository on the first dependency resolution

Open the project in Android Studio and run **Build > Build APK(s)**, or use a local Gradle 9.6+ installation from the project root:

```bash
gradle assembleDebug
```

APK output:
`app/build/outputs/apk/debug/app-debug.apk`

If Android Studio offers to generate/update the Gradle wrapper, accept its generated wrapper for the installed Android Studio toolchain. This environment does not bundle a wrapper JAR or Android SDK, so the source archive intentionally does not claim to contain a locally verified APK build.

The current GeckoView dependency is pinned to `org.mozilla.geckoview:geckoview-stable:156.0.20260823094236` rather than a dynamic version.

## GitHub Actions build and focused error logs

The repository includes `.github/workflows/android-build.yml`.

On a successful workflow run, the debug APK is published as a downloadable Actions artifact.
If the build fails, the workflow creates a separate `orbita-aurora-error-log-<run-number>` artifact containing a filtered error report rather than the full noisy Gradle log. The same focused report is also written to the GitHub Actions Job Summary for quick inspection.

The raw build log is intentionally not included in the error artifact. The extraction keeps Gradle failure markers, exceptions, dependency/AAPT/R8/manifest errors, failed-task context, and the final build tail so the important cause remains copyable and easy to inspect.
