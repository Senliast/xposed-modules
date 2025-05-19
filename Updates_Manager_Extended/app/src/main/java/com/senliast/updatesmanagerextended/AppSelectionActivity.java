package com.senliast.updatesmanagerextended;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import com.google.android.material.materialswitch.MaterialSwitch;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.senliast.MyApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AppSelectionActivity extends AppCompatActivity {

    private ArrayList<AppInfo> appList;
    private List<AppListItem> appListToShow = new ArrayList<>();
    private TextInputEditText textInputEditText;
    private MaterialSwitch switchShowSystemApps;
    private MaterialSwitch switchBlockedFirst;
    private RecyclerView viewAppList;
    private AppListItemAdapter appListItemAdapter;
    private MyPreferencesManager myPreferencesManager = new MyPreferencesManager();
    private List<String> appsToBlockUpdates = new ArrayList<>();
    private Button buttonBack;
    private int[][] colorStatesForSwitch;
    private int[] trackColorsForSwitch;
    private int[] thumbColorsForSwitch;
    private ColorStateList trackColorStateListForSwitch;
    private ColorStateList thumbColorStateListForSwitch;
    private LinearProgressIndicator linearProgressIndicatorLoading;
    private AlertDialog dialogLoading;
    private View viewDialogLoading;
    AlertDialog.Builder builder;
    private final IntentFilter mFilter = new IntentFilter("com.senliast.updatesmanagerextended.BROADCAST");
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getString("type").equals("event") && intent.getExtras().getString("event_name").equals("on_app_switch_toggled")) {
                appsToBlockUpdates = new ArrayList<>(Arrays.asList((myPreferencesManager.getStringPreference("appsToBlockUpdates", "")).split(",")));
                updateAppListToShow();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_app_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appSelection), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonBack = findViewById(R.id.buttonBack);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        textInputEditText = findViewById(R.id.inputFieldFilter);
        switchShowSystemApps = findViewById(R.id.switchShowSystemApps);
        viewAppList = findViewById(R.id.viewAppList);
        appListItemAdapter = new AppListItemAdapter(appListToShow);
        viewAppList.setLayoutManager(new LinearLayoutManager(this));
        viewAppList.setAdapter(appListItemAdapter);
        switchBlockedFirst = findViewById(R.id.switchBlockedFirst);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, mFilter);
        colorStatesForSwitch = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked }
        };
        trackColorsForSwitch = new int[] {
                MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_primary),
                MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_secondary)
        };

        thumbColorsForSwitch = new int[] {
                MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_primary_container),
                MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_secondary_container)
        };
        trackColorStateListForSwitch = new ColorStateList(colorStatesForSwitch, trackColorsForSwitch);
        thumbColorStateListForSwitch = new ColorStateList(colorStatesForSwitch, thumbColorsForSwitch);

        switchShowSystemApps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    updateAppList(1);
                } else {
                    updateAppList(0);
                }
            }
        });

        switchBlockedFirst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateAppListToShow();
                appListItemAdapter.notifyDataSetChanged();
            }
        });

        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateAppListToShow();
                appListItemAdapter.notifyDataSetChanged();
            }
        });

        buttonBack.setBackgroundColor(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, getColor(R.color.primary)));
        switchShowSystemApps.setTrackTintList(trackColorStateListForSwitch);
        switchShowSystemApps.setThumbTintList(thumbColorStateListForSwitch);
        switchBlockedFirst.setTrackTintList(trackColorStateListForSwitch);
        switchBlockedFirst.setThumbTintList(thumbColorStateListForSwitch);

        builder = new AlertDialog.Builder(this);
        viewDialogLoading = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        builder.setView(viewDialogLoading);
        builder.setCancelable(false);
        dialogLoading = builder.create();
        dialogLoading.setOnKeyListener((dialogInterface, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        linearProgressIndicatorLoading = viewDialogLoading.findViewById(R.id.progressBar);
        linearProgressIndicatorLoading.setIndicatorColor(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, getColor(R.color.primary)));
        linearProgressIndicatorLoading.setTrackColor(Utils.changeColorAlpha(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, getColor(R.color.primary)), 30));
        if (Utils.isDarkModeActive()) {
            findViewById(R.id.appSelection).setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_surface));
        } else {
            findViewById(R.id.appSelection).setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_light_surface));
        }
        if (Utils.isDarkModeActive()) {
            viewDialogLoading.findViewById(R.id.dialogLoading).setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_surface));
        } else {
            viewDialogLoading.findViewById(R.id.dialogLoading).setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_light_surface));
        }

        appsToBlockUpdates = new ArrayList<>(Arrays.asList((myPreferencesManager.getStringPreference("appsToBlockUpdates", "")).split(",")));

        if (switchShowSystemApps.isChecked()) {
            updateAppList(1);
        } else {
            updateAppList(0);
        }
    }

    private void updateAppList(Integer includeSystemApps) {
        dialogLoading.show();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                ArrayList<AppInfo> appListI = new ArrayList<>();
                PackageManager packageManager = getPackageManager();
                List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);

                if (includeSystemApps == 0) {
                    for (PackageInfo packageInfo : packages) {
                        if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                            Drawable appIconI = packageInfo.applicationInfo.loadIcon(packageManager);
                            String appNameI = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                            String packageNameI = packageInfo.packageName;
                            appListI.add(new AppInfo(appIconI, appNameI, packageNameI));
                        }
                    }
                } else {
                    for (PackageInfo packageInfo : packages) {
                        Drawable appIconI = packageInfo.applicationInfo.loadIcon(packageManager);
                        String appNameI = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                        String packageNameI = packageInfo.packageName;
                        appListI.add(new AppInfo(appIconI, appNameI, packageNameI));
                    }
                }

                appList = appListI;

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        updateAppListToShow();
                        appListItemAdapter.notifyDataSetChanged();
                        dialogLoading.dismiss();
                    }
                });
            }
        });
    }

    private void updateAppListToShow() {
        // Get blacklisted apps, look for search words in both package name and title name and insert the data into array.
        // As long as list doesn't get new entries, adapter doesn't has to be notified.
        appListToShow.clear();
        if (textInputEditText.getText().toString() != "") {
            appList.forEach(item -> {
                if ((item.getAppName().toString().toLowerCase().contains(textInputEditText.getText().toString().toLowerCase())) || (item.getPackageName().toString().toLowerCase().contains(textInputEditText.getText().toString().toLowerCase()))) {
                    appListToShow.add(new AppListItem(item.getAppIcon(), item.getAppName().toString(), item.getPackageName().toString(), appsToBlockUpdates.toString().contains(item.getPackageName().toString())));
                }
            });
        } else {
            appList.forEach(item -> {
                appListToShow.add(new AppListItem(item.getAppIcon(), item.getAppName().toString(), item.getPackageName().toString(), appsToBlockUpdates.toString().contains(item.getPackageName().toString())));
            });
        }
        sortAppListItemsByAlpabet(appListToShow);
        if (switchBlockedFirst.isChecked()) {
            sortAppListItemsByState(appListToShow);
        }
    }

    private void sortAppListItemsByAlpabet(List<AppListItem> list) {
        list.sort((o1, o2)
                -> o1.getTitle().compareTo(
                o2.getTitle()));
    }

    private void sortAppListItemsByState(List<AppListItem> list) {
        list.sort((o1, o2)
                -> o2.getGuiSwitch().compareTo(
                o1.getGuiSwitch()));
    }


}