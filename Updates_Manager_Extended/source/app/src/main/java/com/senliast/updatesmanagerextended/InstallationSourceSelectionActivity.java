package com.senliast.updatesmanagerextended;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.senliast.MyApplication;

import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

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

public class InstallationSourceSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InstallationSourceAdapter installationSourceAdapter;
    private List<InstallationSourceInfo> appList;
    private List<InstallationSourceInfo> finalAppList;
    private Set<String> toggledApps;
    private boolean sortToggledFirst = false;
    private SearchView searchView;
    MyPreferencesManager myPreferencesManager = new MyPreferencesManager();
    private MaterialAlertDialogBuilder builder;
    private View viewDialogLoading;
    private AlertDialog alertDialogDialogLoading;
    private Toolbar toolbar;
    private List<GroupInfo> groups = new ArrayList<>();
    private MyObjectBackgroundView mobvSwitchBlockAll;
    private MaterialSwitch switchBlockAll;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_installation_source_selection);
        DynamicColors.applyToActivityIfAvailable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityInstallationSourceSelection), (v, insets) -> {
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
        recyclerView.setPadding(0, 0, 0, 0);

        FastScroller fastScroller = new FastScrollerBuilder(recyclerView)
                .setThumbDrawable(getDrawable(R.drawable.fastscroll_thumb))
                .setTrackDrawable(getDrawable(R.drawable.fastscroll_track))
                .setPadding(0, 0, 0, 0)
                .build();

        viewDialogLoading = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        builder = new MaterialAlertDialogBuilder(this);
        alertDialogDialogLoading = builder.create();
        builder.setView(viewDialogLoading);
        alertDialogDialogLoading = builder.create();
        searchView = findViewById(R.id.searchView);
        setupSearchView();

        mobvSwitchBlockAll = findViewById(R.id.mobvSwitchBlockAll);
        if (Utils.isDarkModeActive()) {
            mobvSwitchBlockAll.setRectangleColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_primary_container));
        } else {
            mobvSwitchBlockAll.setRectangleColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_light_primary_container));
        }
        switchBlockAll = findViewById(R.id.switchBlockAll);

        appList = new ArrayList<>();
        finalAppList = new ArrayList<>();
        toggledApps = new HashSet<>();

        installationSourceAdapter = new InstallationSourceAdapter(finalAppList, toggledApps, new InstallationSourceAdapter.OnToggleListener() {
            @Override
            public void onToggle(String packageName, boolean isChecked) {
                if (isChecked) {
                    toggledApps.add(packageName);
                } else {
                    toggledApps.remove(packageName);
                }
                saveGroups();
                sortApps();
            }
        });
        recyclerView.setAdapter(installationSourceAdapter);
        loadGroups();
        loadInstalledApps();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        switchBlockAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                groups.get(myPreferencesManager.getIntPreference("groupToEdit", 0)).setBlockAllInstallationSources(isChecked);
                if (isChecked) {
                    disableRecyclerView();
                } else {
                    enableRecyclerView();
                }
                saveGroups();
            }
        });
        switchBlockAll.setChecked(groups.get(myPreferencesManager.getIntPreference("groupToEdit", 0)).getBlockAllInstallationSources());

        if (groups.get(myPreferencesManager.getIntPreference("groupToEdit", 0)).getBlockAllInstallationSources()) {
            disableRecyclerView();
        } else {
            enableRecyclerView();
        }
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
            for (InstallationSourceInfo app : appList) {
                if (app.getAppName().toLowerCase().contains(lowerQuery) ||
                        app.getPackageName().toLowerCase().contains(lowerQuery)) {
                    finalAppList.add(app);
                }
            }
        }
        installationSourceAdapter.notifyDataSetChanged();
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

            List<PackageInfo> packages = pm.getInstalledPackages(
                    PackageManager.GET_PERMISSIONS
            );

            // As already said, we are interested only in apps, that can update other apps automatically. So, showing
            // only such apps.
            Set<String> targetPermissions = new HashSet<>();
            targetPermissions.add("android.permission.UPDATE_PACKAGES_WITHOUT_USER_ACTION");
            targetPermissions.add("android.permission.INSTALL_PACKAGES");
            targetPermissions.add("android.permission.USE_INSTALLER_V2");

            for (PackageInfo pkg : packages) {
                if (pkg.requestedPermissions == null) continue;

                for (String perm : pkg.requestedPermissions) {
                    if (targetPermissions.contains(perm)) {
                        String appName =  pkg.applicationInfo.loadLabel(pm).toString();
                        Drawable appIcon = pkg.applicationInfo.loadIcon(pm);
                        String packageName = pkg.applicationInfo.packageName;
                        appList.add(new InstallationSourceInfo(appName, packageName, appIcon));
                        break;
                    }
                }
            }

            finalAppList.addAll(appList);

            runOnUiThread(() -> {
                installationSourceAdapter.notifyDataSetChanged();
                filterApps();
                sortApps();
                alertDialogDialogLoading.dismiss();
            });
        });
        executor.shutdown();
    }

    private void sortApps() {
        if (sortToggledFirst) {
            Collections.sort(finalAppList, new Comparator<InstallationSourceInfo>() {
                @Override
                public int compare(InstallationSourceInfo app1, InstallationSourceInfo app2) {
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

    private void loadGroups() {
        groups.addAll(myPreferencesManager.getGroups());
        if (installationSourceAdapter != null) { installationSourceAdapter.notifyDataSetChanged(); }
        toggledApps.clear();
        toggledApps.addAll(Arrays.asList(groups.get(myPreferencesManager.getIntPreference("groupToEdit", 0)).getInstallationSources().split(",")));
    }

    private void saveGroups() {
        groups.get(myPreferencesManager.getIntPreference("groupToEdit", 0)).setInstallationSources(String.join(",", toggledApps));
        myPreferencesManager.saveGroups(groups);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_installation_source_selection, menu);
        menu.findItem(R.id.actionSortToggledFirst).setChecked(sortToggledFirst);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionSortToggledFirst) {
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

    private void disableRecyclerView() {
        if (installationSourceAdapter != null) {
            installationSourceAdapter.setEnabled(false);
        }
        recyclerView.setAlpha(0.85f);
    }

    private void enableRecyclerView() {
        if (installationSourceAdapter != null) {
            installationSourceAdapter.setEnabled(true);
        }
        recyclerView.setAlpha(1.0f);
    }
}
