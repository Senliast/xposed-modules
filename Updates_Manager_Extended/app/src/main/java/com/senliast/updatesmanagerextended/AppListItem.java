package com.senliast.updatesmanagerextended;

import android.graphics.drawable.Drawable;

public class AppListItem {
    private Drawable appIcon;
    private String title;
    private String packageName;
    private Boolean guiSwitch;

    public AppListItem(Drawable appIcon, String title, String packageName, Boolean guiSwitch) {
        this.appIcon = appIcon;
        this.title = title;
        this.packageName = packageName;
        this.guiSwitch = guiSwitch;
    }

    public Drawable getAppIcon() { return appIcon; }

    public String getTitle() {
        return title;
    }

    public String getPackageName() {
        return packageName;
    }

    public Boolean getGuiSwitch() { return guiSwitch; }
}
