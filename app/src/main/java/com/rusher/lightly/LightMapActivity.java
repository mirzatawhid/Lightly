package com.rusher.lightly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import org.osgeo.proj4j.*;


public class LightMapActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    public double lat; // Latitude in degrees
    public double lon; // Longitude in degrees
    public static final int REQUEST_CODE = 100;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_map);


        ImageView sat_image = (ImageView) findViewById(R.id.sat_image);
        TextView textLevel = findViewById(R.id.text_level);
        Log.d("MainActivity", "This is a debug message");


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE);
        } else {
            requestLocation();
        }

        int zoom = 6;

        int row = (int) (((90 - lat) * Math.pow(2, zoom)) / 288);
        int col = (int) (((180 + lon) * Math.pow(2, zoom)) / 288);


        Log.d("convert", "row: " + row);
        Log.d("convert", "Col: " + col);
        Log.d("convert", "lat: " + lat);
        Log.d("convert", "lon: " + lon);

        //Retrofit set
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://gibs.earthdata.nasa.gov/wmts/epsg4326/").addConverterFactory(GsonConverterFactory.create()).build();
        GIBSService services = retrofit.create(GIBSService.class);

        Call<ResponseBody> call = services.getImageData(
                "WMTS",
                "GetTile",
                "1.0.0",
                "VIIRS_SNPP_DayNightBand_At_Sensor_Radiance",
                "500m",
                Integer.toString(zoom),
                Integer.toString(col),
                Integer.toString(row),
                "2023-07-09",
                "default",
                "image/png"
        );
        String requestUrl = call.request().url().toString();
        Log.d("API Request", "URL: " + requestUrl);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        // Load the image using Picasso
                        Log.d("API Request", "URL: suiii" + response.body());
                        try {
                            // Convert the response body InputStream to a byte array
                            InputStream inputStream = response.body().byteStream();
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                byteArrayOutputStream.write(buffer, 0, bytesRead);
                            }
                            byte[] imageBytes = byteArrayOutputStream.toByteArray();

                            // Load the byte array into Picasso
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                            // Load the Bitmap into Picasso and display it in your ImageView
                            sat_image.setImageBitmap(bitmap);
                            //return level
                            double brightnessLevel = calculateBrightness(bitmap);
                            String level = categorizeBrightness(brightnessLevel);
                            textLevel.setText(level);

                        } catch (IOException e) {
                            // Handle the exception
                            e.printStackTrace();
                        }
                    } else {
                        // Handle the case when the response body is null
                        Log.e("Image Load Error", "Response body is null");
                    }
                } else {
                    // Handle unsuccessful response (e.g., non-200 HTTP status code)
                    Log.e("Image Load Error", "Response code: " + response.code());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE);
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(LightMapActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            lat = location.getLatitude();
                            lon = location.getLongitude();
                            // Use latitude and longitude as needed
                            Toast.makeText(LightMapActivity.this, "Latitude: " + lat + ", Longitude: " + lon, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LightMapActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private double calculateBrightness(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        long totalBrightness = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = bitmap.getPixel(x, y);
                int brightness = Color.red(pixel); // Get the red component (since it's already black and white)

                totalBrightness += brightness;
            }
        }

        int numPixels = width * height;
        return totalBrightness / (double) numPixels;
    }

    private String categorizeBrightness(double brightness) {
        if (brightness < 20) {
            return "Low";
        } else if (brightness < 50) {
            return "Medium";
        } else {
            return "High";
        }
    }

}