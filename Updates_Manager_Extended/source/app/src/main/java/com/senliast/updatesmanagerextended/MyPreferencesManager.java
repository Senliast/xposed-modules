// The preferences manager can only handle the settings of current version of the app (except the
// "importAllFromUri()" and "upgradePreferencesDatabase()" methods). Therefore,
// "upgradePreferencesDatabase()" must be called before any other operations (except
// "testPreferences()"), otherwise it can lead to unexpected app behaviour or corruption of
// preferences database. The method determines itself, whether an upgrade is needed or not.
//
// The preferences are first read and saved without changes by "importAllFromUri()". This method is
// responsible only for importing of preferences. Then, they will be upgraded by
// "upgradePreferencesDatabase()", if needed.
//
// MyPreferencesManager is responsible for upgrading preferences. It does create new preferences
// only if they are needed for operation of parts, that already created preferences in previous
// versions of app, where this preferences are missing. Each part of the app is responsible for
// initial creating of preferences, which are needed by it. Each app parts gives or retrieves needed
// preferences to MyPreferencesManager and it only executes requested operations.

package com.senliast.updatesmanagerextended;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.senliast.MyApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import androidx.documentfile.provider.DocumentFile;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MyPreferencesManager {
    private final int APP_VERSION_CODE = 9;
    public String getStringPreference(String a, String b) {
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
        return preferences.getString(a, b);
    }

    public void setStringPreference(String a, String b) {
        SharedPreferences.Editor editor = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE).edit();
        editor.putString(a, b);
        editor.apply();
    }

    public Boolean getBooleanPreference(String a, Boolean b) {
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
        return preferences.getBoolean(a, b);
    }

    public void setBooleanPreference(String a, Boolean b) {
        SharedPreferences.Editor editor = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE).edit();
        editor.putBoolean(a, b);
        editor.apply();
    }

    public SharedPreferences getSharedPreferences() {
        return MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
    }

    // Test if preferences API is available. If not - it means, that Xposed is not installed or module is not enabled.
    // Because an attempt to get preferences with "Context.MODE_WORLD_READABLE" in a normal Android will throw
    // a SecurityException.
    public Boolean testPreferences() {
        SharedPreferences preferences;
        try {
            preferences = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
            return true;
        } catch (SecurityException ignored) {
            return false;
        }
    }

    public void setIntPreference(String a, Integer b) {
        SharedPreferences.Editor editor = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE).edit();
        editor.putInt(a, b);
        editor.apply();
    }

    public Integer getIntPreference(String a, Integer b){
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
        return preferences.getInt(a, b);
    }

    public void setLongPreference(String a, Long b) {
        SharedPreferences.Editor editor = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE).edit();
        editor.putLong(a, b);
        editor.apply();
    }

    public Long getLongPreference(String a, Long b) {
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
        return preferences.getLong(a, b);
    }

    public Boolean testPreference(String a ) {
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
        return preferences.contains(a);
    }

    public void deletePreference(String a ) {
        SharedPreferences.Editor editor = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE).edit();
        editor.remove(a);
        editor.apply();
    }

    public List<GroupInfo> getGroups() {
        Gson gson = new Gson();
        List<GroupInfo> groups = new ArrayList<>();
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences("Groups", Context.MODE_WORLD_READABLE);
        String json = preferences.getString("groups_list", null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<GroupInfo>>(){}.getType();
            List<GroupInfo> loadedList = gson.fromJson(json, type);

            if (loadedList != null) {
                groups.clear();
                groups.addAll(loadedList);
            } else {
                groups.clear();
            }
        } else {
            groups.clear();
        }

        return groups;
    }

    public void saveGroups(List<GroupInfo> groups) {
        Gson gson = new Gson();
        String json = gson.toJson(groups);
        SharedPreferences.Editor editor = MyApplication.getAppContext().getSharedPreferences("Groups", Context.MODE_WORLD_READABLE).edit();
        editor.putString("groups_list", json).apply();
        editor.apply();
    }

    public void exportAllToUri(Uri uri) {
        try {
            OutputStream outputStream = MyApplication.getAppContext()
                    .getContentResolver().openOutputStream(uri);

            if (outputStream == null) return;

            JsonObject backup = new JsonObject();

            SharedPreferences prefs = getSharedPreferences();
            Map<String, ?> allPrefs = prefs.getAll();
            JsonObject prefsJson = new JsonObject();
            for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof Boolean) {
                    prefsJson.addProperty(key, (Boolean) value);
                } else if (value instanceof String) {
                    prefsJson.addProperty(key, (String) value);
                } else if (value instanceof Integer) {
                    prefsJson.addProperty(key, (Integer) value);
                } else if (value instanceof Float) {
                    prefsJson.addProperty(key, (Float) value);
                } else if (value instanceof Long) {
                    prefsJson.addProperty(key, (Long) value);
                }
            }
            backup.add("preferences", prefsJson);
            List<GroupInfo> groups = getGroups();
            Gson gson = new Gson();
            String groupsJsonString = gson.toJson(groups);
            backup.addProperty("groups", groupsJsonString);
            backup.addProperty("isUpdatesManagerExtendedPreferencesFile", true);

            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            writer.write(backup.toString());
            writer.close();

            Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getText(R.string.preferences_successfully_exported) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getText(R.string.error_exporting_preferences) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void importAllFromUri(Uri uri, Context context) {
        Integer preferencesCodeVersion = 0;

        // Determine preferences code version
        // ========================================
        try {
            InputStream inputStream = MyApplication.getAppContext()
                    .getContentResolver().openInputStream(uri);
            if (inputStream == null) return;

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            JsonObject backup = JsonParser.parseString(sb.toString()).getAsJsonObject();
            preferencesCodeVersion = backup.getAsJsonObject("preferences").get("APP_VERSION_CODE").getAsJsonPrimitive().getAsInt();
        } catch (Exception e) {
            if (e.getClass().getName().equals("com.google.gson.JsonSyntaxException")) {
                // The converting of preferences file to JSON format failed. It means, that the initial data format
                // was not JSON, and there are 2 reasons for this. Its either UME version 3.1 and below or its not
                // a valid UME preferences file. The check of the "isUpdatesManagerExtendedBackup" signature is
                // not possible here, we will check it later and will for now assume, its an older UME version.
                // It doesnt meter which one it is, because settings format is the same. So setting version to 0.
                preferencesCodeVersion = 0;
            } else {
                // It means, that either reading of "preferences.APP_VERSION_CODE" value failed or another error
                // occurred while reading file. Since "preferences.APP_VERSION_CODE" is always present in UME 4.0
                // and higher, the file is either damaged or its not an UME preferences file. In both cases, its
                // an unrecoverable error and it doesnt make sense to continue, so returning.
                Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getText(R.string.error_importing_preferences) + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        // ========================================

        // UME 4.0 (version code 9)
        // ========================================
        if (preferencesCodeVersion == 9) {
            try {
                InputStream inputStream = MyApplication.getAppContext()
                        .getContentResolver().openInputStream(uri);
                if (inputStream == null) return;

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JsonObject backup = JsonParser.parseString(sb.toString()).getAsJsonObject();

                if (!backup.has("isUpdatesManagerExtendedPreferencesFile") ||
                        !backup.get("isUpdatesManagerExtendedPreferencesFile").getAsBoolean()) {
                    Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getText(R.string.not_a_valid_preferences_file) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences.Editor editor = getSharedPreferences().edit();
                if (backup.has("preferences")) {
                    JsonObject prefsJson = backup.getAsJsonObject("preferences");

                    // Delete all previous settings, so that no errors occur.
                    editor.clear();

                    for (String key : prefsJson.keySet()) {
                        if (prefsJson.get(key).isJsonPrimitive()) {
                            JsonPrimitive prim = prefsJson.get(key).getAsJsonPrimitive();

                            if (key.equals("isFirstStart")) {
                                editor.putBoolean(key, prim.getAsBoolean());
                            } else if (key.equals("moduleStatus")) {
                                editor.putString(key, prim.getAsString());
                            } else if (key.equals("preferencesCreated")) {
                                editor.putBoolean(key, prim.getAsBoolean());
                            } else if (key.equals("moduleStatusTime")) {
                                editor.putLong(key, prim.getAsLong());
                            } else if (key.equals("APP_VERSION_CODE")) {
                                editor.putInt(key, prim.getAsInt());
                            } else if (key.equals("groupToEdit")) {
                                editor.putInt(key, prim.getAsInt());
                            } else if (key.equals("appsToBlockUpdates")) {
                                editor.putString(key, prim.getAsString());
                            } else if (key.equals("installationSources")) {
                                editor.putString(key, prim.getAsString());
                            } else if (key.equals("blockAllInstallationSources")) {
                                editor.putBoolean(key, prim.getAsBoolean());
                            } else if (key.equals("status")) {
                                editor.putString(key, prim.getAsString());
                            } else if (key.equals("statusTime")) {
                                editor.putLong(key, prim.getAsLong());
                            } else if (key.equals("isUpdatesManagerExtendedPreferencesFile")) {

                            } else if (key.equals("name")) {
                                editor.putString(key, prim.getAsString());
                            } else {
                                Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getText(R.string.error_importing_preferences) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }
                }

                editor.apply();

                if (backup.has("groups")) {
                    String groupsJson = backup.get("groups").getAsString();
                    saveGroupsFromJson(groupsJson);
                }

                Intent intent = new Intent("com.senliast.updatesmanagerextended.BROADCAST");
                intent.putExtra("type", "event");
                intent.putExtra("event_name", "on_preferences_imported");

                LocalBroadcastManager.getInstance(MyApplication.getAppContext())
                        .sendBroadcast(intent);

                Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getText(R.string.preferences_successfully_imported) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getText(R.string.error_importing_preferences) + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return;
        }
        // ========================================

        // UME 3.1 and below (version code 8 and below)
        // ========================================
        if (preferencesCodeVersion == 0) {
            try {
                SharedPreferences.Editor editor = getSharedPreferences().edit();
                Boolean isUpdatesManagerExtendedPreferencesFile = false;

                DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
                if (documentFile != null && documentFile.exists()) {
                    editor.clear();
                    String line;
                    InputStreamReader reader = new InputStreamReader(context.getContentResolver().openInputStream(uri));
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            if (value.equals("true") || value.equals("false")) {
                                if (key.equals("isUpdatesManagerExtendedPreferencesFile") && value.equals("true")) {
                                    isUpdatesManagerExtendedPreferencesFile = true;
                                } else {
                                    editor.putBoolean(key, Boolean.parseBoolean(value));
                                }
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

                        LocalBroadcastManager.getInstance(MyApplication.getAppContext())
                                .sendBroadcast(intent);

                        Toast.makeText(context, MyApplication.getAppContext().getText(R.string.preferences_successfully_imported) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, MyApplication.getAppContext().getText(R.string.not_a_valid_preferences_file) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (IOException e) {
                Toast.makeText(context, MyApplication.getAppContext().getText(R.string.error_importing_preferences) + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return;
        }
        // ========================================

        Toast.makeText(context, MyApplication.getAppContext().getText(R.string.error_importing_preferences) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();
    }
    private void saveGroupsFromJson(String json) {
        if (json == null || json.isEmpty()) return;
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<GroupInfo>>(){}.getType();
        List<GroupInfo> loadedGroups = gson.fromJson(json, type);
        if (loadedGroups != null) {
            saveGroups(loadedGroups);
        }
    }

    public void upgradePreferencesDatabase() {
        if (getBooleanPreference("preferencesCreated", false)) {
            // From version code 8 (UME 3.1 and below)
            if (getIntPreference("APP_VERSION_CODE", 0) <= 8) {
                setBooleanPreference("isFirstStart", !getBooleanPreference("welcomeMessageShown", false));
                deletePreference("welcomeMessageShown");
                List<String> appsToBlockUpdates = Arrays.asList((getStringPreference("appsToBlockUpdates", "")).split(","));
                List<GroupInfo> groups = new ArrayList<>();
                groups.add(new GroupInfo(MyApplication.getAppContext().getResources().getString(R.string.default_group_name), "", String.join(",", appsToBlockUpdates), true, "enabled_immediately", 0L));
                saveGroups(groups);
                deletePreference("appsToBlockUpdates");
                if (getBooleanPreference("moduleEnabled", false)) {
                    setStringPreference("moduleStatus", "enabled_immediately");
                } else {
                    setStringPreference("moduleStatus", "disabled");
                }
                setLongPreference("moduleStatusTime", 0L);
                deletePreference("moduleEnabled");
                setIntPreference("APP_VERSION_CODE", APP_VERSION_CODE);
            }
        }
    }
}
