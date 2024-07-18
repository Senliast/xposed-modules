package com.senliast.updatesmanagerextended;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.senliast.MyApplication;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppListItemAdapter extends RecyclerView.Adapter<AppListItemAdapter.ItemViewHolder> {

    MyPreferencesManager myPreferencesManager = new MyPreferencesManager();
    private List<String> appsToBlockUpdates = new ArrayList<>();
    private List<AppListItem> itemList;
    public AppListItemAdapter(List<AppListItem> itemList) {
        this.itemList = itemList;
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        AppListItem item = itemList.get(position);
        holder.imageViewAppIcon.setImageDrawable(item.getAppIcon());
        holder.titleTextView.setText(item.getTitle());
        holder.packageNameTextView.setText(item.getPackageName());
        holder.switchBlockAppUpdate.setChecked(item.getGuiSwitch());

        holder.switchBlockAppUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                appsToBlockUpdates = new ArrayList<>(Arrays.asList((myPreferencesManager.getStringPreference("appsToBlockUpdates", "")).split(",")));
                if (isChecked) {
                    if (!appsToBlockUpdates.contains(holder.packageNameTextView.getText())) {
                        appsToBlockUpdates.add(holder.packageNameTextView.getText().toString());
                    }
                } else {
                    if (appsToBlockUpdates.contains(holder.packageNameTextView.getText())) {
                        appsToBlockUpdates.remove(holder.packageNameTextView.getText().toString());
                    }
                }
                // Notify ListView about changes
                myPreferencesManager.setStringPreference("appsToBlockUpdates", String.join(",", appsToBlockUpdates));
                Intent intent = new Intent("com.senliast.updatesmanagerextended.BROADCAST");
                intent.putExtra("type", "event");
                intent.putExtra("event_name", "on_app_switch_toggled");
                MyApplication.getAppContext().sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAppIcon;
        TextView titleTextView;
        TextView packageNameTextView;
        MaterialSwitch switchBlockAppUpdate;
        private int[][] colorStatesForSwitch;
        private int[] trackColorsForSwitch;
        private ColorStateList trackColorStateListForSwitch;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAppIcon = itemView.findViewById(R.id.imageViewAppIcon);
            titleTextView = itemView.findViewById(R.id.textVewTitle);
            packageNameTextView = itemView.findViewById(R.id.textViewPackageName);
            switchBlockAppUpdate = itemView.findViewById(R.id.switchBlockAppUpdate);
            colorStatesForSwitch = new int[][] {
                    new int[] { android.R.attr.state_checked },
                    new int[] { -android.R.attr.state_checked }
            };
            trackColorsForSwitch = new int[] {
                    MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, MyApplication.getAppContext().getColor(R.color.primary)),
                    MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorBackground, Color.WHITE)
            };
            trackColorStateListForSwitch = new ColorStateList(colorStatesForSwitch, trackColorsForSwitch);
            switchBlockAppUpdate.setTrackTintList(trackColorStateListForSwitch);
        }
    }
}
