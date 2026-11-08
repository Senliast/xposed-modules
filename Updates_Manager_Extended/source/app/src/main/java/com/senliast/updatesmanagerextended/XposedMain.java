package com.senliast.updatesmanagerextended;

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
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class XposedMain implements IXposedHookLoadPackage {
    private static final String listenPackage = "android";
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
                        Object session = param.thisObject;
                        String installerPackage = String.valueOf(XposedHelpers.getAdditionalInstanceField(session, "myMInstallerPackageName"));
                        String appAboutToUpdate = String.valueOf(getObjectField(param.thisObject, "mPackageName"));

                        if (shouldBlockInstall(appAboutToUpdate, installerPackage, lpparam.classLoader)) {
                            param.args[1] = "";
                        }
                    }
                }
        );

        // The needed field has to be extracted via constructor. Since PackageInstaller can have multiple sessions,
        // its important that the value will be passed to another hook via "setAdditionalInstanceField()", and
        // not via a variable, so that no overlapping / wrong values will be passed.
        XposedBridge.hookAllConstructors(
                PackageInstallerSessionClass,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        // There are 2 properties of the install source in PackageManager - "mInstallerPackageName" and "mInitiatingPackageName".
                        // The difference between them is that the first one says who is installing an app, and the second one - which app did
                        // initiated the install.
                        //
                        // If the installation happened by a privileged app (Google Play or other app store from manufacturer or via the
                        // permission "android.permission.UPDATE_PACKAGES_WITHOUT_USER_ACTION" since Android 12) - this app will be displayed
                        // as "mInstallerPackageName" and "mInitiatingPackageName". But if the installation happened via an APK
                        // (i.e. the calling ap has only the normal "android.permission.REQUEST_INSTALL_PACKAGES" permission) - then the
                        // Package installer will be displayed both as "mInstallerPackageName" and "mInitiatingPackageName". In this case,
                        // Android doesnt forward the original installation initiator app.
                        //
                        // Its probably still possible to find it out, but apps with the regular APK installation permissions cannot update apps
                        // automatically, and this module is intended mainly for disabling automatic updates. If its needed - its still possible
                        // to block the updates of this app via APK by blocking updates by Package installer. So we will take the
                        // "mInstallerPackageName" variable.

                        Object session = param.thisObject;
                        Object installSource = getObjectField(session, "mInstallSource");

                        if (installSource != null) {
                            String installerPackage = (String)
                                    getObjectField(installSource, "mInstallerPackageName");

                            XposedHelpers.setAdditionalInstanceField(
                                    session,
                                    "myMInstallerPackageName",
                                    installerPackage
                            );
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
                // Android 13–14
                packageInfo = XposedHelpers.callMethod(
                        packageManager,
                        "getPackageInfo",
                        packageName,
                        0L,
                        userId
                );
            } else {
                // Android 12
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