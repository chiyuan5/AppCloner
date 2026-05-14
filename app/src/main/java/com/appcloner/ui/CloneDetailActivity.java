package com.appcloner.ui;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.appcloner.R;
import com.appcloner.manager.CloneManager;
import com.appcloner.manager.SpoofConfig;
import com.appcloner.model.AppInfo;
import com.appcloner.model.CloneProfile;
import com.appcloner.model.SpoofType;
import com.appcloner.util.Logger;

import java.util.List;

public class CloneDetailActivity extends AppCompatActivity {

    private static final String TAG = "CloneDetailActivity";

    private ImageView appIcon;
    private TextView appName, packageName, versionInfo, userIdText;
    private EditText editFakePackage, editFakeDeviceId, editFakeAndroidId, editFakeSerial;
    private Switch switchHideIcon, switchSpoofSignature;
    private Button btnStart, btnSave, btnDelete;

    private String packageNameStr;
    private CloneProfile profile;
    private final CloneManager cloneManager = CloneManager.getInstance();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clone_detail);

        packageNameStr = getIntent().getStringExtra("package_name");
        if (packageNameStr == null) {
            Toast.makeText(this, "Invalid package", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadProfile();
    }

    private void initViews() {
        appIcon = findViewById(R.id.app_icon);
        appName = findViewById(R.id.app_name);
        packageName = findViewById(R.id.package_name);
        versionInfo = findViewById(R.id.version_info);
        userIdText = findViewById(R.id.user_id);

        editFakePackage = findViewById(R.id.edit_fake_package);
        editFakeDeviceId = findViewById(R.id.edit_fake_device_id);
        editFakeAndroidId = findViewById(R.id.edit_fake_android_id);
        editFakeSerial = findViewById(R.id.edit_fake_serial);

        switchHideIcon = findViewById(R.id.switch_hide_icon);
        switchSpoofSignature = findViewById(R.id.switch_spoof_signature);

        btnStart = findViewById(R.id.btn_start);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupListeners() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startClone();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        switchHideIcon.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                if (profile != null) {
                    profile.setHideIcon(isChecked);
                }
            }
        });

        switchSpoofSignature.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                if (profile != null) {
                    profile.setSpoofSignature(isChecked);
                }
            }
        });
    }

    private void loadProfile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                profile = cloneManager.getCloneProfile(packageNameStr);

                if (profile == null) {
                    profile = cloneManager.createCloneProfile(packageNameStr);
                }

                final SpoofConfig spoofConfig = cloneManager.getSpoofConfig();

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        displayProfile(profile, spoofConfig);
                    }
                });
            }
        }).start();
    }

    private void displayProfile(CloneProfile profile, SpoofConfig spoofConfig) {
        try {
            android.content.pm.PackageInfo pkgInfo = getPackageManager().getPackageInfo(packageNameStr, 0);
            appName.setText(pkgInfo.applicationInfo.loadLabel(getPackageManager()));
            appIcon.setImageDrawable(pkgInfo.applicationInfo.loadIcon(getPackageManager()));
            packageName.setText("Package: " + packageNameStr);
            versionInfo.setText("Version: " + pkgInfo.versionName + " (" + pkgInfo.versionCode + ")");
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            Logger.e(TAG, "Package not found", e);
        }

        if (profile != null) {
            userIdText.setText("User ID: " + profile.getUserId());

            editFakePackage.setText(profile.getClonedPackageName());
            editFakeDeviceId.setText(profile.getFakeDeviceId());
            editFakeAndroidId.setText(profile.getFakeAndroidId());
            editFakeSerial.setText(profile.getFakeSerial());

            switchHideIcon.setChecked(profile.isHideIcon());
            switchSpoofSignature.setChecked(profile.isSpoofSignature());
        }
    }

    private void startClone() {
        if (profile == null) {
            Toast.makeText(this, "Profile not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progress = ProgressDialog.show(this, "Starting", "Launching cloned app...", true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = cloneManager.startClonedApp(packageNameStr);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        if (success) {
                            Toast.makeText(CloneDetailActivity.this, "Clone started", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CloneDetailActivity.this, "Failed to start clone", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void saveProfile() {
        if (profile == null) {
            return;
        }

        try {
            String fakePackage = editFakePackage.getText().toString().trim();
            String fakeDeviceId = editFakeDeviceId.getText().toString().trim();
            String fakeAndroidId = editFakeAndroidId.getText().toString().trim();
            String fakeSerial = editFakeSerial.getText().toString().trim();

            profile.setClonedPackageName(fakePackage);
            profile.setFakeDeviceId(fakeDeviceId);
            profile.setFakeAndroidId(fakeAndroidId);
            profile.setFakeSerial(fakeSerial);
            profile.setHideIcon(switchHideIcon.isChecked());
            profile.setSpoofSignature(switchSpoofSignature.isChecked());

            cloneManager.updateProfile(profile);

            SpoofConfig spoofConfig = cloneManager.getSpoofConfig();
            spoofConfig.saveProfile(profile);

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Logger.e(TAG, "Failed to save profile", e);
            Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Clone")
                .setMessage("Are you sure you want to delete this clone? This action cannot be undone.")
                .setPositiveButton("Delete", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        deleteClone();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteClone() {
        final ProgressDialog progress = ProgressDialog.show(this, "Deleting", "Removing clone...", true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = cloneManager.uninstallClone(packageNameStr);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        if (success) {
                            Toast.makeText(CloneDetailActivity.this, "Clone deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CloneDetailActivity.this, "Failed to delete clone", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }
}
