package com.senliast.updatesmanagerextended;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView textViewAppStatus;
    private View viewStatusPanel;
    LayoutInflater inflater;
    FrameLayout frameLayoutStatusPanel;
    private ImageView imageViewStatusPanelSymbol;
    private ImageView imageViewStatusPanelBackground;
    private MaterialSwitch switchModuleEnabled;
    private Button buttonSelectBlacklistedApps;
    private Button buttonImportSettings;
    private Button buttonExportSettings;
    private Button buttonAbout;
    private int[][] colorStatesForSwitch;
    private int[] trackColorsForSwitch;
    private ColorStateList trackColorStateListForSwitch;
    private MyPreferencesManager myPreferencesManager = new MyPreferencesManager();
    private PreferencesBackupManager preferencesBackupManager = new PreferencesBackupManager(this);
    private AppsToBlockUpdatesListSanitizer appsToBlockUpdatesListSanitizer = new AppsToBlockUpdatesListSanitizer();
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityAbout), (v, insets) -> {
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
        imageViewStatusPanelBackground = viewStatusPanel.findViewById(R.id.imageViewStatusPanelBackground);
        switchModuleEnabled = findViewById(R.id.switchEnableModule);
        buttonSelectBlacklistedApps = findViewById(R.id.buttonSelectBlacklistedApps);
        buttonImportSettings = findViewById(R.id.buttonImportSettings);
        buttonExportSettings = findViewById(R.id.buttonExportSettings);
        buttonAbout = findViewById(R.id.buttonAbout);
        colorStatesForSwitch = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked }
        };
        trackColorsForSwitch = new int[] {
                MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, MyApplication.getAppContext().getColor(R.color.primary)),
                MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorBackground, Color.WHITE)
        };
        trackColorStateListForSwitch = new ColorStateList(colorStatesForSwitch, trackColorsForSwitch);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mReceiver, mFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mReceiver, mFilter);
        }

        switchModuleEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    textViewAppStatus.setText(R.string.app_status_loaded_enabled);
                    imageViewStatusPanelBackground.setImageResource(R.drawable.status_panel_background_green);
                    imageViewStatusPanelSymbol.setImageResource(R.drawable.green_check_icon);
                    myPreferencesManager.setBooleanPreference("moduleEnabled", true);
                } else {
                    textViewAppStatus.setText(R.string.app_status_loaded_disabled);
                    imageViewStatusPanelBackground.setImageResource(R.drawable.status_panel_background_yellow);
                    imageViewStatusPanelSymbol.setImageResource(R.drawable.yellow_exclamation_mark_icon);
                    myPreferencesManager.setBooleanPreference("moduleEnabled", false);
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
        switchModuleEnabled.setTrackTintList(trackColorStateListForSwitch);
        findViewById(R.id.activityAbout).setBackgroundColor(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorBackground, getColor(R.color.background)));

        updateGui();
    }

    // Check if Xposed is active, read settings or create settings file, if not yet, and set GUI state.
    public void updateGui() {
        if (myPreferencesManager.testPreferences()) {
            SharedPreferences preferences = myPreferencesManager.getSharedPreferences();
            if (!preferences.contains("preferencesCreated")) {
                myPreferencesManager.setBooleanPreference("preferencesCreated", true);
                myPreferencesManager.setBooleanPreference("moduleEnabled", true);
                myPreferencesManager.setBooleanPreference("isUpdatesManagerExtendedPreferencesFile", true);
            }
            if (myPreferencesManager.getBooleanPreference("moduleEnabled", true)) {
                textViewAppStatus.setText(R.string.app_status_loaded_enabled);
                imageViewStatusPanelBackground.setImageResource(R.drawable.status_panel_background_green);
                imageViewStatusPanelSymbol.setImageResource(R.drawable.green_check_icon);
                switchModuleEnabled.setChecked(true);
                appsToBlockUpdatesListSanitizer.sanitizeAppsToBlockUpdatesList();
            } else {
                textViewAppStatus.setText(R.string.app_status_loaded_disabled);
                imageViewStatusPanelBackground.setImageResource(R.drawable.status_panel_background_yellow);
                imageViewStatusPanelSymbol.setImageResource(R.drawable.yellow_exclamation_mark_icon);
                switchModuleEnabled.setChecked(false);
            }
            switchModuleEnabled.setEnabled(true);
            buttonSelectBlacklistedApps.setEnabled(true);
            buttonImportSettings.setEnabled(true);
            buttonExportSettings.setEnabled(true);
        } else {
            textViewAppStatus.setText(R.string.app_status_not_loaded);
            imageViewStatusPanelBackground.setImageResource(R.drawable.status_panel_background_red);
            imageViewStatusPanelSymbol.setImageResource(R.drawable.red_cross_icon);
            switchModuleEnabled.setChecked(false);
            switchModuleEnabled.setEnabled(false);
            buttonSelectBlacklistedApps.setEnabled(false);
            buttonImportSettings.setEnabled(false);
            buttonExportSettings.setEnabled(false);
        }
    }
}

