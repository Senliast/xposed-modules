package com.senliast.updatesmanagerextended;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import com.senliast.MyApplication;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
    private MyPreferencesManager myPreferencesManager = new MyPreferencesManager();
    private PreferencesBackupManager preferencesBackupManager = new PreferencesBackupManager(this);
    private AppsToBlockUpdatesListSanitizer appsToBlockUpdatesListSanitizer = new AppsToBlockUpdatesListSanitizer();
    private MaterialAlertDialogBuilder builder;
    private MyObjectBackgroundView mobvStatusPanelBackground;
    private MyObjectBackgroundView mobvSwitchEnabledBackground;
    private long lastUpdateTime;
    private boolean waitingForReboot;
    private View viewDialogWelcome;
    private AlertDialog alertDialogDialogWelcome;
    private Button buttonDialogWelcomeOkButton;


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
        mobvStatusPanelBackground = viewStatusPanel.findViewById(R.id.mobvStatusPanelBackground);
        switchModuleEnabled = findViewById(R.id.switchEnableModule);
        buttonSelectBlacklistedApps = findViewById(R.id.buttonSelectBlacklistedApps);
        buttonImportSettings = findViewById(R.id.buttonImportSettings);
        buttonExportSettings = findViewById(R.id.buttonExportSettings);
        buttonAbout = findViewById(R.id.buttonAbout);
        mobvSwitchEnabledBackground = findViewById(R.id.mobvSwitchEnableModule);

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
                    mobvStatusPanelBackground.setRectangleColor(Color.YELLOW);
                    imageViewStatusPanelSymbol.setImageResource(R.drawable.yellow_exclamation_mark_icon);
                } else {
                    if (isChecked) {
                        textViewAppStatus.setText(R.string.app_status_loaded_enabled);
                        mobvStatusPanelBackground.setRectangleColor(Color.GREEN);
                        imageViewStatusPanelSymbol.setImageResource(R.drawable.green_check_icon);
                        myPreferencesManager.setBooleanPreference("moduleEnabled", true);
                    } else {
                        textViewAppStatus.setText(R.string.app_status_loaded_disabled);
                        mobvStatusPanelBackground.setRectangleColor(Color.YELLOW);
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

        if (Utils.isDarkModeActive()) {
            mobvSwitchEnabledBackground.setRectangleColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_primary_container));
        } else {
            mobvSwitchEnabledBackground.setRectangleColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_light_primary_container));
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
                mobvStatusPanelBackground.setRectangleColor(Color.GREEN);
                imageViewStatusPanelSymbol.setImageResource(R.drawable.green_check_icon);
                switchModuleEnabled.setChecked(true);
                appsToBlockUpdatesListSanitizer.sanitizeAppsToBlockUpdatesList();
            } else {
                textViewAppStatus.setText(R.string.app_status_loaded_disabled);
                mobvStatusPanelBackground.setRectangleColor(Color.YELLOW);
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
                mobvStatusPanelBackground.setRectangleColor(Color.YELLOW);
                imageViewStatusPanelSymbol.setImageResource(R.drawable.yellow_exclamation_mark_icon);
            }
        } else {
            textViewAppStatus.setText(R.string.app_status_not_loaded);
            mobvStatusPanelBackground.setRectangleColor(Color.RED);
            imageViewStatusPanelSymbol.setImageResource(R.drawable.red_cross_icon);
            switchModuleEnabled.setChecked(false);
            switchModuleEnabled.setEnabled(false);
            buttonSelectBlacklistedApps.setEnabled(false);
            buttonImportSettings.setEnabled(false);
            buttonExportSettings.setEnabled(false);
        }
    }

    public void showWelcomeDialog() {
        viewDialogWelcome = getLayoutInflater().inflate(R.layout.dialog_welcome, null);
        builder = new MaterialAlertDialogBuilder(this);
        buttonDialogWelcomeOkButton = viewDialogWelcome.findViewById(R.id.buttonOk);
        buttonDialogWelcomeOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogDialogWelcome.dismiss();
            }
        });
        alertDialogDialogWelcome = builder.create();
        builder.setView(viewDialogWelcome);
        alertDialogDialogWelcome.setCancelable(false);
        alertDialogDialogWelcome = builder.create();
        alertDialogDialogWelcome.show();
    }
}

