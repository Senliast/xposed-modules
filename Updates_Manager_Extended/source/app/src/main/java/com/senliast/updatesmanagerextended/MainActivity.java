package com.senliast.updatesmanagerextended;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.content.pm.PackageInfo;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import com.senliast.MyApplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends AppCompatActivity {
    private MaterialCardView cardStatus;
    private ImageView imageStatusIcon;
    private TextView textStatus;
    private MaterialCardView cardEnableModule;
    private MaterialSwitch switchModuleEnabled;
    private MaterialCardView cardSelectGroups;
    private MaterialCardView cardImport;
    private MaterialCardView cardExport;
    private MaterialCardView cardAbout;
    private PreferencesManager preferencesManager = new PreferencesManager();
    private PreferencesBackupManager preferencesBackupManager = new PreferencesBackupManager(this);
    private SettingsSanitizer settingsSanitizer = new SettingsSanitizer();
    private MaterialAlertDialogBuilder builder;
    private long lastUpdateTime;
    private boolean waitingForReboot;
    private View viewDialogWelcome;
    private AlertDialog alertDialogDialogWelcome;
    private Button buttonDialogWelcomeOkButton;
    private final Integer APP_VERSION_CODE = 12;
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
    Context contextTheme = new ContextThemeWrapper(MyApplication.getAppContext(), R.style.Theme_MyApp);
    private final int GREEN = getSemanticColor(contextTheme, Color.rgb(76, 175, 80));
    private final int YELLOW = getSemanticColor(contextTheme, Color.rgb(255, 193, 7));
    private final int RED = getSemanticColor(contextTheme, Color.rgb(244, 67, 54));

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
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DynamicColors.applyToActivityIfAvailable(this);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(!Utils.isDarkModeActive());
        controller.setAppearanceLightNavigationBars(!Utils.isDarkModeActive());

        cardStatus = findViewById(R.id.cardStatus);
        imageStatusIcon = findViewById(R.id.imageStatusIcon);
        textStatus = findViewById(R.id.textStatus);

        cardEnableModule = findViewById(R.id.cardEnableModule);
        switchModuleEnabled = findViewById(R.id.switchEnableModule);

        cardSelectGroups = findViewById(R.id.cardSelectGroups);
        cardImport = findViewById(R.id.cardImport);
        cardExport = findViewById(R.id.cardExport);
        cardAbout = findViewById(R.id.cardAbout);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mFilter);

        switchModuleEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!switchModuleEnabledSwitchedListenerEnabled) return;

                if (isChecked) {
                    preferencesManager.setModuleStatus(Status.ENABLED_IMMEDIATELY);
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

                    buttonDialogDisablePermanentlyButton.setOnClickListener(v -> {
                        preferencesManager.setModuleStatus(Status.DISABLED);
                        alertDialogDialogDisable.dismiss();
                        updateGui();
                        dialogDisableAnyOptionSelected = true;
                    });

                    buttonDialogDisable15Min.setOnClickListener(v -> {
                        preferencesManager.setModuleStatus(Status.ENABLED_SINCE, System.currentTimeMillis() + (15 * 60 * 1000));
                        alertDialogDialogDisable.dismiss();
                        updateGui();
                        dialogDisableAnyOptionSelected = true;
                    });

                    buttonDialogDisable30Min.setOnClickListener(v -> {
                        preferencesManager.setModuleStatus(Status.ENABLED_SINCE, System.currentTimeMillis() + (30 * 60 * 1000));
                        alertDialogDialogDisable.dismiss();
                        updateGui();
                        dialogDisableAnyOptionSelected = true;
                    });

                    buttonDialogDisable45Min.setOnClickListener(v -> {
                        preferencesManager.setModuleStatus(Status.ENABLED_SINCE, System.currentTimeMillis() + (45 * 60 * 1000));
                        alertDialogDialogDisable.dismiss();
                        updateGui();
                        dialogDisableAnyOptionSelected = true;
                    });

                    buttonDialogDisable60Min.setOnClickListener(v -> {
                        preferencesManager.setModuleStatus(Status.ENABLED_SINCE, System.currentTimeMillis() + (60 * 60 * 1000));
                        alertDialogDialogDisable.dismiss();
                        updateGui();
                        dialogDisableAnyOptionSelected = true;
                    });

                    buttonDialogDisableCancel.setOnClickListener(v -> {
                        switchModuleEnabledSwitchedListenerEnabled = false;
                        switchModuleEnabled.setChecked(true);
                        switchModuleEnabledSwitchedListenerEnabled = true;
                        alertDialogDialogDisable.dismiss();
                        dialogDisableAnyOptionSelected = true;
                    });

                    builder.setView(viewDialogDisable);
                    alertDialogDialogDisable = builder.create();
                    alertDialogDialogDisable.setCancelable(true);
                    alertDialogDialogDisable.setOnDismissListener(dialog -> {
                        if (!dialogDisableAnyOptionSelected) {
                            switchModuleEnabledSwitchedListenerEnabled = false;
                            switchModuleEnabled.setChecked(true);
                            switchModuleEnabledSwitchedListenerEnabled = true;
                        }
                    });
                    alertDialogDialogDisable.show();
                }
            }
        });

        cardSelectGroups.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GroupSelectionActivity.class);
            startActivity(intent);
        });

        cardImport.setOnClickListener(v -> preferencesBackupManager.importSettingsFromFile());

        cardExport.setOnClickListener(v -> preferencesBackupManager.exportSettingsToFile());

        cardAbout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            lastUpdateTime = packageInfo.lastUpdateTime;
            waitingForReboot = (System.currentTimeMillis() - SystemClock.uptimeMillis()) < lastUpdateTime;
        } catch (Exception e) {
            waitingForReboot = false;
        }

        timerHelper = new CountdownTimerHelper();
        updateGui();
    }

    public void updateGui() {
        switchModuleEnabledSwitchedListenerEnabled = false;

        if (preferencesManager.testPreferences()) {
            createPreferences();

            String status = preferencesManager.getModuleStatus();
            long statusTime = preferencesManager.getModuleStatusTime();

            if (status.equals(Status.ENABLED_IMMEDIATELY)
                    || (status.equals(Status.ENABLED_SINCE) && System.currentTimeMillis() >= statusTime)) {
                setStatus(R.string.app_status_loaded_enabled, GREEN, R.drawable.green_check_icon);
                switchModuleEnabled.setChecked(true);
                timerHelper.stop();
                settingsSanitizer.sanitize();
            } else if (status.equals(Status.ENABLED_SINCE) && System.currentTimeMillis() < statusTime) {
                setStatus(R.string.app_status_paused, YELLOW, R.drawable.yellow_exclamation_mark_icon);
                switchModuleEnabled.setChecked(false);
                bindTimer();
            } else {
                setStatus(R.string.app_status_loaded_disabled, YELLOW, R.drawable.yellow_exclamation_mark_icon);
                switchModuleEnabled.setChecked(false);
            }

            setControlsEnabled(true);

            if (preferencesManager.getIsFirstStart()) {
                preferencesManager.setIsNotFirstStart();
                showWelcomeDialog();
            }

            if (waitingForReboot) {
                setStatus(R.string.app_status_waiting_for_reboot, YELLOW, R.drawable.yellow_exclamation_mark_icon);
                setControlsEnabled(false);
            }
        } else {
            setStatus(R.string.app_status_not_loaded, RED, R.drawable.red_cross_icon);
            switchModuleEnabled.setChecked(false);
            setControlsEnabled(false);
        }

        switchModuleEnabledSwitchedListenerEnabled = true;
    }

    public static int getSemanticColor(Context context, int color) {
        return MaterialColors.harmonizeWithPrimary(context, color);
    }

    private void setStatus(int textRes, int color, int iconRes) {
        textStatus.setText(textRes);
        cardStatus.setCardBackgroundColor(color);
        imageStatusIcon.setImageResource(iconRes);
        textStatus.setTextColor(Color.WHITE);
    }

    private void setControlsEnabled(boolean enabled) {
        switchModuleEnabled.setEnabled(enabled);
        cardEnableModule.setEnabled(enabled);
        cardSelectGroups.setEnabled(enabled);
        cardImport.setEnabled(enabled);
        cardExport.setEnabled(enabled);

        float alpha = enabled ? 1.0f : 0.5f;
        cardEnableModule.setAlpha(alpha);
        cardSelectGroups.setAlpha(alpha);
        cardImport.setAlpha(alpha);
        cardExport.setAlpha(alpha);
    }

    public void showWelcomeDialog() {
        viewDialogWelcome = getLayoutInflater().inflate(R.layout.dialog_welcome, null);
        builder = new MaterialAlertDialogBuilder(this);
        buttonDialogWelcomeOkButton = viewDialogWelcome.findViewById(R.id.buttonOk);
        buttonDialogWelcomeOkButton.setOnClickListener(v -> alertDialogDialogWelcome.dismiss());
        builder.setView(viewDialogWelcome);
        alertDialogDialogWelcome = builder.create();
        alertDialogDialogWelcome.setCancelable(false);
        alertDialogDialogWelcome.show();
    }

    private void bindTimer() {
        timerHelper.stop();

        long endTime = preferencesManager.getModuleStatusTime();

        timerHelper.startCountdown(endTime,
                timeLeft -> textStatus.setText(MyApplication.getAppContext().getString(R.string.app_status_paused) + timeLeft),
                () -> {
                    preferencesManager.setModuleStatus(Status.ENABLED_IMMEDIATELY);
                    updateGui();
                });
    }

    private void createPreferences() {
        if (!preferencesManager.testPreference("preferencesCreated")) {
            preferencesManager.setBooleanPreference("preferencesCreated", true);
            preferencesManager.setModuleStatus(Status.ENABLED_IMMEDIATELY);
            preferencesManager.setIntPreference("APP_VERSION_CODE", APP_VERSION_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGui();
    }
}