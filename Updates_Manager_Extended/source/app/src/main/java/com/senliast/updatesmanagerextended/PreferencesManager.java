// The preferences manager can only handle the settings of current version of the app (except the
// "importAllFromUri()", "upgradePreferencesDatabase()", and "testPreferences()" methods).
// Therefore, "upgradePreferencesDatabase()" must be called before calling any other methods,
// otherwise it can lead to unexpected app behaviour or corruption of preferences database.
// The method determines itself, whether an upgrade is needed or not.
//
// The preferences are first read and saved without changes by "importAllFromUri()". This method is
// responsible only for importing of preferences. Then, they will be upgraded by
// "upgradePreferencesDatabase()", if needed.
//
// PreferencesManager is responsible for upgrading preferences. It does create new preferences
// only if they are needed for operation of parts, that already created preferences in previous
// versions of app, where this preferences are missing. Each part of the app is responsible for
// initial creating of preferences, which are needed by it. Each app parts gives or retrieves needed
// preferences to PreferencesManager and it only executes requested operations.

package com.senliast.updatesmanagerextended;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.senliast.MyApplication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.senliast.updatesmanagerextended.compatibility.GroupInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import androidx.documentfile.provider.DocumentFile;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class PreferencesManager {
    private final int APP_VERSION_CODE = 13;
    private Context context = MyApplication.getAppContext();

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

    public Boolean testPreference(String a ) {
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
        return preferences.contains(a);
    }

    public void deletePreference(String a ) {
        SharedPreferences.Editor editor = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE).edit();
        editor.remove(a);
        editor.apply();
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

    // For compatibility
    public List<GroupInfo> getGroupsCompatibility() {
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

    public <T> T getGroups() {
        SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
        String json = prefs.getString("groups", null);
        if (json == null) return null;
        return gson.fromJson(json, new TypeToken<List<Group>>(){}.getType());
    }

    public void setGroups(Object groups) {
        SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
        String json = gson.toJson(groups);
        prefs.edit().putString("groups", json).apply();
    }

    public void setModuleStatus (String status) {
        SharedPreferences.Editor editor = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE).edit();
        editor.putString("moduleStatus", status);
        editor.putLong("moduleStatusTime", 0L);
        editor.apply();
    }

    public void setModuleStatus (String status, long statusTime) {
        SharedPreferences.Editor editor = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE).edit();
        editor.putString("moduleStatus", status);
        editor.putLong("moduleStatusTime", statusTime);
        editor.apply();
    }

    public String getModuleStatus() {
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
        return preferences.getString("moduleStatus", Status.ENABLED_IMMEDIATELY);
    }

    public long getModuleStatusTime () {
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
        return preferences.getLong("moduleStatusTime", 0L);
    }

    public void setIsNotFirstStart() {
        SharedPreferences.Editor editor = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE).edit();
        editor.putBoolean("isFirstStart", false);
        editor.apply();
    }

    public boolean getIsFirstStart() {
        SharedPreferences preferences = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
        return preferences.getBoolean("isFirstStart", true);
    }
    private static final Gson gson = new Gson();

    public void saveAppIcon(Context context, Drawable icon, String packageName) {
        try {
            if (icon == null) {
                return;
            }

            try {
                Bitmap bitmap = Utils.drawableToBitmap(icon);
                File dir = new File(context.getFilesDir(), "Icons");
                if (!dir.exists() && !dir.mkdirs()) {
                    return;
                }
                File file = new File(dir, packageName + ".png");
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {

            }
        } catch (Exception e) {

        }
    }

    public Drawable getSavedAppIcon(Context context, String packageName) {
        File file = new File(context.getFilesDir(), "Icons/" + packageName + ".png");
        if (!file.exists()) {
            return null;
        }
        return Drawable.createFromPath(file.getAbsolutePath());
    }

    public void deleteAppIcon(Context context, String packageName) {
        File file = new File(context.getFilesDir(), "Icons/" + packageName);
        if (file.exists()) {
            file.delete();
        }
    }

    public void exportAllToUri(Uri uri) {
            try {
                File iconsDir = new File(context.getFilesDir(), "Icons");

                try (ZipOutputStream zos = new ZipOutputStream(
                        new BufferedOutputStream(context.getContentResolver().openOutputStream(uri)))) {
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

                    List<Group> groups = getGroups();
                    Gson gson = new Gson();
                    String groupsJsonString = gson.toJson(groups);
                    prefsJson.addProperty("groups", groupsJsonString);
                    prefsJson.addProperty("isUpdatesManagerExtendedPreferencesFile", true);

                    zos.putNextEntry(new ZipEntry("Preferences.json"));
                    zos.write(prefsJson.toString().getBytes());
                    zos.closeEntry();

                    if (iconsDir.exists()) {
                        File[] files = iconsDir.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.isFile()) {
                                    zos.putNextEntry(new ZipEntry("Icons/" + file.getName()));
                                    try (FileInputStream fis = new FileInputStream(file)) {
                                        byte[] buffer = new byte[8192];
                                        int len;
                                        while ((len = fis.read(buffer)) > 0) {
                                            zos.write(buffer, 0, len);
                                        }
                                    }
                                    zos.closeEntry();
                                }
                            }
                        }
                    }
                }
                Toast.makeText(context, "Export successful", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
    }

    public void importAllFromUri(Uri uri, Context context) {
        Integer preferencesCodeVersion = 0;

        // Determine preferences code version
        // ========================================
        try {
            if (context.getContentResolver().getType(uri).equals("text/plain")) {

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
            } else {
                try (ZipInputStream zis = new ZipInputStream(
                        new BufferedInputStream(context.getContentResolver().openInputStream(uri)))) {

                    ZipEntry entry;
                    byte[] buffer = new byte[8192];

                    while ((entry = zis.getNextEntry()) != null) {
                        String name = entry.getName();

                        if (name.equals("Preferences.json")) {
                            StringBuilder sb = new StringBuilder();
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                sb.append(new String(buffer, 0, len));
                            }

                            JsonObject backup = JsonParser.parseString(sb.toString()).getAsJsonObject();

                            if (!backup.has("isUpdatesManagerExtendedPreferencesFile") ||
                                    !backup.get("isUpdatesManagerExtendedPreferencesFile").getAsBoolean()) {
                                Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getText(R.string.not_a_valid_preferences_file) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();
                                return;
                            }


                                for (String key : backup.keySet()) {
                                    if (backup.get(key).isJsonPrimitive()) {

                                        JsonPrimitive prim = backup.get(key).getAsJsonPrimitive();

                                        if (key.equals("APP_VERSION_CODE")) {
                                            preferencesCodeVersion = prim.getAsInt();
                                        }
                                    }
                                }

                        }
                        zis.closeEntry();
                    }
                }
            }
        } catch (Exception e) {
            if (e.getClass().getName().equals("com.google.gson.JsonSyntaxException")) {
                // The converting of preferences file to JSON format failed. It means, that the initial data format
                // was not JSON, and there are 2 reasons for this. Its either UME version 3.1 and below or its not
                // a valid UME preferences file. The check of the "isUpdatesManagerExtendedBackup" signature is
                // not possible here, we will check it later and will for now assume, its an older UME version.
                // It doesnt meter which one it is, because settings format is the same. So setting version to 0.
                // All version codes between 0 and 8 will be treated like 0.
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

        // UME 3.1 and below (version code 8 and below)
        // ========================================
        if (preferencesCodeVersion == 0) {
            try {
                SharedPreferences.Editor editor = getSharedPreferences().edit();
                Boolean isUpdatesManagerExtendedPreferencesFile = false;

                DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
                if (documentFile != null && documentFile.exists()) {
                    editor.clear();
                    File iconsDir = new File(context.getFilesDir(), "Icons");
                    if (iconsDir.exists()) { iconsDir.delete(); }
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
                        upgradePreferencesDatabase();
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

        // UME 4.0 - 4.2 (version code 9 - 11)
        // ========================================
        if (preferencesCodeVersion == 9 || preferencesCodeVersion == 10 || preferencesCodeVersion == 11) {
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

                if (!backup.has("isUpdatesManagerExtendedPreferencesFile") || !backup.get("isUpdatesManagerExtendedPreferencesFile").getAsBoolean()) {
                    Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getText(R.string.not_a_valid_preferences_file) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences.Editor editor = getSharedPreferences().edit();
                if (backup.has("preferences")) {
                    JsonObject prefsJson = backup.getAsJsonObject("preferences");

                    // Delete all previous settings, so that no errors occur.
                    editor.clear();
                    File iconsDir = new File(context.getFilesDir(), "Icons");
                    if (iconsDir.exists()) { iconsDir.delete(); }

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
                    saveGroupsFromJsonCompatibility(groupsJson);
                }

                Intent intent = new Intent("com.senliast.updatesmanagerextended.BROADCAST");
                intent.putExtra("type", "event");
                intent.putExtra("event_name", "on_preferences_imported");

                LocalBroadcastManager.getInstance(MyApplication.getAppContext())
                        .sendBroadcast(intent);

                Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getText(R.string.preferences_successfully_imported) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();

                upgradePreferencesDatabase();
            } catch (Exception e) {
                Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getText(R.string.error_importing_preferences) + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return;
        }
        // ========================================

        // UME 5.0 - 5.1 (version code 12 - 13)
        // ========================================
        if (preferencesCodeVersion == 12 || preferencesCodeVersion == 13) {
            try {
                File iconsDir = new File(context.getFilesDir(), "Icons");

                try (ZipInputStream zis = new ZipInputStream(
                        new BufferedInputStream(context.getContentResolver().openInputStream(uri)))) {

                    ZipEntry entry;
                    byte[] buffer = new byte[8192];

                    while ((entry = zis.getNextEntry()) != null) {
                        String name = entry.getName();

                        if (name.equals("Preferences.json")) {
                            StringBuilder sb = new StringBuilder();
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                sb.append(new String(buffer, 0, len));
                            }

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
                                if (iconsDir.exists()) {
                                    File[] iconsFiles = iconsDir.listFiles();
                                    if (iconsFiles != null) {
                                        for (File file : iconsFiles) {
                                            file.delete();
                                        }
                                    }
                                    iconsDir.delete();
                                }

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
                                        } else if (key.equals("isUpdatesManagerExtendedPreferencesFile")) {

                                        } else if (key.equals("groups")) {

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
                                List<Group> groups = gson.fromJson(groupsJson, new TypeToken<List<Group>>() {
                                }.getType());
                                setGroups(groups);
                            }

                        } else if (name.startsWith("Icons/")) {
                            if (!iconsDir.exists()) iconsDir.mkdirs();
                            File outFile = new File(context.getFilesDir() + "/" + name);

                            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                            }
                        }

                        zis.closeEntry();
                    }
                }

                Intent intent = new Intent("com.senliast.updatesmanagerextended.BROADCAST");
                intent.putExtra("type", "event");
                intent.putExtra("event_name", "on_preferences_imported");

                LocalBroadcastManager.getInstance(MyApplication.getAppContext())
                        .sendBroadcast(intent);

                Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getText(R.string.preferences_successfully_imported) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();

                upgradePreferencesDatabase();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return;
        }
        // ========================================

        Toast.makeText(context, MyApplication.getAppContext().getText(R.string.error_importing_preferences) + uri.getLastPathSegment(), Toast.LENGTH_LONG).show();
    }
    private void saveGroupsFromJsonCompatibility(String json) {
        if (json == null || json.isEmpty()) return;
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<GroupInfo>>(){}.getType();
        List<GroupInfo> loadedGroups = gson.fromJson(json, type);
        if (loadedGroups != null) {
            saveGroupsCompatibility(loadedGroups);
        }
    }

    private void saveGroupsCompatibility(List<GroupInfo> groups) {
        Gson gson = new Gson();
        String json = gson.toJson(groups);
        SharedPreferences.Editor editor = MyApplication.getAppContext().getSharedPreferences("Groups", Context.MODE_WORLD_READABLE).edit();
        editor.putString("groups_list", json).apply();
        editor.apply();
    }

    public synchronized void upgradePreferencesDatabase() {
        if (getBooleanPreference("preferencesCreated", false)) {
            // From version code 8 and below (UME 3.1 and below)
            if (getIntPreference("APP_VERSION_CODE", 0) == 0) {
                setBooleanPreference("isFirstStart", !getBooleanPreference("welcomeMessageShown", false));
                deletePreference("welcomeMessageShown");
                List<String> appsToBlockUpdates = Arrays.asList((getStringPreference("appsToBlockUpdates", "")).split(","));
                List<GroupInfo> groups = new ArrayList<>();
                groups.add(new GroupInfo(MyApplication.getAppContext().getResources().getString(R.string.default_group_name), "", String.join(",", appsToBlockUpdates), true, "enabled_immediately", 0L));
                saveGroupsCompatibility(groups);
                deletePreference("appsToBlockUpdates");
                if (getBooleanPreference("moduleEnabled", false)) {
                    setStringPreference("moduleStatus", "enabled_immediately");
                } else {
                    setStringPreference("moduleStatus", "disabled");
                }
                setLongPreference("moduleStatusTime", 0L);
                deletePreference("moduleEnabled");
                setIntPreference("APP_VERSION_CODE", 9);
            }

            // From version code 9 - 10 (UME 4.0 - 4.1)
            if (getIntPreference("APP_VERSION_CODE", 0) == 9 || getIntPreference("APP_VERSION_CODE", 0) == 10) {
                setIntPreference("APP_VERSION_CODE", 11);
            }

            // From version code 11 (UME 4.2)
            if (getIntPreference("APP_VERSION_CODE", 0) == 11) {
                PackageManager pm = context.getPackageManager();
                List<Group> newGroups = new ArrayList<>();
                List<GroupInfo> groups = getGroupsCompatibility();
                for (GroupInfo group : groups) {
                    Set<SavedApp> savedApps = new HashSet<>();
                    List<String> appsToBlockUpdates = Arrays.asList(group.getAppsToBlockUpdates().split(","));
                    for (String app : appsToBlockUpdates) {
                        try {
                            ApplicationInfo installedApp = pm.getApplicationInfo(app, PackageManager.GET_META_DATA);
                            savedApps.add(new SavedApp(installedApp.loadLabel(pm).toString(), app, Status.ENABLED_IMMEDIATELY));
                            saveAppIcon(context, installedApp.loadIcon(pm), app);
                        } catch (Exception e) {

                        }
                    }
                    newGroups.add(new Group(group.getName(), group.getInstallationSources(), savedApps, group.getBlockAllInstallationSources(), group.getStatus(), group.getStatusTime()));
                }
                setGroups(newGroups);
                deletePreference("groups_list");
                deletePreference("groupToEdit");
                context.deleteSharedPreferences("Groups");
                setIntPreference("APP_VERSION_CODE", APP_VERSION_CODE);
            }

            // From version code 12 (UME 5.0)
            if (getIntPreference("APP_VERSION_CODE", 0) == 12) {
                setIntPreference("APP_VERSION_CODE", APP_VERSION_CODE);
            }
        }
    }
}
