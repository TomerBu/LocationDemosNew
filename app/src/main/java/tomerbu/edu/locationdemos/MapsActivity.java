package tomerbu.edu.locationdemos;

import android.Manifest;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_LOCATION = 10;
    private static final int RC_WRITE_STORAGE = 9;
    private static final int REQUEST_CODE_GEOFENCE = 11;
    private ProgressDialog dialog;
    private GoogleMap map;
    private GoogleApiClient mApiClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = new SupportMapFragment();
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.mapContainer, mapFragment).commit();

        mapFragment.getMapAsync(this);

        showDialog();

        initRadioGroup();

        initApiClient();

        // try {
        // download();
        requestLocationPermission();
        // } //catch (IOException e) {
        //  e.printStackTrace();
        //  }
    }

    private void download() throws IOException {
/*        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_WRITE_STORAGE);
            return;
        }*/

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        String ref = "http://www.e-reading.club/bookreader.php/142063/Android_-_a_programmers_guide.pdf";
        Uri Download_Uri = Uri.parse(ref);
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

        //Restrict the types of networks over which this download may proceed.
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        //Set whether this download may proceed over a roaming connection.
        request.setAllowedOverRoaming(false);
        //Set the title of this download, to be displayed in notifications.
        request.setTitle("Demo Book");
        //Set the local destination for the downloaded file to a path within the application's external files directory
        //  request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS,"Android_-_a_programmers_guide.pdf");


        File storage = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File f = File.createTempFile("DemoBook", ".pdf", storage);
        final Uri dlUri = FileProvider.getUriForFile(this,
                "tomerbu.edu.locationdemos.fileprovider",
                f);

        request.setDestinationUri(Uri.fromFile(f));
        //ParcelFileDescriptor descriptor = getContentResolver().openFileDescriptor(dlUri, "rw");

        //Enqueue a new download and same the referenceId
        Long downloadReference = downloadManager.enqueue(request);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(MapsActivity.this, "df", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(Intent.ACTION_VIEW, dlUri);
                ComponentName componentName;
                if ((componentName = intent.resolveActivity(getPackageManager())) != null) {
                    startActivity(intent1);
                    String className = componentName.getClassName();
                    Log.d(Constants.TAG, className);
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void initApiClient() {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);

        builder.addApi(LocationServices.API).
                enableAutoManage(this, this).
                addConnectionCallbacks(this);

        mApiClient = builder.build();
        // mApiClient.connect();
    }

    private void initRadioGroup() {
        RadioGroup rgMapType = (RadioGroup) findViewById(R.id.rgMapType);
        rgMapType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (id) {
                    case R.id.radioHybrid:
                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case R.id.radioNormal:
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case R.id.radioSatellite:
                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                }
            }
        });
    }

    private void showDialog() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading your map");
        dialog.setMessage("Please wait");
        dialog.show();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        dialog.dismiss();
        this.map = googleMap;


        //College position / Coordinates
        LatLng nessCollege = new LatLng(31.2634545, 34.8094539);

        //Add a marker
        googleMap.addMarker(new MarkerOptions().position(nessCollege));

        //Move the camera (With Animation) To college coordinates, Zoom 17
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nessCollege, 17));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("Ness", "Connected");
        requestLocation();
    }

    private void requestLocation() {
        requestLocationPermission();
        //If we don't have permission, we don't get here
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //Get the last known location. May be null.!!!
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000 * 10).
                setFastestInterval(1000).
                setMaxWaitTime(1000).
                setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);/*GPS*/


        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest).build();
        PendingResult<LocationSettingsResult> locationSettingsResultPendingResult = LocationServices.SettingsApi.checkLocationSettings(mApiClient, settingsRequest);

        locationSettingsResultPendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                if (locationSettingsResult.getStatus().getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        locationSettingsResult.getStatus().startResolutionForResult(MapsActivity.this, REQUEST_CODE_LOCATION);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(Constants.TAG, e.getLocalizedMessage());
                    }
                } else {
                    requestLocationUpdates();
                }
            }
        });

/*        ResultReceiver resultReceiver = new ResultReceiver(null){
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
            }
        };

        resultReceiver.send(1, new Bundle());

        Intent intent = new Intent();
        intent.putExtra("talkToMe", resultReceiver);*/

        Geofence geofence = new Geofence.Builder().setCircularRegion(31.3690897, 34.8044, 100).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER|Geofence.GEOFENCE_TRANSITION_EXIT).build();
     /*   List<Geofence> geofences = new ArrayList<>();
        geofences.add(geofence);*/
        GeofencingRequest geofencingRequest =  new GeofencingRequest.Builder().addGeofence(geofence).setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).build();
        Intent intent = new Intent(this, MyGeoFenceService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, REQUEST_CODE_GEOFENCE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        LocationServices.GeofencingApi.addGeofences(mApiClient, geofencingRequest, pendingIntent);


    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mApiClient, mLocationRequest, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                        map.addMarker(new MarkerOptions().position(loc));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 19));
                    }
                }
        );
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Request the Location Permission
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_CODE_LOCATION);
            //Get out of this method, Since we don't have the permission yet.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //If we got the permission:
        if (requestCode == REQUEST_CODE_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
            requestLocation();
        }
        //If we got the permission:
        if (requestCode == RC_WRITE_STORAGE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
            try {
                download();
            } catch (IOException e) {
                Log.e(Constants.TAG, e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(Constants.TAG, connectionResult.getErrorMessage());
    }

    public void download(View view) {
        try {
            download();
        } catch (IOException e) {
            Log.e(Constants.TAG, e.getLocalizedMessage());
        }
    }
}
