package com.senliast.disablefingerprintwhenscreenoff;

import androidx.annotation.NonNull;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;

public class Main implements IXposedHookLoadPackage {

    private static final String listenPackage = "com.android.systemui";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;

        Class<?> KeyguardUpdateMonitorClass = findClass("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader);

        hookAllMethods(KeyguardUpdateMonitorClass,
                "shouldListenForFingerprint", new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if(!getBooleanField(param.thisObject, "mDeviceInteractive"))
                        {
                            param.setResult(false);
                        }
                    }
                });

        Class<?> LockIconViewControllerClass = findClass("com.android.keyguard.LockIconViewController", lpparam.classLoader);

        hookAllMethods(LockIconViewControllerClass,
                "inLockIconArea", new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(false);
                    }
                });

        hookAllMethods(LockIconViewControllerClass,
                "isActionable", new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(false);
                    }
                });

        hookAllMethods(LockIconViewControllerClass,
                "onInterceptTouchEvent", new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(false);
                    }
                });
    }
}
