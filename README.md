# AQUOS R7 — Gradicon Statusbar

Independent Gradicon port for Sharp AQUOS R7 (A202SH), Android 14. The implementation is a **libxposed API 102 module for Vector v2.2**. It is delivered as an APK and must be installed/enabled through Vector.

## Included

### Wi-Fi
- Replaces framework resources `ic_wifi_signal_1` through `ic_wifi_signal_4`.
- Targets the status-bar image view named `wifi_signal`.
- Wi-Fi level 0 remains stock.
- Confirmed working on the user's AQUOS R7.

### Cellular signal
- Includes the original Iconify Gradicon cellular vectors for levels 1–4.
- Replaces `ic_signal_cellular_1_4_bar` through `ic_signal_cellular_4_4_bar`.
- Targets the SystemUI image view named `mobile_signal`, found in the AQUOS R7 bugreport view hierarchy.
- Cellular level 0 and all unrelated resources remain stock.
- Cellular replacement is newly added and requires device validation.

## Install route

1. Install/update the APK as a module through Vector v2.2.
2. Keep the module scope on `com.android.systemui`.
3. Activate it using Vector's module controls.

No SystemUI.apk or framework-res.apk is modified. No standalone RRO or KernelSU overlay ZIP is produced by this workflow.

## Build

GitHub Actions workflow: `.github/workflows/build-gradicon-wifi.yml`.

Artifact: `AQUOS-R7-Gradicon-WiFi-Cellular-Vector-APK`.

The workflow checks libxposed API 102 metadata, SystemUI scope, and all eight Gradicon drawables.

## Logging

Log tag: `AQUOSR7Gradicon`. Successful replacements log the view name and source resource. If cellular signal remains stock, collect this tag from logcat before changing the hook.

## Isolation

This project is separate from:
- IMS / VoLTE / VoWiFi icons
- Colored Notifications
- Battery Colors

Network traffic arrows remain stock and are a separate future task. The horizontal iOS 16 battery is also a later, separate stage.

## Upstream

- Iconify v7.3.0 source commit: `ee491e884487959d364f07797c60adcd984ea948`
- Original Gradicon cellular vectors: Iconify commit `d09fecd5ae6da883367f6c3a3602e32dc26e9987`
- Vector v2.2 uses libxposed API 102.
