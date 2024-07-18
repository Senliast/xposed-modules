package com.senliast.updatesmanagerextended;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    private AppsToBlockUpdatesListSanitizer appsToBlockUpdatesListSanitizer = new AppsToBlockUpdatesListSanitizer();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            appsToBlockUpdatesListSanitizer.sanitizeAppsToBlockUpdatesList();
        }
    }
}
