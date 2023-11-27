package com.moutamid.quickdrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.moutamid.quickdrop.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

public class VerficationCode extends AppCompatActivity {

    private String phoneNumber;
    private String verificationId;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference userdb,driverdb;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private EditText mEtCode;
    private AppCompatButton mBtNext;
    private TextView  mTvResend, mTvTime,numberTxt;
    String code;
    ProgressDialog dialog;
    private CountDownTimer timer;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String utype = "";
    private SharedPreferencesManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verfication_code);
        mEtCode = findViewById(R.id.code);
        mBtNext = findViewById(R.id.buttonContinue);
        mTvResend = findViewById(R.id.resendCode);
        mTvTime = findViewById(R.id.tv_countdown_sms);
        numberTxt = findViewById(R.id.number);
        phoneNumber = getIntent().getStringExtra("phone");
        dialog = new ProgressDialog(VerficationCode.this);
        numberTxt.setText("+92"+phoneNumber);
        mFirebaseAuth = FirebaseAuth.getInstance();
        manager = new SharedPreferencesManager(this);
        utype = manager.retrieveString("utype","");
        userdb = FirebaseDatabase.getInstance().getReference("Users");
        driverdb = FirebaseDatabase.getInstance().getReference("Drivers");
        sendVerificationCode(phoneNumber);
        resetTimer();
        mBtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = mEtCode.getText().toString();

                if (TextUtils.isEmpty(code)) {
                    Toast.makeText(VerficationCode.this, "Please Enter the code", Toast.LENGTH_LONG).show();
                } else {
                    verifyCode(code);
                }
            }
        });
        mTvResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeResend(phoneNumber);
            }
        });

    }


    private void setUpVerificationCallbacks() {
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationId = s;
                resendToken = forceResendingToken;
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                code = phoneAuthCredential.getSmsCode();
                if (code != null) {
                    mEtCode.setText(code);
                    verifyCode(code);
                }
                signInWithCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Log.d("TAG", "Invalid credential: "
                            + e.getLocalizedMessage());
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // SMS quota exceeded
                    Log.d("TAG", "SMS Quota exceeded.");
                }
            }
        };
    }

    private void codeResend(String phone) {
        //textView.setText("00:00");

        setUpVerificationCallbacks();
        resetTimer();
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mFirebaseAuth)
                .setPhoneNumber("+92" +phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setForceResendingToken(resendToken)
                .setActivity(this).setCallbacks(mCallback).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
        //timeStamp = false;
        //startStop();
    }

    private void resetTimer() {
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Used for formatting digit to be in 2 digits only
                NumberFormat f = new DecimalFormat("00");
                long sec = (millisUntilFinished / 1000) % 60;
                mTvTime.setText("00" + ":" + f.format(sec));
            }
            // When the task is over it will print 00:00:00 there
            public void onFinish() {
                mTvTime.setText("0:30");
             //   mTvResend.setEnabled(true);
                //timer.cancel();
                //disableButton();
            }
        }.start();
    }

   /* private void disableButton() {

        mTvResend.setEnabled(false);
        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                mTvTime.setText("seconds remaining: " + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                mTvResend.setEnabled(true);
                mTvResend.setText("0:30");
            }

        }.start();

    }*/


    private void verifyCode(String code) {
        dialog.setMessage("Verifying....");
        dialog.show();
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithCredential(credential);
        } catch (Exception e) {
            Toast.makeText(this, "Verification code wrong", Toast.LENGTH_SHORT).show();
        }

    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                            if (utype.equals("user")){
                                checkUserExists();
                            }else {
                                checkDriverExists();
                            }
                            dialog.dismiss();
                        } else {
                            dialog.dismiss();
                            Toast.makeText(VerficationCode.this, "Cannot verify : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkDriverExists() {
        driverdb.child(mFirebaseAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            sendDriverToMainScreen();
                        }else {
                            Intent intent = new Intent(VerficationCode.this, ProfileActivity.class);
                            intent.putExtra("phone","+92"+phoneNumber);
                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void checkUserExists() {
        userdb.child(mFirebaseAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            sendUserToMainScreen();
                        }else {
                            Intent intent = new Intent(VerficationCode.this, ProfileActivity.class);
                            intent.putExtra("phone","+92"+phoneNumber);
                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendDriverToMainScreen() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(VerficationCode.this, com.moutamid.quickdrop.driver.MainScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void sendUserToMainScreen() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(VerficationCode.this, com.moutamid.quickdrop.customer.MainScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void sendVerificationCode(String number) {
        setUpVerificationCallbacks();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mFirebaseAuth)
                .setPhoneNumber("+92" + number)
                .setTimeout(60L,TimeUnit.SECONDS)
                .setActivity(this).setCallbacks(mCallback).build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }



}