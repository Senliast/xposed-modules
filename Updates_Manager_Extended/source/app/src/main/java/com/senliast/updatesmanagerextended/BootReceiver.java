package com.senliast.updatesmanagerextended;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    PreferencesManager preferencesManager = new PreferencesManager();
    SettingsSanitizer settingsSanitizer = new SettingsSanitizer();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (preferencesManager.testPreferences()) {
                preferencesManager.upgradePreferencesDatabase();
                settingsSanitizer.sanitize();
            }
        }
    }
}
