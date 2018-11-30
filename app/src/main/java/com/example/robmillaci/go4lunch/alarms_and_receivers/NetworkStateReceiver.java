package com.example.robmillaci.go4lunch.alarms_and_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.robmillaci.go4lunch.activities.MainActivity;
import com.example.robmillaci.go4lunch.utils.NetworkInfoChecker;

public class NetworkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (!NetworkInfoChecker.isNetworkAvailable(context)) {
            Toast.makeText(context, "No internet connection available", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, MainActivity.class));
        }
    }
}
