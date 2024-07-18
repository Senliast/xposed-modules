package com.senliast.updatesmanagerextended;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import com.senliast.MyApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

public class PreferencesBackupManager {

    private static final int EXPORT_SETTINGS_REQUEST_CODE = 101;
    private static final int IMPORT_SETTINGS_REQUEST_CODE = 102;
    private Context context;
    private MyPreferencesManager myPreferencesManager = new MyPreferencesManager();
    private ActivityResultLauncher<Intent> exportSettingsLauncher;
    private ActivityResultLauncher<Intent> importSettingsLauncher;

    public PreferencesBackupManager (Context context) {
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
        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                SharedPreferences preferences = myPreferencesManager.getSharedPreferences();
                Map<String, ?> allSettings = preferences.getAll();
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                for (Map.Entry<String, ?> entry : allSettings.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();if (value instanceof Boolean) {
                        writer.write(key + ":" + value.toString() + "\n");
                    }
                    else if (value instanceof String) {
                        writer.write(key + ":" + value.toString() + "\n");
                    }
                }
                writer.close();
                Toast.makeText(context, MyApplication.getAppContext().getText(R.string.preferences_successfully_exported) + uri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(context, MyApplication.getAppContext().getText(R.string.error_exporting_preferences) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importSettingsFromFile(Uri uri) {
        try {
            SharedPreferences preferences = myPreferencesManager.getSharedPreferences();
            SharedPreferences.Editor editor = preferences.edit();
            Boolean isUpdatesManagerExtendedPreferencesFile = false;

            DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
            if (documentFile != null && documentFile.exists()) {
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                InputStreamReader reader = new InputStreamReader(context.getContentResolver().openInputStream(uri));
                BufferedReader bufferedReader = new BufferedReader(reader);
                while ((line = bufferedReader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        if (value.equals("true") || value.equals("false")) {
                            if (key.equals("isUpdatesManagerExtendedPreferencesFile") && value.equals("true")) { isUpdatesManagerExtendedPreferencesFile = true; }
                            editor.putBoolean(key, Boolean.parseBoolean(value));
                        } else {
                            editor.putString(key, value);
                        }
                    }
                }
                bufferedReader.close();
                if (isUpdatesManagerExtendedPreferencesFile) {
                    editor.apply();
                    Intent intent = new Intent("com.senliast.updatesmanagerextended.BROADCAST");
                    intent.putExtra("type", "event");
                    intent.putExtra("event_name", "on_preferences_imported");
                    MyApplication.getAppContext().sendBroadcast(intent);
                    Toast.makeText(context, MyApplication.getAppContext().getText(R.string.preferences_successfully_imported) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();
                }  else {
                    Toast.makeText(context, MyApplication.getAppContext().getText(R.string.not_a_valid_preferences_file) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();
                }
            }
        } catch (IOException e) {
            Toast.makeText(context, MyApplication.getAppContext().getText(R.string.error_importing_preferences) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

