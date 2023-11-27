package com.moutamid.quickdrop.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.moutamid.quickdrop.Model.Vehicle;
import com.moutamid.quickdrop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class VehicleManagement extends AppCompatActivity {

    private EditText brandTxt,modelTxt,yearTxt,licenseTxt,colorTxt;
    private AppCompatButton saveBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference db;
    private ImageView backImg;
    private String brand,model,year,license,color;
    private ProgressDialog pd;
    private Spinner category;
    private ArrayAdapter<String> spinnerArrayAdapter;
    String disabilityList[] = {"Disable from arms","Backbone Problem","Mental Illness","Disable from Legs"};
    private String disability = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_management);

        brandTxt = findViewById(R.id.brand);
        modelTxt = findViewById(R.id.model);
        yearTxt = findViewById(R.id.year);
        licenseTxt = findViewById(R.id.plate);
        colorTxt = findViewById(R.id.color);
        saveBtn = findViewById(R.id.saveBtn);
        backImg = findViewById(R.id.back);
        category = findViewById(R.id.spinner);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        spinnerArrayAdapter = new ArrayAdapter<String>(VehicleManagement.this,
                android.R.layout.simple_spinner_dropdown_item, disabilityList);
        category.setAdapter(spinnerArrayAdapter);
        category.setSelection(spinnerArrayAdapter.getPosition(disabilityList[0]));
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                disability = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        db = FirebaseDatabase.getInstance().getReference().child("Vehicles");
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validInfo()){
                    pd = new ProgressDialog(VehicleManagement.this);
                    pd.setMessage("Adding Vehicle Information....");
                    pd.show();
                    addVehicles();
                }
            }
        });
        checkVehicles();
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VehicleManagement.this,MainScreen.class));
                finish();
            }
        });
    }

    private void checkVehicles() {
        db.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Vehicle model = snapshot.getValue(Vehicle.class);
                    brandTxt.setText(model.getBrand());
                    modelTxt.setText(model.getModel());
                    yearTxt.setText(model.getYear());
                    licenseTxt.setText(model.getLicense());
                    colorTxt.setText(model.getColor());
                    for(int i = 0; i < disabilityList.length; i++){
                        if (disabilityList[i].equals(model.getDisability())){
                            category.setSelection(spinnerArrayAdapter.getPosition(disabilityList[i]));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addVehicles() {
        String key = db.push().getKey();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id",key);
        hashMap.put("riderId",user.getUid());
        hashMap.put("brand",brand);
        hashMap.put("model",model);
        hashMap.put("year",year);
        hashMap.put("license",license);
        hashMap.put("color",color);
        hashMap.put("disability",disability);

       // Vehicle vehicle = new Vehicle(key,user.getUid(),brand,model,year,license,color,disability);
        db.child(user.getUid()).updateChildren(hashMap);
        pd.dismiss();
        Intent intent = new Intent(VehicleManagement.this, MainScreen.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    public boolean validInfo() {
        brand = brandTxt.getText().toString();
        model = modelTxt.getText().toString();
        year = yearTxt.getText().toString();
        license = licenseTxt.getText().toString();
        color = colorTxt.getText().toString();

        if(brand.isEmpty()){
            brandTxt.setText("Input Brand");
            brandTxt.requestFocus();
            return false;
        }

        if(model.isEmpty()){
            modelTxt.setText("Input Model No");
            modelTxt.requestFocus();
            return false;
        }
        if(year.isEmpty()){
            yearTxt.setText("Input Year");
            yearTxt.requestFocus();
            return false;
        }
        if(license.isEmpty()){
            licenseTxt.setText("Input License No");
            licenseTxt.requestFocus();
            return false;
        }
        if(color.isEmpty()){
            colorTxt.setText("Input Color");
            colorTxt.requestFocus();
            return false;
        }
        return true;
    }


}