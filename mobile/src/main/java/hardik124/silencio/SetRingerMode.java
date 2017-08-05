package hardik124.silencio;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SetRingerMode extends IntentService {

    public SetRingerMode(String name) {
        super(name);
    }

    public SetRingerMode() {
        super("Set Ringer Mode");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
            Bundle extra = intent.getExtras();
            if (extra == null)
                return;
            final int mode = extra.getInt("mode");
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT < 24 ||
                    (android.os.Build.VERSION.SDK_INT >= 24 && nm.isNotificationPolicyAccessGranted())) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setRingerMode(mode);
        }
    }
}
