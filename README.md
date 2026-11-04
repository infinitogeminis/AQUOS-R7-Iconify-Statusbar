# AQUOS R7 — Gradicon Statusbar

Independent Gradicon port for Sharp AQUOS R7 (A202SH), Android 14. The implementation is a **libxposed API 102 module for Vector v2.2**. It is delivered as an APK and must be installed/enabled through Vector.

## Current stage: Gradicon Wi-Fi

The module hooks `android.widget.ImageView.setImageResource(int)` inside `com.android.systemui`. It replaces only framework resources named `ic_wifi_signal_1` through `ic_wifi_signal_4`, and only on the status-bar ImageView whose entry name is `wifi_signal`.

- Wi-Fi level 0 remains stock.
- Traffic arrows remain stock.
- Cellular icons are not included yet.
- No SystemUI.apk or framework-res.apk is modified.
- No standalone RRO or KernelSU overlay ZIP is produced by this workflow.

## Install route

1. Build/download the APK artifact from GitHub Actions.
2. Install the APK as a module through Vector v2.2.
3. Enable the module for `com.android.systemui` (scope is also declared in `META-INF/xposed/scope.list`).
4. Activate it using Vector's module controls.

Do not install the earlier standalone RRO/KernelSU test artifact. It is a different implementation and is superseded by this Vector module.

## Build

GitHub Actions workflow: `.github/workflows/build-gradicon-wifi.yml`.

Artifact: `AQUOS-R7-Gradicon-WiFi-Vector-APK`.

The workflow checks that the APK contains libxposed API 102 metadata, the SystemUI scope, and all four Gradicon Wi-Fi drawables.

## Device validation still required

A successful APK build does not prove the hook matches the exact AQUOS SystemUI call path. The first device test must confirm the log tag `AQUOSR7Gradicon` reports hook initialization and resource replacement. If the stock icon remains, collect the module log before changing the hook.

## Isolation

This project is separate from:
- IMS / VoLTE / VoWiFi icons
- Colored Notifications
- Battery Colors

Stage 2 (Gradicon cellular signal and network traffic arrows) follows only after Wi-Fi is confirmed on the phone. The horizontal iOS 16 battery is a later, separate stage.

## Upstream

- Iconify v7.3.0 source commit: `ee491e884487959d364f07797c60adcd984ea948`
- Vector v2.2 uses libxposed API 102.
