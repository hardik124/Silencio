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

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            GoogleApiClient client = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(ActivityRecognition.API)
                    .build();

            GeoFencing  geoFencing = new GeoFencing(context,client);
            geoFencing.registerAllGeofences();

            boolean mIsEnabled = context.getSharedPreferences("Geofence",MODE_PRIVATE).getBoolean(context.getString(R.string.settings_enabled), false);
            if(mIsEnabled)
                geoFencing.registerAllGeofences();
            else
                geoFencing.unsRegisterAllGeofences();
        }
    }
}
