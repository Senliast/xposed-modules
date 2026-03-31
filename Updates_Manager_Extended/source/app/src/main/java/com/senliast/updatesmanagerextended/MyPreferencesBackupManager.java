// This is only responsible for Android UI for opening and saving files. The main import and export
// logic is in the "MyPreferencesManager".

package com.senliast.updatesmanagerextended;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MyPreferencesBackupManager {

    private Context context;
    private MyPreferencesManager myPreferencesManager = new MyPreferencesManager();
    private ActivityResultLauncher<Intent> exportSettingsLauncher;
    private ActivityResultLauncher<Intent> importSettingsLauncher;

    public MyPreferencesBackupManager(Context context) {
        this.context = context;

        exportSettingsLauncher = ((AppCompatActivity) context).registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        exportSettingsToFile(uri);
                    }
                });

        importSettingsLauncher = ((AppCompatActivity) context).registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        importSettingsFromFile(uri);
                    }
                });
    }

    public void exportSettingsToFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "Updates_Manager_Extended_settings.txt");

        exportSettingsLauncher.launch(intent);
    }

    public void importSettingsFromFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        importSettingsLauncher.launch(intent);
    }

    private void exportSettingsToFile(Uri uri) {
        myPreferencesManager.exportAllToUri(uri);
    }

    private void importSettingsFromFile(Uri uri) {
        myPreferencesManager.importAllFromUri(uri, context);
    }
}









