package com.moutamid.quickdrop.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moutamid.quickdrop.Model.Trip;
import com.moutamid.quickdrop.adapters.NotificationsListAdapter;
import com.moutamid.quickdrop.databinding.ActivityMyTripsBinding;
import com.moutamid.quickdrop.driver.TripDetails;
import com.moutamid.quickdrop.listener.ItemClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyTrips extends AppCompatActivity {

    private ActivityMyTripsBinding b;
    private List<Trip> tripList;
    private DatabaseReference mRequestTrip;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMyTripsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        b.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),MenuItemsActivity.class));
                finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.VERTICAL);
        b.recyclerView.setLayoutManager(manager);
        mRequestTrip = FirebaseDatabase.getInstance().getReference().child("Requests");
        tripList = new ArrayList<>();
        getRequestList();
    }
    private void getRequestList() {
        Query query = mRequestTrip.orderByChild("status").equalTo("completed");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    tripList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Trip model = ds.getValue(Trip.class);
                        if (model.getUserId().equals(user.getUid())) {
                            tripList.add(model);
                        }
                    }
                    Collections.sort(tripList, new Comparator<Trip>() {
                        @Override
                        public int compare(Trip trip, Trip t1) {
                            return Long.compare(trip.getTime(),t1.getTime());
                        }
                    });
                    b.recyclerView.setVisibility(View.VISIBLE);
                    b.noDataLayout.setVisibility(View.GONE);
                    NotificationsListAdapter adapter = new NotificationsListAdapter(MyTrips.this,
                            tripList);
                    b.recyclerView.setAdapter(adapter);
                    adapter.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onItemClick(int position, View view) {
                            Intent intent = new Intent(MyTrips.this, TripDetails.class);
                            intent.putExtra("trip",tripList.get(position));
                            startActivity(intent);
                        }
                    });
                    adapter.notifyDataSetChanged();
                }else {

                    b.recyclerView.setVisibility(View.GONE);
                    b.noDataLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}