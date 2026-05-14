package com.appcloner.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.appcloner.R;
import com.appcloner.binder.BinderInterceptor;
import com.appcloner.manager.CloneManager;
import com.appcloner.manager.SpoofConfig;
import com.appcloner.model.SpoofType;
import com.appcloner.util.Logger;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private Switch switchGlobalSpoof, switchLogging;
    private EditText editGlobalDeviceId, editGlobalAndroidId, editGlobalSerial, editGlobalImei;
    private TextView cloneCountText;

    private final CloneManager cloneManager = CloneManager.getInstance();
    private final BinderInterceptor binderInterceptor = BinderInterceptor.getInstance();
    private SpoofConfig spoofConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        spoofConfig = cloneManager.getSpoofConfig();

        initViews();
        setupListeners();
        loadSettings();
    }

    private void initViews() {
        switchGlobalSpoof = findViewById(R.id.switch_global_spoof);
        switchLogging = findViewById(R.id.switch_logging);

        editGlobalDeviceId = findViewById(R.id.edit_global_device_id);
        editGlobalAndroidId = findViewById(R.id.edit_global_android_id);
        editGlobalSerial = findViewById(R.id.edit_global_serial);
        editGlobalImei = findViewById(R.id.edit_global_imei);

        cloneCountText = findViewById(R.id.clone_count);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btn_generate_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateNewDeviceIds();
            }
        });

        findViewById(R.id.btn_save_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }

    private void setupListeners() {
        switchLogging.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                binderInterceptor.setLoggingEnabled(isChecked);
            }
        });
    }

    private void loadSettings() {
        try {
            cloneCountText.setText("Total Clones: " + cloneManager.getCloneCount());

            editGlobalDeviceId.setText(spoofConfig.getGlobalSpoofedValue(SpoofType.DEVICE_ID));
            editGlobalAndroidId.setText(spoofConfig.getGlobalSpoofedValue(SpoofType.ANDROID_ID));
            editGlobalSerial.setText(spoofConfig.getGlobalSpoofedValue(SpoofType.SERIAL));
            editGlobalImei.setText(spoofConfig.getGlobalSpoofedValue(SpoofType.IMEI));

        } catch (Exception e) {
            Logger.e(TAG, "Failed to load settings", e);
        }
    }

    private void saveSettings() {
        try {
            spoofConfig.setGlobalSpoofedValue(SpoofType.DEVICE_ID,
                editGlobalDeviceId.getText().toString().trim());
            spoofConfig.setGlobalSpoofedValue(SpoofType.ANDROID_ID,
                editGlobalAndroidId.getText().toString().trim());
            spoofConfig.setGlobalSpoofedValue(SpoofType.SERIAL,
                editGlobalSerial.getText().toString().trim());
            spoofConfig.setGlobalSpoofedValue(SpoofType.IMEI,
                editGlobalImei.getText().toString().trim());

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Logger.e(TAG, "Failed to save settings", e);
            Toast.makeText(this, "Failed to save settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateNewDeviceIds() {
        try {
            java.util.UUID uuid = java.util.UUID.randomUUID();
            editGlobalDeviceId.setText("15" + String.format("%014d", Math.abs(uuid.getMostSignificantBits() % 100000000000000L)));
            editGlobalAndroidId.setText(String.format("%016x", Math.abs(uuid.getLeastSignificantBits() % 0xFFFFFFFFFFFFL)));

            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder serial = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                serial.append(chars.charAt((int)(Math.random() * chars.length())));
            }
            editGlobalSerial.setText(serial.toString());

            StringBuilder imei = new StringBuilder("35");
            imei.append(String.format("%02d", (int)(Math.random() * 100)));
            imei.append(String.format("%08d", Math.abs(uuid.getMostSignificantBits() % 100000000)));
            int sum = 0;
            String tempImei = imei.toString();
            for (int i = 0; i < tempImei.length(); i++) {
                int n = Character.getNumericValue(tempImei.charAt(i));
                if (i % 2 == 0) {
                    n *= 2;
                    if (n > 9) n -= 9;
                }
                sum += n;
            }
            imei.append((10 - (sum % 10)) % 10);
            editGlobalImei.setText(imei.toString());

            Toast.makeText(this, "New device IDs generated", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Logger.e(TAG, "Failed to generate device IDs", e);
        }
    }
}
