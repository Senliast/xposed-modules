package com.senliast.updatesmanagerextended;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.materialswitch.MaterialSwitch;

import com.senliast.MyApplication;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView textViewAppStatus;
    private View viewStatusPanel;
    LayoutInflater inflater;
    FrameLayout frameLayoutStatusPanel;
    private ImageView imageViewStatusPanelSymbol;
    private MaterialSwitch switchModuleEnabled;
    private Button buttonSelectBlacklistedApps;
    private Button buttonImportSettings;
    private Button buttonExportSettings;
    private Button buttonAbout;
    private int[][] colorStatesForSwitch;
    private int[] trackColorsForSwitchActive;
    private int[] trackColorsForSwitchInactive;
    private int[] thumbColorsForSwitch;
    private ColorStateList trackColorStateListForSwitchActive;
    private ColorStateList trackColorStateListForSwitchInactive;
    private ColorStateList thumbColorStateListForSwitch;
    private MyPreferencesManager myPreferencesManager = new MyPreferencesManager();
    private PreferencesBackupManager preferencesBackupManager = new PreferencesBackupManager(this);
    private AppsToBlockUpdatesListSanitizer appsToBlockUpdatesListSanitizer = new AppsToBlockUpdatesListSanitizer();
    private AlertDialog dialog;
    AlertDialog.Builder builder;
    private MyObjectBackgroundView viewStatusPanelBackground;
    private long lastUpdateTime;
    private boolean waitingForReboot;

    private final IntentFilter mFilter = new IntentFilter("com.senliast.updatesmanagerextended.BROADCAST");
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getString("type").equals("event") && intent.getExtras().getString("event_name").equals("on_preferences_imported")) {
                updateGui();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        viewStatusPanel = inflater.inflate(R.layout.status_panel, null);
        frameLayoutStatusPanel = findViewById(R.id.layoutStatusPanel);
        frameLayoutStatusPanel.addView(viewStatusPanel);
        textViewAppStatus = viewStatusPanel.findViewById(R.id.textViewAppStatus);
        imageViewStatusPanelSymbol = viewStatusPanel.findViewById(R.id.imageViewStatusPanelSymbol);
        viewStatusPanelBackground = viewStatusPanel.findViewById(R.id.viewStatusPanelBackground);
        switchModuleEnabled = findViewById(R.id.switchEnableModule);
        buttonSelectBlacklistedApps = findViewById(R.id.buttonSelectBlacklistedApps);
        buttonImportSettings = findViewById(R.id.buttonImportSettings);
        buttonExportSettings = findViewById(R.id.buttonExportSettings);
        buttonAbout = findViewById(R.id.buttonAbout);

        colorStatesForSwitch = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked }
        };

        trackColorsForSwitchActive = new int[] {
                MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_primary),
                MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_secondary)
        };

        if (Utils.isDarkModeActive()) {
            trackColorsForSwitchInactive = new int[]{
                    MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_ref_palette_dynamic_primary20),
                    MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_ref_palette_dynamic_secondary20)
            };
        } else {
            trackColorsForSwitchInactive = new int[]{
                    MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_ref_palette_dynamic_primary90),
                    MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_ref_palette_dynamic_secondary90)
            };
        }

        thumbColorsForSwitch = new int[] {
                MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_primary_container),
                MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_secondary_container)
        };

        trackColorStateListForSwitchActive = new ColorStateList(colorStatesForSwitch, trackColorsForSwitchActive);
        trackColorStateListForSwitchInactive = new ColorStateList(colorStatesForSwitch, trackColorsForSwitchInactive);
        thumbColorStateListForSwitch = new ColorStateList(colorStatesForSwitch, thumbColorsForSwitch);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mReceiver, mFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mReceiver, mFilter);
        }

        switchModuleEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (waitingForReboot) {
                    textViewAppStatus.setText(R.string.app_status_waiting_for_reboot);
                    viewStatusPanelBackground.setRectangleColor(Color.YELLOW);
                    imageViewStatusPanelSymbol.setImageResource(R.drawable.yellow_exclamation_mark_icon);
                } else {
                    if (isChecked) {
                        textViewAppStatus.setText(R.string.app_status_loaded_enabled);
                        viewStatusPanelBackground.setRectangleColor(Color.GREEN);
                        imageViewStatusPanelSymbol.setImageResource(R.drawable.green_check_icon);
                        myPreferencesManager.setBooleanPreference("moduleEnabled", true);
                    } else {
                        textViewAppStatus.setText(R.string.app_status_loaded_disabled);
                        viewStatusPanelBackground.setRectangleColor(Color.YELLOW);
                        imageViewStatusPanelSymbol.setImageResource(R.drawable.yellow_exclamation_mark_icon);
                        myPreferencesManager.setBooleanPreference("moduleEnabled", false);
                    }
                }
            }
        });

        buttonSelectBlacklistedApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AppSelectionActivity.class);
                startActivity(intent);
            }
        });

        buttonImportSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferencesBackupManager.importSettingsFromFile();
            }
        });

        buttonExportSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferencesBackupManager.exportSettingsToFile();
            }
        });

        buttonAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        buttonSelectBlacklistedApps.setBackgroundColor(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, getColor(R.color.primary)));
        buttonImportSettings.setBackgroundColor(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, getColor(R.color.primary)));
        buttonExportSettings.setBackgroundColor(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, getColor(R.color.primary)));
        buttonAbout.setBackgroundColor(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, getColor(R.color.primary)));
        switchModuleEnabled.setTrackTintList(trackColorStateListForSwitchActive);
        switchModuleEnabled.setThumbTintList(thumbColorStateListForSwitch);

        if (Utils.isDarkModeActive()) {
            findViewById(R.id.activityMain).setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_surface));
        } else {
            findViewById(R.id.activityMain).setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_light_surface));
        }

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            lastUpdateTime = packageInfo.lastUpdateTime;
            if ((System.currentTimeMillis() - SystemClock.uptimeMillis()) < lastUpdateTime ) {
                waitingForReboot = true;
            } else {
                waitingForReboot = false;
            }
        } catch (PackageManager.NameNotFoundException e) {

        }

        updateGui();
    }

    // Check if Xposed is active, read settings or create settings file, if not yet, and set GUI state.
    public void updateGui() {
        if (myPreferencesManager.testPreferences()) {
            SharedPreferences preferences = myPreferencesManager.getSharedPreferences();
            if (!preferences.contains("preferencesCreated")) { myPreferencesManager.setBooleanPreference("preferencesCreated", true); }
            if (!preferences.contains("moduleEnabled")) { myPreferencesManager.setBooleanPreference("moduleEnabled", true); }
            if (!preferences.contains("isUpdatesManagerExtendedPreferencesFile"))  { myPreferencesManager.setBooleanPreference("isUpdatesManagerExtendedPreferencesFile", true); }
            if (!preferences.contains("welcomeMessageShown")) { myPreferencesManager.setBooleanPreference("welcomeMessageShown", false); }

            if (myPreferencesManager.getBooleanPreference("moduleEnabled", true)) {
                textViewAppStatus.setText(R.string.app_status_loaded_enabled);
                viewStatusPanelBackground.setRectangleColor(Color.GREEN);
                imageViewStatusPanelSymbol.setImageResource(R.drawable.green_check_icon);
                switchModuleEnabled.setChecked(true);
                appsToBlockUpdatesListSanitizer.sanitizeAppsToBlockUpdatesList();
            } else {
                textViewAppStatus.setText(R.string.app_status_loaded_disabled);
                viewStatusPanelBackground.setRectangleColor(Color.YELLOW);
                imageViewStatusPanelSymbol.setImageResource(R.drawable.yellow_exclamation_mark_icon);
                switchModuleEnabled.setChecked(false);
            }

            switchModuleEnabled.setEnabled(true);
            buttonSelectBlacklistedApps.setEnabled(true);
            buttonImportSettings.setEnabled(true);
            buttonExportSettings.setEnabled(true);

            if (!myPreferencesManager.getBooleanPreference("welcomeMessageShown", false)) {
                myPreferencesManager.setBooleanPreference("welcomeMessageShown", true);
                showWelcomeDialog();
            }
            if (waitingForReboot) {
                textViewAppStatus.setText(R.string.app_status_waiting_for_reboot);
                viewStatusPanelBackground.setRectangleColor(Color.YELLOW);
                imageViewStatusPanelSymbol.setImageResource(R.drawable.yellow_exclamation_mark_icon);
            }
        } else {
            textViewAppStatus.setText(R.string.app_status_not_loaded);
            viewStatusPanelBackground.setRectangleColor(Color.RED);
            imageViewStatusPanelSymbol.setImageResource(R.drawable.red_cross_icon);
            switchModuleEnabled.setChecked(false);
            switchModuleEnabled.setEnabled(false);
            buttonSelectBlacklistedApps.setEnabled(false);
            buttonImportSettings.setEnabled(false);
            buttonExportSettings.setEnabled(false);
            if (Utils.isDarkModeActive()) {
                buttonSelectBlacklistedApps.setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_ref_palette_dynamic_primary10));
                buttonImportSettings.setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_ref_palette_dynamic_primary10));
                buttonExportSettings.setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_ref_palette_dynamic_primary10));
            } else {
                buttonSelectBlacklistedApps.setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_ref_palette_dynamic_primary90));
                buttonImportSettings.setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_ref_palette_dynamic_primary90));
                buttonExportSettings.setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_ref_palette_dynamic_primary90));
            }
            switchModuleEnabled.setTrackTintList(trackColorStateListForSwitchInactive);
        }
    }

    private void showWelcomeDialog() {
        builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.welcome_message_title);
        builder.setMessage(R.string.info_always_reboot_after_update);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        if (Utils.isDarkModeActive()) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_surface)));
        } else {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_light_surface)));
        }
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getColor(R.color.text_adaptive));
    }
}

