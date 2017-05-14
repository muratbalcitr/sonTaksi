package com.murat.murat.sontaxi.Haritaprocess;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.murat.murat.sontaxi.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class TaksiCagir extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<LocationSettingsResult> {

    GoogleMap mMap;
    ArrayList<LatLng> MarkerPoints;
    protected GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    TextView tvDistanceDuration;
    GPSTracker gpsTracker;
    public static final String ANONYMOUS = "anonymous";
    private String mUsername;
    MarkerOptions options;
    private ProgressBar mProgressBar;
    private DatabaseReference mMessagesDatabaseReference;
    private FirebaseDatabase mFirebaseDatabase;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth frAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    String stringLatitude, stringLongitude, country, city, postalCode, addressLine;
    CallbackManager fcallbackManager;
    public static final int RC_SIGN_IN = 1;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    // Executes in UI thread, after the parsing process
    AlertDialog.Builder kacpara;
    TextView tvSehirAdi;
    String genel = "3.1 tl";
    double kilometre = 2.10;
    String sehirAdi;
    String distance = "";
    String duration = "";
    Boolean isGPSEnabled = false;
    protected LocationManager locationManager;
    Button taksicagir;
    LatLng latLngCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taksi_cagir);
        gpsTracker = new GPSTracker(getApplicationContext());
        MarkerPoints = new ArrayList<>();
        taksicagir = (Button) findViewById(R.id.btnTaxiCagir);
        taksicagir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TaksiCagir.this, Taksi_yerleri.class);
                startActivity(i);
            }
        });
        tvDistanceDuration = (TextView) findViewById(R.id.tvMesafe);
        tvSehirAdi = (TextView) findViewById(R.id.tvSehirAdi);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
         loginprocess();


    }

    private void loginprocess() {
        mUsername = ANONYMOUS;
        frAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //user sign-in
                    onSignedInitialize(user.getDisplayName());
                } else {
                    //user sign-out
                    onSignedOutCleanUp();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)//şifrenin telefona kaydedilmesini sağlar
                                    .setProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());

        frAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Toast.makeText(getApplicationContext(), "başarılı", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Signed is succesfuly", Toast.LENGTH_SHORT).show();
                    // handleFacebookAccessToken(AccessToken.getCurrentAccessToken());
                    finish();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Signed is canceled", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        // All required changes were successfully made
                        Toast.makeText(getApplicationContext(), "Location enabled by user!", Toast.LENGTH_LONG).show();
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(getApplicationContext(), "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show();
                        break;
                    }
                    default: {
                        Toast.makeText(this, "afghhh", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                break;
        }
    }

    private void onSignedInitialize(String username) {
        mUsername = username;
    }

    private void onSignedOutCleanUp() {
        mUsername = ANONYMOUS;
        detachDatabaseReadListener();

    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            buildGoogleApiClient();
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setMaxZoomPreference(14);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                return true;
            }
        });
        // Setting onclick event listener for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                // Already two locations
                if (MarkerPoints.size() > 1) {
                    MarkerPoints.clear();
                    mMap.clear();
                }

                // Adding new item to the ArrayList
                MarkerPoints.add(point);
                MarkerPoints.add(latLngCurrentLocation);
                // MarkerPoints.add(latLngCurrentLocation);
                // Creating MarkerOptions
                options = new MarkerOptions();

                // Setting the position of the marker
                options.position(point);

                /**
                 * For the start location, the color of marker is GREEN and
                 * for the end location, the color of marker is RED.
                 */
                if (MarkerPoints.size() == 1) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if (MarkerPoints.size() == 2) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }


                // Add new marker to the Google Map Android API V2
                mMap.addMarker(options);

                // Checks, whether start and end locations are captured
                if (MarkerPoints.size() >= 2) {
                    LatLng origin = MarkerPoints.get(0);
                    LatLng dest = MarkerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getUrl(origin, dest);
                    Log.d("onMapClick", url.toString());
                    FetchUrl FetchUrl = new FetchUrl();

                    // Start downloading json data from Google Directions API
                    FetchUrl.execute(url);
                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                }

            }
        });

    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerPoints = new MarkerOptions();
        /*   */
            if (result.size() < 1) {
                Toast.makeText(getBaseContext(), "Puansız", Toast.LENGTH_SHORT).show();
                return;
            }
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        kacpara = new AlertDialog.Builder(TaksiCagir.this);
                        double para = Double.parseDouble(distance.toString().split(" ")[0]);
                        sehirAdi = gpsTracker.getCityName(latLngCurrentLocation, getApplicationContext());
                        tvSehirAdi.setText(sehirAdi);
                        if (para < 1) {
                            double para2 = para * 10;
                            double para1 = (para2 * 2.10 + 3.45) / 10;
                            kacpara.create();
                            kacpara.setTitle("Toplam yol ücreti");
                            kacpara.setIcon(R.drawable.ic_monetization_on);
                            kacpara.setMessage("Mesafe:" + distance + ", Sure:" + duration + "\n" + " Tahmini ödeyeceğiniz miktar = " + para1 + " tl'dir");
                            kacpara.setPositiveButton("taksi Çağır", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(TaksiCagir.this, Taksi_yerleri.class);
                                    i.putExtra("sehiradi", sehirAdi);
                                    startActivity(i);
                                }
                            });
                            kacpara.show();

                        } else {
                            double para1 = para * 2.10 + 3.45;
                            kacpara.create();
                            kacpara.setTitle("Toplam yol ücreti");
                            kacpara.setIcon(R.drawable.ic_monetization_on);
                            kacpara.setMessage("Mesafe:" + distance + ", Sure:" + duration + "\n" + " Tahmini ödeyeceğiniz miktar = " + para1 + " tl'dir");
                            kacpara.setPositiveButton("taksi Çağır", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(TaksiCagir.this, Taksi_yerleri.class);
                                    i.putExtra("sehiradi", sehirAdi);
                                    startActivity(i);
                                }
                            });
                            kacpara.show();
                        }
                    }
                }, 1500);


                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    if (j == 0) {
                        distance = (String) point.get("mesafe");
                        continue;
                    } else if (j == 1) {
                        duration = (String) point.get("sure");
                        continue;
                    }
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);

                    tvDistanceDuration.setText("Mesafe:" + distance + ", Sure:" + duration);

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(14);//çizilecek yolun kalınlığını belirlemektedir.
                    lineOptions.color(Color.BLUE);

                    Log.d("onPostExecute", "onPostExecute lineoptions decoded");

                }
            }
            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

    protected LocationSettingsRequest mLocationSettingsRequest;
    protected LocationRequest getmLocationRequest;


    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    status.startResolutionForResult(TaksiCagir.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {

                    break;
                }
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are inadequate, and cannot be fixed here. Dialog not created
                break;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    PendingResult<LocationSettingsResult> result;
    final static int REQUEST_LOCATION = 199;
    Context context;

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        //...
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    TaksiCagir.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Toast.makeText(TaksiCagir.this, "asdasdasd", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        //...
                        break;
                }
            }
        });
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
       /* if (!gpsTracker.isGPSEnabled) {
            gpsTracker.showSettingsAlert();
            checkLocationSettings();
        } else {
            sehirBilgileri();
        }*/
        //Place current location marker
        latLngCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLngCurrentLocation);
        markerOptions.title("mevcut konum");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngCurrentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        /* zoom levels
        1: World
        5: Landmass/continent
        10: City
        15: Streets
        20: Buildings
         */

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.


                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);


            }
            return;
        }

        // other 'case' lines to check for other permissions this app might request.
        // You can add here other case statements according to your requirement.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            frAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        frAuth.addAuthStateListener(mAuthStateListener);

    }
}