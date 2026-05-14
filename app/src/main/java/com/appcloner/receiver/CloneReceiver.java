package com.appcloner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.appcloner.manager.CloneManager;
import com.appcloner.util.Logger;

public class CloneReceiver extends BroadcastReceiver {

    private static final String TAG = "CloneReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logger.d(TAG, "Received broadcast: " + action);

        if (Intent.ACTION_PACKAGE_ADDED.equals(action) ||
            Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            android.net.Uri data = intent.getData();
            if (data != null) {
                String packageName = data.getSchemeSpecificPart();
                Logger.d(TAG, "Package changed: " + packageName);

                handlePackageChange(packageName, Intent.ACTION_PACKAGE_ADDED.equals(action));
            }
        }
    }

    private void handlePackageChange(String packageName, boolean added) {
        CloneManager manager = CloneManager.getInstance();

        if (added) {
            Logger.d(TAG, "Cloned package installed: " + packageName);
        } else {
            Logger.d(TAG, "Cloned package removed: " + packageName);

            if (manager.isAppCloned(packageName)) {
                manager.removeCloneProfile(packageName);
            }
        }
    }
}
