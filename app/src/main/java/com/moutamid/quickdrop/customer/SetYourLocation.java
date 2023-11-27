package com.moutamid.quickdrop.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.moutamid.quickdrop.R;
import com.moutamid.quickdrop.adapters.LocationListAdapter;
import com.moutamid.quickdrop.listener.ItemClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.moutamid.quickdrop.Model.Address;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SetYourLocation extends AppCompatActivity {

    private EditText searchLocation;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private ImageView searchBtn,backImg;
    private RequestQueue requestQueue;
    private DatabaseReference db;
    private FirebaseAuth mAuth;
    private String uId;
    private List<Address> addressList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_your_location);
        searchLocation = findViewById(R.id.location);
        searchBtn = findViewById(R.id.search);
        backImg = findViewById(R.id.back);
        recyclerView = findViewById(R.id.recyclerView);
        manager = new LinearLayoutManager(SetYourLocation.this);
        manager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(manager);
        mAuth = FirebaseAuth.getInstance();
        uId = mAuth.getCurrentUser().getUid();
        db = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
        requestQueue = Volley.newRequestQueue(SetYourLocation.this);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(searchLocation.getText().toString())){
                    getData(searchLocation.getText().toString());
                }
            }
        });
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SetYourLocation.this,MainScreen.class));
            }
        });
    }


    private void getData(String location) {
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?input="+location+"&key=AIzaSyAywE2WbCBtd5oeitbemZ4Yr3B99efVylU";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //  bar.setVisibility(View.INVISIBLE);
                        try {
                            addressList.clear();
                            JSONObject user = new JSONObject(response);
                            JSONArray friends = user.getJSONArray("results");
                            for (int i = 0; i < friends.length(); i++) {
                                JSONObject jsonObject = friends.getJSONObject(i);
                                Address model = new Address();
                                model.setName(jsonObject.getString("name"));
                                model.setDescription(jsonObject.getString("formatted_address"));
                                JSONObject geometryObject = jsonObject.getJSONObject("geometry");
                                model.setLat(geometryObject.getJSONObject("location").getDouble("lat"));
                                model.setLng(geometryObject.getJSONObject("location").getDouble("lng"));
                                addressList.add(model);
                                LocationListAdapter adapter = new LocationListAdapter(SetYourLocation.this,addressList);
                                recyclerView.setAdapter(adapter);
                                adapter.setItemClickListener(new ItemClickListener() {
                                    @Override
                                    public void onItemClick(int position, View view) {
                                        HashMap<String,Object> hashMap = new HashMap<>();
                                        hashMap.put("location",addressList.get(position).getName());
                                        db.updateChildren(hashMap);
                                        Intent intent = new Intent(SetYourLocation.this,MainScreen.class);
                                      //  intent.putExtra("source",addressList.get(position).getName());
                                        //intent.putExtra("clat",addressList.get(position).getLat());
                                        //intent.putExtra("clng",addressList.get(position).getLng());
                                        startActivity(intent);
                                    }
                                });
                                adapter.notifyDataSetChanged();
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
}