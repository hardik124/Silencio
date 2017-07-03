package hardik124.silencio;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

public class Home extends AppCompatActivity implements
                GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener {

    private final int PLACE_PICKER_INTENT = 7;
    private final int LOCATION_PERMISSION = 8;
    private static final String TAG = "Home Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button addPlace = (Button) findViewById(R.id.addPlace);
        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(view, "Please grant location permission first.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {
                    try {
                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        Intent i = builder.build(Home.this);
                        startActivityForResult(i, PLACE_PICKER_INTENT);
                    } catch (GooglePlayServicesRepairableException e) {
                        Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
                    } catch (GooglePlayServicesNotAvailableException e) {
                        Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
                    } catch (Exception e) {
                        Log.e(TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
                    }
                }
            }
        });

        setPermission();
        GoogleApiClient client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this,this)
                .build();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPermission();
    }

    private void setPermission ()
    {
        CheckBox locationPermission = (CheckBox) findViewById(R.id.location_permissionCB);
        CheckBox ringerPermission = (CheckBox) findViewById(R.id.ringer_permissionCB);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            locationPermission.setChecked(true);
            locationPermission.setEnabled(false);
        }
        else
            locationPermission.setChecked(false);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=24&& !notificationManager.isNotificationPolicyAccessGranted())
            ringerPermission.setChecked(false);
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
        if(requestCode == LOCATION_PERMISSION&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
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

            Toast.makeText(this,placeAddress,Toast.LENGTH_LONG).show();
//
//            // Insert a new place into DB
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, placeID);
//            getContentResolver().insert(PlaceContract.PlaceEntry.CONTENT_URI, contentValues);
//
//            // Get live data information
//            refreshPlacesData();
        }
    }

    //Google API methods........................

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this,"Connected",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"Suspended",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"failed",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
