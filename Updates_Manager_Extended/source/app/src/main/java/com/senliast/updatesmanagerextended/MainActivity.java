package com.senliast.updatesmanagerextended;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.content.pm.PackageInfo;

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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
    private MyPreferencesBackupManager myPreferencesBackupManager = new MyPreferencesBackupManager(this);
    private SettingsSanitizer settingsSanitizer = new SettingsSanitizer();
    private MaterialAlertDialogBuilder builder;
    private MyObjectBackgroundView mobvStatusPanelBackground;
    private MyObjectBackgroundView mobvSwitchEnabledBackground;
    private long lastUpdateTime;
    private boolean waitingForReboot;
    private View viewDialogWelcome;
    private AlertDialog alertDialogDialogWelcome;
    private Button buttonDialogWelcomeOkButton;
    private final Integer APP_VERSION_CODE = 9;
    private boolean switchModuleEnabledSwitchedListenerEnabled = true;
    private AlertDialog alertDialogDialogDisable;
    private View viewDialogDisable;
    private Button buttonDialogDisablePermanentlyButton;
    private Button buttonDialogDisable15Min;
    private Button buttonDialogDisable30Min;
    private Button buttonDialogDisable45Min;
    private Button buttonDialogDisable60Min;
    private Button buttonDialogDisableCancel;
    private TextView textViewDialogDisableTitle;
    private CountdownTimerHelper timerHelper;
    private boolean dialogDisableAnyOptionSelected = false;
    private final IntentFilter mFilter = new IntentFilter("com.senliast.updatesmanagerextended.BROADCAST");

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            String eventName = intent.getStringExtra("event_name");

            if ("event".equals(type) && "on_preferences_imported".equals(eventName)) {
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
        viewStatusPanel = inflater.inflate(R.layout.panel_status, null);
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
        textViewAppStatus = viewStatusPanel.findViewById(R.id.textViewAppStatus);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mFilter);

        switchModuleEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switchModuleEnabledSwitchedListenerEnabled) {
                    if (isChecked) {
                        myPreferencesManager.setStringPreference("moduleStatus", "enabled_immediately");
                        myPreferencesManager.setLongPreference("moduleStatusTime", 0L);
                        timerHelper.stop();
                        updateGui();
                    } else {
                        dialogDisableAnyOptionSelected = false;
                        viewDialogDisable = getLayoutInflater().inflate(R.layout.dialog_disable, null);
                        builder = new MaterialAlertDialogBuilder(MainActivity.this);
                        buttonDialogDisablePermanentlyButton = viewDialogDisable.findViewById(R.id.buttonPermanently);
                        buttonDialogDisable15Min = viewDialogDisable.findViewById(R.id.button15Min);
                        buttonDialogDisable30Min = viewDialogDisable.findViewById(R.id.button30Min);
                        buttonDialogDisable45Min = viewDialogDisable.findViewById(R.id.button45Min);
                        buttonDialogDisable60Min = viewDialogDisable.findViewById(R.id.button60Min);
                        buttonDialogDisableCancel = viewDialogDisable.findViewById(R.id.buttonCancel);
                        textViewDialogDisableTitle = viewDialogDisable.findViewById(R.id.textViewTitle);
                        textViewDialogDisableTitle.setText(getString(R.string.disable_module));
                        buttonDialogDisablePermanentlyButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myPreferencesManager.setStringPreference("moduleStatus", "disabled");
                                myPreferencesManager.setLongPreference("moduleStatusTime", 0L);
                                alertDialogDialogDisable.dismiss();
                                updateGui();
                                dialogDisableAnyOptionSelected = true;
                            }
                        });
                        buttonDialogDisable15Min.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myPreferencesManager.setStringPreference("moduleStatus", "enabled_since");
                                myPreferencesManager.setLongPreference("moduleStatusTime", System.currentTimeMillis() + (15 * 60 * 1000));
                                alertDialogDialogDisable.dismiss();
                                updateGui();
                                dialogDisableAnyOptionSelected = true;
                            }
                        });
                        buttonDialogDisable30Min.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myPreferencesManager.setStringPreference("moduleStatus", "enabled_since");
                                myPreferencesManager.setLongPreference("moduleStatusTime", System.currentTimeMillis() + (30 * 60 * 1000));
                                alertDialogDialogDisable.dismiss();
                                updateGui();
                                dialogDisableAnyOptionSelected = true;
                            }
                        });
                        buttonDialogDisable45Min.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myPreferencesManager.setStringPreference("moduleStatus", "enabled_since");
                                myPreferencesManager.setLongPreference("moduleStatusTime", System.currentTimeMillis() + (45 * 60 * 1000));
                                alertDialogDialogDisable.dismiss();
                                updateGui();
                                dialogDisableAnyOptionSelected = true;
                            }
                        });
                        buttonDialogDisable60Min.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myPreferencesManager.setStringPreference("moduleStatus", "enabled_since");
                                myPreferencesManager.setLongPreference("moduleStatusTime", System.currentTimeMillis() + (60 * 60 * 1000));
                                alertDialogDialogDisable.dismiss();
                                updateGui();
                                dialogDisableAnyOptionSelected = true;
                            }
                        });
                        buttonDialogDisableCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                switchModuleEnabledSwitchedListenerEnabled = false;
                                switchModuleEnabled.setChecked(true);
                                switchModuleEnabledSwitchedListenerEnabled = true;
                                alertDialogDialogDisable.dismiss();
                                dialogDisableAnyOptionSelected = true;
                            }
                        });
                        builder.setView(viewDialogDisable);
                        alertDialogDialogDisable = builder.create();
                        alertDialogDialogDisable.setCancelable(true);
                        alertDialogDialogDisable.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (!dialogDisableAnyOptionSelected) {
                                    switchModuleEnabledSwitchedListenerEnabled = false;
                                    switchModuleEnabled.setChecked(true);
                                    switchModuleEnabledSwitchedListenerEnabled = true;
                                }
                            }
                        });
                        alertDialogDialogDisable.show();
                    }
                }
            }
        });

        buttonSelectBlacklistedApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GroupSelectionActivity.class);
                startActivity(intent);
            }
        });

        buttonImportSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPreferencesBackupManager.importSettingsFromFile();
            }
        });

        buttonExportSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPreferencesBackupManager.exportSettingsToFile();
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

            if ((System.currentTimeMillis() - SystemClock.uptimeMillis()) < lastUpdateTime) {
                waitingForReboot = true;
            } else {
                waitingForReboot = false;
            }
        } catch (Exception e) {

        }

        timerHelper = new CountdownTimerHelper();

        updateGui();
    }

    // Check if Xposed is active, read settings or create settings file, if not yet, and set GUI state.
    public void updateGui() {
        // "bindTimer()" must be called only if the timer actually needs to start, otherwise it will result in a loop
        // because it itself calls "updateGui()", even if timer didnt start. This way, it will call "updateGui()"
        // only once and "updateGui()" will then not call it again.
        switchModuleEnabledSwitchedListenerEnabled = false;
        if (myPreferencesManager.testPreferences()) {
            // Always upgrade preferences database before doing anything with it.
            myPreferencesManager.upgradePreferencesDatabase();

            createPreferences();

            if (myPreferencesManager.getStringPreference("moduleStatus", "disabled").equals("enabled_immediately") || (myPreferencesManager.getStringPreference("moduleStatus", "disabled").equals("enabled_since") && System.currentTimeMillis() >= myPreferencesManager.getLongPreference("moduleStatusTime", 0L))) {
                textViewAppStatus.setText(R.string.app_status_loaded_enabled);
                mobvStatusPanelBackground.setRectangleColor(Color.GREEN);
                imageViewStatusPanelSymbol.setImageResource(R.drawable.green_check_icon);
                switchModuleEnabledSwitchedListenerEnabled = false;
                switchModuleEnabled.setChecked(true);
                switchModuleEnabledSwitchedListenerEnabled = true;
                settingsSanitizer.sanitize();
            } else {
                if (myPreferencesManager.getStringPreference("moduleStatus", "disabled").equals("enabled_since") && System.currentTimeMillis() < myPreferencesManager.getLongPreference("moduleStatusTime", 0L)) {
                    mobvStatusPanelBackground.setRectangleColor(Color.YELLOW);
                    imageViewStatusPanelSymbol.setImageResource(R.drawable.yellow_exclamation_mark_icon);
                    switchModuleEnabledSwitchedListenerEnabled = false;
                    switchModuleEnabled.setChecked(false);
                    bindTimer();
                } else {
                    textViewAppStatus.setText(R.string.app_status_loaded_disabled);
                    mobvStatusPanelBackground.setRectangleColor(Color.YELLOW);
                    imageViewStatusPanelSymbol.setImageResource(R.drawable.yellow_exclamation_mark_icon);
                    switchModuleEnabledSwitchedListenerEnabled = false;
                    switchModuleEnabled.setChecked(false);
                }
            }

            switchModuleEnabled.setEnabled(true);
            buttonSelectBlacklistedApps.setEnabled(true);
            buttonImportSettings.setEnabled(true);
            buttonExportSettings.setEnabled(true);

            if (myPreferencesManager.getBooleanPreference("isFirstStart", true)) {
                myPreferencesManager.setBooleanPreference("isFirstStart", false);
                showWelcomeDialog();
            }

            if (waitingForReboot) {
                textViewAppStatus.setText(R.string.app_status_waiting_for_reboot);
                mobvStatusPanelBackground.setRectangleColor(Color.YELLOW);
                imageViewStatusPanelSymbol.setImageResource(R.drawable.yellow_exclamation_mark_icon);
                switchModuleEnabled.setEnabled(false);
                buttonSelectBlacklistedApps.setEnabled(false);
                buttonImportSettings.setEnabled(false);
                buttonExportSettings.setEnabled(false);
            }
        } else {
            switchModuleEnabledSwitchedListenerEnabled = false;
            textViewAppStatus.setText(R.string.app_status_not_loaded);
            mobvStatusPanelBackground.setRectangleColor(Color.RED);
            imageViewStatusPanelSymbol.setImageResource(R.drawable.red_cross_icon);
            switchModuleEnabled.setChecked(false);
            switchModuleEnabled.setEnabled(false);
            buttonSelectBlacklistedApps.setEnabled(false);
            buttonImportSettings.setEnabled(false);
            buttonExportSettings.setEnabled(false);
        }
        switchModuleEnabledSwitchedListenerEnabled = true;
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
        builder.setView(viewDialogWelcome);
        alertDialogDialogWelcome = builder.create();
        alertDialogDialogWelcome.setCancelable(false);
        alertDialogDialogWelcome.show();
    }

    private void bindTimer() {
        timerHelper.stop();

        long endTime = myPreferencesManager.getLongPreference("moduleStatusTime" ,0L);

        timerHelper.startCountdown(endTime,
                timeLeft -> textViewAppStatus.setText(MyApplication.getAppContext().getString(R.string.app_status_paused) + timeLeft),
                () -> {
                    myPreferencesManager.setStringPreference("moduleStatus", "enabled_immediately");
                    myPreferencesManager.setLongPreference("moduleStatusTime", 0L);
                    updateGui();
                });
    }

    private void createPreferences() {
        if (!myPreferencesManager.testPreference("preferencesCreated")) {
            myPreferencesManager.setBooleanPreference("preferencesCreated", true);
            myPreferencesManager.setStringPreference("moduleStatus", "enabled_immediately");
            myPreferencesManager.setLongPreference("moduleStatusTime", 0l);
            myPreferencesManager.setIntPreference("APP_VERSION_CODE", APP_VERSION_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGui();
    }
}

