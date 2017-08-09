package hardik124.silencio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import static android.content.Context.MODE_PRIVATE;

public class onBootReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        context.startActivity(new Intent(context,StarterService.class));

    }
}
