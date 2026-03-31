// Remove uninstalled apps from the blacklist. Main intention is to prevent the situation,
// where user uninstalled an app and is unable to install this app later again, because the
// module cannot differentiate between installing and updating process and will block
// installation as well.

package com.senliast.updatesmanagerextended;

import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;

import com.senliast.MyApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SettingsSanitizer extends AppCompatActivity {

    private List<String> appsToBlockUpdates;
    private List<String> installationSources;
    private MyPreferencesManager myPreferencesManager = new MyPreferencesManager();
    private List<GroupInfo> groups = new ArrayList<>();

    public void sanitize() {
        // Only if module is loaded and enabled.
        if (myPreferencesManager.getStringPreference("moduleStatus", "disabled").equals("enabled_immediately") || (myPreferencesManager.getStringPreference("moduleStatus", "disabled").equals("enabled_since") && System.currentTimeMillis() >= myPreferencesManager.getLongPreference("moduleStatusTime", 0L))) {
            groups = myPreferencesManager.getGroups();
            for (GroupInfo group : groups) {
                appsToBlockUpdates = new ArrayList<>(Arrays.asList(group.getAppsToBlockUpdates().split(",")));
                installationSources = new ArrayList<>(Arrays.asList(group.getAppsToBlockUpdates().split(",")));
                Iterator<String> iteratorAppsToBlockUpdates = appsToBlockUpdates.iterator();
                while (iteratorAppsToBlockUpdates.hasNext()) {
                    String item = iteratorAppsToBlockUpdates.next();
                    if (!isPackageInstalled(item.toString(), MyApplication.getAppContext().getPackageManager())) {
                        iteratorAppsToBlockUpdates.remove();
                    }
                }
                Iterator<String> iteratorInstallationSources = installationSources.iterator();
                while (iteratorInstallationSources.hasNext()) {
                    String item = iteratorInstallationSources.next();
                    if (!isPackageInstalled(item.toString(), MyApplication.getAppContext().getPackageManager())) {
                        iteratorInstallationSources.remove();
                    }
                }
                group.setAppsToBlockUpdates(String.join(",", appsToBlockUpdates));
                group.setInstallationSources(String.join(",", installationSources));
            }
            myPreferencesManager.saveGroups(groups);
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
