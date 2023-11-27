package com.moutamid.quickdrop.driver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moutamid.quickdrop.databinding.ActivityDocumentManagementBinding;

public class DocumentManagementActivity extends AppCompatActivity {

    private ActivityDocumentManagementBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityDocumentManagementBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        b.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RiderMenuItemActivity.class));
                finish();
            }
        });
    }
}