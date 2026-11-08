package com.senliast.updatesmanagerextended;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.senliast.MyApplication;

import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppSelectionActivity1 extends AppCompatActivity implements AppCountdownTimerHelper.OnTimerFinishListener {
    private RecyclerView recyclerView;
    private AppAdapter1 appAdapter1;
    private List<AppInfo1> appList;
    private List<AppInfo1> finalAppList;
    private Set<SavedApp> savedApps;
    private SearchView searchView;
    PreferencesManager preferencesManager = new PreferencesManager();
    private MaterialAlertDialogBuilder builder;
    private View viewDialogLoading;
    private AlertDialog alertDialogDialogLoading;
    private Toolbar toolbar;
    private List<Group> groups = new ArrayList<>();
    private FloatingActionButton fabAdd;
    private ColorMatrix matrix = new ColorMatrix();
    private View viewDialogDisable;
    private AlertDialog alertDialogDialogDisable;
    private Button buttonDialogDisablePermanentlyButton;
    private Button buttonDialogDisable15Min;
    private Button buttonDialogDisable30Min;
    private Button buttonDialogDisable45Min;
    private Button buttonDialogDisable60Min;
    private Button buttonDialogDisableCancel;
    private TextView textViewDialogDisableTitle;
    private boolean dialogDisableAnyOptionSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_app_selection_1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityAppSelection1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    0,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );

            return insets;
        });
        DynamicColors.applyToActivityIfAvailable(this);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(!Utils.isDarkModeActive());
        controller.setAppearanceLightNavigationBars(!Utils.isDarkModeActive());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationIcon(R.drawable.back_button);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setVerticalScrollBarEnabled(true);
        fabAdd = findViewById(R.id.fabAdd);
        FastScroller fastScroller = new FastScrollerBuilder(recyclerView)
            .setThumbDrawable(getDrawable(R.drawable.fastscroll_thumb))
            .setTrackDrawable(getDrawable(R.drawable.fastscroll_track))
            .build();
        viewDialogLoading = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        builder = new MaterialAlertDialogBuilder(this);
        alertDialogDialogLoading = builder.create();
        builder.setView(viewDialogLoading);
        alertDialogDialogLoading = builder.create();
        searchView = findViewById(R.id.searchView);
        setupSearchView();
        matrix.setSaturation(0f);

        appList = new ArrayList<>();
        finalAppList = new ArrayList<>();
        savedApps = new HashSet<>();

        groups = preferencesManager.getGroups();

        appAdapter1 = new AppAdapter1(groups, AppGlobals.groupToEdit, finalAppList, savedApps, new AppAdapter1.OnToggleListener() {
            @Override
            public void onToggle(int position, boolean desiredState, String packageName) {
                // We will update the preferences and tell the adapter to reload them.
                handleSwitchToggle(position, desiredState, packageName);
            }

        }, this);
        recyclerView.setAdapter(appAdapter1);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppSelectionActivity1.this, AppSelectionActivity2.class);
                startActivity(intent);
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterApps();
                sortApps();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterApps();
                sortApps();
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            filterApps();
            sortApps();
            return false;
        });
    }

    private void filterApps() {
        String query = searchView.getQuery().toString();
        finalAppList.clear();
        if (query == null || query.trim().isEmpty()) {
            finalAppList.addAll(appList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (AppInfo1 app : appList) {
                if (app.getAppName().toLowerCase().contains(lowerQuery) ||
                        app.getPackageName().toLowerCase().contains(lowerQuery)) {
                    finalAppList.add(app);
                }
            }
        }
        appAdapter1.notifyDataSetChanged();
    }

    private void buildList() {
        alertDialogDialogLoading.show();
        alertDialogDialogLoading.setCancelable(false);
        alertDialogDialogLoading.setCanceledOnTouchOutside(false);
        alertDialogDialogLoading.setOnKeyListener((dialog, keyCode, event) -> true);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {

            appList.clear();
            finalAppList.clear();
            PackageManager pm = getPackageManager();
            Iterator<SavedApp> iteratorToggledApps = savedApps.iterator();
            while (iteratorToggledApps.hasNext()) {
                SavedApp item = iteratorToggledApps.next();
                try {
                    Drawable icon = pm.getApplicationIcon(item.packageName);
                    appList.add(new AppInfo1(item.appName, item.packageName, icon, item.status, item.statusTime));
                } catch (Exception e){
                    Drawable icon = preferencesManager.getSavedAppIcon(MyApplication.getAppContext(), item.packageName);
                    icon.setColorFilter(new ColorMatrixColorFilter(matrix));
                    appList.add(new AppInfo1(item.appName, item.packageName, icon, item.status, item.statusTime));
                }
            }

            finalAppList.addAll(appList);

            runOnUiThread(() -> {
                filterApps();
                sortApps();
                alertDialogDialogLoading.dismiss();
            });
        });
        executor.shutdown();
    }

    private void sortApps() {
        Collections.sort(finalAppList, (app1, app2) -> app1.getAppName().compareToIgnoreCase(app2.getAppName()));
        appAdapter1.notifyItemRangeChanged(0, finalAppList.size());
    }

    private void loadSavedApps() {
        groups.clear();
        savedApps.clear();

        groups.addAll(preferencesManager.getGroups());
        savedApps.addAll(groups.get(AppGlobals.groupToEdit).getAppsToBlockUpdates());

        if (appAdapter1 != null) { appAdapter1.notifyDataSetChanged(); }
    }

    private void saveToggledApps() {
        groups.get(AppGlobals.groupToEdit).setAppsToBlockUpdates(savedApps);
        preferencesManager.setGroups(groups);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Adapter will loose connection to the list if it will be updated via "groups = new ArrayList<>()",
        // because a new list with a new reference will be created, but adapter will still has the old
        // list - even after "adapter.notifyDataSetChanged()". So its needed to keep the current list
        // at all cost. Therefore, the list will be cleared and filled again. Because, for the inventor of
        // adapter, it was of course not possible to make adapter refresh the reference automatically...
        loadSavedApps();
        buildList();
    }

    private void handleSwitchToggle(int position, boolean desiredState, String packageName) {
        if (desiredState) {
            for (SavedApp savedApp: savedApps) {
                if (savedApp.packageName.equals(packageName)) {
                    savedApp.setStatus(Status.ENABLED_IMMEDIATELY);
                    saveToggledApps();
                }
            }
            appAdapter1.notifyItemChanged(position);
        } else {
            dialogDisableAnyOptionSelected = false;
            viewDialogDisable = getLayoutInflater().inflate(R.layout.dialog_disable, null);
            builder = new MaterialAlertDialogBuilder(this);
            buttonDialogDisablePermanentlyButton = viewDialogDisable.findViewById(R.id.buttonPermanently);
            buttonDialogDisable15Min = viewDialogDisable.findViewById(R.id.button15Min);
            buttonDialogDisable30Min = viewDialogDisable.findViewById(R.id.button30Min);
            buttonDialogDisable45Min = viewDialogDisable.findViewById(R.id.button45Min);
            buttonDialogDisable60Min = viewDialogDisable.findViewById(R.id.button60Min);
            buttonDialogDisableCancel = viewDialogDisable.findViewById(R.id.buttonCancel);
            textViewDialogDisableTitle = viewDialogDisable.findViewById(R.id.textViewTitle);
            textViewDialogDisableTitle.setText(getString(R.string.disable_app));
            buttonDialogDisablePermanentlyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (SavedApp savedApp: savedApps) {
                        if (savedApp.packageName.equals(packageName)) {
                            savedApp.setStatus(Status.DISABLED);
                            saveToggledApps();
                        }
                    }
                    dialogDisableAnyOptionSelected = true;
                    alertDialogDialogDisable.dismiss();
                    appAdapter1.notifyDataSetChanged();
                }
            });
            buttonDialogDisable15Min.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (SavedApp savedApp: savedApps) {
                        if (savedApp.packageName.equals(packageName)) {
                            savedApp.setStatus(Status.ENABLED_SINCE, System.currentTimeMillis() + (15 * 60 * 1000));
                            saveToggledApps();
                        }
                    }
                    dialogDisableAnyOptionSelected = true;
                    alertDialogDialogDisable.dismiss();
                    appAdapter1.notifyDataSetChanged();
                }
            });
            buttonDialogDisable30Min.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (SavedApp savedApp: savedApps) {
                        if (savedApp.packageName.equals(packageName)) {
                            savedApp.setStatus(Status.ENABLED_SINCE, System.currentTimeMillis() + (30 * 60 * 1000));
                            saveToggledApps();
                        }
                    }
                    dialogDisableAnyOptionSelected = true;
                    alertDialogDialogDisable.dismiss();
                    appAdapter1.notifyDataSetChanged();
                }
            });
            buttonDialogDisable45Min.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (SavedApp savedApp: savedApps) {
                        if (savedApp.packageName.equals(packageName)) {
                            savedApp.setStatus(Status.ENABLED_SINCE, System.currentTimeMillis() + (45 * 60 * 1000));
                            saveToggledApps();
                        }
                    }
                    dialogDisableAnyOptionSelected = true;
                    alertDialogDialogDisable.dismiss();
                    appAdapter1.notifyDataSetChanged();
                }
            });
            buttonDialogDisable60Min.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (SavedApp savedApp: savedApps) {
                        if (savedApp.packageName.equals(packageName)) {
                            savedApp.setStatus(Status.ENABLED_SINCE, System.currentTimeMillis() + (60 * 60 * 1000));
                            saveToggledApps();
                        }
                    }
                    dialogDisableAnyOptionSelected = true;
                    alertDialogDialogDisable.dismiss();
                    appAdapter1.notifyDataSetChanged();
                }
            });
            buttonDialogDisableCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (appAdapter1 != null) {
                        appAdapter1.revertSwitch(position);
                    }
                    alertDialogDialogDisable.dismiss();
                    appAdapter1.notifyDataSetChanged();
                }
            });
            builder.setView(viewDialogDisable);
            alertDialogDialogDisable = builder.create();
            alertDialogDialogDisable.setCancelable(true);

            // Revert switch as well if dialog has been dismissed not by "Cancel" button, but,
            // for example, by clicking outside.
            alertDialogDialogDisable.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!dialogDisableAnyOptionSelected) {
                        if (appAdapter1 != null) {
                            appAdapter1.revertSwitch(position);
                        }
                    }
                }
            });
            alertDialogDialogDisable.show();
        }
    }

    @Override
    public void onTimerFinished(String packageName, int position) {
        for (SavedApp savedApp: savedApps) {
            if (savedApp.packageName.equals(packageName)) {
                savedApp.setStatus(Status.ENABLED_IMMEDIATELY);
                appAdapter1.notifyItemChanged(position);
            }
        }
    }
}
