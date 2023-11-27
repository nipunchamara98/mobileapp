package com.moutamid.quickdrop.driver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moutamid.quickdrop.databinding.ActivityLanguagesBinding;

public class LanguagesActivity extends AppCompatActivity {

    private ActivityLanguagesBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLanguagesBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        b.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RiderMenuItemActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}