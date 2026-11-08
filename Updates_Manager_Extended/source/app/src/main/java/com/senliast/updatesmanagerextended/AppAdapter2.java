package com.senliast.updatesmanagerextended;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AppAdapter2 extends RecyclerView.Adapter<AppAdapter2.AppViewHolder> {

    private List<AppInfo2> appList;
    private Set<SavedApp> savedApps;
    private OnToggleListener toggleListener;

    public interface OnToggleListener {
        void onToggle(String appName, String packageName, Drawable appIcon, boolean isChecked);
    }

    public AppAdapter2(List<AppInfo2> appList, Set<SavedApp> savedApps, OnToggleListener toggleListener) {
        this.appList = appList;
        this.savedApps = savedApps;
        this.toggleListener = toggleListener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_2, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo2 app = appList.get(position);
        holder.appName.setText(app.getAppName());
        holder.packageName.setText(app.getPackageName());
        holder.appIcon.setImageDrawable(app.getAppIcon());

        holder.materialCheckBox.setOnCheckedChangeListener(null);

        boolean isToggled = false;
        for (SavedApp item : savedApps) {
            if (item.packageName.equals(app.getPackageName())) {
                isToggled = true;
                break;
            }
        }
        holder.materialCheckBox.setChecked(isToggled);

        holder.materialCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleListener.onToggle(
                    app.getAppName(),
                    app.getPackageName(),
                    app.getAppIcon(),
                    isChecked
            );
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
        MaterialCheckBox materialCheckBox;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.imageViewAppIcon);
            appName = itemView.findViewById(R.id.textViewAppName);
            packageName = itemView.findViewById(R.id.textViewPackageName);
            materialCheckBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
