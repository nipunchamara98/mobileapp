package com.moutamid.quickdrop.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moutamid.quickdrop.EditProfile;
import com.moutamid.quickdrop.Model.Rider;
import com.moutamid.quickdrop.ModuleScreen;
import com.moutamid.quickdrop.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RiderMenuItemActivity extends AppCompatActivity {

    private ImageView closeImg;
    private CircleImageView profileImg;
    private TextView nameTxt,editTxt,vehicleBtn,notificationBtn,documentBtn,reviewsBtn,languageBtn,termsBtn,
            logoutBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private GoogleApiClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_menu_item);
        profileImg = findViewById(R.id.profile);
        closeImg = findViewById(R.id.close);
        nameTxt = findViewById(R.id.name);
        editTxt = findViewById(R.id.edit);
        profileImg = findViewById(R.id.profile);
        vehicleBtn = findViewById(R.id.vehicle);
        notificationBtn = findViewById(R.id.notification);
        documentBtn = findViewById(R.id.document);
        reviewsBtn = findViewById(R.id.review);
        logoutBtn = findViewById(R.id.logout);
        languageBtn = findViewById(R.id.language);
        termsBtn = findViewById(R.id.terms);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(RiderMenuItemActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        getUserDetail();
        editTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(RiderMenuItemActivity.this, EditProfile.class);
                intent.putExtra("utype","driver");
                startActivity(intent);
                finish();
            }
        });

        vehicleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RiderMenuItemActivity.this, VehicleManagement.class));
                finish();
            }
        });
        documentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RiderMenuItemActivity.this, DocumentManagementActivity.class));
                finish();
            }
        });
        languageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RiderMenuItemActivity.this, LanguagesActivity.class));
                finish();
            }
        });

        termsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RiderMenuItemActivity.this, PrivacyPolicyScreen.class));
                finish();
            }
        });
        notificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RiderMenuItemActivity.this, AvailableRequestsActivity.class));
                finish();
            }
        });

        reviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RiderMenuItemActivity.this, RideReviewsActivity.class));
                finish();
            }
        });

        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RiderMenuItemActivity.this, MainScreen.class);
                startActivity(intent);
            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleSignInClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

                    }
                });
                mGoogleSignInClient.disconnect();
                Intent intent = new Intent(RiderMenuItemActivity.this, ModuleScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void getUserDetail() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Drivers")
                .child(user.getUid());
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Rider rider = snapshot.getValue(Rider.class);
                    nameTxt.setText(rider.getFullname());
                    if (rider.getImageUrl().equals("")){
                        Picasso.with(RiderMenuItemActivity.this)
                                .load(R.drawable.profile)
                                .into(profileImg);
                    }else {
                        Picasso.with(RiderMenuItemActivity.this)
                                .load(rider.getImageUrl())
                                .into(profileImg);
                    }
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