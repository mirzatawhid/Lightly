package com.rusher.lightly;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
        textViewname = findViewById(R.id.your_name);
        textViewemail = findViewById(R.id.Profile_email);
        textViewcountry = findViewById(R.id.Profile_country);
        textViewcity = findViewById(R.id.Profile_city);
        textViewage = findViewById(R.id.Profile_age);
        textViewsleepingtime = findViewById(R.id.Profile_sleepintime);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        DocumentReference documentReference = firestore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                textViewname.setText(documentSnapshot.getString("fName"));
                textViewemail.setText(documentSnapshot.getString("Email"));
                textViewcountry.setText("Your Country: "+documentSnapshot.getString("CountryName"));
                textViewcity.setText("Your Local Town: "+documentSnapshot.getString("City"));
                textViewage.setText("Your Age: "+documentSnapshot.getString("Age"));
                textViewsleepingtime.setText("Your Sleeping Time(H): "+documentSnapshot.getString("Sleepingtime"));


            }
        });
    }
}