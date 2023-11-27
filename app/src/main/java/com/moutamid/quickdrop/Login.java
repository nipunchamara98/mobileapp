package com.moutamid.quickdrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.moutamid.quickdrop.Model.User;
import com.moutamid.quickdrop.R;
import com.moutamid.quickdrop.customer.MainScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private EditText emailInput,passInput;
    private Button loginBtn;
    private String email,password;
    private ProgressDialog pd;
    FirebaseAuth mAuth;
    private TextView signUpBtn;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailInput = findViewById(R.id.email);
        passInput = findViewById(R.id.pass);
        signUpBtn = findViewById(R.id.signUp);
        loginBtn = findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference().child("Users");
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this,AccountLoginScreen.class));
                finish();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validInfo()){
                    pd = new ProgressDialog(Login.this);
                    pd.setMessage("Login your Account....");
                    pd.show();
                    loginUser();
                }
            }
        });

    }

    private void loginUser() {
     //   Query query = db.orderByChild("email").equalTo(email);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        User user = ds.getValue(User.class);
                        if (user.getEmail().equals(email) && user.getPassword().equals(password)){
                            pd.dismiss();
                            Toast.makeText(Login.this,"Login Successfully",Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(Login.this, MainScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }else {
                            pd.dismiss();
                            Toast.makeText(Login.this,"Incorrect email and password",Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Validate Input Fields
    public boolean validInfo() {
        email = emailInput.getText().toString();
        password = passInput.getText().toString();

        if (email.isEmpty()) {
            emailInput.setError("Input email!");
            emailInput.requestFocus();
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please input valid email!");
            emailInput.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passInput.setError("Input password!");
            passInput.requestFocus();
            return false;
        }

        return true;
    }


}