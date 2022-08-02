package com.dushyant.bda;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class RecipientRegistrationActivity extends AppCompatActivity {
    private TextView backbutton;

    private CircleImageView profile_image;

    private TextInputEditText registerFullName,registerIdNumber,registerPhoneNumber,registerEmail,registerPassword;

    private Spinner bloodGroupsSpinner;

    private Button registerButton;

    private Uri resulturi;

    private ProgressDialog loader;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipient_registration);

        backbutton=findViewById(R.id.backButton);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RecipientRegistrationActivity.this,LoginActivity.class);
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
                registerButton=findViewById(R.id.registerButton);

                loader=new ProgressDialog(RecipientRegistrationActivity.this);

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
                            Toast.makeText(RecipientRegistrationActivity.this, "Select Blood Group", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        else{
                            loader.setMessage("Registering You...");
                            loader.setCanceledOnTouchOutside(false);
                            loader.show();

                            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(!task.isSuccessful()){
                                        String error=task.getException().toString();
                                        Toast.makeText(RecipientRegistrationActivity.this, "Error"+error, Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        String currentUserId=mAuth.getCurrentUser().getUid();
                                        userDatabaseRef= FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
                                        HashMap userInfo= new HashMap();
                                        userInfo.put("id",currentUserId);
                                        userInfo.put("name",fullName);
                                        userInfo.put("email",email);
                                        userInfo.put("idnumber",idNumber);
                                        userInfo.put("phoneNumber",phoneNumber);
                                        userInfo.put("bloodgroup",bloodGroup);
                                        userInfo.put("type","recipient");
                                        userInfo.put("search","recipient"+bloodGroup);


                                        if(resulturi!=null){
                                            final StorageReference filePath= FirebaseStorage.getInstance().getReference().child("profile images").child(currentUserId);

                                            StorageTask<UploadTask.TaskSnapshot> uploadTask = filePath.putFile(resulturi);

// Register observers to listen for when the download is done or if it fails
                                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                @Override
                                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                    if (!task.isSuccessful()) {
                                                        throw Objects.requireNonNull(task.getException());
                                                    }
                                                    return filePath.getDownloadUrl();
                                                }
                                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if (task.isSuccessful()) {
                                                        Uri downloadUri = task.getResult();
                                                        userInfo.put("profilepictureurl", downloadUri);
                                                    }
                                                }
                                            });

                                            userDatabaseRef.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(RecipientRegistrationActivity.this, "Data Set Successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else{
                                                        Log.d("DonorRegistration", task.getException().toString());
                                                        Toast.makeText(RecipientRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                            Intent intent =new Intent(RecipientRegistrationActivity.this,MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                            loader.dismiss();
                                        }
                                    }
                                }
                            });
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