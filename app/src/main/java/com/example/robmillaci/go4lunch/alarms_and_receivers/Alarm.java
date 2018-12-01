package com.example.robmillaci.go4lunch.alarms_and_receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.Calendar;

/**
 * Creates an alarm that will notify the user of their selected place to eat at midday.
 * Sends the place name, the place image, the place address and the users eating here to {@link Notifications}
 */
public class Alarm {
    private static AlarmManager alarm;

    public static void scheduleAlarm(Context mcontext, String placeName, Bitmap placeImage, String placeAddress, String[] usersEating) {
        int hourNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        long scheduleTime;

        if (hourNow > 12) {//it is past midday so set the alarm for the next day
            // Create a calendar object that is set to 12pm the same day
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 1);
            c.set(Calendar.HOUR_OF_DAY, 12);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            scheduleTime = c.getTimeInMillis();
        }else{ //its not midday yet, so set the alarm for midday
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 0);
            c.set(Calendar.HOUR_OF_DAY, 12);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            scheduleTime = c.getTimeInMillis();
        }

        // Create an Intent and set the class that will execute when the Alarm triggers. Here we have
        // specified the notifications class in the Intent. The onReceive() method of this class will execute when the broadcast from the alarm is received.
        Intent intentAlarm = new Intent(mcontext, Notifications.class);
        intentAlarm.putExtra(Notifications.PLACE_NAME, placeName);
        intentAlarm.putExtra(Notifications.PLACE_IMAGE, placeImage);
        intentAlarm.putExtra(Notifications.PLACE_ADDRESS, placeAddress);
        intentAlarm.putExtra(Notifications.USERS_EATING_HERE, usersEating);

        // Get the Alarm Service.
        alarm = (AlarmManager) mcontext.getSystemService(Context.ALARM_SERVICE);

        if (alarm != null) {
            alarm.set(AlarmManager.RTC, scheduleTime, PendingIntent.getBroadcast(mcontext, 1, intentAlarm, PendingIntent.FLAG_CANCEL_CURRENT));
        }
    }

    public static void cancelAlarm(Context mContext) {
        Intent intentAlarm = new Intent(mContext, Notifications.class);
        if (alarm != null) {
            alarm.cancel(PendingIntent.getBroadcast(mContext, 1, intentAlarm, PendingIntent.FLAG_CANCEL_CURRENT));
            alarm = null;
        }
    }
}
