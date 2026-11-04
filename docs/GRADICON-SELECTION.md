# Gradicon selection lock — Stage 1

User selection confirmed: **Gradicon for both Wi-Fi and cellular signal**.

## Pinned source
- Iconify v7.3.0
- Commit: `ee491e884487959d364f07797c60adcd984ea948`
- Framework overlay source: `app/src/main/assets/Overlays/android/IPAS2/`
- SystemUI overlay source: `app/src/main/assets/Overlays/com.android.systemui/IPSUI2/`

## Initial allow-list candidates (subject to AQUOS resource-table verification)

### Framework target: `android`
- `ic_signal_cellular_0_4_bar` … `ic_signal_cellular_4_4_bar`
- `ic_signal_cellular_0_5_bar` … `ic_signal_cellular_5_5_bar`
- `ic_wifi_signal_0` … `ic_wifi_signal_4`
- `ic_wifi_4_signal_*`, `ic_wifi_5_signal_*`, `ic_wifi_6_signal_*`
- Wi-Fi transient animation resources, only if the device target table confirms the matching names.

### SystemUI target: `com.android.systemui`
- Cellular signal: `ic_mobile_0_4_bar` … `ic_mobile_4_4_bar`
- Wi-Fi signal: `ic_wifi_0` … `ic_wifi_4`
- Quick Settings Wi-Fi / signal states and no-internet states, only where AQUOS resource names match.
- Network traffic: `stat_sys_network_traffic`, `_down`, `_down_arrow`, `_up`, `_up_arrow`, `_updown`
- Stacked traffic: `stat_sys_signal_stacked_in`, `_inout`, `_none`, `_out`

## Explicit exclusions
- Every battery resource.
- Airplane, location, Bluetooth, volume, and all other unrelated icon families.
- No changes to IMS, Colored Notifications, or Battery Colors projects.

## Compatibility gate — not yet passed
The source list is not an AQUOS resource map. Before building an installable overlay, inspect the original device APK tables and overlayability:
- `/system_ext/priv-app/SystemUI/SystemUI.apk`
- `/system/framework/framework-res.apk`

No APKs are currently available in the project workspace. Therefore no installable ZIP is produced at this stage. The two APKs are build inputs only and must not be committed to this public repository.
