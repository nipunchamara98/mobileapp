package com.moutamid.quickdrop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moutamid.quickdrop.R;
import com.moutamid.quickdrop.customer.MainScreen;
import com.moutamid.quickdrop.walkthrough.WalkthroughScreens;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeScreen extends AppCompatActivity {

    private AppCompatButton startBtn;
    private FirebaseAuth mAuth;
    private String utype = "";
    private SharedPreferencesManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        startBtn = (AppCompatButton) findViewById(R.id.startBtn);
        mAuth = FirebaseAuth.getInstance();
        manager = new SharedPreferencesManager(this);
        utype = manager.retrieveString("utype","");
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeScreen.this, WalkthroughScreens.class));
                finish();
            }
        });
    }

    private void sendUserToMainScreen() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (utype.equals("user")) {
                Intent intent = new Intent(WelcomeScreen.this, MainScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }else {
                Intent intent = new Intent(WelcomeScreen.this, com.moutamid.quickdrop.driver.MainScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }else {

        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        sendUserToMainScreen();
    }
}