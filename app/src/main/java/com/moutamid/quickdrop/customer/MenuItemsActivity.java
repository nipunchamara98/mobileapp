package com.moutamid.quickdrop.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moutamid.quickdrop.EditProfile;
import com.moutamid.quickdrop.Model.User;
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

public class MenuItemsActivity extends AppCompatActivity {

    private ImageView closeImg;
    private CircleImageView profileImg;
    private TextView nameTxt,editTxt,tripBtn,notificationBtn,paymentBtn,promosBtn,helpBtn,settingsBtn,logoutBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private GoogleApiClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_items);

        profileImg = findViewById(R.id.profile);
        closeImg = findViewById(R.id.close);
        nameTxt = findViewById(R.id.name);
        editTxt = findViewById(R.id.edit);
        profileImg = findViewById(R.id.profile);
        tripBtn = findViewById(R.id.trips);
        notificationBtn = findViewById(R.id.notification);
        paymentBtn = findViewById(R.id.payment);
        promosBtn = findViewById(R.id.promos);
        helpBtn = findViewById(R.id.help);
        settingsBtn = findViewById(R.id.settings);
        logoutBtn = findViewById(R.id.logout);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        getUserDetail();

        editTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(MenuItemsActivity.this, EditProfile.class);
                intent.putExtra("utype","user");
                startActivity(intent);
                finish();
            }
        });

        tripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuItemsActivity.this,MyTrips.class));
                finish();
            }
        });

        notificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuItemsActivity.this,NotificationActivity.class));
                finish();
            }
        });
        paymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuItemsActivity.this,PaymentActivity.class));
                finish();
            }
        });
        promosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuItemsActivity.this,PromosScreen.class));
                finish();
            }
        });
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuItemsActivity.this,HelpActivity.class));
                finish();
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuItemsActivity.this,SettingsActivity.class));
                finish();
            }
        });

        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuItemsActivity.this,MainScreen.class);
                startActivity(intent);
                finish();
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MenuItemsActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
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
                Intent intent = new Intent(MenuItemsActivity.this, ModuleScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

    }

    private void getUserDetail() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(user.getUid());
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User rider = snapshot.getValue(User.class);
                    nameTxt.setText(rider.getFullname());
                    if (rider.getImageUrl().equals("")){
                        Picasso.with(MenuItemsActivity.this)
                                .load(R.drawable.profile)
                                .into(profileImg);
                    }else {
                        Picasso.with(MenuItemsActivity.this)
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
}