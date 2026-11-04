# Gradicon source audit

## Verified upstream reference

- Repository: https://github.com/Mahmud0808/Iconify
- Inspected source revision: `d09fecd5ae6da883367f6c3a3602e32dc26e9987`
- This is an inspected commit, not yet asserted to be the user's installed Iconify v7.3.0 tag. Exact tag equivalence still needs confirmation.

## Confirmed Gradicon entries

### Wi-Fi signal
Source file:
`app/src/main/java/com/drdisagree/iconify/features/home/wifiicons/screens/WifiIconScreen.kt`

Gradicon preview resource identifiers:
- `preview_gradicon_ic_wifi_signal_1`
- `preview_gradicon_ic_wifi_signal_2`
- `preview_gradicon_ic_wifi_signal_3`
- `preview_gradicon_ic_wifi_signal_4`

### Cellular signal
Source file:
`app/src/main/java/com/drdisagree/iconify/features/home/cellularicons/screens/CellularIconScreen.kt`

Gradicon preview resource identifiers:
- `preview_gradicon_ic_signal_cellular_1_4_bar`
- `preview_gradicon_ic_signal_cellular_2_4_bar`
- `preview_gradicon_ic_signal_cellular_3_4_bar`
- `preview_gradicon_ic_signal_cellular_4_4_bar`

The two source screens establish that Gradicon is offered as a selectable Wi-Fi and cellular signal style. Preview resource names are not proof of the actual overlay drawable paths.

## Still to verify before implementation

- Exact overlay asset paths and the full contents of the IPAS2 overlay package.
- How the upstream overlay is installed/applied and which framework/SystemUI resources it replaces.
- Whether the network traffic indicators are included in IPAS2, or are a separate Iconify customization.
- Exact source tag corresponding to the user's Iconify v7.3.0.
- AQUOS R7 resource names, drawable formats, dimensions, and compatibility.

## Safety gate

Do not build an installable module from preview identifiers alone. First obtain the actual overlay assets and map every target resource against the AQUOS R7 firmware. Do not modify SystemUI.apk or unrelated modules.
