package com.rusher.lightly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    public static final String TAG = "TAG";
    TextInputEditText editTextusername,editTextemail,editTextpassword,editTextcountry,editTextcity,editTextage,editTextsleepingTime;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    FirebaseFirestore firestore;
    String userID;
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        editTextusername=findViewById(R.id.name);
        editTextemail = findViewById(R.id.email);
        editTextpassword = findViewById(R.id.password);
        editTextcountry = findViewById(R.id.country);
        editTextcity=findViewById(R.id.city);
        editTextage=findViewById(R.id.age);
        editTextsleepingTime=findViewById(R.id.sleepingtime);
        buttonReg=findViewById(R.id.btn_register);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        progressBar=findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(view.VISIBLE);
                String name,email,password,country,city,age,sleepingtime;
                name = String.valueOf(editTextusername.getText());
                email = String.valueOf(editTextemail.getText());
                password=String.valueOf(editTextpassword.getText());
                country=String.valueOf(editTextcountry.getText());
                city=String.valueOf(editTextcity.getText());
                age=String.valueOf(editTextage.getText());
                sleepingtime=String.valueOf(editTextsleepingTime.getText());

                if(TextUtils.isEmpty(name)){
                    Toast.makeText(Register.this, "Enter Username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(country)){
                    Toast.makeText(Register.this, "Enter Country name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(city)){
                    Toast.makeText(Register.this, "Enter City", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(age)){
                    Toast.makeText(Register.this, "Enter your age", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(sleepingtime)){
                    Toast.makeText(Register.this, "Enter your sleeping time(how much time?)", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(view.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Account Created.",
                                            Toast.LENGTH_SHORT).show();

                                    userID = mAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference = firestore.collection("users").document(userID);
                                    Map<String,Object> user= new HashMap<>();
                                    user.put("fName",name);
                                    user.put("Email",email);
                                    user.put("CountryName",country);
                                    user.put("City",city);
                                    user.put("Age",age);
                                    user.put("Sleepingtime",sleepingtime);
                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d(TAG, "onSuccess: User profile is created for "+userID);
                                        }
                                    });
                                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Register.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

            }
        });
    }
}