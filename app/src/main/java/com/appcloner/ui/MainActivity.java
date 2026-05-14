package com.appcloner.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appcloner.R;
import com.appcloner.manager.CloneManager;
import com.appcloner.model.AppInfo;
import com.appcloner.util.Logger;
import com.appcloner.adapter.AppListAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private AppListAdapter adapter;
    private TextView emptyView;
    private Button btnManage, btnSettings;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final CloneManager cloneManager = CloneManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApps();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        emptyView = findViewById(R.id.empty_view);
        btnManage = findViewById(R.id.btn_manage);
        btnSettings = findViewById(R.id.btn_settings);
    }

    private void setupRecyclerView() {
        adapter = new AppListAdapter(this, new ArrayList<>());
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new AppListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AppInfo appInfo) {
                if (appInfo.isCloned()) {
                    openCloneDetail(appInfo);
                } else {
                    showCloneDialog(appInfo);
                }
            }

            @Override
            public void onItemLongClick(AppInfo appInfo) {
                showAppOptions(appInfo);
            }
        });
    }

    private void setupListeners() {
        btnManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openManageScreen();
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingsScreen();
            }
        });
    }

    private void loadApps() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<AppInfo> apps = cloneManager.getInstalledApps(false);

                    mainHandler.post(new Runnable() {
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
                } catch (Exception e) {
                    Logger.e(TAG, "Failed to load apps", e);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Failed to load apps", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void showCloneDialog(final AppInfo appInfo) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Clone App")
                .setMessage("Do you want to create a clone of " + appInfo.getAppName() + "?")
                .setPositiveButton("Clone", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        startClone(appInfo);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startClone(final AppInfo appInfo) {
        final ProgressDialog progress = ProgressDialog.show(this, "Cloning", "Creating clone...", true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cloneManager.createCloneProfile(appInfo.getPackageName());

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            Toast.makeText(MainActivity.this, "Clone created successfully", Toast.LENGTH_SHORT).show();
                            loadApps();
                        }
                    });
                } catch (final Exception e) {
                    Logger.e(TAG, "Failed to clone app", e);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            Toast.makeText(MainActivity.this, "Failed to clone: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void openCloneDetail(AppInfo appInfo) {
        Intent intent = new Intent(this, CloneDetailActivity.class);
        intent.putExtra("package_name", appInfo.getPackageName());
        startActivity(intent);
    }

    private void showAppOptions(final AppInfo appInfo) {
        String[] options;
        if (appInfo.isCloned()) {
            options = new String[]{"Open", "Settings", "Uninstall Clone"};
        } else {
            options = new String[]{"Clone", "App Info"};
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle(appInfo.getAppName())
                .setItems(options, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        handleAppOption(appInfo, which);
                    }
                })
                .show();
    }

    private void handleAppOption(AppInfo appInfo, int which) {
        if (appInfo.isCloned()) {
            switch (which) {
                case 0:
                    cloneManager.startClonedApp(appInfo.getPackageName());
                    break;
                case 1:
                    openCloneDetail(appInfo);
                    break;
                case 2:
                    confirmUninstall(appInfo);
                    break;
            }
        } else {
            switch (which) {
                case 0:
                    startClone(appInfo);
                    break;
                case 1:
                    openAppInfo(appInfo);
                    break;
            }
        }
    }

    private void confirmUninstall(final AppInfo appInfo) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Uninstall Clone")
                .setMessage("Are you sure you want to uninstall the clone of " + appInfo.getAppName() + "?")
                .setPositiveButton("Uninstall", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        cloneManager.uninstallClone(appInfo.getPackageName());
                        loadApps();
                        Toast.makeText(MainActivity.this, "Clone uninstalled", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openAppInfo(AppInfo appInfo) {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + appInfo.getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            Logger.e(TAG, "Failed to open app info", e);
        }
    }

    private void openManageScreen() {
        Intent intent = new Intent(this, AppListActivity.class);
        startActivity(intent);
    }

    private void openSettingsScreen() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
