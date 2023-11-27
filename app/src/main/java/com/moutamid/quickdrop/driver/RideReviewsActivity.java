package com.moutamid.quickdrop.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.moutamid.quickdrop.Model.Reviews;
import com.moutamid.quickdrop.R;
import com.moutamid.quickdrop.adapters.ReviewListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RideReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private ImageView menuImg;
    private DatabaseReference mReviewsDB;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private List<Reviews> reviewsList;
    private LinearLayout dataLayout,noDataLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_reviews);
        menuImg = findViewById(R.id.menu);
        recyclerView = findViewById(R.id.recyclerView);
        manager = new LinearLayoutManager(RideReviewsActivity.this);
        manager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(manager);
        dataLayout = findViewById(R.id.data_layout);
        noDataLayout = findViewById(R.id.no_data_layout);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mReviewsDB = FirebaseDatabase.getInstance().getReference().child("Reviews");
        reviewsList = new ArrayList<>();
        menuImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RideReviewsActivity.this,RiderMenuItemActivity.class));
            }
        });

        getReviewsList();
    }

    private void getReviewsList() {
        Query query = mReviewsDB.orderByChild("riderId").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    reviewsList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Reviews model = ds.getValue(Reviews.class);
                        reviewsList.add(model);
                    }
                    noDataLayout.setVisibility(View.GONE);
                    dataLayout.setVisibility(View.VISIBLE);
                    ReviewListAdapter adapter = new ReviewListAdapter(RideReviewsActivity.this,
                            reviewsList);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }else {
                    noDataLayout.setVisibility(View.VISIBLE);
                    dataLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}