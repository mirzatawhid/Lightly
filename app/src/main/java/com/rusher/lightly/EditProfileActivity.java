package com.rusher.lightly;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    String userID;

    EditText editName,editAge,editCity,editCountry,editSleepingTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth= FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        editName = findViewById(R.id.edit_name);
        editAge = findViewById(R.id.edit_age);
        editCity = findViewById(R.id.edit_city);
        editCountry = findViewById(R.id.edit_country);
        editSleepingTime = findViewById(R.id.edit_sleeping_hours);
        TextView editMail =findViewById(R.id.edit_email);
        ImageView btnOK = findViewById(R.id.btn_ok);

        mAuth = FirebaseAuth.getInstance();

        firestore = FirebaseFirestore.getInstance();

        userID = mAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firestore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                editName.setText(documentSnapshot.getString("fName"));
                editAge.setText(documentSnapshot.getString("Age"));
                editMail.setText(documentSnapshot.getString("Email"));
                editCity.setText(documentSnapshot.getString("City"));
                editCountry.setText(documentSnapshot.getString("CountryName"));
                editSleepingTime.setText(documentSnapshot.getString("Sleepingtime"));
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dName = String.valueOf(editName.getText());
                String dAge = String.valueOf(editAge.getText());
                String dCity = String.valueOf(editCity.getText());
                String dCountry = String.valueOf(editCountry.getText());
                String dSleepingTime = String.valueOf(editSleepingTime.getText());


                if (TextUtils.isEmpty(dName)) {
                    Toast.makeText(getApplicationContext(), "Enter Full Name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(dAge)) {
                    Toast.makeText(getApplicationContext(), "Enter the Appropriate Age.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(dCity) && dCity.length()>11 ) {
                    Toast.makeText(getApplicationContext(), "Enter the Appropriate City Name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(dCountry)) {
                    Toast.makeText(getApplicationContext(), "Enter Country Name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(dSleepingTime) && dSleepingTime.length()>11 ) {
                    Toast.makeText(getApplicationContext(), "Enter the Appropriate Sleeping time(Hours).", Toast.LENGTH_SHORT).show();
                    return;
                }

                userID = mAuth.getCurrentUser().getUid();
                DocumentReference documentReference = firestore.collection("users").document(userID);
                Map<String,Object> updateUser= new HashMap<>();
                updateUser.put("fName",dName);
                updateUser.put("Age",dAge);
                updateUser.put("City",dCity);
                updateUser.put("CountryName",dCountry);
                updateUser.put("Sleepingtime",dSleepingTime);

                documentReference.update(updateUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(), "User Profile Updated.",
                                Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), Profile.class);
                        startActivity(intent);
                        finish();
                    }
                });

            }
        });

    }

    public void goToMain(View view) {
        Intent intent = new Intent(getApplicationContext(), Profile.class);
        startActivity(intent);
        finish();
    }
}