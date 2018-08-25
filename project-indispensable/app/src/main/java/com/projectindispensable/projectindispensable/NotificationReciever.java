package com.projectindispensable.projectindispensable;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

public class NotificationReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent repeating_intent = new Intent(context, NotificationActivity.class);

        String medicineName = intent.getStringExtra("medicineName");
        String dosage = intent.getStringExtra("dosage");
        String startDate = intent.getStringExtra("startDate");
//        String time = intent.getStringExtra("time");
        String numDays = intent.getStringExtra("numDays");
        String notes = intent.getStringExtra("notes");
        List<String> allTimes = intent.getStringArrayListExtra("allTimes");

        int numTimes = intent.getIntExtra("numTimes", 0);
        int hour = intent.getIntExtra("hour", 0);
        int min = intent.getIntExtra("min", 0);

        int reqID = intent.getIntExtra("reqID", 0);

        repeating_intent.putExtra("medicineName", medicineName);
        repeating_intent.putExtra("dosage", dosage);
        repeating_intent.putExtra("startDate", startDate);
//        repeating_intent.putExtra("time", time);
        repeating_intent.putExtra("numDays", numDays);
        repeating_intent.putExtra("notes", notes);
//        repeating_intent.putExtra("reqID", reqID);
        repeating_intent.putExtra("numTimes", numTimes);
        repeating_intent.putStringArrayListExtra("allTimes", (ArrayList<String>) allTimes);

        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqID, repeating_intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Reminder")
//                .setContentText("Take " + medicineName + " - " + time)
            .setSound(defaultSoundURI)
            .setAutoCancel(true);

        String text;
        if (min < 10 && hour < 10) {
            text = "0" + hour +":" + "0" + min;
        } else if (min < 10) {
            text = hour + ":0" + min;
        } else if (hour < 10) {
            text = "0" + hour + ":" + min;
        }
        else {
            text = hour + ":" + min;
        }

        builder.setContentText("Take " + medicineName + " - " + text);

        if (notificationManager != null) {
            notificationManager.notify(reqID, builder.build());
        }
    }
}
