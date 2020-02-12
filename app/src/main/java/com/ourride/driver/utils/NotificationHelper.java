package com.ourride.driver.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.ourride.driver.R;

public class NotificationHelper {

    private Context mContext;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";

    public NotificationHelper(Context context) {
        mContext = context;
    }

    /**
     * Create and push the notification
     */
    public void createNotification(String title, String message, String strPostId, String strGetFrom, String strFanId) {
        /**Creates an explicit intent for an Activity in your app**/
        Intent resultIntent = null;

        if (title.equalsIgnoreCase("Comment") || title.equalsIgnoreCase("Like")) {
            //resultIntent = new Intent(mContext, PostDetailActivity.class);
            resultIntent.putExtra("get_from", strGetFrom);
            resultIntent.putExtra("post_id", strPostId);
        } else {
            //resultIntent = new Intent(mContext, UserProfileActivity.class);
            resultIntent.putExtra("fan_id", strFanId);
        }

        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent);

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
    }

    public void showNotification(String title, String message, String strPostId, String strGetFrom, String strFanId) {

        Intent resultIntent = null;

        if (title.equalsIgnoreCase("Comment") || title.equalsIgnoreCase("Like")) {
            //resultIntent = new Intent(mContext, PostDetailActivity.class);
            resultIntent.putExtra("get_from", strGetFrom);
            resultIntent.putExtra("post_id", strPostId);
        } else {
            //resultIntent = new Intent(mContext, UserProfileActivity.class);
            resultIntent.putExtra("fan_id", strFanId);
        }

        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        /*if (type.equalsIgnoreCase("ARTICLE")) {

            intent = new Intent(mContext, ProfileActivity.class);
            intent.putExtra("from", "notification");
            intent.putExtra("id", id);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent = new Intent(mContext, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }*/

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0 /* Request code */, resultIntent,
                PendingIntent.FLAG_ONE_SHOT);
        Log.v("dip", message);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setSmallIcon(getNotificationIcon())
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setChannelId(title)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // The user-visible name of the channel.
            CharSequence name = "Notification";
            // The user-visible description of the channel.
            String description = "Notifications regarding our products";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(title, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(mContext.getResources().getColor(R.color.colorAccent));
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
