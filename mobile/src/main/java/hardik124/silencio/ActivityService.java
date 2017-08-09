package hardik124.silencio;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;

import java.util.List;

public class ActivityService extends IntentService {


    public ActivityService(String name) {
        super(name);
    }
    public ActivityService() {
        super("Activity recognizer service");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if(!getSharedPreferences("Driving", MODE_PRIVATE).getBoolean(getString(R.string.settings_enabled), false))
            return;
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleActivities(result.getMostProbableActivity());
            Log.d("act",result.getProbableActivities().toString());
        }

    }

    private void handleActivities(DetectedActivity activity) {
        if(activity.getConfidence()<90)
            return;
        int type = activity.getType();
        if (type == DetectedActivity.IN_VEHICLE) {
            Intent setMode = new Intent(this, SetRingerMode.class);
            setMode.putExtra("mode", AudioManager.RINGER_MODE_SILENT);
            notificationCreator(AudioManager.RINGER_MODE_SILENT);
            Log.d("act","Driv");
            startService(setMode);

        }

        if (type == DetectedActivity.ON_BICYCLE || type == DetectedActivity.RUNNING) {
            SharedPreferences pref = getSharedPreferences("Geofence", MODE_PRIVATE);
            boolean geofence = pref.getBoolean("isEnabledNow", false);
            if (!geofence) {
                Intent setMode = new Intent(this, SetRingerMode.class);
                setMode.putExtra("mode", AudioManager.RINGER_MODE_VIBRATE);
                notificationCreator(AudioManager.RINGER_MODE_VIBRATE);
                Log.d("act","bic/rn");
                startService(setMode);
            }
        }

        if (type == DetectedActivity.STILL) {
            SharedPreferences pref = getSharedPreferences("Geofence", MODE_PRIVATE);
            boolean geofence = pref.getBoolean("isEnabledNow", false);
            if (!geofence) {
                Intent setMode = new Intent(this, SetRingerMode.class);
                setMode.putExtra("mode", AudioManager.RINGER_MODE_NORMAL);
                startService(setMode);
            }
        }
    }
    private void notificationCreator(int mode) {

        Intent notificationIntent = new Intent(this, Home.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(Home.class);
        stackBuilder.addNextIntent(notificationIntent);
         PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        if(mode == AudioManager.RINGER_MODE_NORMAL){
            builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_volume_up_white_24dp))
                    .setContentTitle(getString(R.string.normal_mode));
        }
        else if(mode == AudioManager.RINGER_MODE_SILENT){
            builder.setSmallIcon(R.drawable.ic_volume_off_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_volume_off_white_24dp))
                    .setContentTitle(getString(R.string.driving_mode));
        }
        else if(mode == AudioManager.RINGER_MODE_VIBRATE){
            builder.setSmallIcon(R.drawable.ic_vibrate)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_vibrate))
                    .setContentTitle(getString(R.string.running_mode));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_MAX);
        }


        // Continue building the notification
        builder.setContentText(getString(R.string.touch_to_relaunch));
        builder.setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Issue the notification
        Notification notification = builder.build();
        notification.flags |=Notification.FLAG_NO_CLEAR;
        mNotificationManager.notify(0, notification);
    }
}
