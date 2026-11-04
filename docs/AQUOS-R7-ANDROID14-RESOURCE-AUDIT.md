# AQUOS R7 Android 14 resource audit — Gradicon

Device inputs retrieved from the user's Library folder:
`/AQUOS R7 Colored Notifications/Android 14 device files/`

| APK | Size | SHA-256 |
|---|---:|---|
| SystemUI.apk | 40,380,837 bytes | `11e5e4c29a8fa2a7dc7c1b3747afa02f3dcd7492cfe456b2c422df91df549627` |
| framework-res.apk | 35,383,675 bytes | `13a42b834e4005db3ff86ad5377d0e3112356b6f3bffb50337afe7d935ada0ae` |

## Iconify source confirmation

Pinned Iconify source: v7.3.0, commit `ee491e884487959d364f07797c60adcd984ea948`.

The selected Gradicon source XMLs are present in `app/src/main/res/drawable-v24/`:
- `preview_gradicon_ic_signal_cellular_1_4_bar.xml` through `_4_4_bar.xml`
- `preview_gradicon_ic_wifi_signal_1.xml` through `_4.xml`

The source files are 24dp vector drawables using AAPT gradient attributes. They can be used as source artwork for an overlay resource only after the target mapping and overlay policy are validated. Preview drawable names are not themselves target resource names.

## Exact AQUOS framework resource IDs — parsed

A custom parser for the Android resource-table sparse type-entry format successfully resolved these names in the `android` package of `framework-res.apk`:

| Resource | Resource ID | Type |
|---|---:|---|
| `ic_signal_cellular_0_4_bar` | `0x01080557` | drawable |
| `ic_signal_cellular_1_4_bar` | `0x01080559` | drawable |
| `ic_signal_cellular_2_4_bar` | `0x0108055b` | drawable |
| `ic_signal_cellular_3_4_bar` | `0x0108055d` | drawable |
| `ic_signal_cellular_4_4_bar` | `0x0108055f` | drawable |
| `ic_wifi_signal_0` | `0x01080589` | drawable |
| `ic_wifi_signal_1` | `0x0108058a` | drawable |
| `ic_wifi_signal_2` | `0x0108058b` | drawable |
| `ic_wifi_signal_3` | `0x0108058c` | drawable |
| `ic_wifi_signal_4` | `0x0108058d` | drawable |

All target entries are in the default configuration (the parsed ResTable_config has only the standard size field set). Their compiled XML resource files are present in the APK.

## Runtime reference check

A literal little-endian resource-ID scan of `SystemUI.apk/classes.dex` found direct references to:
- `ic_wifi_signal_0` — 9 occurrences
- `ic_wifi_signal_1` — 2 occurrences
- `ic_wifi_signal_4` — 2 occurrences

This is strong evidence that SystemUI consumes the AQUOS framework Wi-Fi signal resource family. It does not yet identify every code path or establish that all five levels are used in the status bar.

No direct DEX references to the five `ic_signal_cellular_*_4_bar` IDs were found in SystemUI. The SystemUI APK has its own `ic_mobile_call_strength_0..4` resources. Therefore the cellular framework resources are not yet confirmed as the status-bar cellular source. Do not replace them until the cellular pipeline is traced further.

## Overlay policy — AOSP rule confirmed; Sharp mount path still unverified

The absence of `RES_TABLE_OVERLAYABLE` chunks is not, by itself, a hard blocker.

AOSP Android 14 `idmap2` checks the target package as follows:
- If the target APK defines no overlayable groups at all, a resource may be overlaid when the RRO fulfills a default partition/signature policy.
- The current AOSP implementation includes `PRODUCT_PARTITION` and `SYSTEM_PARTITION` among those default policies.
- If the target APK does define overlayable groups, a resource omitted from those groups cannot be overlaid by an ordinary RRO.

Sources:
- AOSP Android 14 idmap2 implementation: https://android.googlesource.com/platform/frameworks/base/+/f2d4156f5b0edb346347b22c963b7df24fa14096/cmds/idmap2/libidmap2/ResourceMapping.cpp
- Android RRO documentation: https://source.android.com/docs/core/runtime/rros

KernelSU's module layout supports a `system/product/` subtree, and its metamodule provides the systemless mounting layer. This makes a product-partition RRO a plausible implementation without modifying either target APK. It is not yet proven on this AQUOS build: the actual mounted path, SELinux labeling, OverlayConfig discovery, and idmap policy reported by the device must be verified.

Important distinction:
- An APK merely stored under `/data/adb/modules` is not automatically a preinstalled product overlay.
- The RRO must actually be exposed to Android as a product-partition overlay (or otherwise fulfill the target's policy).
- Android 11+ devices can use `/product/overlay/config/config.xml` to configure overlays. Existing Sharp overlay configuration and partition layout must be inspected before choosing the exact module layout.

No test overlay has been installed on the phone. No installable ZIP has been built.

## Current implementation decision

Use a small standalone RRO APK, delivered through a KernelSU module, rather than modifying `SystemUI.apk` or `framework-res.apk`. First validate a single Wi-Fi resource in a controlled Android 14 environment and inspect the generated idmap. Only after that succeeds should the full Wi-Fi family be packaged. Cellular remains a separate mapping task because the SystemUI APK contains its own `ic_mobile_call_strength_*` family.

## Traffic icons

No Iconify traffic drawable names `stat_sys_network_traffic*` or `stat_sys_signal_stacked_*` were found in the ordinary `res/` entries of either APK. Traffic indicators remain out of scope until their real AQUOS resource owner and resource names are identified.

## Decision and next tests

1. **Wi-Fi:** framework resource IDs are confirmed and SystemUI DEX references them. Next test is whether Android 14 on this Sharp build permits an overlay of these `android` resources without an overlayable declaration. Do not yet install a module.
2. **Cellular:** IDs are confirmed, but SystemUI's direct references point to a separate `ic_mobile_call_strength_*` family. Trace the active status-bar cellular pipeline before selecting replacement targets.
3. **Traffic:** separate audit required.

## Safety/scope

- No APK has been modified.
- No installable ZIP has been built or installed.
- No physical-device test has been performed.
- No IMS/VoLTE/VoWiFi, Colored Notifications, or Battery Colors resources are in scope.
- Battery resources remain excluded.
