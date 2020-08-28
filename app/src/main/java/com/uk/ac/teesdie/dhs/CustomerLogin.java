package com.uk.ac.teesdie.dhs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CustomerLogin extends AppCompatActivity {

    private final static  int LOGIN_REQUEST_CODE = 0077; //Could be any unique code to the project
    private EditText mEmail, mPassword;
    private Button mLogin,mRegister;

    FirebaseDatabase database;
    DatabaseReference driverInfoRef;

    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener fireaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        //init();

        mAuth =FirebaseAuth.getInstance();
        fireaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();
                if (user != null){
                    Intent intent = new Intent(CustomerLogin.this,DriverHomeActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);


        mLogin = (Button) findViewById(R.id.btn_login);
        mRegister = (Button) findViewById(R.id.btn_register);


        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(CustomerLogin.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            Toast.makeText(CustomerLogin.this,"Sign up Failed", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            String user_id = mAuth.getCurrentUser().getUid();
                            DatabaseReference currentUser_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                            currentUser_db .setValue(true);
                        }
                        if (task.isSuccessful()){
                            Toast.makeText(CustomerLogin.this,"You have Successfully Signed Up",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(CustomerLogin.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            Toast.makeText(CustomerLogin.this,"Sign In Failed",Toast.LENGTH_SHORT).show();
                        }else {
                            if (task.isSuccessful()) {
                                Toast.makeText(CustomerLogin.this, "You have Successfully Signed In", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }

    //Fire Base Auth Failed , Check out later

//    private void init() {
//
//        database = FirebaseDatabase.getInstance();
//        driverInfoRef = database.getReference(Common.DRIVER_INFO_REFERENCE);
//        checkUserfromFirebase();
//    }
//
//    private void checkUserfromFirebase() {
//
//        driverInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists())
//                        {
//                            Toast.makeText(DriverLogin.this,"USer ALready Registered",Toast.LENGTH_SHORT).show();
//                        }
//                        else {
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                        Toast.makeText(DriverLogin.this,""+error.getMessage(),Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(fireaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(fireaseAuthListener);
    }
}