package com.senliast.updatesmanagerextended;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                        if (shouldBlockInstall(appAboutToUpdate, installerPackage)) {
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
                        // to block the updates of this app via APK by blocking updates by Package installer. So will will take the
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

        /*
        Hook for Android 11 and below. I dont have an Android 11 device, so i cannot test it myself.
        And because the code is running in the system process, an error in it can potentially cause
        a crash of Android, in worst a bootloop. Its very unlikely to happen, but in order to be on
        the safe side, i will not turn it on for now. Its here for the future, when there is a
        possibility to safely test it, so that i already have a possible solution.

        XposedHelpers.findAndHookMethod(
            "com.android.server.pm.PackageInstallerSession",
            lpparam.classLoader,
            "commit",
            android.content.IntentSender.class,
            new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                    Object session = param.thisObject;

                    String installerPackageName = (String) XposedHelpers.getObjectField(session, "mInstallerPackageName");
                    String sessionPackageName = (String) XposedHelpers.getObjectField(session, "mPackageName");

                    XposedHelpers.callMethod(session, "onSessionVerificationFailure",
                            android.content.pm.PackageInstaller.STATUS_FAILURE,
                            "Installation blocked by Updates Manager Extended");

                    param.setResult(null);
                }
            });
         */
    }

    public boolean shouldBlockInstall(String app, String source) {
        XSharedPreferences globalPreferences = new XSharedPreferences("com.senliast.updatesmanagerextended", "Preferences");
        List<String> appsToBlockUpdates = new ArrayList<>();
        List<String> installationSourcesToBlock = new ArrayList<>();
        List<GroupInfo> groups = new ArrayList<>();

        if (globalPreferences.getString("moduleStatus", "disabled").equals("enabled_immediately") || (globalPreferences.getString("moduleStatus", "disabled").equals("enabled_since") && System.currentTimeMillis() >= globalPreferences.getLong("moduleStatusTime", 0L))) {
            groups = getGroups();
            for (GroupInfo group : groups) {
                if (group.getStatus().equals("enabled_immediately") || (group.getStatus().equals("enabled_since") && System.currentTimeMillis() >= group.getStatusTime())) {
                    appsToBlockUpdates = Arrays.asList(group.getAppsToBlockUpdates().split(","));
                    installationSourcesToBlock = Arrays.asList(group.getInstallationSources().split(","));
                    if (appsToBlockUpdates.contains(app) && (installationSourcesToBlock.contains(source) ||group.getBlockAllInstallationSources())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public List<GroupInfo> getGroups() {
        Gson gson = new Gson();
        List<GroupInfo> groups = new ArrayList<>();
        XSharedPreferences groupsPreferences = new XSharedPreferences("com.senliast.updatesmanagerextended", "Groups");
        String json = groupsPreferences.getString("groups_list", null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<GroupInfo>>(){}.getType();
            List<GroupInfo> loadedList = gson.fromJson(json, type);

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
}