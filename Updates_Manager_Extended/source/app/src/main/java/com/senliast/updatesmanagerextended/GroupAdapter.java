package com.senliast.updatesmanagerextended;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.senliast.MyApplication;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private final List<GroupInfo> groups;
    private final GroupActionListener listener;
    private final Context context;
    private int rectangleColor = -1;
    private final CountdownTimerHelper.OnTimerFinishListener finishListener;
    private MyPreferencesManager myPreferencesManager = new MyPreferencesManager();

    public GroupAdapter(List<GroupInfo> groups, GroupActionListener listener, Context context, CountdownTimerHelper.OnTimerFinishListener finishListener) {
        this.groups = groups;
        this.listener = listener;
        this.context = context;
        this.finishListener = finishListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupInfo group = groups.get(position);

        holder.textViewName.setText(group.getName());
        holder.textViewName.setSelected(true);

        holder.buttonSelectInstallationSources.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onButtonSelectInstallationSourceClicked(pos);
        });

        holder.buttonSelectBlacklistedApps.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onButtonSelectBlacklistedAppsClicked(pos);
        });

        // Remove listener to avoid loops
        holder.switchToggle.setOnCheckedChangeListener(null);

        if (myPreferencesManager.getStringPreference("moduleStatus", "disabled").equals("enabled_immediately")  || (myPreferencesManager.getStringPreference("moduleStatus", "disabled").equals("enabled_since") && System.currentTimeMillis() >= myPreferencesManager.getLongPreference("moduleStatusTime", 0L))) {
            if (groups.get(position).getStatus().equals("enabled_immediately") || (groups.get(position).getStatus().equals("enabled_since") && System.currentTimeMillis() >= groups.get(position).getStatusTime())) {
                holder.switchToggle.setChecked(true);
                holder.textViewStatus.setText(MyApplication.getAppContext().getText(R.string.group_status_enabled));
            } else {
                if (groups.get(position).getStatus().equals("disabled")) {
                    holder.switchToggle.setChecked(false);
                    holder.textViewStatus.setText(MyApplication.getAppContext().getText(R.string.group_status_disabled));
                } else {
                    holder.timerHelper.stop();
                    long endTime = group.getStatusTime();
                    holder.timerHelper.startCountdown(
                            endTime,
                            timeLeft -> holder.textViewStatus.setText(MyApplication.getAppContext().getString(R.string.group_status_paused) + timeLeft),
                            pos -> {
                                finishListener.onTimerFinished(pos);
                            },
                            position
                    );
                }
            }
        } else {
            holder.switchToggle.setChecked(false);
            holder.switchToggle.setEnabled(false);
            holder.textViewStatus.setText(MyApplication.getAppContext().getText(R.string.app_status_disabled_for_groups));
        }

        holder.switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onSwitchToggled(position, isChecked, group);
            }
        });

        holder.buttonRename.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onRename(pos);
        });

        holder.buttonRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onDelete(pos);
        });

        if (rectangleColor != -1) {
            holder.mobvSwitchEnableGroup.setRectangleColor(
                    ContextCompat.getColor(context, rectangleColor)
            );
        }

        if (Utils.isDarkModeActive()) {
            holder.mobvSwitchEnableGroup.setRectangleColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_primary));
            holder.llCard.setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_secondary_container));
        } else {
            holder.mobvSwitchEnableGroup.setRectangleColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_light_primary));
            holder.llCard.setBackgroundColor(MyApplication.getAppContext().getColor(com.google.android.material.R.color.m3_sys_color_dynamic_light_secondary_container));
        }
    }

    public void revertSwitch(int position) {
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        Button buttonSelectInstallationSources, buttonSelectBlacklistedApps, buttonRename, buttonRemove;
        MaterialSwitch switchToggle;
        MyObjectBackgroundView mobvSwitchEnableGroup;
        LinearLayout llCard;
        TextView textViewStatus;
        CountdownTimerHelper timerHelper = new CountdownTimerHelper();

        ViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewGroupName);
            buttonSelectInstallationSources = itemView.findViewById(R.id.buttonSelectInstallationSources);
            buttonSelectBlacklistedApps = itemView.findViewById(R.id.buttonSelectBlacklistedApps);
            switchToggle = itemView.findViewById(R.id.switchToggle);
            buttonRename = itemView.findViewById(R.id.buttonRename);
            buttonRemove = itemView.findViewById(R.id.buttonDelete);
            mobvSwitchEnableGroup = itemView.findViewById(R.id.mobvSwitchEnableGroup);
            llCard = itemView.findViewById(R.id.llCard);
            textViewStatus = itemView.findViewById(R.id.textViewGroupStatus);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.timerHelper.stop();
    }
}