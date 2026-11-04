# AQUOS R7 resource-table handoff

To compile and validate the two Gradicon RRO overlays against the exact device firmware, the build workspace needs the original target APKs. They are used only as build inputs; they will not be modified or included in the public repository.

From a root shell on the AQUOS R7, copy:

```sh
cp /system_ext/priv-app/SystemUI/SystemUI.apk /sdcard/Download/AQUOS-R7-SystemUI.apk
cp /system/framework/framework-res.apk /sdcard/Download/AQUOS-R7-framework-res.apk
```

Then attach both copied files to the project chat.

Expected targets:
- `SystemUI.apk` → overlay target package `com.android.systemui`
- `framework-res.apk` → overlay target package `android`

If either source path does not exist, do not guess another path or rename an unrelated APK. Report the exact error/path and inspect the installed package location first.

The APKs must remain outside this public GitHub repository. Do not commit them.
