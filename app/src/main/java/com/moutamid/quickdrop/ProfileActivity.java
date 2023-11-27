package com.moutamid.quickdrop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.moutamid.quickdrop.Model.Rider;
import com.moutamid.quickdrop.Model.User;
import com.moutamid.quickdrop.R;
import com.moutamid.quickdrop.customer.MainScreen;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private EditText fnameTxt,emailInput,passInput,cpassTxt;
    private Button signUpBtn;
    private String fname,email,password,cpassword;
    private CircleImageView profileImg;
    private ProgressDialog pd;
    FirebaseAuth mAuth;
    private DatabaseReference userdb,driverdb;
    private String image = "";
    private static final int PICK_IMAGE_REQUEST = 1;
    Uri uri;
    private String phoneNumber = "";
    StorageReference mStorage;
    private Bitmap bitmap;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private SharedPreferencesManager manager;
    private String utype = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        fnameTxt = findViewById(R.id.fname);
        emailInput = findViewById(R.id.email);
        passInput = findViewById(R.id.pass);
        cpassTxt = findViewById(R.id.cpass);
        profileImg = findViewById(R.id.imageView);
        signUpBtn = findViewById(R.id.signUp);
        mStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        manager = new SharedPreferencesManager(this);
        utype = manager.retrieveString("utype","");
        phoneNumber = getIntent().getStringExtra("phone");
        userdb = FirebaseDatabase.getInstance().getReference("Users");
        driverdb = FirebaseDatabase.getInstance().getReference("Drivers");
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validInfo()){
                    pd = new ProgressDialog(ProfileActivity.this);
                    pd.setMessage("Creating Account....");
                    pd.show();
                    if(utype.equals("user")){
                        storeUser();
                    }else {
                        storeDriver();
                    }
                }
            }
        });
        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });
    }
    public void checkPermission()
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, STORAGE_PERMISSION_CODE);
        }
        else {
            openGallery();
            Toast.makeText(ProfileActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
                Toast.makeText(ProfileActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(ProfileActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"SELECT IMAGE"),PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            uri = data.getData();
            profileImg.setImageURI(uri);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                saveInformation();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void saveInformation() {
        ProgressDialog dialog = new ProgressDialog(ProfileActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading your profile....");
        dialog.show();
        if (uri != null) {
            profileImg.setDrawingCacheEnabled(true);
            profileImg.buildDrawingCache();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream);
            byte[] thumb_byte_data = byteArrayOutputStream.toByteArray();

            final StorageReference reference = mStorage.child("Profile Images").child(System.currentTimeMillis() + ".jpg");
            final UploadTask uploadTask = reference.putBytes(thumb_byte_data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return reference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                image = downloadUri.toString();
                                dialog.dismiss();
                            }
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }else {
            Toast.makeText(getApplicationContext(), "Please Select Image ", Toast.LENGTH_LONG).show();

        }
    }

    private void registerUser() {
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            //if (mAuth.getCurrentUser() != null){

                            pd.dismiss();
                            Intent intent = new Intent(ProfileActivity.this, MainScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                            //}
                        }
                    }
                });

    }

    private void storeUser(){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        User user = new User(firebaseUser.getUid(),fname,email,phoneNumber,password,"",image);
        userdb.child(firebaseUser.getUid()).setValue(user);
        pd.dismiss();
        Toast.makeText(ProfileActivity.this,"Registered Successfully",Toast.LENGTH_LONG).show();

        Intent intent = new Intent(ProfileActivity.this, MainScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void storeDriver(){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        Rider user = new Rider(firebaseUser.getUid(),fname,email,phoneNumber,password,"",image);
        driverdb.child(firebaseUser.getUid()).setValue(user);
        pd.dismiss();
        Toast.makeText(ProfileActivity.this,"Registered Successfully",Toast.LENGTH_LONG).show();

        Intent intent = new Intent(ProfileActivity.this, com.moutamid.quickdrop.driver.MainScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    //Validate Input Fields
    public boolean validInfo() {
        fname = fnameTxt.getText().toString();
        email = emailInput.getText().toString();
        password = passInput.getText().toString();
        cpassword = cpassTxt.getText().toString();

        if(fname.isEmpty()){
            fnameTxt.setText("Input Fullname");
            fnameTxt.requestFocus();
            return false;
        }

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

        if(cpassword.isEmpty()){
            cpassTxt.setText("Input Confirm Password");
            cpassTxt.requestFocus();
            return false;
        }


        return true;
    }

}