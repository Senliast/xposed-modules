package com.senliast.updatesmanagerextended;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupSelectionActivity extends AppCompatActivity implements GroupActionListener, CountdownTimerHelper.OnTimerFinishListener {
    private List<GroupInfo> groups = new ArrayList<>();
    private GroupAdapter adapter;
    private RecyclerView recyclerView;
    private MaterialAlertDialogBuilder builder;
    private View viewDialogAdd;
    private AlertDialog alertDialogDialogAdd;
    private Button buttonDialogAddOkButton;
    private Button buttonDialogAddCancelButton;
    private EditText editTextGroupNameCreate;
    private View viewDialogRemove;
    private AlertDialog alertDialogDialogRemove;
    private Button buttonDialogRemoveOkButton;
    private Button buttonDialogRemoveCancelButton;
    private TextView textViewDialogRemoveMessage;
    private AlertDialog alertDialogDialogRename;
    private View viewDialogRename;
    private Button buttonDialogRenameOkButton;
    private Button buttonDialogRenameCancelButton;
    private EditText editTextGroupNameRename;
    private Toolbar toolbar;
    private MyPreferencesManager myPreferencesManager = new MyPreferencesManager();
    private View viewDialogDisable;
    private AlertDialog alertDialogDialogDisable;
    private Button buttonDialogDisablePermanentlyButton;
    private Button buttonDialogDisable15Min;
    private Button buttonDialogDisable30Min;
    private Button buttonDialogDisable45Min;
    private Button buttonDialogDisable60Min;
    private Button buttonDialogDisableCancel;
    private TextView textViewDialogDisableTitle;
    private boolean dialogDisableAnyOptionSelected = false;
    private View viewScrollbarProtector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityGroupSelection), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EdgeToEdge.enable(this);
        DynamicColors.applyToActivityIfAvailable(this);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.back_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setVerticalScrollBarEnabled(true);

        FastScroller fastScroller = new FastScrollerBuilder(recyclerView)
                .setThumbDrawable(getDrawable(R.drawable.fastscroll_thumb))
                .setTrackDrawable(getDrawable(R.drawable.fastscroll_track))
                .setPadding(0, 0,  0, 100)
                .build();

        groups.addAll(myPreferencesManager.getGroups());
        if (adapter != null) { adapter.notifyDataSetChanged(); }

        adapter = new GroupAdapter(groups, this, this, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setPadding(0, 0, 0, Utils.converDpToPx(this, 100));
        recyclerView.setClipToPadding(false);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showAddDialog());

        enableDragAndDrop();

        viewScrollbarProtector = findViewById(R.id.viewScrollbarProtector);
        adapter.notifyDataSetChanged();
    }

    public void showAddDialog() {
        viewDialogAdd = getLayoutInflater().inflate(R.layout.dialog_add_group, null);
        builder = new MaterialAlertDialogBuilder(this);
        editTextGroupNameCreate = viewDialogAdd.findViewById(R.id.editTextGroupName);
        buttonDialogAddOkButton = viewDialogAdd.findViewById(R.id.buttonOk);
        buttonDialogAddOkButton.setEnabled(false);
        buttonDialogAddCancelButton = viewDialogAdd.findViewById(R.id.buttonCancel);
        buttonDialogAddOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogDialogAdd.dismiss();
                String name = editTextGroupNameCreate.getText().toString();
                groups.add(new GroupInfo(name, "", "", false, "enabled_immediately", 0L));
                adapter.notifyItemInserted(groups.size() - 1);
                myPreferencesManager.saveGroups(groups);
                recyclerView.scrollToPosition(groups.size() - 1);
            }
        });
        buttonDialogAddCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogDialogAdd.dismiss();
            }
        });
        editTextGroupNameCreate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonDialogAddOkButton.setEnabled(!editTextGroupNameCreate.getText().toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        builder.setView(viewDialogAdd);
        alertDialogDialogAdd = builder.create();
        alertDialogDialogAdd.setOnShowListener(d -> {
            editTextGroupNameCreate.requestFocus();

            alertDialogDialogAdd.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            );

            editTextGroupNameCreate.post(() -> {
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.showSoftInput(editTextGroupNameCreate,
                            InputMethodManager.SHOW_IMPLICIT);
                }
            });
        });
        alertDialogDialogAdd.setCancelable(true);
        alertDialogDialogAdd.show();
    }

    private void enableDragAndDrop() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {

                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
                    return false;
                }

                Collections.swap(groups, fromPosition, toPosition);
                adapter.notifyItemMoved(fromPosition, toPosition);
                myPreferencesManager.saveGroups(groups);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // No swipe support
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;   // This enables long-press to start dragging
            }

            @Override
            public void onSelectedChanged(@NonNull RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);

                if (viewHolder instanceof GroupAdapter.ViewHolder) {
                    GroupAdapter.ViewHolder holder = (GroupAdapter.ViewHolder) viewHolder;

                    if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                        viewScrollbarProtector.setVisibility(View.VISIBLE);
                        applyDarkDragEffect(holder);
                    }
                    else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                        viewScrollbarProtector.setVisibility(View.GONE);
                        removeDarkDragEffect(holder);
                    }
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (viewHolder instanceof GroupAdapter.ViewHolder) {
                    removeDarkDragEffect((GroupAdapter.ViewHolder) viewHolder);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onRename(int position) {
        if (position < 0 || position >= groups.size()) return;
        GroupInfo group = groups.get(position);

        viewDialogRename = getLayoutInflater().inflate(R.layout.dialog_rename_group, null);
        builder = new MaterialAlertDialogBuilder(this);
        editTextGroupNameRename = viewDialogRename.findViewById(R.id.editTextGroupName);
        buttonDialogRenameOkButton = viewDialogRename.findViewById(R.id.buttonOk);
        buttonDialogRenameOkButton.setEnabled(false);
        buttonDialogRenameCancelButton = viewDialogRename.findViewById(R.id.buttonCancel);
        buttonDialogRenameOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogDialogRename.dismiss();
                String newName = editTextGroupNameRename.getText().toString();
                group.setName(newName);
                adapter.notifyItemChanged(position);
                myPreferencesManager.saveGroups(groups);
            }
        });
        buttonDialogRenameCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogDialogRename.dismiss();
            }
        });
        editTextGroupNameRename.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonDialogRenameOkButton.setEnabled(!editTextGroupNameRename.getText().toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        builder.setView(viewDialogRename);
        alertDialogDialogRename = builder.create();
        alertDialogDialogRename.setOnShowListener(d -> {
            editTextGroupNameRename.requestFocus();

            alertDialogDialogRename.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            );

            editTextGroupNameRename.post(() -> {
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.showSoftInput(editTextGroupNameRename,
                            InputMethodManager.SHOW_IMPLICIT);
                }
            });
        });
        alertDialogDialogRename.setCancelable(true);
        alertDialogDialogRename.show();
        editTextGroupNameRename.requestFocus();
    }

    @Override
    public void onDelete(int position) {
        if (position < 0 || position >= groups.size()) return;

        viewDialogRemove = getLayoutInflater().inflate(R.layout.dialog_remove_group, null);
        builder = new MaterialAlertDialogBuilder(this);
        buttonDialogRemoveOkButton = viewDialogRemove.findViewById(R.id.buttonOk);
        buttonDialogRemoveCancelButton = viewDialogRemove.findViewById(R.id.buttonCancel);
        textViewDialogRemoveMessage = viewDialogRemove.findViewById(R.id.textViewMessage);
        textViewDialogRemoveMessage.setText(getString(R.string.remove_group_text_1) + groups.get(position).getName() + getString(R.string.remove_group_text_2));
        buttonDialogRemoveOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogDialogRemove.dismiss();
                groups.remove(position);
                adapter.notifyItemRemoved(position);
                myPreferencesManager.saveGroups(groups);
            }
        });
        buttonDialogRemoveCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogDialogRemove.dismiss();
            }
        });

        builder.setView(viewDialogRemove);
        alertDialogDialogRemove = builder.create();
        alertDialogDialogRemove.setCancelable(true);
        alertDialogDialogRemove.show();
    }

    @Override
    public void onButtonSelectInstallationSourceClicked(int position) {
        myPreferencesManager.setIntPreference("groupToEdit", position);
        Intent intent = new Intent(GroupSelectionActivity.this, InstallationSourceSelectionActivity.class);
        startActivity(intent);
    }

    @Override
    public void onButtonSelectBlacklistedAppsClicked(int position) {
        myPreferencesManager.setIntPreference("groupToEdit", position);
        Intent intent = new Intent(GroupSelectionActivity.this, AppSelectionActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Adapter will loose connection to the list if it will be updated via "groups = new ArrayList<>()",
        // because a new list with a new reference will be created, but adapter will still has the old
        // list - even after "adapter.notifyDataSetChanged()". So its needed to keep the current list
        // at all cost. Therefore, the list will be cleared and filled again. Because, for the inventor of
        // adapter, it was of course not possible to make adapter refresh the reference automatically...
        groups.clear();
        groups.addAll(myPreferencesManager.getGroups());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSwitchToggled(int position, boolean desiredState, GroupInfo group) {
        // We will update the preferences and tell the adapter to reload them.
        if (desiredState) {
            group.setStatus("enabled_immediately");
            group.setStatusTime(0L);
            myPreferencesManager.saveGroups(groups);
            adapter.notifyItemChanged(position);
        } else {
            dialogDisableAnyOptionSelected = false;
            viewDialogDisable = getLayoutInflater().inflate(R.layout.dialog_disable, null);
            builder = new MaterialAlertDialogBuilder(this);
            buttonDialogDisablePermanentlyButton = viewDialogDisable.findViewById(R.id.buttonPermanently);
            buttonDialogDisable15Min = viewDialogDisable.findViewById(R.id.button15Min);
            buttonDialogDisable30Min = viewDialogDisable.findViewById(R.id.button30Min);
            buttonDialogDisable45Min = viewDialogDisable.findViewById(R.id.button45Min);
            buttonDialogDisable60Min = viewDialogDisable.findViewById(R.id.button60Min);
            buttonDialogDisableCancel = viewDialogDisable.findViewById(R.id.buttonCancel);
            textViewDialogDisableTitle = viewDialogDisable.findViewById(R.id.textViewTitle);
            textViewDialogDisableTitle.setText(getString(R.string.disable_group));
            buttonDialogDisablePermanentlyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    group.setStatus("disabled");
                    group.setStatusTime(0L);
                    myPreferencesManager.saveGroups(groups);
                    dialogDisableAnyOptionSelected = true;
                    alertDialogDialogDisable.dismiss();
                    adapter.notifyDataSetChanged();
                }
            });
            buttonDialogDisable15Min.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    group.setStatus("enabled_since");
                    group.setStatusTime(System.currentTimeMillis() + (15 * 60 * 1000));
                    myPreferencesManager.saveGroups(groups);
                    dialogDisableAnyOptionSelected = true;
                    alertDialogDialogDisable.dismiss();
                    adapter.notifyDataSetChanged();
                }
            });
            buttonDialogDisable30Min.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    group.setStatus("enabled_since");
                    group.setStatusTime(System.currentTimeMillis() + (30 * 60 * 1000));
                    myPreferencesManager.saveGroups(groups);
                    dialogDisableAnyOptionSelected = true;
                    alertDialogDialogDisable.dismiss();
                    adapter.notifyDataSetChanged();
                }
            });
            buttonDialogDisable45Min.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    group.setStatus("enabled_since");
                    group.setStatusTime(System.currentTimeMillis() + (45 * 60 * 1000));
                    myPreferencesManager.saveGroups(groups);
                    dialogDisableAnyOptionSelected = true;
                    alertDialogDialogDisable.dismiss();
                    adapter.notifyDataSetChanged();
                }
            });
            buttonDialogDisable60Min.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    group.setStatus("enabled_since");
                    group.setStatusTime(System.currentTimeMillis() + (60 * 60 * 1000));
                    myPreferencesManager.saveGroups(groups);
                    dialogDisableAnyOptionSelected = true;
                    alertDialogDialogDisable.dismiss();
                    adapter.notifyDataSetChanged();
                }
            });
            buttonDialogDisableCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter != null) {
                        adapter.revertSwitch(position);
                    }
                    alertDialogDialogDisable.dismiss();
                    adapter.notifyDataSetChanged();
                }
            });
            builder.setView(viewDialogDisable);
            alertDialogDialogDisable = builder.create();
            alertDialogDialogDisable.setCancelable(true);

            // Revert switch as well if dialog has been dismissed not by "Cancel" button, but,
            // for example, by clicking outside.
            alertDialogDialogDisable.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!dialogDisableAnyOptionSelected) {
                        if (adapter != null) {
                            adapter.revertSwitch(position);
                        }
                    }
                }
            });
            alertDialogDialogDisable.show();
        }
    }

    @Override
    public void onTimerFinished(int position) {
        groups.get(position).setStatus("enabled_immediately");
        groups.get(position).setStatusTime(0L);
        adapter.notifyItemChanged(position);
    }

    private void applyDarkDragEffect(GroupAdapter.ViewHolder holder) {
        if (holder.llCard != null) {
            holder.llCard.setForeground(ContextCompat.getDrawable(this, R.drawable.drag_overlay));
        }

        holder.itemView.setScaleX(1.03f);
        holder.itemView.setScaleY(1.03f);
    }

    private void removeDarkDragEffect(GroupAdapter.ViewHolder holder) {
        if (holder.llCard != null) {
            holder.llCard.setForeground(null);
        }

        holder.itemView.setScaleX(1.0f);
        holder.itemView.setScaleY(1.0f);
    }
}

interface GroupActionListener {
    void onRename(int position);
    void onDelete(int position);
    void onButtonSelectInstallationSourceClicked(int position);
    void onButtonSelectBlacklistedAppsClicked(int position);
    void onSwitchToggled(int position, boolean desiredState, GroupInfo group);
}