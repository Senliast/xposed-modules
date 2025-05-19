package com.senliast.updatesmanagerextended;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class XposedMain implements IXposedHookLoadPackage {

    private static final String listenPackage = "android";
    public String appAboutToUpdate = "";
    public String appsToBlockUpdates = "";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;

        Class<?> PackageInstallerSessionClass = findClass("com.android.server.pm.PackageInstallerSession", lpparam.classLoader);

        XposedHelpers.findAndHookMethod(
            PackageInstallerSessionClass,
            "assertPackageConsistentLocked",
            String.class,
            String.class,
            long.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XSharedPreferences preferences = new XSharedPreferences("com.senliast.updatesmanagerextended", "Preferences");

                    if (preferences.getBoolean("moduleEnabled", false)) {
                        appsToBlockUpdates = preferences.getString("appsToBlockUpdates", "");

                        if (!appsToBlockUpdates.equals("")) {
                            appAboutToUpdate = String.valueOf(getObjectField(param.thisObject, "mPackageName"));
                            if (appsToBlockUpdates.contains(appAboutToUpdate)) {
                                param.args[1] = "";
                            }
                        }
                    }
                }
            }
        );
    }
}