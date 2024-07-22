package com.senliast.updatesmanagerextended;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
    private AlertDialog.Builder builder;
    private View viewDialogLoading;
    private AlertDialog alertDialogDialogLoading;
    private int[][] colorStatesForSwitch;
    private int[] trackColorsForSwitch;
    private ColorStateList colorStateListForSwitch;
    private LinearProgressIndicator linearProgressIndicatorLoading;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mReceiver, mFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mReceiver, mFilter);
        }
        switchShowSystemApps = findViewById(R.id.switchShowSystemApps);
        switchBlockedFirst = findViewById(R.id.switchBlockedFirst);
        colorStatesForSwitch = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked }
        };
        trackColorsForSwitch = new int[] {
                MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, getColor(R.color.primary)),
                MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorBackground, Color.WHITE)
        };
        colorStateListForSwitch = new ColorStateList(colorStatesForSwitch, trackColorsForSwitch);
        builder = new AlertDialog.Builder(this);
        viewDialogLoading = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        alertDialogDialogLoading = builder.create();
        builder.setView(viewDialogLoading);
        alertDialogDialogLoading.setCancelable(false);
        alertDialogDialogLoading.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        alertDialogDialogLoading = builder.create();
        linearProgressIndicatorLoading = viewDialogLoading.findViewById(R.id.progressBar);

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
        switchShowSystemApps.setTrackTintList(colorStateListForSwitch);
        switchBlockedFirst.setTrackTintList(colorStateListForSwitch);
        findViewById(R.id.appSelection).setBackgroundColor(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorBackground, getColor(R.color.background)));
        viewDialogLoading.findViewById(R.id.dialogLoading).setBackgroundColor(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorBackground, getColor(R.color.background)));
        linearProgressIndicatorLoading.setIndicatorColor(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, getColor(R.color.primary)));
        linearProgressIndicatorLoading.setTrackColor(Utils.changeColorAlpha(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, getColor(R.color.primary)), 30));

        appsToBlockUpdates = new ArrayList<>(Arrays.asList((myPreferencesManager.getStringPreference("appsToBlockUpdates", "")).split(",")));

        if (switchShowSystemApps.isChecked()) {
            updateAppList(1);
        } else {
            updateAppList(0);
        }
    }

    private void updateAppList(Integer includeSystemApps) {
        alertDialogDialogLoading.show();

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
                        alertDialogDialogLoading.dismiss();
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