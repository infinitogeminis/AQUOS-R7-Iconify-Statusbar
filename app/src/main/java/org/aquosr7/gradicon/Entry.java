package org.aquosr7.gradicon;

import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

public final class Entry extends XposedModule {
    private static final String TAG = "AQUOSR7Gradicon";
    private static final String SYSTEM_UI = "com.android.systemui";
    private static final String WIFI_VIEW_ID = "wifi_signal";
    private static final String CELLULAR_VIEW_ID = "mobile_signal";
    private static final String MOBILE_VIEW_CLASS_SUFFIX = ".StatusBarMobileView";

    private final Map<Drawable, ImageView> cellularViews =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<Drawable, ColorStateList> cellularTints =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final Set<Class<?>> tintHooks =
            Collections.newSetFromMap(new WeakHashMap<>());
    private Resources moduleResources;

    @Override
    public void onPackageReady(PackageReadyParam param) {
        if (!SYSTEM_UI.equals(param.getPackageName())) return;
        try {
            moduleResources = createModuleResources();
            installWifiHook();
            installCellularHooks();
            Log.i(TAG, "Initialized: Wi-Fi image hook + SignalDrawable cellular hooks");
        } catch (Throwable t) {
            Log.e(TAG, "Initialization failed; stock status icons remain active", t);
        }
    }

