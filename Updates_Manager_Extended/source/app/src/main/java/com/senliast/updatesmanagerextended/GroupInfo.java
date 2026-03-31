package com.senliast.updatesmanagerextended;

public class GroupInfo {
    private String name;
    private String status;
    private String installationSources;
    private String appsToBlockUpdates;
    private boolean blockAllInstallationSources;
    private Long statusTime;

    public GroupInfo() {
    }

    public GroupInfo(String name, String installationSources, String appsToBlockUpdates, boolean blockAllInstallationSources, String status, Long statusTime) {
        this.name = name;
        this.status = status;
        this.installationSources = installationSources;
        this.appsToBlockUpdates = appsToBlockUpdates;
        this.blockAllInstallationSources = blockAllInstallationSources;
        this.statusTime = statusTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getStatusTime () { return statusTime; }

    public void setStatusTime(Long statusTime) { this.statusTime = statusTime; }

    public void setInstallationSources(String installationSources) { this.installationSources = installationSources; }

    public String getInstallationSources() { return installationSources;}

    public void setAppsToBlockUpdates(String appsToBlockUpdates) { this.appsToBlockUpdates = appsToBlockUpdates; }

    public String getAppsToBlockUpdates() { return appsToBlockUpdates; }

    public void setBlockAllInstallationSources(boolean blockAllInstallationSources) { this.blockAllInstallationSources = blockAllInstallationSources; }

    public boolean getBlockAllInstallationSources() { return blockAllInstallationSources; }
}