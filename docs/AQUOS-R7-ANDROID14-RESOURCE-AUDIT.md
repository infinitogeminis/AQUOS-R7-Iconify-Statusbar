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

The second archive-level check confirms that the matching compiled drawable files physically exist as:
- `res/drawable/ic_signal_cellular_0_4_bar.xml`
- `res/drawable/ic_signal_cellular_4_4_bar.xml`
- `res/drawable/ic_wifi_signal_0.xml`
- `res/drawable/ic_wifi_signal_4.xml`

These names match the Iconify Gradicon preview/resource family. This confirms resource-name and file-presence compatibility, not yet runtime usage or overlay eligibility.

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

A literal-string scan of SystemUI DEX/XML entries did not find the framework drawable names. This is inconclusive: framework resource references may be compiled as numeric resource IDs.

## Resource-table / overlayability test — not yet passed

- Both APKs contain uncompressed `resources.arsc` tables; the original file hashes were rechecked after materialization.
- A structural chunk scan found no `RES_TABLE_OVERLAYABLE` (0x0204) or `RES_TABLE_OVERLAYABLE_POLICY` (0x0205) chunks in either table.
- The container does not have `aapt2`, `aapt`, or `apktool`; installing Androguard was blocked by unavailable package-network access.
- A preliminary custom parser was not sufficient to resolve the target resource IDs/configurations. No IDs or overlay permissions are being guessed from it.

Therefore overlay eligibility is **unverified**. The existence of drawable XML files alone does not prove that Android 14 on this Sharp build will accept an RRO for them.

## Current decision

The framework-res drawables remain the leading candidate for Gradicon cellular/Wi-Fi, but no overlay has been generated or installed. Before building, the remaining mandatory checks are:
1. Decode the resource tables with a trusted Android resource-table tool and record exact IDs, type, configurations and any overlayable declarations.
2. Confirm through SystemUI resource references or runtime evidence that the status bar consumes these framework resources.
3. Audit traffic indicators separately. No Iconify traffic drawable names were found in either APK's ordinary `res/` entries. Do not include traffic replacements until the real AQUOS resource owner and names are identified.

## Safety/scope

- No APK has been modified.
- No installable ZIP has been built.
- No device-side test has been performed.
- No IMS/VoLTE/VoWiFi, Colored Notifications, or Battery Colors resources are in scope.
- Battery resources remain excluded.
