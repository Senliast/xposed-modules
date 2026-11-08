package com.senliast.updatesmanagerextended;

import android.graphics.drawable.Drawable;

public class AppInfo2 {
    private String appName;
    private String packageName;
    private Drawable appIcon;

    public AppInfo2(String appName, String packageName, Drawable appIcon) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }
    public String getPackageName() {
        return packageName;
    }
    public Drawable getAppIcon() {
        return appIcon;
    }
}