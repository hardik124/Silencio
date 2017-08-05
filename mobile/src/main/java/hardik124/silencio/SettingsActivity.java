package hardik124.silencio;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import java.util.List;

public class SettingsActivity extends AppCompatActivity implements    GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {
    private boolean mIsEnabled;
    private GoogleApiClient mClient;
    private GeoFencing mGeofencing;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SwitchCompat onOffSwitch = (SwitchCompat) findViewById(R.id.geofences_switch);


        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(ActivityRecognition.API)
                .enableAutoManage(this, this)
                .build();
        mGeofencing = new GeoFencing(this, mClient);

        mIsEnabled = getSharedPreferences("Geofence",MODE_PRIVATE).getBoolean(getString(R.string.settings_enabled), false);
        onOffSwitch.setChecked(mIsEnabled);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getSharedPreferences("Geofence",MODE_PRIVATE).edit();
                editor.putBoolean(getString(R.string.settings_enabled), isChecked);
                mIsEnabled = isChecked;
                editor.apply();
                if (isChecked) mGeofencing.registerAllGeofences();
                else mGeofencing.unsRegisterAllGeofences();
            }

        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(mClient.hasConnectedApi(LocationServices.API)&&mClient.hasConnectedApi(Places.GEO_DATA_API))
            mGeofencing.registerAllGeofences();
        if(mClient.hasConnectedApi(ActivityRecognition.API))
        {}
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"Error in connection",Toast.LENGTH_LONG).show();
    }
}
