package com.senliast.updatesmanagerextended;


import android.graphics.drawable.Drawable;

public class AppInfo1 {
    private String appName;
    private String packageName;
    private Drawable appIcon;
    public String status;
    public long statusTime;

    public AppInfo1(String appName, String packageName, Drawable appIcon, String status, long statusTime) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.status = status;
        this.statusTime = statusTime;
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