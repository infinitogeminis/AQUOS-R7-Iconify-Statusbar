# AQUOS R7 — Iconify Statusbar

Independent KernelSU module project adapting selected Iconify status-bar icon resources for the Sharp AQUOS R7 (A202SH), Android 14.

## Scope and order

### Stage 1 — Gradicon
- Wi-Fi signal icons
- Cellular signal icons
- Network traffic indicators

### Stage 2 — iOS 16 horizontal battery
Begins only after Stage 1 is built, inspected, and validated on the device. Battery work remains a separate stage and release.

## Target environment
- Sharp AQUOS R7 (A202SH)
- Android 14, reported build 03.00.11
- KernelSU; integration target: Vector

## Isolation rules
This repository is independent of the user's other AQUOS R7 projects. It must not modify, replace, or depend on:
- IMS / VoLTE / VoWiFi icon module
- Colored Notifications module
- Battery Colors module

No SystemUI.apk patching/replacement, partition flashing, or bootloader unlocking. Prefer a reversible KernelSU module and resource-overlay approach where supported by the device.

## Development checklist
- [x] Repository created
- [x] Project scope and isolation rules documented
- [ ] Audit exact Gradicon source assets and application mechanism in Iconify upstream
- [ ] Map source resources to AQUOS R7 SystemUI resources
- [ ] Implement Stage 1 module
- [ ] Build and inspect installable ZIP
- [ ] Validate on device
- [ ] Start Stage 2 only after Stage 1 passes

## Upstream
- Iconify: https://github.com/Mahmud0808/Iconify
- The exact upstream tag/commit and resource mapping must be recorded before implementation.

**Release policy:** an untested build must never be described as ready for installation.
