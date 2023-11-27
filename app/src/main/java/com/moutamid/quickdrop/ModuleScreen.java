package com.moutamid.quickdrop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moutamid.quickdrop.R;

public class ModuleScreen extends AppCompatActivity {

    private AppCompatButton custBtn,riderBtn;
    private SharedPreferencesManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_screen);
        custBtn = findViewById(R.id.customer);
        riderBtn = findViewById(R.id.driver);
        manager = new SharedPreferencesManager(this);

        custBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ModuleScreen.this,AccountLoginScreen.class);
                manager.storeString("utype","user");
                startActivity(intent);
            }
        });
        riderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ModuleScreen.this,AccountLoginScreen.class);
                manager.storeString("utype","driver");
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}