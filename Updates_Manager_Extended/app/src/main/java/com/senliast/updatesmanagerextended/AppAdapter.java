package com.senliast.updatesmanagerextended;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.List;
import java.util.Set;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    private List<AppInfo> appList;
    private Set<String> toggledApps;
    private OnToggleListener toggleListener;

    public interface OnToggleListener {
        void onToggle(String packageName, boolean isChecked);
    }

    public AppAdapter(List<AppInfo> appList, Set<String> toggledApps, OnToggleListener toggleListener) {
        this.appList = appList;
        this.toggledApps = toggledApps;
        this.toggleListener = toggleListener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo app = appList.get(position);
        holder.appName.setText(app.getAppName());
        holder.packageName.setText(app.getPackageName());
        holder.appIcon.setImageDrawable(app.getAppIcon());

        holder.toggleSwitch.setOnCheckedChangeListener(null);
        holder.toggleSwitch.setChecked(toggledApps.contains(app.getPackageName()));
        holder.toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleListener.onToggle(app.getPackageName(), isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView packageName;
        MaterialSwitch toggleSwitch;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.appIcon);
            appName = itemView.findViewById(R.id.appName);
            packageName = itemView.findViewById(R.id.packageName);
            toggleSwitch = itemView.findViewById(R.id.toggleSwitch);
        }
    }
}
