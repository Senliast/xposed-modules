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

public class InstallationSourceAdapter extends RecyclerView.Adapter<InstallationSourceAdapter.InstallationSourceViewHolder> {

    private List<InstallationSourceInfo> appList;
    private Set<String> toggledApps;
    private OnToggleListener toggleListener;
    private boolean isEnabled = true;
    private MyPreferencesManager myPreferencesManager = new MyPreferencesManager();

    public interface OnToggleListener {
        void onToggle(String packageName, boolean isChecked);
    }

    public InstallationSourceAdapter(List<InstallationSourceInfo> appList, Set<String> toggledApps, OnToggleListener toggleListener) {
        this.appList = appList;
        this.toggledApps = toggledApps;
        this.toggleListener = toggleListener;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InstallationSourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_installation_source, parent, false);
        return new InstallationSourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstallationSourceViewHolder holder, int position) {
        InstallationSourceInfo app = appList.get(position);
        holder.appName.setText(app.getAppName());
        holder.packageName.setText(app.getPackageName());
        holder.appIcon.setImageDrawable(app.getAppIcon());

        holder.toggleSwitch.setOnCheckedChangeListener(null);
        holder.toggleSwitch.setChecked(toggledApps.contains(app.getPackageName()) || isEnabled == false);
        holder.toggleSwitch.setEnabled(isEnabled);
        holder.toggleSwitch.setAlpha(isEnabled ? 1.0f : 0.6f);

        if (isEnabled) {
            holder.toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                toggleListener.onToggle(app.getPackageName(), isChecked);
            });
        }

        holder.appName.setAlpha(isEnabled ? 1.0f : 0.7f);
        holder.packageName.setAlpha(isEnabled ? 1.0f : 0.7f);
        holder.appIcon.setAlpha(isEnabled ? 1.0f : 0.7f);
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class InstallationSourceViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView packageName;
        MaterialSwitch toggleSwitch;

        public InstallationSourceViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.imageViewAppIcon);
            appName = itemView.findViewById(R.id.textViewAppName);
            packageName = itemView.findViewById(R.id.textViewPackageName);
            toggleSwitch = itemView.findViewById(R.id.toggleSwitch);
        }
    }
}
