package com.dushyant.bda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.ktx.Firebase;

public class LoginActivity extends AppCompatActivity {
    private TextView backButton;
    private TextInputEditText LoginEmail,LoginPassword;
    private TextView forgotPassword;
private Button LoginButton;

private ProgressDialog loader;

private FirebaseAuth mAuth;

private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        backButton=findViewById(R.id.backButton);
                LoginEmail=findViewById(R.id.LoginEmail);
                LoginPassword=findViewById(R.id.LoginPassword);
               forgotPassword=findViewById(R.id.forgotPassword);
                LoginButton=findViewById(R.id.LoginButton);

                 loader= new ProgressDialog(this);

                 mAuth=FirebaseAuth.getInstance();

                 authStateListener=new FirebaseAuth.AuthStateListener() {
                     @Override
                     public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                         FirebaseUser user= mAuth.getCurrentUser();
                         if(user!=null){
                             Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                             startActivity(intent);
                             finish();
                         }
                     }
                 };

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,SelectRegistrationActivity.class);
                finish();
                startActivity(intent);
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email=LoginEmail.getText().toString().trim();
                final String password=LoginPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    LoginEmail.setError("Email is Required");
                }
                if(TextUtils.isEmpty(password)){
                    LoginPassword.setError("Password is Required");
                }

                else{
              loader.setMessage("Log in in Progress");
              loader.setCanceledOnTouchOutside(false);
              loader.show();

              mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                  @Override
                  public void onComplete(@NonNull Task<AuthResult> task) {
                      if(task.isSuccessful()){
                          Toast.makeText(LoginActivity.this, "Log in Successfull", Toast.LENGTH_SHORT).show();
                          Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                          startActivity(intent);
                          finish();
                      }
                      else{
                          Toast.makeText(LoginActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                      }
                      loader.dismiss();
                  }
              });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.addAuthStateListener(authStateListener);
    }
}