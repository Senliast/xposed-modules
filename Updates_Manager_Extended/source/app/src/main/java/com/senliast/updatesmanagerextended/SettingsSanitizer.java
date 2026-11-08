package com.senliast.updatesmanagerextended;

import android.content.pm.PackageManager;

import com.senliast.MyApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SettingsSanitizer {
    private Set<String> installationSources;
    private PreferencesManager preferencesManager = new PreferencesManager();
    private List<Group> groups = new ArrayList<>();

    public synchronized void sanitize() {
        if (preferencesManager.testPreferences()) {
            if (preferencesManager.getBooleanPreference("preferencesCreated", false) && preferencesManager.getGroups() != null)
            groups = preferencesManager.getGroups();
            for (Group group : groups) {
                installationSources = new HashSet<>();
                installationSources.addAll(Arrays.asList(group.getInstallationSources().split(",")));
                Iterator<String> iteratorInstallationSources = installationSources.iterator();
                while (iteratorInstallationSources.hasNext()) {
                    String item = iteratorInstallationSources.next();
                    if (!isPackageInstalled(item.toString(), MyApplication.getAppContext().getPackageManager())) {
                        iteratorInstallationSources.remove();
                    }
                }
                group.setInstallationSources(String.join(",", installationSources));
            }
            preferencesManager.setGroups(groups);
        }
    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
