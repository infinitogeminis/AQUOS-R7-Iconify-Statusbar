# AQUOS R7 Android 14 resource audit — Gradicon

Device inputs retrieved from the user's Library folder:
`/AQUOS R7 Colored Notifications/Android 14 device files/`

| APK | Size | SHA-256 |
|---|---:|---|
| SystemUI.apk | 40,380,837 bytes | `11e5e4c29a8fa2a7dc7c1b3747afa02f3dcd7492cfe456b2c422df91df549627` |
| framework-res.apk | 35,383,675 bytes | `13a42b834e4005db3ff86ad5377d0e3112356b6f3bffb50337afe7d935ada0ae` |

## Verified resource-name comparison

### framework-res.apk — exact Gradicon drawable names exist

- Cellular: `ic_signal_cellular_0_4_bar` through `ic_signal_cellular_4_4_bar`
- Also present: `ic_signal_cellular_0_5_bar` through `ic_signal_cellular_5_5_bar`
- Wi-Fi: `ic_wifi_signal_0` through `ic_wifi_signal_4`
- Wi-Fi transient animation resources are present.

These names match the Iconify Gradicon preview/resource family. This is a strong candidate target for the selected signal glyphs.

### SystemUI.apk — names differ

Present:
- `ic_mobile_call_strength_0` through `ic_mobile_call_strength_4`
- `ic_no_internet_wifi_signal_0` through `ic_no_internet_wifi_signal_4`
- `ic_vowifi_tw`, `ic_vowifi_tw1`, `ic_vowifi_tw2`
- Quick Settings signal/Wi-Fi resources.

Not found in SystemUI APK under the expected names:
- `ic_mobile_0_4_bar` … `ic_mobile_4_4_bar`
- `ic_wifi_0` … `ic_wifi_4`
- Iconify traffic drawables `stat_sys_network_traffic*` and `stat_sys_signal_stacked_*`

Therefore do not blindly apply the AOSP/SystemUI resource mapping from Iconify to AQUOS. The status-bar cellular/Wi-Fi drawables appear to be framework-res resources on this build; runtime references and overlay policy still need confirmation.

## Next compatibility checks

1. Decode resource tables to identify resource IDs, types, configurations and overlayable policy for the exact framework drawables.
2. Inspect SystemUI resource references/layouts and runtime status-bar signal pipeline to confirm which framework resources are actually used.
3. Audit traffic indicator separately: no Iconify traffic drawable names were found in either APK's ordinary `res/` entries. Do not include traffic replacements until its real AQUOS resource owner and drawable names are identified.

## Safety/scope

- No APK has been modified.
- No installable ZIP has been built.
- No IMS/VoLTE/VoWiFi, Colored Notifications, or Battery Colors resources are in scope.
- Battery resources remain excluded.