    private void installWifiHook() throws Exception {
        Method method = ImageView.class.getDeclaredMethod("setImageResource", int.class);
        method.setAccessible(true);
        hook(method)
                .setId("aquosr7-gradicon-wifi-image-resource")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    Object receiver = chain.getThisObject();
                    if (!(receiver instanceof ImageView)) return chain.proceed();
                    ImageView image = (ImageView) receiver;
                    int sourceId = (Integer) chain.getArg(0);
                    int replacement = getWifiReplacement(image, sourceId);
                    if (replacement == 0) return chain.proceed();
                    Drawable drawable = moduleResources.getDrawable(replacement);
                    image.setImageDrawable(drawable.mutate());
                    Log.i(TAG, "Applied Gradicon Wi-Fi view=" + viewName(image));
                    return null;
                });
        Log.i(TAG, "Wi-Fi hook installed");
    }

    private void installCellularHooks() throws Exception {
        Method setImageDrawable = ImageView.class.getDeclaredMethod(
                "setImageDrawable", Drawable.class);
        setImageDrawable.setAccessible(true);
        hook(setImageDrawable)
                .setId("aquosr7-gradicon-cellular-image")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    Object result = chain.proceed();
                    Object receiver = chain.getThisObject();
                    Object argument = chain.getArg(0);
                    if (receiver instanceof ImageView && argument instanceof Drawable) {
                        ImageView image = (ImageView) receiver;
                        Drawable drawable = (Drawable) argument;
                        if (isStatusBarMobileSignal(image) && isSignalDrawable(drawable)) {
                            cellularViews.put(drawable, image);
                            installTintHook(drawable.getClass());
                            applyCellularDrawable(drawable, image);
                            Log.i(TAG, "Bound SignalDrawable to status-bar mobile_signal");
                        }
                    }
                    return result;
                });

        Method setLevel = Drawable.class.getDeclaredMethod("setLevel", int.class);
        setLevel.setAccessible(true);
        hook(setLevel)
                .setId("aquosr7-gradicon-cellular-level")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    Object result = chain.proceed();
                    Object receiver = chain.getThisObject();
                    if (receiver instanceof Drawable) {
                        Drawable drawable = (Drawable) receiver;
                        ImageView image = cellularViews.get(drawable);
                        if (image != null) applyCellularDrawable(drawable, image);
                    }
                    return result;
                });
        Log.i(TAG, "Cellular hooks installed: ImageView binding + Drawable.setLevel");
    }

    private void installTintHook(Class<?> drawableClass) {
        synchronized (tintHooks) {
            if (tintHooks.contains(drawableClass)) return;
            try {
                Method method = drawableClass.getMethod("setTintList", ColorStateList.class);
                method.setAccessible(true);
                hook(method)
                        .setId("aquosr7-gradicon-cellular-tint-" + drawableClass.getName())
                        .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                        .intercept(chain -> {
                            Object result = chain.proceed();
                            Object receiver = chain.getThisObject();
                            Object argument = chain.getArg(0);
                            if (receiver instanceof Drawable && argument instanceof ColorStateList) {
                                Drawable original = (Drawable) receiver;
                                ColorStateList tint = (ColorStateList) argument;
                                cellularTints.put(original, tint);
                                ImageView image = cellularViews.get(original);
                                if (image != null) {
                                    Drawable current = image.getDrawable();
                                    if (current != null && current != original) {
                                        current.setTintList(tint);
                                    }
                                }
                            }
                            return result;
                        });
                tintHooks.add(drawableClass);
                Log.i(TAG, "SignalDrawable tint hook installed: " + drawableClass.getName());
            } catch (Throwable t) {
                Log.w(TAG, "Could not hook SignalDrawable tint method: "
                        + drawableClass.getName(), t);
            }
        }
    }

    private void applyCellularDrawable(Drawable signalDrawable, ImageView image) {
        if (moduleResources == null || image == null) return;
        try {
            int packedState = signalDrawable.getLevel();
            int state = (packedState >>> 16) & 0xff;
            if (state != 0) {
                if (image.getDrawable() != signalDrawable) image.setImageDrawable(signalDrawable);
                return;
            }

            int bars = packedState & 0xff;
            if (bars <= 0) {
                if (image.getDrawable() != signalDrawable) image.setImageDrawable(signalDrawable);
                Log.i(TAG, "Keeping stock no-signal drawable; packedLevel=0x"
                        + Integer.toHexString(packedState));
                return;
            }
            if (bars > 4) bars = 4;

            int replacement = getCellularReplacement(bars);
            Drawable custom = moduleResources.getDrawable(replacement).mutate();
            ColorStateList tint = cellularTints.get(signalDrawable);
            if (tint == null) {
                try { tint = signalDrawable.getTintList(); } catch (Throwable ignored) { }
            }
            if (tint != null) custom.setTintList(tint);
            image.setImageDrawable(custom);
            Log.i(TAG, "Applied Gradicon cellular bars=" + bars
                    + " packedLevel=0x" + Integer.toHexString(packedState));
        } catch (Throwable t) {
            Log.w(TAG, "Cellular Gradicon update failed; restoring stock drawable", t);
            try {
                if (image.getDrawable() != signalDrawable) image.setImageDrawable(signalDrawable);
            } catch (Throwable ignored) { }
        }
    }

    private int getCellularReplacement(int bars) {
        switch (bars) {
            case 1: return R.drawable.gradicon_cellular_1_4_bar;
            case 2: return R.drawable.gradicon_cellular_2_4_bar;
            case 3: return R.drawable.gradicon_cellular_3_4_bar;
            default: return R.drawable.gradicon_cellular_4_4_bar;
        }
    }

    private boolean isStatusBarMobileSignal(ImageView image) {
        if (image.getId() == View.NO_ID) return false;
        try {
            if (!CELLULAR_VIEW_ID.equals(
                    image.getResources().getResourceEntryName(image.getId()))) return false;
            ViewParent parent = image.getParent();
            while (parent != null) {
                if (parent.getClass().getName().endsWith(MOBILE_VIEW_CLASS_SUFFIX)) return true;
                if (parent instanceof View) parent = ((View) parent).getParent();
                else break;
            }
        } catch (Throwable ignored) { }
        return false;
    }

    private boolean isSignalDrawable(Drawable drawable) {
        return drawable.getClass().getName().endsWith(".SignalDrawable");
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

    private int getWifiReplacement(ImageView image, int sourceId) {
        if (moduleResources == null || image.getId() == View.NO_ID) return 0;
        try {
            String view = image.getResources().getResourceEntryName(image.getId());
            String name = image.getResources().getResourceEntryName(sourceId);
            if (!WIFI_VIEW_ID.equals(view)) return 0;
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

    private String viewName(ImageView image) {
        try {
            return image.getResources().getResourceEntryName(image.getId());
        } catch (Throwable ignored) {
            return "unknown";
        }
    }
}
