package com.example.robmillaci.go4lunch.firebase;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.example.robmillaci.go4lunch.firebase.FirebaseHelper.DATABASE_TOKEN_PATH;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private String messageFromUserId;

    private static final String FROM_USER_ID_FIELD = "uId";
    private static final String FROM_USER_NAME_FIELD = "msg";
    private static final String FROM_USER_PIC = "picture_url";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        /*
      Called when message is received from trigger on firestore chat.
      see Functions -> index.js
     */
        Map<String, String> recievedData = remoteMessage.getData();

        messageFromUserId = recievedData.get(FROM_USER_ID_FIELD);

        sendNotification(recievedData);
    }

    /**
     * Create and show a custom notification containing the received message.
     **/
    private void sendNotification(Map<String, String> data) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.lunch_icon);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        @SuppressLint("IconColors") NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(getString(R.string.new_message_notification))
                .setContentText(data.get(FROM_USER_NAME_FIELD) + getString(R.string.new_message_from))
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo(getString(R.string.new_message_notification))
                .setLargeIcon(icon)
                .setColor(Color.RED)
                .setLights(Color.RED, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.lunch_icon);

        try {
            String picture_url = data.get(FROM_USER_PIC);
            if (picture_url != null && !"".equals(picture_url)) {
                URL url = new URL(picture_url);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(
                        new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(data.get(FROM_USER_NAME_FIELD))
                );
                notificationBuilder.setLargeIcon(bigPicture);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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
            FirebaseHelper.newMessage(messageFromUserId);
        }
    }

    @Override
    public void onNewToken(String s) {
        Log.d("MyFirebaseMessaging", "onNewToken: called");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> data = new HashMap<>();
        if (currentUser != null) {
            data.put(currentUser.getUid(), s);
            //noinspection ConstantConditions
            FirebaseFirestore.getInstance().collection(DATABASE_TOKEN_PATH).document(FirebaseAuth.getInstance().getUid()).update(data);
        }
    }


}

