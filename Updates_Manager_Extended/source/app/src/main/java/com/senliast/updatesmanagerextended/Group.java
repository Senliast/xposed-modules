package com.senliast.updatesmanagerextended;

import java.util.Set;

public class Group {
    private String name;
    private String status;
    private String installationSources;
    private Set<SavedApp> appsToBlockUpdates;
    private boolean blockAllInstallationSources;
    private Long statusTime;

    public Group(String name, String installationSources, Set<SavedApp> appsToBlockUpdates, boolean blockAllInstallationSources, String status, Long statusTime) {
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

    public void setAppsToBlockUpdates(Set<SavedApp> appsToBlockUpdates) { this.appsToBlockUpdates = appsToBlockUpdates; }

    public Set<SavedApp> getAppsToBlockUpdates() {
        if (appsToBlockUpdates != null) {
            return appsToBlockUpdates;
        } else {
            return null;
        }
    }

    public void setBlockAllInstallationSources(boolean blockAllInstallationSources) { this.blockAllInstallationSources = blockAllInstallationSources; }

    public boolean getBlockAllInstallationSources() { return blockAllInstallationSources; }
}