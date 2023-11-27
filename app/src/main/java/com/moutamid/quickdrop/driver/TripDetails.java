package com.moutamid.quickdrop.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.moutamid.quickdrop.Model.Trip;
import com.moutamid.quickdrop.Model.User;
import com.moutamid.quickdrop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class TripDetails extends AppCompatActivity {

    private CircleImageView profileImg;
    private TextView nameTxt,priceTxt,timeTxt,dropoffTxt,pickupTxt;
    private AppCompatButton ignoreBtn,acceptBtn;
    private Trip model;
    private DatabaseReference mRequestTrip;
    private FirebaseAuth mAuth;
    private double custLat = 0;
    private double custLng = 0;
    private double desLat = 0;
    private double desLng = 0;
    private FirebaseUser user;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        profileImg  = findViewById(R.id.profile);
        nameTxt = findViewById(R.id.name);
        priceTxt  =findViewById(R.id.rupees);
        timeTxt  = findViewById(R.id.distance);
        dropoffTxt = findViewById(R.id.drop_off);
        pickupTxt = findViewById(R.id.pick_up);

        ignoreBtn = findViewById(R.id.ignore);
        acceptBtn = findViewById(R.id.confirm);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        requestQueue = Volley.newRequestQueue(TripDetails.this);
        model = getIntent().getParcelableExtra("trip");
        mRequestTrip = FirebaseDatabase.getInstance().getReference().child("Requests");
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users").child(model.getUserId());
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    nameTxt.setText(user.getFullname());
                    if (user.getImageUrl().equals("")){
                        Picasso.with(TripDetails.this)
                                .load(R.drawable.profile)
                                .into(profileImg);
                    }else {
                        Picasso.with(TripDetails.this)
                                .load(user.getImageUrl())
                                .into(profileImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        pickupTxt.setText(model.getPickup());
        dropoffTxt.setText(model.getDropoff());
        timeTxt.setText(model.getTime() + " mins");
        priceTxt.setText(model.getPrice());
        getCustomerLatLng(model.getUserId());
        getLatLng(model.getDropoff());
        ignoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TripDetails.this,AvailableRequestsActivity.class));
                finish();
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("riderId",user.getUid());
                hashMap.put("status","accepted");
                mRequestTrip.child(model.getId()).updateChildren(hashMap);

                Intent intent = new Intent(TripDetails.this,MainScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("key",model.getId());
                intent.putExtra("pickup",model.getPickup());
                intent.putExtra("destination",model.getDropoff());
                intent.putExtra("lat",desLat);
                intent.putExtra("lng",desLng);
                intent.putExtra("cust_lat",custLat);
                intent.putExtra("cust_lng",custLng);
                startActivity(intent);
                finish();
            }
        });

    }
    private void getLatLng(String destinationLocation) {
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?input="+destinationLocation+"&key=AIzaSyAywE2WbCBtd5oeitbemZ4Yr3B99efVylU";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //  bar.setVisibility(View.INVISIBLE);
                        try {
                            JSONObject user = new JSONObject(response);
                            JSONArray friends = user.getJSONArray("results");
                            for (int i = 0; i < friends.length(); i++) {
                                JSONObject jsonObject = friends.getJSONObject(i);
                           //     if (jsonObject.getString("name").equals(destinationLocation)) {
                                    JSONObject geometryObject = jsonObject.getJSONObject("geometry");
                                    desLat = geometryObject.getJSONObject("location").getDouble("lat");
                                    desLng = geometryObject.getJSONObject("location").getDouble("lng");
                             //   }
                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                            // progressDialog.dismiss();
                            //  bar.setVisibility(View.INVISIBLE);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(stringRequest);
    }

    private void getCustomerLatLng(String custId) {
        DatabaseReference driversOnlineDB = FirebaseDatabase.getInstance().getReference()
                .child("Customers Available").child(custId);
        driversOnlineDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    custLat = (double) snapshot.child("l").child("0").getValue();
                    custLng = (double) snapshot.child("l").child("1").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}