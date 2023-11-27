package com.moutamid.quickdrop.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moutamid.quickdrop.Model.Reviews;
import com.moutamid.quickdrop.Model.Rider;
import com.moutamid.quickdrop.Model.Trip;
import com.moutamid.quickdrop.Model.Vehicle;
import com.moutamid.quickdrop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RateTrip extends AppCompatActivity {

    private Trip request;
    private CircleImageView profileImg;
    private TextView nameTxt,carTxt;
    private ImageView menuImg;
    private DatabaseReference reference,reference1;
    private EditText feedbackTxt;
    private AppCompatButton submitBtn;
    private RatingBar simpleRatingBar;
    private String feedback = "";
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_trip);
        profileImg = findViewById(R.id.profile);
        nameTxt = findViewById(R.id.name);
        carTxt = findViewById(R.id.car);
        menuImg  =findViewById(R.id.menu);
        feedbackTxt = findViewById(R.id.feedback);
        submitBtn  =findViewById(R.id.submit);
        simpleRatingBar = (RatingBar) findViewById(R.id.simpleRatingBar);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        request = getIntent().getParcelableExtra("trip");
        reference = FirebaseDatabase.getInstance().getReference().child("Drivers").child(request.getRiderId());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Rider model = snapshot.getValue(Rider.class);
                    nameTxt.setText(model.getFullname());
                    if (model.getImageUrl().equals("")){
                        Picasso.with(RateTrip.this)
                                .load(R.drawable.profile)
                                .into(profileImg);
                    }else {
                        Picasso.with(RateTrip.this)
                                .load(model.getImageUrl())
                                .into(profileImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        reference1 = FirebaseDatabase.getInstance().getReference().child("Vehicles").child(request.getRiderId());
        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Vehicle vehicle = snapshot.getValue(Vehicle.class);
                    carTxt.setText(vehicle.getBrand());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedback = feedbackTxt.getText().toString();
                saveFeedback();
            }
        });

        menuImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RateTrip.this,MenuItemsActivity.class));
                finish();
            }
        });
    }

    private void saveFeedback() {

        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Reviews");
        String key = db.push().getKey();

        Reviews reviews = new Reviews(key,user.getUid(),simpleRatingBar.getRating(),feedback,request.getRiderId(),request.getId());
        db.child(key).setValue(reviews);

        Toast.makeText(RateTrip.this, "Thanks for your feedback...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RateTrip.this, MainScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RateTrip.this,MainScreen.class));
        finish();
    }
}