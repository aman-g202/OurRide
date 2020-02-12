package com.ourride.driver.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ourride.driver.R;
import com.ourride.driver.receiver.MyBroadCastReceiver;
import com.ourride.driver.ui.MainHomeActivity;
import com.ourride.driver.ui.activity.DriverMapActivity;
import com.ourride.driver.ui.activity.LoginActivity;
import com.ourride.driver.utils.Button_Listener_Accept;
import com.ourride.driver.utils.Button_Listener_Accept_User;
import com.ourride.driver.utils.Button_Listener_Reject;
import com.ourride.driver.utils.Button_Listener_Reject_User;
import com.ourride.driver.utils.SharedPrefrence;
import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private int notification_id;
    private RemoteViews remoteViews;
    private Context context;
    private MyBroadCastReceiver myBroadcastReceiver;
    public  String NOTIFICATION_CHANNEL_ID = "10001";

    @Override
    public void onNewToken(String s) {
        context = this;
        sendRegistrationToServer(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage == null)
            return;

        context = this;
        int requestID = (int) System.currentTimeMillis();

        Map<String, String> data = remoteMessage.getData();
        Log.d("Messaging Service:::::", data.toString());

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        remoteViews = new RemoteViews(getPackageName(),R.layout.custom_notification);
        remoteViews.setImageViewResource(R.id.imageNotification,R.drawable.logo_driver);
        remoteViews.setTextViewText(R.id.titleNotification,"Ride Request");
        remoteViews.setTextViewText(R.id.messageNotification,data.get("message]"));

//        notification_id = (int) System.currentTimeMillis();
        if (data.containsKey("notification_id]")) {

            notification_id = Integer.parseInt(data.get("notification_id]"));

            Intent button_intent_accept = new Intent("button_click_accept");
            button_intent_accept.putExtra("id",notification_id);
            button_intent_accept.putExtra("driver_id", data.get("driver_id]"));
            button_intent_accept.setClass(context, Button_Listener_Accept.class);
            PendingIntent button_accept_event = PendingIntent.getBroadcast(context,notification_id,
                    button_intent_accept,0);

            Intent button_intent_reject = new Intent("button_click_reject");
            button_intent_reject.putExtra("id",notification_id);
            button_intent_reject.setClass(context, Button_Listener_Reject.class);
            PendingIntent button_reject_event = PendingIntent.getBroadcast(context,notification_id,
                    button_intent_reject,0);

            remoteViews.setOnClickPendingIntent(R.id.acceptButton,button_accept_event);
            remoteViews.setOnClickPendingIntent(R.id.rejectButton,button_reject_event);

            Intent notification_intent = new Intent(context, MainHomeActivity.class);
            notification_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,requestID,notification_intent,PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

                // Configure the notification channel.
                notificationChannel.setDescription("Channel description");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

            builder.setSmallIcon(R.drawable.logo_driver)
                    .setAutoCancel(false) // don't remove notification upon clicking
                    .setOngoing(true) // don't remove notification upon sliding
                    .setContentTitle("Our Ride")
                    .setPriority(Notification.PRIORITY_MAX)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE)
                    .setCustomBigContentView(remoteViews)
                    .setContentIntent(pendingIntent);

        }
        else if (data.containsKey("request_type]")) {
            if (data.get("request_type]").equals("bookride")) {
                notification_id = (int) System.currentTimeMillis();

                Intent button_click_accept_bookride = new Intent("button_click_accept_bookride");
                button_click_accept_bookride.putExtra("id",notification_id);
                button_click_accept_bookride.putExtra("passenger_id", data.get("passanger_id]"));
                button_click_accept_bookride.putExtra("booking_id", data.get("booking_request_id]"));
                button_click_accept_bookride.putExtra("booking_pk_id", data.get("booking_id]"));
                button_click_accept_bookride.putExtra("user_to_lat", data.get("to_lat]"));
                button_click_accept_bookride.putExtra("user_to_lng", data.get("to_lng]"));
                button_click_accept_bookride.putExtra("user_from_lat", data.get("from_lat]"));
                button_click_accept_bookride.putExtra("user_from_lng", data.get("from_lng]"));
                button_click_accept_bookride.setClass(context, Button_Listener_Accept_User.class);
                PendingIntent button_accept_event_bookride = PendingIntent.getBroadcast(context,notification_id,
                        button_click_accept_bookride,0);

                Intent button_click_reject_bookride = new Intent("button_click_reject_bookride");
                button_click_reject_bookride.putExtra("id",notification_id);
                button_click_reject_bookride.putExtra("passenger_id", data.get("passenger_id]"));
                button_click_reject_bookride.putExtra("booking_id", data.get("booking_request_id]"));
                button_click_reject_bookride.setClass(context, Button_Listener_Reject_User.class);
                PendingIntent button_reject_event_bookride = PendingIntent.getBroadcast(context,notification_id,
                        button_click_reject_bookride,0);

                remoteViews.setOnClickPendingIntent(R.id.acceptButton,button_accept_event_bookride);
                remoteViews.setOnClickPendingIntent(R.id.rejectButton,button_reject_event_bookride);

                Intent notification_intent = new Intent(context, MainHomeActivity.class);
                notification_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(context,requestID,notification_intent,PendingIntent.FLAG_UPDATE_CURRENT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

                    // Configure the notification channel.
                    notificationChannel.setDescription("Channel description");
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                    notificationChannel.enableVibration(true);
                    notificationManager.createNotificationChannel(notificationChannel);
                }

                builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

                builder.setSmallIcon(R.drawable.logo_driver)
                        .setAutoCancel(false) // don't remove notification upon clicking
                        .setOngoing(true) // don't remove notification upon sliding
                        .setContentTitle("Our Ride")
                        .setPriority(Notification.PRIORITY_MAX)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                        .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE)
                        .setCustomBigContentView(remoteViews)
                        .setContentIntent(pendingIntent);
            }
        }
        else {
            notification_id = (int) System.currentTimeMillis();
            String mess = data.get("message]");
            String word[] = mess.split(" ");
            Intent notification_intent;
            int flag = 0;
            for (int i=0; i< word.length; i++) {
                if (word[i].equals("rejected")) {
                    flag = 1;
                    break;
                }
                if (word[i].equals("cancelled.")) {
                    flag = 2;
                    break;
                }
            }

            if (flag == 1) {
                SharedPrefrence.saveInt(context, "selected_wingman_id", 0);
                SharedPrefrence.save(context, "selected_wingman_lat", "0.0");
                SharedPrefrence.save(context, "selected_wingman_lng", "0.0");
                if (SharedPrefrence.getInt(context,"user_id") > 0) {
                    notification_intent = new Intent(context, MainHomeActivity.class);
                } else {
                    notification_intent = new Intent(context, LoginActivity.class);
                }
            }
            else if (flag == 2) {
                SharedPrefrence.saveInt(context, "connected_driver_id", 0);
                SharedPrefrence.saveInt(context, "selected_wingman_id", 0);
                SharedPrefrence.save(context, "selected_wingman_lat", "0.0");
                SharedPrefrence.save(context, "selected_wingman_lng", "0.0");
                SharedPrefrence.saveInt(context, "connected_passenger_id", 0);
                SharedPrefrence.saveInt(context, "connected_passenger_booking_id", 0);
                SharedPrefrence.save(context, "connected_passenger_user_to_lat", "0.0");
                SharedPrefrence.save(context, "connected_passenger_user_to_lng", "0.0");
                SharedPrefrence.save(context, "connected_passenger_user_from_lat", "0.0");
                SharedPrefrence.save(context, "connected_passenger_user_from_lng", "0.0");
                SharedPrefrence.save(context, "WingmanAcceptedRequest", "");
                SharedPrefrence.save(context, "RideStartedWithPassenger", "");
                if (SharedPrefrence.getInt(context,"user_id") > 0) {
                    notification_intent = new Intent(context, MainHomeActivity.class);
                } else {
                    notification_intent = new Intent(context, LoginActivity.class);
                }
            }
            else {
                if (SharedPrefrence.getInt(context,"user_id") > 0) {
                    notification_intent = new Intent(context, DriverMapActivity.class);
                } else {
                    notification_intent = new Intent(context, LoginActivity.class);
                }
                SharedPrefrence.save(context, "WingmanAcceptedRequest", "Request Accepted By Wingman");
            }

            notification_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, requestID, notification_intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

                // Configure the notification channel.
                notificationChannel.setDescription("Channel description");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

            builder.setSmallIcon(R.drawable.logo_driver)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setTicker("Hearty365")
                    .setContentTitle("Ride Request")
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentText(data.get("message]"))
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE)
                    .setContentIntent(pendingIntent);

        }

        notificationManager.notify(notification_id,builder.build());
    }

    private void sendRegistrationToServer(final String token) {
        Log.e(TAG, "sendRegistrationToServer: " + token);
        if (SharedPrefrence.getInt(context,"user_id") > 0) {
//            Registering receiver using Local Broadcast---------
            myBroadcastReceiver=new MyBroadCastReceiver();
            IntentFilter filter1=new IntentFilter();
            filter1.addAction("com.ourride.android.action.broadcast");
            LocalBroadcastManager.getInstance(context).registerReceiver(myBroadcastReceiver,filter1);

//            sending broadcast to MyBroadCastReceiver

            Intent intent = new Intent("com.ourride.android.action.broadcast");
            intent.putExtra("token", token);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.e(TAG, "sendRegistrationToServer: " + token);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(myBroadcastReceiver);
    }
}
