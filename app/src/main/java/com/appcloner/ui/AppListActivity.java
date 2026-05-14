package com.appcloner.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appcloner.R;
import com.appcloner.manager.CloneManager;
import com.appcloner.model.AppInfo;
import com.appcloner.adapter.CloneListAdapter;

import java.util.List;
import java.util.ArrayList;

public class AppListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CloneListAdapter adapter;
    private TextView emptyView;

    private final CloneManager cloneManager = CloneManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        initViews();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClonedApps();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        emptyView = findViewById(R.id.empty_view);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new CloneListAdapter(this, new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new CloneListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AppInfo appInfo) {
                openCloneDetail(appInfo);
            }

            @Override
            public void onStartClick(AppInfo appInfo) {
                cloneManager.startClonedApp(appInfo.getPackageName());
            }

            @Override
            public void onDeleteClick(AppInfo appInfo) {
                confirmDelete(appInfo);
            }
        });
    }

    private void loadClonedApps() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<AppInfo> apps = cloneManager.getClonedApps();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.updateData(apps);

                        if (apps.isEmpty()) {
                            emptyView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }).start();
    }

    private void openCloneDetail(AppInfo appInfo) {
        android.content.Intent intent = new android.content.Intent(this, CloneDetailActivity.class);
        intent.putExtra("package_name", appInfo.getPackageName());
        startActivity(intent);
    }

    private void confirmDelete(final AppInfo appInfo) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Clone")
                .setMessage("Are you sure you want to delete this clone?")
                .setPositiveButton("Delete", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        cloneManager.uninstallClone(appInfo.getPackageName());
                        loadClonedApps();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
