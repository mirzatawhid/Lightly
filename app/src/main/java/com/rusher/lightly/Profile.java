package com.rusher.lightly;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class Profile extends AppCompatActivity {

    TextView textViewname,textViewemail,textViewcountry,textViewcity,textViewage,textViewsleepingtime;
    ProgressBar progressBar;
    Button button;
    String Name,Email,Country,City,Age,SlpTime;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        textViewname = findViewById(R.id.tv_name);
        textViewemail = findViewById(R.id.tv_email);
        textViewcountry = findViewById(R.id.tv_country);
        textViewcity = findViewById(R.id.tv_city);
        textViewage = findViewById(R.id.tv_age);
        textViewsleepingtime = findViewById(R.id.tv_sleeping_hours);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firestore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                textViewname.setText(documentSnapshot.getString("fName"));
                textViewemail.setText(documentSnapshot.getString("Email"));
                textViewcountry.setText(documentSnapshot.getString("CountryName"));
                textViewcity.setText(documentSnapshot.getString("City"));
                textViewage.setText(documentSnapshot.getString("Age"));
                textViewsleepingtime.setText(documentSnapshot.getString("Sleepingtime"));


            }
        });
    }

    public void goToEditProfile(View view) {
        Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToMain(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}