package com.appcloner.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.appcloner.R;
import com.appcloner.manager.CloneManager;
import com.appcloner.util.Logger;

public class CloneAppActivity extends AppCompatActivity {

    private static final String TAG = "CloneAppActivity";

    private TextView statusText;
    private String packageName;

    private final CloneManager cloneManager = CloneManager.getInstance();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clone_app);

        statusText = findViewById(R.id.status_text);

        packageName = getIntent().getStringExtra("package_name");
        if (packageName == null) {
            Toast.makeText(this, "Invalid package", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        startClonedApp();
    }

    private void startClonedApp() {
        statusText.setText("Starting " + packageName + "...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cloneManager.createCloneProfile(packageName);

                    boolean success = cloneManager.startClonedApp(packageName);

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (success) {
                                Logger.d(TAG, "Successfully started clone: " + packageName);
                                finish();
                            } else {
                                statusText.setText("Failed to start clone");
                                Toast.makeText(CloneAppActivity.this,
                                    "Failed to start clone. Make sure the app is cloned first.",
                                    Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Logger.e(TAG, "Error starting clone", e);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            statusText.setText("Error: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }
}
