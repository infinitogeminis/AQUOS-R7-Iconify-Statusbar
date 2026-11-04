# Gradicon source audit — Iconify v7.3.0

## Pinned upstream

- Repository: https://github.com/Mahmud0808/Iconify
- Release: https://github.com/Mahmud0808/Iconify/releases/tag/v7.3.0
- Exact tag commit: `ee491e884487959d364f07797c60adcd984ea948`

## How Iconify applies Gradicon

In v7.3.0, the Icon Pack screen lists Gradicon as the second pack. Its click handler calls:

`IconPackManager.enableOverlay(index = position + 1, "IPAS", "IPSUI")`

The manager enables two overlays together:
- `IconifyComponentIPAS2.overlay` — target package `android`
- `IconifyComponentIPSUI2.overlay` — target package `com.android.systemui`

This is the exact Gradicon pack path, rather than the separate Wi-Fi-only / cellular-only selector screens.

## Source asset roots

### Framework overlay
`app/src/main/assets/Overlays/android/IPAS2/res/`

Confirmed network/signal resources include:
- `drawable/ic_signal_cellular_0_4_bar.xml` through `ic_signal_cellular_4_4_bar.xml`
- `drawable/ic_signal_cellular_*_5_bar.xml` variants
- `drawable/ic_wifi_signal_0.xml` through `ic_wifi_signal_4.xml`
- `drawable/ic_wifi_4_signal_*.xml`, `ic_wifi_5_signal_*.xml`, and `ic_wifi_6_signal_*.xml`
- Wi-Fi transient animation drawables

The full source overlay also contains unrelated icons, including battery. Those must not be copied into this project.

### SystemUI overlay
`app/src/main/assets/Overlays/com.android.systemui/IPSUI2/res/`

Confirmed network-related families include:
- Cellular signal: `ic_mobile_0_4_bar.xml` through `ic_mobile_4_4_bar.xml`
- Wi-Fi signal: `ic_wifi_0.xml` through `ic_wifi_4.xml`
- Signal / Wi-Fi quick-settings states: `ic_qs_signal_*`, `ic_qs_wifi_*`
- No-internet / disabled Wi-Fi states
- Wi-Fi and mobile call-strength indicators
- Mobile network type indicators (LTE / 3G / 4G / 5G etc.)
- Network traffic: `stat_sys_network_traffic.xml`, `_down.xml`, `_down_arrow.xml`, `_up.xml`, `_up_arrow.xml`, `_updown.xml`
- Stacked traffic arrows: `stat_sys_signal_stacked_in.xml`, `_inout.xml`, `_none.xml`, `_out.xml`

The SystemUI overlay also contains many unrelated resources. The AQUOS port will use a deliberately reduced allow-list and exclude battery resources.

## Important implementation detail

The upstream pack is a pair of resource overlays targeting different packages: `android` and `com.android.systemui`. The AQUOS module must preserve that separation. It must not copy the full Iconify overlays wholesale.

## Device compatibility gate

The target is AQUOS R7 A202SH, Android 14, build 03.00.11. Before producing an installable ZIP, compare the exact resource names and overlayability in the device's own:
- `/system_ext/priv-app/SystemUI/SystemUI.apk`
- `/system/framework/framework-res.apk`

The exact device APK resource tables have not yet been supplied to the build workspace. Therefore no AQUOS-specific resource compatibility or installable ZIP is claimed yet.

## Isolation / safety

- Do not patch or replace SystemUI.apk or framework-res.apk.
- Do not flash partitions or unlock the bootloader.
- Do not include battery drawables in Stage 1.
- Do not touch the separate IMS, Colored Notifications, or Battery Colors projects.
- Do not label an untested ZIP as ready.
