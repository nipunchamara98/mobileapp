package com.moutamid.quickdrop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.moutamid.quickdrop.Model.Rider;
import com.moutamid.quickdrop.Model.User;
import com.moutamid.quickdrop.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AccountLoginScreen extends AppCompatActivity {

    private EditText emailEt, passwordEt;

    private AppCompatButton loginBtn, signUpBtn;

    private ImageView googleBtn;
    private ImageView facebookBtn;
    private static final int RC_SIGN_IN = 234;
    GoogleApiClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    FirebaseUser currrentUser;
    ProgressDialog dialog;
    //    private TextView loginBtn;
    private DatabaseReference userdb, driverdb;
    private String utype = "";
    private SharedPreferencesManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_login_screen);

        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);

        loginBtn = findViewById(R.id.loginBtn);
        signUpBtn = findViewById(R.id.signUpBtn);

        googleBtn = findViewById(R.id.google);
        facebookBtn = findViewById(R.id.facebook);
//        loginBtn = findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();
        currrentUser = mAuth.getCurrentUser();
        manager = new SharedPreferencesManager(this);
        utype = manager.retrieveString("utype", "");
        userdb = FirebaseDatabase.getInstance().getReference("Users");
        driverdb = FirebaseDatabase.getInstance().getReference("Drivers");
        dialog = new ProgressDialog(AccountLoginScreen.this);
        /*nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = phoneTxt.getText().toString();
                if (!phone.isEmpty()){
                    Intent intent = new Intent(AccountLoginScreen.this,VerficationCode.class);
                    intent.putExtra("phone",phone);
                    startActivity(intent);
                }
            }
        });*/

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(AccountLoginScreen.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        facebookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        loginBtn.setOnClickListener(v -> {
            String email = emailEt.getText().toString();
            String password = passwordEt.getText().toString();

            if (email.isEmpty()) {
                return;
            }
            if (password.isEmpty()) {
                return;
            }
            dialog.setTitle("Logging into your Account");
            dialog.setMessage("Please wait, while logging your Account.....");
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (utype.equals("user")) {
                                User user = new User(firebaseUser.getUid(), email, email, "", "", "account.getPhotoUrl().toString()");
                                userdb.child(firebaseUser.getUid()).setValue(user);
                                sendUserToMainScreen();
                            } else {
                                Rider user = new Rider(firebaseUser.getUid(), email, email,
                                        "", "", "account.getPhotoUrl().toString()");
                                driverdb.child(firebaseUser.getUid()).setValue(user);
                                sendDriverToMainScreen();
                            }
                            dialog.dismiss();
                        } else {
                            dialog.dismiss();
                            Toast.makeText(AccountLoginScreen.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        });

        signUpBtn.setOnClickListener(v -> {
            String email = emailEt.getText().toString();
            String password = passwordEt.getText().toString();

            if (email.isEmpty()) {
                return;
            }
            if (password.isEmpty()) {
                return;
            }
            dialog.setTitle("Creating your Account");
            dialog.setMessage("Please wait, while creating your Account.....");
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (utype.equals("user")) {
                                User user = new User(firebaseUser.getUid(), email, email, "", "", "account.getPhotoUrl().toString()");
                                userdb.child(firebaseUser.getUid()).setValue(user);
                                sendUserToMainScreen();
                            } else {
                                Rider user = new Rider(firebaseUser.getUid(), email, email,
                                        "", "", "account.getPhotoUrl().toString()");
                                driverdb.child(firebaseUser.getUid()).setValue(user);
                                sendDriverToMainScreen();
                            }
                            dialog.dismiss();
                        } else {
                            dialog.dismiss();
                            Toast.makeText(AccountLoginScreen.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });


    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(
                mGoogleSignInClient
        );
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dialog.setTitle("Logging into your Account");
        dialog.setMessage("Please wait, while logging your Account.....");
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            try {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } catch (Exception e) {
                Toast.makeText(AccountLoginScreen.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (utype.equals("user")) {
                        User user = new User(firebaseUser.getUid(), account.getDisplayName(), account.getEmail(), "", "", account.getPhotoUrl().toString());
                        userdb.child(firebaseUser.getUid()).setValue(user);
                        sendUserToMainScreen();
                    } else {
                        Rider user = new Rider(firebaseUser.getUid(), account.getDisplayName(), account.getEmail(),
                                "", "", account.getPhotoUrl().toString());
                        driverdb.child(firebaseUser.getUid()).setValue(user);
                        sendDriverToMainScreen();
                    }
                    dialog.dismiss();
                    // Toast.makeText(Login.this, "User Signed In", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    Toast.makeText(AccountLoginScreen.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    private void sendDriverToMainScreen() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(AccountLoginScreen.this, com.moutamid.quickdrop.driver.MainScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            //       Toast.makeText(AccountLoginScreen.this,"Login First",Toast.LENGTH_LONG).show();
        }
    }

    private void sendUserToMainScreen() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(AccountLoginScreen.this, com.moutamid.quickdrop.customer.MainScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            //     Toast.makeText(AccountLoginScreen.this,"Login First",Toast.LENGTH_LONG).show();
        }
    }


}