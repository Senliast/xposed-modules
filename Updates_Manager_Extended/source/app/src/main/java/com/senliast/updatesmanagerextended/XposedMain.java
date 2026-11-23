package com.senliast.updatesmanagerextended;

import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findClass;

public class XposedMain implements IXposedHookLoadPackage {
    private static final String listenPackage = "android";
    private Class<?> packageManagerExceptionClass;
    private static final int INSTALL_FAILED_INVALID_APK = -2;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;

        Class<?> PackageInstallerSessionClass = findClass(
                "com.android.server.pm.PackageInstallerSession",
                lpparam.classLoader);

        if (packageManagerExceptionClass == null) {
            try {
                packageManagerExceptionClass = XposedHelpers.findClass(
                        "com.android.server.pm.PackageManagerException",
                        lpparam.classLoader
                );
            } catch (Throwable t) {
                return;
            }
        }

        // Android 8.0
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            hookAndroid8Dot0(PackageInstallerSessionClass, lpparam.classLoader);
        }

        // Android 8.1 - 10
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            hookAndroid8Dot1to10(PackageInstallerSessionClass, lpparam.classLoader);
        }

        // Android 11 and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hookAndroid11AndUp(PackageInstallerSessionClass, lpparam.classLoader);
        }
    }

    private Throwable newBlockedInstallException() {
        if (packageManagerExceptionClass == null) { return null; }

        try {
            return (Throwable) XposedHelpers.newInstance(
                    packageManagerExceptionClass,
                    INSTALL_FAILED_INVALID_APK,
                    "Installation blocked by Updates Manager Extended"
            );
        } catch (Throwable t) {
            return null;
        }
    }

    private Class<?> findApkLiteClass(ClassLoader cl) {
        try {
            return XposedHelpers.findClass("android.content.pm.parsing.ApkLite", cl);
        } catch (Throwable ignored) {

        }

        try {
            return XposedHelpers.findClass("android.content.pm.PackageParser$ApkLite", cl);
        } catch (Throwable ignored) {

        }

        return null;
    }

    private void hookAndroid8Dot0(Class<?> sessionClass, ClassLoader cl) {
        Class<?> apkLiteClass = findApkLiteClass(cl);

        if (apkLiteClass == null) { return; }

        XposedHelpers.findAndHookMethod(
                sessionClass,
                "assertApkConsistent",
                String.class,
                apkLiteClass,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object session = param.thisObject;
                        Object apk = param.args[1];

                        String packageName = null;
                        try {
                            packageName = (String) XposedHelpers.getObjectField(apk, "packageName");
                        } catch (Throwable ignored) {

                        }

                        String installerPackageName = null;
                        try {
                            installerPackageName = (String) XposedHelpers.getObjectField(
                                    session,
                                    "mInstallerPackageName");
                        } catch (Throwable ignored) {

                        }

                        if (shouldBlockInstall(packageName, installerPackageName, cl)) {
                            Throwable pme = newBlockedInstallException();
                            if (pme != null) {
                                param.setThrowable(pme);
                            }
                        }
                    }
                }
        );
    }

    private void hookAndroid8Dot1to10(Class<?> sessionClass, ClassLoader cl) {
        Class<?> apkLiteClass = findApkLiteClass(cl);

        if (apkLiteClass == null) { return; }

        XposedHelpers.findAndHookMethod(
                sessionClass,
                "assertApkConsistentLocked",
                String.class,
                apkLiteClass,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object session = param.thisObject;
                        Object apk = param.args[1];

                        String packageName = null;
                        try {
                            packageName = (String) XposedHelpers.getObjectField(
                                    apk,
                                    "packageName");
                        } catch (Throwable ignored) {
                            try {
                                packageName = (String) XposedHelpers.callMethod(
                                        apk,
                                        "getPackageName");
                            } catch (Throwable ignored2) {

                            }
                        }

                        String installerPackageName = null;
                        try {
                            installerPackageName = (String) XposedHelpers.getObjectField(
                                    session,
                                    "mInstallerPackageName");
                        } catch (Throwable ignored) {

                        }

                        if (shouldBlockInstall(packageName, installerPackageName, cl)) {
                            Throwable pme = newBlockedInstallException();
                            if (pme != null) {
                                param.setThrowable(pme);
                            }
                        }
                    }
                }
        );
    }

    private void hookAndroid11AndUp(Class<?> sessionClass, ClassLoader cl) {
        Class<?> apkLiteClass = findApkLiteClass(cl);

        if (apkLiteClass == null) { return; }

        XposedHelpers.findAndHookMethod(
                sessionClass,
                "assertApkConsistentLocked",
                String.class,
                apkLiteClass,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object session = param.thisObject;
                        Object apk = param.args[1];

                        String packageName = null;
                        try {
                            packageName = (String) XposedHelpers.callMethod(
                                    apk,
                                    "getPackageName");
                        } catch (Throwable ignored) {
                            try {
                                packageName = (String) XposedHelpers.getObjectField(
                                        apk,
                                        "packageName");
                            } catch (Throwable ignored2) {

                            }
                        }

                        String installerPackageName = null;
                        try {
                            Object installSource = XposedHelpers.getObjectField(session, "mInstallSource");
                            if (installSource != null) {
                                try {
                                    installerPackageName = (String) XposedHelpers.getObjectField(
                                            installSource,
                                            "mInstallerPackageName");
                                } catch (Throwable ignored) {
                                    installerPackageName = (String) XposedHelpers.getObjectField(
                                            installSource,
                                            "installerPackageName");
                                }
                            }
                        } catch (Throwable ignored) {

                        }

                        if (shouldBlockInstall(packageName, installerPackageName, cl)) {
                            Throwable pme = newBlockedInstallException();
                            if (pme != null) {
                                param.setThrowable(pme);
                            }
                        }
                    }
                }
        );
    }

    public boolean shouldBlockInstall(String app, String source, ClassLoader classLoader) {
        // Do not block installation after the app has been uninstalled.
        if (isPackageInstalledForUser(app, 0, classLoader)) {
            XSharedPreferences globalPreferences = new XSharedPreferences("com.senliast.updatesmanagerextended", "Preferences");

            if (globalPreferences.getString("moduleStatus", "disabled").equals("enabled_immediately")
                    || (globalPreferences.getString("moduleStatus", "disabled").equals("enabled_since")
                    && System.currentTimeMillis() >= globalPreferences.getLong("moduleStatusTime", 0L))) {
                List<Group> groups = getGroups();
                for (Group group : groups) {
                    if (group.getStatus().equals("enabled_immediately")
                            || (group.getStatus().equals("enabled_since")
                            && System.currentTimeMillis() >= group.getStatusTime())) {
                        Set<SavedApp> appsToBlockUpdates = group.getAppsToBlockUpdates();
                        List<String> installationSourcesToBlock = Arrays.asList(group.getInstallationSources().split(","));

                        if (installationSourcesToBlock.contains(source) || group.getBlockAllInstallationSources()) {
                            for (SavedApp savedApp : appsToBlockUpdates) {
                                if (savedApp.packageName.equals(app)
                                        && (savedApp.status.equals("enabled_immediately")
                                        || (savedApp.status.equals("enabled_since")
                                        && (System.currentTimeMillis() >= savedApp.statusTime)))) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public List<Group> getGroups() {
        Gson gson = new Gson();
        List<Group> groups = new ArrayList<>();
        XSharedPreferences groupsPreferences = new XSharedPreferences("com.senliast.updatesmanagerextended", "Preferences");
        String json = groupsPreferences.getString("groups", null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<Group>>(){}.getType();
            List<Group> loadedList = gson.fromJson(json, type);

            if (loadedList != null) {
                groups.clear();
                groups.addAll(loadedList);
            } else {
                groups.clear();
            }
        } else {
            groups.clear();
        }

        return groups;
    }

    // PackageManager is not available because we dont have a context. But since we are in system_server -
    // we can use reflection and check if an app is installed using Android internal methods.
    public boolean isPackageInstalledForUser(String packageName, int userId, ClassLoader classLoader) {
        try {
            Class<?> appGlobalsClass = XposedHelpers.findClass(
                    "android.app.AppGlobals",
                    classLoader
            );

            Object packageManager = XposedHelpers.callStaticMethod(
                    appGlobalsClass,
                    "getPackageManager"
            );

            Object packageInfo;

            if (android.os.Build.VERSION.SDK_INT >= 33) {
                // Android 13 and up
                packageInfo = XposedHelpers.callMethod(
                        packageManager,
                        "getPackageInfo",
                        packageName,
                        0L,
                        userId
                );
            } else {
                // Android 12 and below
                packageInfo = XposedHelpers.callMethod(
                        packageManager,
                        "getPackageInfo",
                        packageName,
                        0,
                        userId
                );
            }

            return packageInfo != null;
        } catch (Throwable t) {
            return false;
        }
    }
}