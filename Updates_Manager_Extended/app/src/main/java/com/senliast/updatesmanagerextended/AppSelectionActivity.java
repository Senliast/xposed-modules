package com.senliast.updatesmanagerextended;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

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
    private List<AppInfo> filteredAppList;
    private Set<String> toggledApps;
    private boolean showSystemApps = false;
    private boolean sortToggledFirst = false;
    private SearchView searchView;
    private Button buttonBack;
    MyPreferencesManager myPreferencesManager = new MyPreferencesManager();
    private MaterialAlertDialogBuilder builder;
    private View viewDialogLoading;
    private AlertDialog alertDialogDialogLoading;
    private LinearProgressIndicator linearProgressIndicatorLoading;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_app_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityAppSelection), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setVerticalScrollBarEnabled(true);
        viewDialogLoading = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        builder = new MaterialAlertDialogBuilder(this);
        alertDialogDialogLoading = builder.create();
        builder.setView(viewDialogLoading);
        alertDialogDialogLoading = builder.create();
        linearProgressIndicatorLoading = viewDialogLoading.findViewById(R.id.progressBar);
        searchView = findViewById(R.id.searchView);
        setupSearchView();
        appList = new ArrayList<>();
        filteredAppList = new ArrayList<>();
        toggledApps = loadToggledApps();
        loadApps();

        appAdapter = new AppAdapter(filteredAppList, toggledApps, new AppAdapter.OnToggleListener() {
            @Override
            public void onToggle(String packageName, boolean isChecked) {
                if (isChecked) {
                    toggledApps.add(packageName);
                } else {
                    toggledApps.remove(packageName);
                }
                saveToggledApps();
                if (sortToggledFirst) {
                    sortApps();
                    filterApps(searchView.getQuery().toString());
                    appAdapter.notifyDataSetChanged();
                }
            }
        });
        recyclerView.setAdapter(appAdapter);

        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        appAdapter.notifyDataSetChanged();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterApps(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterApps(newText);
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            filterApps("");
            return false;
        });
    }

    private void filterApps(String query) {
        filteredAppList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredAppList.addAll(appList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (AppInfo app : appList) {
                if (app.getAppName().toLowerCase().contains(lowerQuery) ||
                        app.getPackageName().toLowerCase().contains(lowerQuery)) {
                    filteredAppList.add(app);
                }
            }
        }
        appAdapter.notifyDataSetChanged();
    }

    private void loadApps() {
        alertDialogDialogLoading.show();
        alertDialogDialogLoading.setCancelable(false);
        alertDialogDialogLoading.setCanceledOnTouchOutside(false);
        alertDialogDialogLoading.setOnKeyListener((dialog, keyCode, event) -> true);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {

            appList.clear();
            filteredAppList.clear();
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

            sortApps();
            filteredAppList.addAll(appList);

            runOnUiThread(() -> {
                appAdapter.notifyDataSetChanged();
                alertDialogDialogLoading.dismiss();
            });
        });
        executor.shutdown();
    }

    private void sortApps() {
        if (sortToggledFirst) {
            Collections.sort(appList, new Comparator<AppInfo>() {
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
            Collections.sort(appList, (app1, app2) -> app1.getAppName().compareToIgnoreCase(app2.getAppName()));
        }
    }

    private Set<String> loadToggledApps() {
        Set<String> toggled = new HashSet<>();
        toggled.addAll(Arrays.asList((myPreferencesManager.getStringPreference("appsToBlockUpdates", "")).split(",")));
        return toggled;
    }

    private void saveToggledApps() {
        myPreferencesManager.setStringPreference("appsToBlockUpdates", String.join(",", toggledApps));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_selection_menu, menu);
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
            loadApps();
            filterApps(searchView.getQuery().toString());
            appAdapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.actionSortToggledFirst) {
            sortToggledFirst = !sortToggledFirst;
            item.setChecked(sortToggledFirst);
            sortApps();
            filterApps(searchView.getQuery().toString());
            appAdapter.notifyDataSetChanged();
            return true;
        } else if (id == android.R.id.home) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            filterApps("");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
