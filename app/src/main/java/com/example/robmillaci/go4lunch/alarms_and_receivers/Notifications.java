package com.example.robmillaci.go4lunch.alarms_and_receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.activities.MainActivity;

public class Notifications extends BroadcastReceiver {
    public static final String PLACE_NAME = "placename";
    public static final String PLACE_IMAGE = "placeimageurl";
    public static final String PLACE_ADDRESS = "placeaddress";
    public static final String USERS_EATING_HERE = "usersEatingHere";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            String placeName = intent.getStringExtra(PLACE_NAME);
            Bitmap placeImage = (Bitmap) intent.getExtras().get(PLACE_IMAGE);
            String placeAddress = intent.getStringExtra(PLACE_ADDRESS);
            String[] usersEating = intent.getStringArrayExtra(USERS_EATING_HERE);
            createNotification(context, placeName, placeImage, placeAddress, usersEating);
        }
    }


    public static void createNotification(Context mContext, String placeName, Bitmap placeImage, String placeAddress, String[] usersEating) {
        StringBuilder sb = new StringBuilder();

        if (usersEating != null && usersEating.length > 0) {
            for (int i = 0; i < usersEating.length; i++) {
                if (i == 0) {
                    sb.append(usersEating[i]);
                } else if (i == usersEating.length - 1) {
                    sb.append(" and ");
                    sb.append(usersEating[i]);
                } else {
                    sb.append(", ");
                    sb.append(usersEating[i]);
                }
            }
        }

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String title = mContext.getString(R.string.your_lunch_at) + placeName;
        String message;
        if (sb.length() > 0) {
            message = "Join " + sb.toString() + " @ " + placeAddress;
        } else {
            message = placeAddress;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, "channel_id")
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setLargeIcon(placeImage)
                .setColor(Color.RED)
                .setLights(Color.RED, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.drawable.lunch_icon);


        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification Channel is required for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("channel description");
            channel.setShowBadge(true);
            channel.canShowBadge();
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }
}
