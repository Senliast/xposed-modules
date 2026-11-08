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

public class AppAdapter1 extends RecyclerView.Adapter<AppAdapter1.AppViewHolder> {

    private List<AppInfo1> appList;
    private Set<SavedApp> savedApps;
    private OnToggleListener toggleListener;
    private PreferencesManager preferencesManager = new PreferencesManager();
    private List<Group> groups;
    private int groupPosition;
    private final AppCountdownTimerHelper.OnTimerFinishListener finishListener;

    public interface OnToggleListener {
        void onToggle(int position, boolean isChecked, String packageName);
    }

    public AppAdapter1(List<Group> groups, int groupPosition, List<AppInfo1> appList, Set<SavedApp> savedApps, OnToggleListener toggleListener, AppCountdownTimerHelper.OnTimerFinishListener finishListener) {
        this.groups = groups;
        this.groupPosition = groupPosition;
        this.appList = appList;
        this.savedApps = savedApps;
        this.toggleListener = toggleListener;
        this.finishListener = finishListener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_1, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo1 app = appList.get(position);
        holder.appName.setText(app.getAppName());
        holder.packageName.setText(app.getPackageName());
        holder.appIcon.setImageDrawable(app.getAppIcon());

        holder.timerHelper.stop();
        holder.timerText.setVisibility(View.GONE);
        holder.timerDivider.setVisibility(View.GONE);

        if (preferencesManager.getModuleStatus().equals(Status.ENABLED_IMMEDIATELY)  || (preferencesManager.getModuleStatus().equals(Status.ENABLED_SINCE) && System.currentTimeMillis() >= preferencesManager.getModuleStatusTime())) {
            if (groups.get(groupPosition).getStatus().equals(Status.ENABLED_IMMEDIATELY) || (groups.get(groupPosition).getStatus().equals(Status.ENABLED_SINCE) && System.currentTimeMillis() >= groups.get(groupPosition).getStatusTime())) {
                holder.toggleSwitch.setEnabled(true);
                holder.toggleSwitch.setOnCheckedChangeListener(null);

                for (SavedApp savedApp : savedApps) {
                    if (savedApp.packageName.equals(app.getPackageName())) {
                        if (savedApp.status.equals("enabled_immediately") || (savedApp.status.equals("enabled_since") && System.currentTimeMillis() >= savedApp.statusTime)) {
                            holder.toggleSwitch.setChecked(true);
                            holder.timerHelper.stop();
                            holder.timerText.setVisibility(View.GONE);
                            holder.timerDivider.setVisibility(View.GONE);
                        } else if (savedApp.status.equals("enabled_since") && System.currentTimeMillis() < savedApp.statusTime) {
                            holder.toggleSwitch.setChecked(false);
                            holder.timerText.setVisibility(View.VISIBLE);
                            holder.timerDivider.setVisibility(View.VISIBLE);
                            holder.timerHelper.stop();
                            long endTime = savedApp.statusTime;
                            holder.timerHelper.startCountdown(
                                    endTime,
                                    timeLeft -> holder.timerText.setText(timeLeft),
                                    pos -> {
                                        finishListener.onTimerFinished(app.getPackageName(), pos);
                                    },
                                    position
                            );
                        } else {
                            holder.timerText.setVisibility(View.GONE);
                            holder.timerDivider.setVisibility(View.GONE);
                        }
                    }
                }

                holder.toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    toggleListener.onToggle(position, isChecked, app.getPackageName());
                });
            } else {
                holder.toggleSwitch.setChecked(false);
                holder.toggleSwitch.setEnabled(false);
            }
        } else {
            holder.toggleSwitch.setChecked(false);
            holder.toggleSwitch.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView packageName;
        TextView timerText;
        MaterialSwitch toggleSwitch;
        CountdownTimerHelper timerHelper = new CountdownTimerHelper();
        View timerDivider;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.imageViewAppIcon);
            appName = itemView.findViewById(R.id.textViewAppName);
            packageName = itemView.findViewById(R.id.textViewPackageName);
            toggleSwitch = itemView.findViewById(R.id.toggleSwitch);
            timerText = itemView.findViewById(R.id.timerText);
            timerDivider = itemView.findViewById(R.id.timerDivider);
        }
    }

    public void revertSwitch(int position) {
        notifyItemChanged(position);
    }

    @Override
    public void onViewRecycled(@NonNull AppAdapter1.AppViewHolder holder) {
        super.onViewRecycled(holder);
        holder.timerHelper.stop();
    }
}
