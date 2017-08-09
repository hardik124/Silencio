package hardik124.silencio;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

public class StarterService extends Service implements    GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient mClient;
    GeoFencing mGeofencing;
    public StarterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

       connect();
       return null;
    }

    private void connect()
    {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);
        builder.addConnectionCallbacks(this);
        builder.addOnConnectionFailedListener(this);
        builder.addApi(LocationServices.API);
        builder.addApi(Places.GEO_DATA_API);
        builder.addApi(ActivityRecognition.API);
        builder.build();
        mGeofencing = new GeoFencing(this, mClient);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        boolean mIsEnabledDriving = getSharedPreferences("Driving", MODE_PRIVATE).getBoolean(getString(R.string.settings_enabled), false)
                ,            mIsEnabledGeofence = getSharedPreferences("Geofence", MODE_PRIVATE).getBoolean(getString(R.string.settings_enabled), false);;

        if((mClient.hasConnectedApi(LocationServices.API)||mClient.hasConnectedApi(Places.GEO_DATA_API))&&mIsEnabledGeofence)
            mGeofencing.registerAllGeofences();
        if(mClient.hasConnectedApi(ActivityRecognition.API)&&mIsEnabledDriving)
        {
            Intent intent = new Intent( this, ActivityService.class );
            PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mClient, 3000, pendingIntent );

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        connect();
    }
}
