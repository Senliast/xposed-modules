package com.senliast.updatesmanagerextended;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private Drawable appIcon;
    private String appName;
    private String packageName;

    public AppInfo(Drawable appIcon, String appName, String packageName) {
        this.appIcon = appIcon;
        this.appName = appName;
        this.packageName = packageName;
    }

    public Drawable getAppIcon() { return appIcon; }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public String toString() {
        return "appInfo{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                '}';
    }
}
