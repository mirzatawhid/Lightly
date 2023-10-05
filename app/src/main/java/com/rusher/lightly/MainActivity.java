package com.rusher.lightly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user == null){
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
        }
    }

    public void lightPollution(View view) {
        Intent intent = new Intent(MainActivity.this,LightMapActivity.class);
        startActivity(intent);
    }

    public void aboutUs(View view) {
        Intent intent = new Intent(MainActivity.this,AboutUsActivity.class);
        startActivity(intent);
    }

    public void lightSuggestion(View view) {
        Intent intent = new Intent(MainActivity.this,LightSuggestionActivity.class);
        startActivity(intent);
    }

    public void uvMap(View view) {
        Intent intent = new Intent(MainActivity.this,UVMapActivity.class);
        startActivity(intent);
    }

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(),Login.class);
        startActivity(intent);
        finish();
    }

    public void userProfile(View view) {
        Intent intent = new Intent(getApplicationContext(),Profile.class);
        startActivity(intent);
        finish();
    }

    public void uvSuggestion(View view) {
        Intent intent = new Intent(MainActivity.this,UVSuggestionActivity.class);
        startActivity(intent);
    }
}