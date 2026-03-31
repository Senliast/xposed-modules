package com.senliast.updatesmanagerextended;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppAdapter appAdapter;
    private List<AppInfo> appList;
    private List<AppInfo> finalAppList;
    private Set<String> toggledApps;
    private boolean showSystemApps = false;
    private boolean sortToggledFirst = false;
    private SearchView searchView;
    MyPreferencesManager myPreferencesManager = new MyPreferencesManager();
    private MaterialAlertDialogBuilder builder;
    private View viewDialogLoading;
    private AlertDialog alertDialogDialogLoading;
    private Toolbar toolbar;
    private List<GroupInfo> groups = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_app_selection);
        DynamicColors.applyToActivityIfAvailable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityAppSelection), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        appList = new ArrayList<>();
        finalAppList = new ArrayList<>();
        toggledApps = new HashSet<>();

        appAdapter = new AppAdapter(finalAppList, toggledApps, new AppAdapter.OnToggleListener() {
            @Override
            public void onToggle(String packageName, boolean isChecked) {
                if (isChecked) {
                    toggledApps.add(packageName);
                } else {
                    toggledApps.remove(packageName);
                }
                saveToggledApps();
                sortApps();
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
            for (AppInfo app : appList) {
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
                    appList.add(new AppInfo(appName, packageName, appIcon));
                }
            }

            finalAppList.addAll(appList);

            runOnUiThread(() -> {
                appAdapter.notifyDataSetChanged();
                filterApps();
                sortApps();
                alertDialogDialogLoading.dismiss();
            });
        });
        executor.shutdown();
    }

    private void sortApps() {
        if (sortToggledFirst) {
            Collections.sort(finalAppList, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo app1, AppInfo app2) {
                    boolean isToggled1 = toggledApps.contains(app1.getPackageName());
                    boolean isToggled2 = toggledApps.contains(app2.getPackageName());
                    if (isToggled1 && !isToggled2) return -1;
                    if (!isToggled1 && isToggled2) return 1;
                    return app1.getAppName().compareToIgnoreCase(app2.getAppName());
                }
            });
        } else {
            Collections.sort(finalAppList, (app1, app2) -> app1.getAppName().compareToIgnoreCase(app2.getAppName()));
        }
    }

    private void loadToggledApps() {
        groups.addAll(myPreferencesManager.getGroups());
        if (appAdapter != null) { appAdapter.notifyDataSetChanged(); }
        toggledApps.clear();
        toggledApps.addAll(Arrays.asList(groups.get(myPreferencesManager.getIntPreference("groupToEdit", 0)).getAppsToBlockUpdates().split(",")));
    }

    private void saveToggledApps() {
        groups.get(myPreferencesManager.getIntPreference("groupToEdit", 0)).setAppsToBlockUpdates(String.join(",", toggledApps));
        myPreferencesManager.saveGroups(groups);
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
            filterApps();
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
