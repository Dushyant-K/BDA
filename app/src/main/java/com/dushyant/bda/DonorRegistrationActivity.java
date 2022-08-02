package com.dushyant.bda;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dushyant.bda.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class DonorRegistrationActivity extends AppCompatActivity {
    private TextView backbutton;

    private CircleImageView profile_image;

    private TextInputEditText registerFullName,registerIdNumber,registerPhoneNumber,registerEmail,registerPassword;

    private Spinner bloodGroupsSpinner;

    private Button registerButton;

    private Uri resulturi;

    private ProgressDialog loader;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;
    private Uri firebaseUri;
    HashMap userInfo= new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_registration);
        backbutton=findViewById(R.id.backButton);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DonorRegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

                profile_image=findViewById(R.id.profile_image);
                registerFullName=findViewById(R.id.registerFullName);
                registerIdNumber=findViewById(R.id.registerIdNumber);
                registerPhoneNumber=findViewById(R.id.registerPhoneNumber);
                registerEmail=findViewById(R.id.registerEmail);
                registerPassword=findViewById(R.id.registerPassword);
                bloodGroupsSpinner=findViewById(R.id.bloodGroupsSpinner);
                registerButton=findViewById(R.id.btnRegister);

                loader=new ProgressDialog(DonorRegistrationActivity.this);

                mAuth=FirebaseAuth.getInstance();


                profile_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent,1);
                    }
                });

                registerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String email=registerEmail.getText().toString().trim();
                        final String password=registerPassword.getText().toString().trim();
                        final String fullName=registerFullName.getText().toString().trim();
                        final String idNumber=registerIdNumber.getText().toString().trim();
                        final String phoneNumber=registerPhoneNumber.getText().toString().trim();
                        final String bloodGroup=bloodGroupsSpinner.getSelectedItem().toString().trim();

                        if(TextUtils.isEmpty(email)){
                            registerEmail.setError("Email is Required");
                            return;
                        }
                        if(TextUtils.isEmpty(password)){
                            registerPassword.setError("Password is Required");
                            return;
                        }
                        if(TextUtils.isEmpty(fullName)){
                            registerFullName.setError("FullName is Required");
                            return;
                        }
                        if(TextUtils.isEmpty(idNumber)){
                            registerIdNumber.setError("IdNumber is Required");
                            return;
                        }
                        if(TextUtils.isEmpty(phoneNumber)){
                            registerPhoneNumber.setError("Phone Number is Required");
                            return;
                        }
                        if(bloodGroup.equals("Select a blood Group")){
                            Toast.makeText(DonorRegistrationActivity.this, "Select Blood Group", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        else {
                           loader.setMessage("Registering You...");
                           loader.setCanceledOnTouchOutside(false);
                           loader.show();

                           mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                               @Override
                               public void onComplete(@NonNull Task<AuthResult> task) {
                                   if(!task.isSuccessful()){
                                       String error=task.getException().toString();

                                       Log.i("DonorRegistration", task.getException().toString());
                                       Toast.makeText(DonorRegistrationActivity.this, "Error"+error, Toast.LENGTH_SHORT).show();
                                   }
                                   else {
                                       String currentUserId=mAuth.getCurrentUser().getUid();
                                       userDatabaseRef= FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
                                       if(resulturi!=null){
                                           final StorageReference filePath= FirebaseStorage.getInstance().getReference().child("profile images").child(currentUserId);

                                           StorageTask<UploadTask.TaskSnapshot> uploadTask = filePath.putFile(resulturi);

// Register observers to listen for when the download is done or if it fails
                                            uploadTask.continueWithTask(task1 -> {
                                                if (!task1.isSuccessful()) {
                                                    throw Objects.requireNonNull(task1.getException());
                                                }
                                                return filePath.getDownloadUrl();
                                            }).addOnCompleteListener(task12 -> {
                                                if (task12.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "URL Fetched", Toast.LENGTH_SHORT).show();
                                                    firebaseUri = task12.getResult();
                                                    User u = new User(fullName, bloodGroup, currentUserId, email, idNumber, phoneNumber,
                                                            firebaseUri.toString(), "donor"+bloodGroup, Constants.DONOR);
                                                    setData(u);
                                                }
                                            });
                                       }
                                   }
                               }
                           });
                        }
                    }
                });
    }

    private void setData(User u) {
        userDatabaseRef.setValue(u).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Log.d("Paranormal", "Activity");
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Data Set Successfully", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d("DonorRegistration", task.getException().toString());
                    Toast.makeText(DonorRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==1 && resultCode ==RESULT_OK && data !=null){
     resulturi=data.getData();
     profile_image.setImageURI(resulturi);
        }
    }
}