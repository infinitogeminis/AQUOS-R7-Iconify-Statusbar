# AQUOS R7 — Iconify Statusbar

Independent KernelSU module project adapting the Gradicon network/signal resources from Iconify for Sharp AQUOS R7 (A202SH), Android 14.

## Choose icon styles

[Open the visual gallery](docs/gallery/README.md): 16 Wi-Fi styles and 36 cellular signal styles from Iconify v7.3.0, including Gradicon. These are original-source preview shapes converted for comparison.

## Stage 1 — Gradicon network indicators

Port only the relevant Gradicon resources:
- Wi-Fi and cellular signal indicators
- Network traffic arrows / activity indicators
- Related connected, no-internet, and quick-settings signal states where the AQUOS resource map confirms them

Do not port unrelated icon families from the full Iconify pack. In particular, exclude battery resources and airplane/location icons.

## Stage 2 — iOS 16 horizontal battery

Begins only after Stage 1 is built, inspected, and validated on the device. It remains a separate implementation stage and release.

## Target environment
- Sharp AQUOS R7 (A202SH)
- Android 14, reported build 03.00.11
- KernelSU; integration target: Vector

## Isolation rules
This repository is independent of the user's other AQUOS R7 projects. It must not modify, replace, or depend on:
- IMS / VoLTE / VoWiFi icon module
- Colored Notifications module
- Battery Colors module

No SystemUI.apk patching/replacement, partition flashing, or bootloader unlocking. Prefer reversible systemless resource overlays.

## Development checklist
- [x] Repository created
- [x] Scope and isolation rules documented
- [x] Iconify v7.3.0 source revision pinned
- [x] Gradicon's two upstream overlay families identified
- [x] Visual gallery added: 16 Wi-Fi + 36 cellular styles
- [ ] Obtain AQUOS R7 target APK resource tables for exact compatibility mapping
- [ ] Implement the minimal Gradicon overlay set (network/signal only)
- [ ] Build and inspect KernelSU ZIP
- [ ] Validate on device
- [ ] Start Stage 2 only after Stage 1 passes

## Upstream
- Iconify v7.3.0: https://github.com/Mahmud0808/Iconify/releases/tag/v7.3.0
- Exact source commit: `ee491e884487959d364f07797c60adcd984ea948`

**Release policy:** an untested build must never be described as ready for installation.
