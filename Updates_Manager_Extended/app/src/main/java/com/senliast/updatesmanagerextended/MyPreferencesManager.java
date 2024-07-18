package com.senliast.updatesmanagerextended;

import android.content.Context;
import android.content.SharedPreferences;

import com.senliast.MyApplication;

public class MyPreferencesManager {

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

    public Boolean testPreferences() {
        SharedPreferences preferences;
        try {
            preferences = MyApplication.getAppContext().getSharedPreferences("Preferences", Context.MODE_WORLD_READABLE);
            return true;
        } catch (SecurityException ignored) {
            return false;
        }
    }
}
