package com.senliast.updatesmanagerextended;

import java.util.Objects;

public class SavedApp {
    public String appName;
    public String packageName;
    public String status;
    public long statusTime;

    public SavedApp(String appName, String packageName, String status) {
        this.appName = appName;
        this.packageName = packageName;
        this.status = status;
        this.statusTime = 0L;
    }

    public SavedApp(String appName, String packageName, String status, long statusTime) {
        this.appName = appName;
        this.packageName = packageName;
        this.status = status;
        this.statusTime = statusTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedApp myItem = (SavedApp) o;
        return Objects.equals(appName, myItem.appName) &&
                Objects.equals(packageName, myItem.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, packageName);
    }

    public void setStatus (String status, long statusTime) {
        this.status = status;
        this.statusTime = statusTime;
    }

    public void setStatus (String status) {
        this.status = status;
        this.statusTime = 0L;
    }
}
