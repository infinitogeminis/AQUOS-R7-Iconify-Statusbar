package org.aquosr7.gradicon;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

/**
 * AQUOS R7 Gradicon Wi-Fi module.
 *
 * Replaces only framework ic_wifi_signal_1..4 when SystemUI applies them
 * to the status-bar view whose resource entry name is wifi_signal.
 * Level 0 and every other ImageView/resource are left untouched.
 */
public final class Entry extends XposedModule {
    private static final String TAG = "AQUOSR7Gradicon";
    private static final String SYSTEM_UI = "com.android.systemui";
    private static final String WIFI_VIEW_ID = "wifi_signal";

    private Resources moduleResources;

    @Override
    public void onPackageReady(PackageReadyParam param) {
        if (!SYSTEM_UI.equals(param.getPackageName())) return;

        try {
            moduleResources = createModuleResources();
            Method method = ImageView.class.getDeclaredMethod("setImageResource", int.class);
            method.setAccessible(true);

            hook(method)
                    .setId("aquosr7-gradicon-wifi-image-resource")
                    .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        Object receiver = chain.getThisObject();
                        if (!(receiver instanceof ImageView)) return chain.proceed();

                        ImageView image = (ImageView) receiver;
                        int frameworkResId = (Integer) chain.getArg(0);
                        int replacement = getReplacement(image, frameworkResId);
                        if (replacement == 0) return chain.proceed();

                        Drawable drawable = moduleResources.getDrawable(replacement);
                        image.setImageDrawable(drawable.mutate());
                        Log.i(TAG, "Applied Gradicon Wi-Fi resource=" + resourceName(image, frameworkResId));
                        return null;
                    });

            Log.i(TAG, "Hook installed for SystemUI; module resources loaded");
        } catch (Throwable t) {
            Log.e(TAG, "Initialization failed; stock Wi-Fi icons remain active", t);
        }
    }

    private Resources createModuleResources() throws Exception {
        String apkPath = getModuleApplicationInfo().sourceDir;
        AssetManager assets = AssetManager.class.getDeclaredConstructor().newInstance();
        Method addAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
        addAssetPath.setAccessible(true);
        Object cookie = addAssetPath.invoke(assets, apkPath);
        if (!(cookie instanceof Integer) || (Integer) cookie == 0) {
            throw new IllegalStateException("AssetManager rejected module APK: " + apkPath);
        }
        Resources systemResources = Resources.getSystem();
        return new Resources(assets, systemResources.getDisplayMetrics(),
                systemResources.getConfiguration());
    }

    private int getReplacement(ImageView image, int frameworkResId) {
        if (moduleResources == null || image.getId() == View.NO_ID) return 0;

        try {
            String viewName = image.getResources().getResourceEntryName(image.getId());
            if (!WIFI_VIEW_ID.equals(viewName)) return 0;

            String name = image.getResources().getResourceEntryName(frameworkResId);
            switch (name) {
                case "ic_wifi_signal_1": return R.drawable.gradicon_wifi_1;
                case "ic_wifi_signal_2": return R.drawable.gradicon_wifi_2;
                case "ic_wifi_signal_3": return R.drawable.gradicon_wifi_3;
                case "ic_wifi_signal_4": return R.drawable.gradicon_wifi_4;
                default: return 0;
            }
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private String resourceName(ImageView image, int id) {
        try {
            return image.getResources().getResourceName(id);
        } catch (Throwable ignored) {
            return "0x" + Integer.toHexString(id);
        }
    }
}
