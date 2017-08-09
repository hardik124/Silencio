package hardik124.silencio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    /***
     * Handles the Broadcast message sent when the Geofence Transition is triggered
     * Careful here though, this is running on the main thread so make sure you start an AsyncTask for
     * anything that takes longer than say 10 second to run
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the Geofence Event from the Intent sent through
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, String.format("Error code : %d", geofencingEvent.getErrorCode()));
            return;
        }

        // Get the transition type.
        boolean vibrate = context.getSharedPreferences("Geofence",context.MODE_PRIVATE).getBoolean("Vibrate", false);
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        Intent setMode = new Intent(context,SetRingerMode.class);
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER||geofenceTransition==Geofence.GEOFENCE_TRANSITION_DWELL) {
            if(vibrate)
                setMode.putExtra("mode",AudioManager.RINGER_MODE_VIBRATE);
            else
                setMode.putExtra("mode",AudioManager.RINGER_MODE_SILENT);

            SharedPreferences.Editor editor = context.getSharedPreferences("Geofence", context.MODE_PRIVATE).edit();
            editor.putBoolean("isEnabledNow", true);
            editor.apply();
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            setMode.putExtra("mode",AudioManager.RINGER_MODE_NORMAL);
            SharedPreferences.Editor editor = context.getSharedPreferences("Geofence", context.MODE_PRIVATE).edit();
            editor.putBoolean("isEnabledNow", false);
            editor.apply();
        }
        context.startService(setMode);
        // Send the notification
        sendNotification(context, geofenceTransition);
    }


    /**
     * Posts a notification in the notification bar when a transition is detected
     * Uses different icon drawables for different transition types
     * If the user clicks the notification, control goes to the MainActivity
     *
     * @param context        The calling context for building a task stack
     * @param transitionType The geofence transition type, can be Geofence.GEOFENCE_TRANSITION_ENTER
     *                       or Geofence.GEOFENCE_TRANSITION_EXIT
     */
    private void sendNotification(Context context, int transitionType) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, Home.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(Home.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Check the transition type to display the relevant icon image


        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER||transitionType ==Geofence.GEOFENCE_TRANSITION_DWELL) {
            boolean vibrate = context.getSharedPreferences("GeofenceVibrate",context.MODE_PRIVATE).getBoolean(context.getString(R.string.settings_enabled), false);
            if(!vibrate)
                    builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.ic_volume_off_white_24dp))
                            .setSmallIcon(R.drawable.ic_volume_off_white_24dp);

            else
                builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_vibrate))
                        .setSmallIcon(R.drawable.ic_vibrate);

            builder.setAutoCancel(false)
                    .setContentTitle(context.getString(R.string.silent_mode_activated));

        } else// if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)
             {
            builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.ic_volume_up_white_24dp))
                    .setContentTitle(context.getString(R.string.back_to_normal));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_MAX);
        }


        // Continue building the notification
        builder.setContentText(context.getString(R.string.touch_to_relaunch));
        builder.setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        Notification notification = builder.build();
        notification.flags |=Notification.FLAG_NO_CLEAR;
        mNotificationManager.notify(0, notification);
    }
}