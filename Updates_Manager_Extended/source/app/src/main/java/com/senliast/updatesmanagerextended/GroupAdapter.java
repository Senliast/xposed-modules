package com.senliast.updatesmanagerextended;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.senliast.UmeApplication;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private final List<Group> groups;
    private final GroupActionListener listener;
    private final Context context;
    private int rectangleColor = -1;
    private final CountdownTimerHelper.OnTimerFinishListener finishListener;
    private PreferencesManager preferencesManager = new PreferencesManager();

    public GroupAdapter(List<Group> groups, GroupActionListener listener, Context context, CountdownTimerHelper.OnTimerFinishListener finishListener) {
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
        Group group = groups.get(position);

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
        holder.obvSwitchEnableGroup.setOnClickListener(null);

        // Set initial state as the items can be re-used
        holder.switchToggle.setEnabled(true);
        holder.obvSwitchEnableGroup.setClickable(true);

        holder.timerHelper.stop();

        if (preferencesManager.getModuleStatus().equals(Status.ENABLED_IMMEDIATELY)  || (preferencesManager.getModuleStatus().equals(Status.ENABLED_SINCE) && System.currentTimeMillis() >= preferencesManager.getModuleStatusTime())) {
            if (groups.get(position).getStatus().equals(Status.ENABLED_IMMEDIATELY) || (groups.get(position).getStatus().equals(Status.ENABLED_SINCE) && System.currentTimeMillis() >= groups.get(position).getStatusTime())) {
                holder.switchToggle.setChecked(true);
                holder.textViewStatus.setText(UmeApplication.getAppContext().getText(R.string.group_status_enabled));
            } else {
                if (groups.get(position).getStatus().equals(Status.DISABLED)) {
                    holder.switchToggle.setChecked(false);
                    holder.textViewStatus.setText(UmeApplication.getAppContext().getText(R.string.group_status_disabled));
                } else {
                    holder.timerHelper.stop();
                    long endTime = group.getStatusTime();
                    holder.timerHelper.startCountdown(
                            endTime,
                            timeLeft -> holder.textViewStatus.setText(UmeApplication.getAppContext().getString(R.string.group_status_paused) + timeLeft),
                            pos -> {
                                finishListener.onTimerFinished(pos);
                            },
                            position
                    );
                }
            }

            holder.switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onSwitchToggled(position, isChecked, group);
                }
            });

            holder.obvSwitchEnableGroup.setOnClickListener(v -> {
                holder.switchToggle.setChecked(!holder.switchToggle.isChecked());
            });
        } else {
            holder.switchToggle.setChecked(false);
            holder.switchToggle.setEnabled(false);
            holder.textViewStatus.setText(UmeApplication.getAppContext().getText(R.string.app_status_disabled_for_groups));
            holder.obvSwitchEnableGroup.setClickable(false);
            holder.obvSwitchEnableGroup.setOnClickListener(null);
        }

        holder.buttonRename.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onRename(pos);
        });

        holder.buttonRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onDelete(pos);
        });

        holder.obvSwitchEnableGroup.setRectangleColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary));
        holder.llCard.setBackgroundColor(MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorSecondaryContainer));
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
        ObjectBackgroundView obvSwitchEnableGroup;
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
            obvSwitchEnableGroup = itemView.findViewById(R.id.obvSwitchEnableGroup);
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