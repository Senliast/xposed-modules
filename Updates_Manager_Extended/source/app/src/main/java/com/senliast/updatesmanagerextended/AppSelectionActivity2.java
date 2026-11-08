package com.senliast.updatesmanagerextended;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.senliast.MyApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.RequiresApi;
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
import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class AppSelectionActivity2 extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppAdapter2 appAdapter;
    private List<AppInfo2> appList;
    private List<AppInfo2> finalAppList;
    private Set<SavedApp> savedApps;
    private boolean showSystemApps = false;
    private boolean sortToggledFirst = false;
    private SearchView searchView;
    PreferencesManager preferencesManager = new PreferencesManager();
    private MaterialAlertDialogBuilder builder;
    private View viewDialogLoading;
    private AlertDialog alertDialogDialogLoading;
    private Toolbar toolbar;
    private List<Group> groups = new ArrayList<>();
    private Set<String> savedPackageNamesOnly = new HashSet<>();
    private ColorMatrix matrix = new ColorMatrix();

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_app_selection_2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityAppSelection2), (v, insets) -> {
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

        appAdapter = new AppAdapter2(finalAppList, savedApps, new AppAdapter2.OnToggleListener() {
            @Override
            public void onToggle(String appName, String packageName, Drawable appIcon, boolean isChecked) {
                if (isChecked) {
                    savedApps.add(new SavedApp(appName, packageName, Status.ENABLED_IMMEDIATELY, 0L));
                    preferencesManager.saveAppIcon(MyApplication.getAppContext(), appIcon, packageName);
                    savedPackageNamesOnly.add(packageName);
                } else {
                    Iterator<SavedApp> iteratorToggledApps = savedApps.iterator();
                    while (iteratorToggledApps.hasNext()) {
                        SavedApp item = iteratorToggledApps.next();
                        if (item.packageName.equals(packageName)) {
                            iteratorToggledApps.remove();
                            preferencesManager.deleteAppIcon(MyApplication.getAppContext(), packageName);
                            savedPackageNamesOnly.remove(packageName);
                        }
                    }
                }

                saveToggledApps();

                if (sortToggledFirst) {
                    sortApps();
                }
            }
        });
        recyclerView.setAdapter(appAdapter);

        loadToggledApps();
        loadInstalledApps();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
            for (AppInfo2 app : appList) {
                if (app.getAppName().toLowerCase().contains(lowerQuery) ||
                        app.getPackageName().toLowerCase().contains(lowerQuery)) {
                    finalAppList.add(app);
                }
            }
        }
        appAdapter.notifyDataSetChanged();
    }

    private void loadInstalledApps() {
        alertDialogDialogLoading.show();
        alertDialogDialogLoading.setCancelable(false);
        alertDialogDialogLoading.setCanceledOnTouchOutside(false);
        alertDialogDialogLoading.setOnKeyListener((dialog, keyCode, event) -> true);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {

            appList.clear();
            finalAppList.clear();
            PackageManager pm = getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo app : apps) {
                if (showSystemApps || (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    String appName = app.loadLabel(pm).toString();
                    Drawable appIcon = app.loadIcon(pm);
                    String packageName = app.packageName;
                    appList.add(new AppInfo2(appName, packageName, appIcon));
                }
            }

            savedPackageNamesOnly.clear();

            for (SavedApp savedApp: savedApps) {
                boolean hasItem = false;
                for (AppInfo2 appInfo2: appList) { if (appInfo2.getPackageName().equals(savedApp.packageName)) { hasItem = true; } }
                if (!hasItem) {
                    Drawable icon = preferencesManager.getSavedAppIcon(MyApplication.getAppContext(), savedApp.packageName);
                    icon.setColorFilter(new ColorMatrixColorFilter(matrix));
                    appList.add(new AppInfo2(savedApp.appName, savedApp.packageName, icon));
                }
                savedPackageNamesOnly.add(savedApp.packageName);
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
        if (sortToggledFirst) {
            Collections.sort(finalAppList, new Comparator<AppInfo2>() {
                @Override
                public int compare(AppInfo2 app1, AppInfo2 app2) {
                    boolean isToggled1 = savedPackageNamesOnly.contains(app1.getPackageName());
                    boolean isToggled2 = savedPackageNamesOnly.contains(app2.getPackageName());
                    if (isToggled1 && !isToggled2) return -1;
                    if (!isToggled1 && isToggled2) return 1;
                    return app1.getAppName().compareToIgnoreCase(app2.getAppName());
                }
            });
        } else {
            Collections.sort(finalAppList, (app1, app2) -> app1.getAppName().compareToIgnoreCase(app2.getAppName()));
        }
        appAdapter.notifyItemRangeChanged(0, finalAppList.size());
    }

    private void loadToggledApps() {
        groups = preferencesManager.getGroups();

        if (appAdapter != null) { appAdapter.notifyDataSetChanged(); }
        savedApps.clear();
        savedApps.addAll(groups.get(AppGlobals.groupToEdit).getAppsToBlockUpdates());
    }

    private void saveToggledApps() {
        groups.get(AppGlobals.groupToEdit).setAppsToBlockUpdates(savedApps);
        preferencesManager.setGroups(groups);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_selection, menu);
        menu.findItem(R.id.actionShowSystemApps).setChecked(showSystemApps);
        menu.findItem(R.id.actionSortToggledFirst).setChecked(sortToggledFirst);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionShowSystemApps) {
            showSystemApps = !showSystemApps;
            item.setChecked(showSystemApps);
            loadInstalledApps();
            return true;
        } else if (id == R.id.actionSortToggledFirst) {
            sortToggledFirst = !sortToggledFirst;
            item.setChecked(sortToggledFirst);
            sortApps();
            return true;
        } else if (id == android.R.id.home) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            filterApps();
            sortApps();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}