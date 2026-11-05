package org.aquosr7.gradicon;

import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
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

    private final Map<Drawable, WeakReference<ImageView>> cellularViews =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<Drawable, ColorStateList> cellularTints =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<ImageView, Drawable> stockCellularDrawables =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final Set<Class<?>> tintHooks =
            Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<Class<?>> signalLevelHooks =
            Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<String> loggedCellularDrawableClasses =
            Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<String, Boolean>());
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
                .setId("aquosr7-gradicon-image-resource-wifi-cellular")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    Object receiver = chain.getThisObject();
                    if (!(receiver instanceof ImageView)) return chain.proceed();
                    ImageView image = (ImageView) receiver;
                    int sourceId = (Integer) chain.getArg(0);
                    int replacement = getWifiReplacement(image, sourceId);
                    if (replacement != 0) {
                        image.setImageDrawable(moduleResources.getDrawable(replacement).mutate());
                        Log.i(TAG, "Applied Gradicon Wi-Fi view=" + viewName(image));
                        return null;
                    }
                    int cellularReplacement = getCellularResourceReplacement(image, sourceId);
                    if (cellularReplacement != 0) {
                        String sourceName = image.getResources().getResourceEntryName(sourceId);
                        image.setImageDrawable(moduleResources.getDrawable(cellularReplacement).mutate());
                        Log.i(TAG, "Applied Gradicon cellular resource view=mobile_signal source=" + sourceName);
                        return null;
                    }
                    return chain.proceed();
                });
        Log.i(TAG, "ImageView.setImageResource hook installed for Wi-Fi + cellular resources");
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
                        if (isStatusBarMobileSignal(image)) {
                            String drawableClass = drawable.getClass().getName();
                            if (loggedCellularDrawableClasses.add(drawableClass)) {
                                Log.i(TAG, "mobile_signal received drawable class=" + drawableClass);
                            }
                        }
                        if (isStatusBarMobileSignal(image) && isSignalDrawable(drawable)) {
                            cellularViews.put(drawable, new WeakReference<>(image));
                            stockCellularDrawables.put(image, drawable);
                            installTintHook(drawable.getClass());
                            installSignalLevelHook(drawable.getClass());
                            applyCellularDrawable(drawable, image);
                            Log.i(TAG, "Bound SignalDrawable to status-bar mobile_signal");
                        }
                    }
                    return result;
                });

        Method setImageLevel = ImageView.class.getDeclaredMethod("setImageLevel", int.class);
        setImageLevel.setAccessible(true);
        hook(setImageLevel)
                .setId("aquosr7-gradicon-cellular-image-level")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    Object result = chain.proceed();
                    Object receiver = chain.getThisObject();
                    if (receiver instanceof ImageView) {
                        ImageView image = (ImageView) receiver;
                        if (isStatusBarMobileSignal(image)) {
                            Drawable stock = stockCellularDrawables.get(image);
                            if (stock != null) {
                                int packedState = (Integer) chain.getArg(0);
                                applyCellularDrawable(stock, image, packedState);
                                Log.i(TAG, "Updated mobile_signal from ImageView.setImageLevel packedLevel=0x"
                                        + Integer.toHexString(packedState));
                            }
                        }
                    }
                    return result;
                });
        try {
            Class<?> signalClass = Class.forName(
                    "com.android.settingslib.graph.SignalDrawable", false,
                    Entry.class.getClassLoader());
            installTintHook(signalClass);
        } catch (Throwable t) {
            Log.w(TAG, "Could not preload AOSP SignalDrawable tint hook", t);
        }
        Log.i(TAG, "Cellular hooks installed: SignalDrawable.onLevelChange + ImageView.setImageLevel");
    }

    private void installSignalLevelHook(Class<?> drawableClass) {
        synchronized (signalLevelHooks) {
            if (signalLevelHooks.contains(drawableClass)) return;
            try {
                Method method = drawableClass.getDeclaredMethod("onLevelChange", int.class);
                method.setAccessible(true);
                hook(method)
                        .setId("aquosr7-gradicon-signal-on-level-change-" + drawableClass.getName())
                        .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                        .intercept(chain -> {
                            Object result = chain.proceed();
                            Object receiver = chain.getThisObject();
                            if (receiver instanceof Drawable) {
                                Drawable signal = (Drawable) receiver;
                                ImageView image = getCellularView(signal);
                                if (image != null) {
                                    int packedState = (Integer) chain.getArg(0);
                                    applyCellularDrawable(signal, image, packedState);
                                    Log.i(TAG, "SignalDrawable.onLevelChange packedLevel=0x"
                                            + Integer.toHexString(packedState));
                                }
                            }
                            return result;
                        });
                signalLevelHooks.add(drawableClass);
                Log.i(TAG, "SignalDrawable.onLevelChange hook installed: "
                        + drawableClass.getName());
            } catch (Throwable t) {
                Log.e(TAG, "Could not hook SignalDrawable.onLevelChange: "
                        + drawableClass.getName(), t);
            }
        }
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
                            if (receiver instanceof Drawable) {
                                Drawable original = (Drawable) receiver;
                                ColorStateList tint = argument instanceof ColorStateList
                                        ? (ColorStateList) argument : null;
                                if (tint == null) cellularTints.remove(original);
                                else cellularTints.put(original, tint);
                                ImageView image = getCellularView(original);
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
        if (signalDrawable == null) return;
        applyCellularDrawable(signalDrawable, image, signalDrawable.getLevel());
    }

    private void applyCellularDrawable(Drawable signalDrawable, ImageView image, int packedState) {
        if (moduleResources == null || image == null) return;
        try {
            int state = (packedState >>> 16) & 0xff;
            if (state != 0) {
                if (image.getDrawable() != signalDrawable) image.setImageDrawable(signalDrawable);
                return;
            }

            // SignalDrawable stores the live bar level in the low byte; bits 8..15 are numLevels.
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
            ColorStateList tint = getSignalTint(signalDrawable);
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

    private ImageView getCellularView(Drawable signalDrawable) {
        WeakReference<ImageView> reference = cellularViews.get(signalDrawable);
        return reference == null ? null : reference.get();
    }

    private ColorStateList getSignalTint(Drawable signalDrawable) {
        ColorStateList tint = cellularTints.get(signalDrawable);
        if (tint != null) return tint;
        Class<?> type = signalDrawable.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField("mForegroundPaint");
                field.setAccessible(true);
                Object value = field.get(signalDrawable);
                if (value instanceof Paint) {
                    return ColorStateList.valueOf(((Paint) value).getColor());
                }
            } catch (Throwable ignored) { }
            type = type.getSuperclass();
        }
        Log.w(TAG, "SignalDrawable tint unavailable; using vector's source colors");
        return null;
    }

    private int getCellularResourceReplacement(ImageView image, int sourceId) {
        if (moduleResources == null || !isStatusBarMobileSignal(image)) return 0;
        try {
            String name = image.getResources().getResourceEntryName(sourceId);
            Log.i(TAG, "mobile_signal setImageResource source=" + name);
            switch (name) {
                case "ic_signal_cellular_1_4_bar": return R.drawable.gradicon_cellular_1_4_bar;
                case "ic_signal_cellular_2_4_bar": return R.drawable.gradicon_cellular_2_4_bar;
                case "ic_signal_cellular_3_4_bar": return R.drawable.gradicon_cellular_3_4_bar;
                case "ic_signal_cellular_4_4_bar": return R.drawable.gradicon_cellular_4_4_bar;
                default: return 0;
            }
        } catch (Throwable ignored) {
            return 0;
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
            return CELLULAR_VIEW_ID.equals(
                    image.getResources().getResourceEntryName(image.getId()));
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean isSignalDrawable(Drawable drawable) {
        return drawable.getClass().getName().endsWith("SignalDrawable");
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
