package hardik124.silencio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Date;

public class CallReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context.getSharedPreferences("Activity", context.MODE_PRIVATE).getBoolean("ActivityEnabled", false)) {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            if (stateStr == null)
                return;
            if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                SharedPreferences preferences = context.getSharedPreferences("Driving", context.MODE_PRIVATE);
                String number = preferences.getString("number", null);

                preferences.edit().remove("number").apply();

                if (number == null)
                    return;

                Vibrator vibrator;
                vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = {0, 100, 1000, 300};
                Log.d("driving", "offhook");

                vibrator.vibrate(pattern, -1);

                if (context.getSharedPreferences("Driving", context.MODE_PRIVATE).getBoolean("text", false)) {
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(number, null, context.getSharedPreferences("Driving", context.MODE_PRIVATE).getString("message", null), null, null);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                SharedPreferences.Editor editor = context.getSharedPreferences("Driving", context.MODE_PRIVATE).edit();
                editor.putString("number", number);
                Log.d("driving", number);
                editor.apply();
            }
        }
    }
}
