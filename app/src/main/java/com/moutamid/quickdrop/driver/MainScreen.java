package com.moutamid.quickdrop.driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moutamid.quickdrop.Model.Trip;
import com.moutamid.quickdrop.Model.User;
import com.moutamid.quickdrop.R;
import com.moutamid.quickdrop.databinding.ActivityMainScreensBinding;
import com.moutamid.quickdrop.directionhelpers.FetchURL;
import com.moutamid.quickdrop.directionhelpers.TaskLoadedCallback;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainScreen extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, TaskLoadedCallback {

    private GoogleMap mMap;
    private ActivityMainScreensBinding binding;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private DatabaseReference mRequestTrip, driversOnlineDB;
    private FirebaseAuth mAuth;
    private String uId;
    private ImageView menuImg;
    private RelativeLayout info_card;
    private TextView pickupTxt;
    double currentLat, currentLng = 0;
    private static final int REQUEST_LOCATION = 1;
    private double desLat, desLng = 0;
    private double custLat, custLng = 0;
    private String destination = "";
    private String pickup = "";
    private Polyline currentPolyline;
    private ImageView callImg,whatsappImg,googleMapImg;
    private AppCompatButton endBtn;
    private String key = "";
    private String phone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainScreensBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        menuImg = (ImageView) findViewById(R.id.menu);
        info_card = (RelativeLayout) findViewById(R.id.pickup_layout);
        pickupTxt = (TextView) findViewById(R.id.pickup);
        endBtn = (AppCompatButton) findViewById(R.id.end);
        googleMapImg = findViewById(R.id.google_map);
        whatsappImg = findViewById(R.id.chat);
        callImg = findViewById(R.id.call);
        mAuth = FirebaseAuth.getInstance();
        uId = mAuth.getCurrentUser().getUid();
        driversOnlineDB = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        mRequestTrip = FirebaseDatabase.getInstance().getReference().child("Requests");
        checkInternetAndGPSConnection();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            bulidGoogleApiClient();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CALL_PHONE}, REQUEST_LOCATION);
            showGPSDialogBox();
        }

        if (getIntent() != null) {
            key = getIntent().getStringExtra("key");
            pickup = getIntent().getStringExtra("pickup");
            destination = getIntent().getStringExtra("destination");
            desLat = getIntent().getDoubleExtra("lat", 0);
            desLng = getIntent().getDoubleExtra("lng", 0);
            custLat = getIntent().getDoubleExtra("cust_lat", 0);
            custLng = getIntent().getDoubleExtra("cust_lng", 0);
        }

        googleMapImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=loc:%f,%f", custLat,custLng);
                String uri = "http://maps.google.com/maps?f=d&hl=en&saddr="+currentLat+","+currentLng+"&daddr="+custLat+","+custLng;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                Intent chooser = Intent.createChooser(intent,"Launch Map");
                startActivity(chooser);
            }
        });

        callImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL);

                intent.setData(Uri.parse("tel:" + phone));
                startActivity(intent);
            }
        });

        whatsappImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://api.whatsapp.com/send?phone="+phone;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        menuImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainScreen.this, RiderMenuItemActivity.class));
            }
        });

        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("status","completed");
                mRequestTrip.child(key).updateChildren(hashMap);
                desLng = 0;
                desLat = 0;
                custLat = 0;
                custLng = 0;
                mMap.clear();
                endBtn.setVisibility(View.GONE);
                info_card.setVisibility(View.GONE);
            }
        });

    }

    private void showGPSDialogBox() {
        LocationManager enable_gps = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!enable_gps.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder gps = new AlertDialog.Builder(this);
            gps.setMessage("Turn on GPS to find Location").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainScreen.this, "No location updation without GPS", Toast.LENGTH_SHORT).show();
                }
            }).show();
        }
    }


    private void checkInternetAndGPSConnection() {
        ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!connect.isActiveNetworkMetered() && !info.isConnected()) {
            AlertDialog.Builder internet = new AlertDialog.Builder(MainScreen.this);
            internet.setMessage("Turn on Internet to see rider location")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(intent);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainScreen.this, "No route description without Internet", Toast.LENGTH_SHORT).show();
                        }
                    }).show();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bulidGoogleApiClient();
                // Toast.makeText(MainScreen.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(MainScreen.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
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
        mMap = googleMap;
      //  bulidGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        if (currentLat !=0 && currentLng != 0){
            LatLng latLng = new LatLng(currentLat, currentLng);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Location");
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map));
            mMap.addMarker(markerOptions);

            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }
        if (desLat != 0 && desLng != 0) {
            LatLng latLng = new LatLng(desLat, desLng);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(destination);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
            mMap.addMarker(markerOptions);

            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        }

        if (custLat != 0 && custLng != 0) {
            LatLng latLng = new LatLng(custLat, custLng);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(pickup);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map));
            mMap.addMarker(markerOptions);

            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            info_card.setVisibility(View.VISIBLE);
            pickupTxt.setText(pickup);

            getCustomerInfo();
        }
    }

    private void getCustomerInfo() {
        mRequestTrip.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Trip model = snapshot.getValue(Trip.class);
                    DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users")
                            .child(model.getUserId());
                    db.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                User users = snapshot.getValue(User.class);
                                if (users.getPhone().equals("")){
                                    Toast.makeText(MainScreen.this, "Phone Number not exists", Toast.LENGTH_SHORT).show();
                                }else {
                                    phone = users.getPhone();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        client.connect();

    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLat = location.getLatitude();
        currentLng = location.getLongitude();


        LatLng latLng = new LatLng(currentLat, currentLng);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        if (custLat != 0 && custLng != 0){
            new FetchURL(MainScreen.this).execute(getUrl(currentLat,currentLng,custLat,custLng, "driving"), "driving");
        }

        GeoFire geoFire =  new GeoFire(driversOnlineDB);
        geoFire.setLocation(uId,new GeoLocation(location.getLatitude(),location.getLongitude()));

        if (Math.round(currentLat) == Math.round(custLat) && Math.round(currentLng) == Math.round(custLng)){
           // endBtn.setVisibility(View.VISIBLE);
            custLat = 0.0;
            custLng = 0.0;
            new FetchURL(MainScreen.this).execute(getUrl(currentLat,currentLng,desLat,desLng, "driving"), "driving");
        }
        if (Math.round(currentLat) == Math.round(desLat) && Math.round(currentLng) == Math.round(desLng)){
             endBtn.setVisibility(View.VISIBLE);
           // new FetchURL(MainScreen.this).execute(getUrl(currentLat,currentLng,desLat,desLng, "driving"), "driving");
        }

    }

    private String getUrl(double sourceLat,double sourceLng,double desLat,double desLng,String directionMode) {


        // Origin of route
        String str_origin = "origin=" + sourceLat + "," + sourceLng;
        // Destination of route
        String str_dest = "destination=" + desLat + "," + desLng;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters +
                "&key=AIzaSyAywE2WbCBtd5oeitbemZ4Yr3B99efVylU";
        return url;
    }

    @Override
    protected void onStop() {
        super.onStop();
        GeoFire geoFire =  new GeoFire(driversOnlineDB);
        geoFire.removeLocation(uId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        GeoFire geoFire =  new GeoFire(driversOnlineDB);
        geoFire.removeLocation(uId);
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}