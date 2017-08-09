package hardik124.silencio;

import android.Manifest;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;

import hardik124.silencio.database.DB_Contract;

public class Home extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Home Activity";
    private final int PLACE_PICKER_INTENT = 7;
    private final int LOCATION_PERMISSION = 8,REQUEST_CHECK_SETTINGS=9;


    private GoogleApiClient mClient;
    private ArrayList<places_model> placeList;
    private RecyclerView placesRV;
    private PlacesRV adapter;
    private boolean mIsEnabled;
    private GeoFencing mGeofencing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button addPlace = (Button) findViewById(R.id.addPlace);
        placesRV = (RecyclerView) findViewById(R.id.places_rv);
        placeList = new ArrayList<>();

        setPermission();
        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();
//
//        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setMessage("Your GPS seems to be disabled, please enable it?")
//                        .setCancelable(false)
//                        .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
//                            public void onClick(final DialogInterface dialog, final int id) {
//                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                            }
//                        })
//                        .setNegativeButton("No", null);
//                final AlertDialog alert = builder.create();
//                alert.show();
//            }
//        }

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
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(Home.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                }
            }
        });
        mGeofencing = new GeoFencing(this, mClient);

        adapter = new PlacesRV(placeList,this);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        placesRV.setLayoutManager(linearLayoutManager);
        placesRV.setAdapter(adapter);


        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(view, "Please grant location permission first.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    try {
                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        Intent i = builder.build(Home.this);
                        startActivityForResult(i, PLACE_PICKER_INTENT);
                    } catch (GooglePlayServicesRepairableException e) {
                        Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
                    } catch (Exception e) {
                        Log.e(TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
                    }
                }
            }
        });
        mIsEnabled = getSharedPreferences("Geofence",MODE_PRIVATE).getBoolean(getString(R.string.settings_enabled), false);
        refreshPlacesData();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPermission();
    }

    private void setPermission() {
        CheckBox locationPermission = (CheckBox) findViewById(R.id.location_permissionCB);
        CheckBox ringerPermission = (CheckBox) findViewById(R.id.ringer_permissionCB);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermission.setChecked(true);
            locationPermission.setEnabled(false);
        } else {
            locationPermission.setChecked(false);
            locationPermission.setEnabled(true);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 24 && !notificationManager.isNotificationPolicyAccessGranted()) {
            ringerPermission.setChecked(false);
            ringerPermission.setEnabled(true);
        }
        else {
            ringerPermission.setChecked(true);
            ringerPermission.setEnabled(false);
        }

        ringerPermission.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
            }
        });

        locationPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(Home.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            setPermission();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_INTENT && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            if (place == null) {
                return;
            }

            // Extract the place information from the API
            String placeName = place.getName().toString();
            String placeAddress = place.getAddress().toString();
            String placeID = place.getId();

            // Insert a new place into DB
            ContentValues contentValues = new ContentValues();
            contentValues.put(DB_Contract.PlacesTable.COLOUMN_PLACE_ID, placeID);
            contentValues.put(DB_Contract.PlacesTable.COLOUMN_PLACE_Address,placeAddress);
            contentValues.put(DB_Contract.PlacesTable.COLOUMN_PLACE_Name,placeName);
            getContentResolver().insert(DB_Contract.PlacesTable.CONTENT_URI, contentValues);
//
//            // Get live data information
            mGeofencing.registerAllGeofences();
            refreshPlacesData();

        }
    }


    public void refreshPlacesData()
    {
        final View mProg = findViewById(R.id.progBar);

        final Uri tableUri = DB_Contract.PlacesTable.CONTENT_URI;
        Cursor data = getContentResolver().query(tableUri,null,null,null,null);

        if(data == null||data.getCount()==0)
            return;
        ArrayList<String> ids = new ArrayList<String>();

        while (data.moveToNext()) {
            ids.add(data.getString(data.getColumnIndex(DB_Contract.PlacesTable.COLOUMN_PLACE_ID)));
        }
        if(ids.isEmpty())
        {
            mProg.setVisibility(View.INVISIBLE);
            placesRV.setVisibility(View.VISIBLE);
            data.close();
            return;
        }
        mProg.setVisibility(View.VISIBLE);
        placesRV.setVisibility(View.INVISIBLE);
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient,ids.toArray(new String[ids.size()]));
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                placeList.clear();
                for(int i=0;i<places.getCount();i++)
                {
                    Place currentPlace = places.get(i);
                    places_model item = new places_model();
                    item.setAddress(currentPlace.getAddress().toString());
                    item.setName(currentPlace.getName().toString());
                    item.setKey(currentPlace.getId());

                    placeList.add(item);
                }

                mGeofencing.updateGeofencesList(places);
                if(mIsEnabled) mGeofencing.registerAllGeofences();
                adapter.notifyDataSetChanged();
                mProg.setVisibility(View.INVISIBLE);
                placesRV.setVisibility(View.VISIBLE);
            }
        });
        data.close();


    }
    //Google API methods........................

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        refreshPlacesData();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}
