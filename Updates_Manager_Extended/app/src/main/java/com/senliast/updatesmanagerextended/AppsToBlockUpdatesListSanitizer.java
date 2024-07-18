// Remove uninstalled apps from the blacklist. Main intention is to prevent the situation,
// where user uninstalled an app and is unable to install this app later again, because the
// module cannot differentiate between installing and updating process and will block
// installation as well.

package com.senliast.updatesmanagerextended;

import android.content.pm.PackageManager;

import com.senliast.MyApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class AppsToBlockUpdatesListSanitizer extends AppCompatActivity {

    private List<String> appsToBlockUpdates = new ArrayList<>();
    private MyPreferencesManager myPreferencesManager = new MyPreferencesManager();

    public void sanitizeAppsToBlockUpdatesList() {
        // Only if module is loaded and enabled.
        if (!myPreferencesManager.getBooleanPreference("moduleEnabled", false) || !myPreferencesManager.testPreferences()) { return; }
        appsToBlockUpdates = new ArrayList<>(Arrays.asList((myPreferencesManager.getStringPreference("appsToBlockUpdates", "")).split(",")));
        Iterator<String> iterator = appsToBlockUpdates.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            if (!isPackageInstalled(item.toString(), MyApplication.getAppContext().getPackageManager())) { iterator.remove(); }
        }
        myPreferencesManager.setStringPreference("appsToBlockUpdates", String.join(",", appsToBlockUpdates));
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
