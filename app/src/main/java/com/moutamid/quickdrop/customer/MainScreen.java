package com.moutamid.quickdrop.customer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moutamid.quickdrop.Model.Rider;
import com.moutamid.quickdrop.Model.Trip;
import com.moutamid.quickdrop.Model.User;
import com.moutamid.quickdrop.R;
import com.moutamid.quickdrop.databinding.ActivityMainScreenBinding;
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

public class MainScreen extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,TaskLoadedCallback {

    private GoogleMap mMap;
    private ActivityMainScreenBinding binding;
    private ImageView menuImg;
    private TextView top_location, bottom_location;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private DatabaseReference db,customerOnlineDB,mRequestTrip;
    private FirebaseAuth mAuth;
    private String uId;
    private double currentLat, currentLng = 0;
    private double desLat, desLng = 0;
    private double driverLat, driverLng = 0;
    //private String location = "";
    private String destination = "";
    private String source = "";
    private String key = "";
    private AppCompatButton nextBtn;
    private boolean complete = false;
    private static final int REQUEST_LOCATION = 1;
    private Polyline currentPolyline;
    private ImageView callImg,whatsappImg;
    private LinearLayout call_layout;
    private MarkerOptions place1, place2;
    private String phone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        menuImg = (ImageView) findViewById(R.id.menu);
        top_location = findViewById(R.id.location);
        bottom_location = findViewById(R.id.select_location);
        nextBtn = findViewById(R.id.next);
        call_layout = findViewById(R.id.linear);
        whatsappImg = findViewById(R.id.chat);
        callImg = findViewById(R.id.call);
        mAuth = FirebaseAuth.getInstance();
        uId = mAuth.getCurrentUser().getUid();
        db = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
        customerOnlineDB = FirebaseDatabase.getInstance().getReference().child("Customers Available");
        mRequestTrip = FirebaseDatabase.getInstance().getReference().child("Requests");
        menuImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainScreen.this, MenuItemsActivity.class));
            }
        });

        checkInternetAndGPSConnection();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            bulidGoogleApiClient();
            //   mMap.setMyLocationEnabled(true);
        } else {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CALL_PHONE}, REQUEST_LOCATION);
            showGPSDialogBox();
        }
        checkLocation();
        if (getIntent() != null) {
            key = getIntent().getStringExtra("key");
            //source = getIntent().getStringExtra("source");
            destination = getIntent().getStringExtra("destination");
            //currentLat = getIntent().getDoubleExtra("clat", 0);
            //currentLng = getIntent().getDoubleExtra("clng", 0);
            desLat = getIntent().getDoubleExtra("lat", 0);
            desLng = getIntent().getDoubleExtra("lng", 0);
            driverLat = getIntent().getDoubleExtra("driver_lat", 0);
            driverLng = getIntent().getDoubleExtra("driver_lng", 0);

        }


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

       // Toast.makeText(MainScreen.this, desLat +" , "+ desLng + " , "+ driverLat + " , "+ driverLng, Toast.LENGTH_SHORT).show();

        top_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainScreen.this, SetYourLocation.class));
            }
        });

        bottom_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainScreen.this, SetDestination.class));
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainScreen.this,RequestRide.class);
                intent.putExtra("source",source);
                intent.putExtra("destination",destination);
                startActivity(intent);
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
            nextBtn.setVisibility(View.VISIBLE);
        }

        if (driverLat != 0 && driverLng != 0) {
            LatLng latLng = new LatLng(driverLat, driverLng);
            place2 = new MarkerOptions();
            place2.position(latLng);
            place2.title("Rider");
            place2.icon(BitmapDescriptorFactory.fromResource(R.drawable.map));
            mMap.addMarker(place2);

            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            nextBtn.setVisibility(View.GONE);
            call_layout.setVisibility(View.VISIBLE);
            getRiderInfo();
        }
    }

    private void getRiderInfo() {
        mRequestTrip.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Trip model = snapshot.getValue(Trip.class);
                    DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Drivers")
                            .child(model.getRiderId());
                    db.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                Rider users = snapshot.getValue(Rider.class);
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
    protected void onResume() {
        super.onResume();
        checkInternetAndGPSConnection();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        //getCompleteAddressString(location.getLatitude(),location.getLongitude());

        currentLat = location.getLatitude();
        currentLng = location.getLongitude();


        LatLng latLng = new LatLng(currentLat, currentLng);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        if (driverLat != 0 && driverLng != 0){
            new FetchURL(MainScreen.this).execute(getUrl(currentLat,currentLng,driverLat,driverLng,
                    "driving"), "driving");
            if (!complete) {
                checkRideStatus();
            }
        }

     //   Toast.makeText(this, ""  +driverLat, Toast.LENGTH_SHORT).show();

        GeoFire geoFire =  new GeoFire(customerOnlineDB);
        geoFire.setLocation(uId,new GeoLocation(location.getLatitude(),location.getLongitude()));
//        resetTimer();
    }

    private void checkRideStatus() {
        DatabaseReference mRequestTrip = FirebaseDatabase.getInstance().getReference().child("Requests")
                .child(key);
        mRequestTrip.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Trip model = snapshot.getValue(Trip.class);
                    if (model.getStatus().equals("completed")) {
                        Intent intent = new Intent(MainScreen.this, RateTrip.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("trip", model);
                        startActivity(intent);
                        finish();
                        desLng = 0;
                        desLat = 0;
                        driverLat = 0;
                        driverLng = 0;
                        mMap.clear();
                        complete = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



  /*  private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                strAdd = returnedAddress.getLocality() + " , " +returnedAddress.getCountryName();
          //      HashMap<String,Object> hashMap = new HashMap<>();
            //    hashMap.put("location",strAdd);
              //  db.updateChildren(hashMap);
                location = strAdd;
                checkLocation();

            } else {
                Log.w("My Current", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current", "Canont get Address!");
        }
        return strAdd;
    }*/

    private void checkLocation() {
     //   Query query = db.orderByChild("location").equalTo(location);
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User model = snapshot.getValue(User.class);
                    if (model.getLocation().equals("")){
                        top_location.setText("Pickup Location");
                    }else {
                        source = model.getLocation();
                        top_location.setText(source);
                    }
                } else {
                    top_location.setText("Pickup Location");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        GeoFire geoFire =  new GeoFire(customerOnlineDB);
        geoFire.removeLocation(uId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GeoFire geoFire =  new GeoFire(customerOnlineDB);
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