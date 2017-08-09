package hardik124.silencio;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;

import java.util.List;

public class SettingsActivity extends AppCompatActivity implements    GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {
    private boolean mIsEnabledGeofence,mIsEnabledGeofenceVibrate ,mIsEnabledDriving,mIsEnabledDrivingText;
    private EditText rejectText;
    private GoogleApiClient mClient;
    private GeoFencing mGeofencing;
    private final int LOCATION_PERMISSION =1,TEXT_PERMISSION=2;

    @Override
    public void onBackPressed() {
        getSharedPreferences("Driving", MODE_PRIVATE).edit().putString("message", rejectText.getText().toString()).apply();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

//        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setMessage("Your GPS seems to be disabled, please enable it?")
//                    .setCancelable(false)
//                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
//                        public void onClick(final DialogInterface dialog, final int id) {
//                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                        }
//                    })
//                    .setNegativeButton("No", null);
//            final AlertDialog alert = builder.create();
//            alert.show();
//        }


        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(ActivityRecognition.API)
                .enableAutoManage(this, this)
                .build();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(SettingsActivity.this, 1);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                }
            }
        });
        mGeofencing = new GeoFencing(this, mClient);

        {
            SwitchCompat onOffSwitch = (SwitchCompat) findViewById(R.id.driving_switch);
            mIsEnabledDriving = getSharedPreferences("Driving", MODE_PRIVATE).getBoolean(getString(R.string.settings_enabled), false);
            onOffSwitch.setChecked(mIsEnabledDriving);
            onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences.Editor editor = getSharedPreferences("Driving", MODE_PRIVATE).edit();
                    editor.putBoolean(getString(R.string.settings_enabled), isChecked);
                    mIsEnabledDriving = isChecked;
                    SwitchCompat onOffSwitch = (SwitchCompat) findViewById(R.id.reject_text_switch);
                    onOffSwitch.setEnabled(mIsEnabledDriving);
                    if(!mIsEnabledDriving)
                        onOffSwitch.setChecked(mIsEnabledDriving);
                    editor.apply();

                }

            });
            getSharedPreferences("Driving", MODE_PRIVATE).edit()
                    .putBoolean("text", false).apply();


        }
        {
            final SwitchCompat onOffSwitch = (SwitchCompat) findViewById(R.id.vibrate);

            mIsEnabledGeofenceVibrate = getSharedPreferences("Geofences", MODE_PRIVATE).getBoolean("Vibrate", false);
            onOffSwitch.setChecked(mIsEnabledGeofenceVibrate);
            onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences.Editor editor = getSharedPreferences("Geofences", MODE_PRIVATE).edit();
                    editor.putBoolean("Vibrate", isChecked);
                    mIsEnabledGeofenceVibrate = isChecked;
                    editor.apply();

                }

            });
        }
        rejectText = (EditText) findViewById(R.id.rejectText);
        {
            final SwitchCompat onOffSwitch = (SwitchCompat) findViewById(R.id.geofences_switch);
            mIsEnabledGeofence = getSharedPreferences("Geofence", MODE_PRIVATE).getBoolean(getString(R.string.settings_enabled), false);
            onOffSwitch.setChecked(mIsEnabledGeofence);
            onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(SettingsActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_PERMISSION);
                            onOffSwitch.setChecked(false);
                            return;
                        }
                    }

                    SharedPreferences.Editor editor = getSharedPreferences("Geofence", MODE_PRIVATE).edit();
                    editor.putBoolean(getString(R.string.settings_enabled), isChecked);
                    mIsEnabledGeofence = isChecked;
                    editor.apply();
                    View v = findViewById(R.id.vibrate);
                    v.setEnabled(isChecked);
                    if (isChecked) mGeofencing.registerAllGeofences();

                    else {
                        mGeofencing.unsRegisterAllGeofences();
                        ((SwitchCompat)v).setChecked(false);
                    }
                    getSharedPreferences("Geofences", MODE_PRIVATE).edit()
                            .putBoolean("Vibrate", false)
                            .apply();
                }
            });

        }

        {
            final SwitchCompat onOffSwitch = (SwitchCompat) findViewById(R.id.reject_text_switch);

            mIsEnabledDrivingText = getSharedPreferences("Driving", MODE_PRIVATE).getBoolean("text", false);
            onOffSwitch.setChecked(mIsEnabledDrivingText);
            onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(SettingsActivity.this,
                                    new String[]{Manifest.permission.SEND_SMS},
                                    TEXT_PERMISSION);
                            onOffSwitch.setChecked(false);
                            return;
                        }


                    }
                    SharedPreferences.Editor editor = getSharedPreferences("Driving", MODE_PRIVATE).edit();
                    editor.putBoolean("text", isChecked);
                    mIsEnabledDrivingText = isChecked;
                    rejectText.setEnabled(mIsEnabledDrivingText);
                    editor.apply();

                }

            });
        }

        {
            rejectText.setText(getSharedPreferences("Driving", MODE_PRIVATE).getString("message", null));
            rejectText.setEnabled(mIsEnabledDrivingText);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Toast.makeText(this,"Permission Granted",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
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

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"Error in connection",Toast.LENGTH_LONG).show();
    }
}
