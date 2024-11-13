package com.rusher.lightly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LightMapActivity extends AppCompatActivity {

    private com.google.android.gms.location.LocationRequest locationRequest;


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    byte[] gen_image_bytes;

    Intent processingIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_map);
        processingIntent = new Intent(LightMapActivity.this, DipProcessActivity.class);

        if(OpenCVLoader.initLocal()){
            //System.loadLibrary("opencv_java490");
            Log.d("opencv","Install successful");
        }else{
            Log.d("opencv","Install failed");
        }

        //Initialize the elements
        ImageView sat_image = findViewById(R.id.sat_image);
        TextView textLevel = findViewById(R.id.text_level);
        ProgressBar progressBar = findViewById(R.id.light_map_progressBar);
        Log.d("MainActivity", "This is a debug message");

        //Tile Zoom index
        int zoom = 7;

        locationRequest = new com.google.android.gms.location.LocationRequest.Builder(5000).setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(2000).build();


        // Check for location permissions
        if (checkLocationPermission()) {
            // Show the progress bar while fetching location and image
            progressBar.setVisibility(View.VISIBLE);
            gen_image_bytes = getCurrentLocation(progressBar, sat_image, textLevel, zoom);

        } else {
            requestLocationPermission();
        }

    }

    // Check if the location permission is granted
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Request location permissions
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }


    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        ImageView sat_image = findViewById(R.id.sat_image);
        TextView textLevel = findViewById(R.id.text_level);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Show the progress bar before fetching location
                progressBar.setVisibility(View.VISIBLE);

                // Now you can start fetching the location and image
                gen_image_bytes = getCurrentLocation(progressBar, sat_image, textLevel, 7);
            } else {
                // Permission was denied, hide the progress bar
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @SuppressLint("MissingPermission")
    private byte[] getCurrentLocation(ProgressBar progressBar, ImageView satImage, TextView textLevel, int zoom) {
        double[] coordinates = new double[2];
        final byte[][] rawImageBytes = new byte[1][1];

        if (isGPSEnabled()) {
            LocationServices.getFusedLocationProviderClient(LightMapActivity.this)
                    .requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);

                            LocationServices.getFusedLocationProviderClient(LightMapActivity.this)
                                    .removeLocationUpdates(this);

                            if (!locationResult.getLocations().isEmpty()) {
                                int index = locationResult.getLocations().size() - 1;
                                double latitude = locationResult.getLocations().get(index).getLatitude();
                                double longitude = locationResult.getLocations().get(index).getLongitude();

                                coordinates[0] = latitude;
                                coordinates[1] = longitude;

                                // Fetch image using coordinates
                                rawImageBytes[0] =fetchImage(progressBar, satImage, textLevel, coordinates, zoom);

                                Toast.makeText(LightMapActivity.this, "Location: " + coordinates[0] + ", " + coordinates[1], Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, Looper.getMainLooper());

        } else {
            turnOnGPS();
            getCurrentLocation(progressBar, satImage, textLevel, zoom); // Retry after enabling GPS
        }

        return rawImageBytes[0];
    }


    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(LightMapActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(LightMapActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }


    //Useful methods


    // Function to get the resolution per pixel based on zoom level (only for zoom level 7 or less)
    private double getResolutionPerPixel(int zoomLevel) {
        // Ensure zoom level is 7 or less
        if (zoomLevel > 7 || zoomLevel < 0) {
            throw new IllegalArgumentException("Zoom level must be 7 or less.");
        }

        // Resolution per pixel for zoom levels 0 to 7
        switch (zoomLevel) {
            case 7:
                return 0.0087890625;  // 1 km resolution
            case 6:
                return 0.017578125;   // 2 km resolution
            case 5:
                return 0.03515625;    // 4 km resolution
            case 4:
                return 0.0703125;     // 8 km resolution
            case 3:
                return 0.140625;      // 16 km resolution
            case 2:
                return 0.28125;       // 32 km resolution
            case 1:
                return 0.5625;        // 64 km resolution
            case 0:
                return 1.125;         // 128 km resolution
            default:
                throw new IllegalArgumentException("Unsupported zoom level.");
        }
    }

    private int latToRow(double lat,int zoom){
        // Get the resolution per pixel for the zoom level (7 or less)
        double tileResolution = getResolutionPerPixel(zoom);

        // Tile size in pixels (256x256)
        int tileSize = 256;
        // Tile height in degrees
        double tileHeightDegrees = tileResolution * tileSize;

        // Maximum latitude for EPSG:4326
        double northBound = 90.0;

        // Calculate the row (TileRow)
        double tileRow = (northBound - lat) / tileHeightDegrees;
        return (int) Math.floor(tileRow);
    }

    private int lonToCol(double lon,int zoom){
        // Get the resolution per pixel for the zoom level (7 or less)
        double tileResolution = getResolutionPerPixel(zoom);

        // Tile size in pixels (256x256)
        int tileSize = 256;
        // Tile width in degrees
        double tileWidthDegrees = tileResolution * tileSize;

        // Minimum longitude for EPSG:4326
        double westBound = -180.0;

        // Calculate the column (TileCol)
        double tileCol = (lon - westBound) / tileWidthDegrees;
        return (int) Math.floor(tileCol);
    }


    private String getPreviousDateString(int numOfDay){
        Date currentDate = new Date();

        // Create a Calendar instance and set it to the current date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Subtract 3 day from the current date
        calendar.add(Calendar.DAY_OF_YEAR, -numOfDay);

        // Get the previous date as a Date object
        Date previousDate = calendar.getTime();

        // Format the previous date as a string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(previousDate);
    }


    private byte[] fetchImage(ProgressBar progressBar, ImageView satImage, TextView textLevel, double[] coordinates, int zoom) {
        int row = latToRow(coordinates[0], zoom);
        int col = lonToCol(coordinates[1], zoom);
        final byte[][] rawImageBytes = new byte[1][1];
        Toast.makeText(this, "Row: "+row+" Col: "+col, Toast.LENGTH_SHORT).show();
        int numOfDays=3;
        String previousDateString = getPreviousDateString(numOfDays);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gibs.earthdata.nasa.gov/wmts/epsg4326/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        GIBSService lservices = retrofit.create(GIBSService.class);

        Call<ResponseBody> call = lservices.getImageData(
                "WMTS",
                "GetTile",
                "1.0.0",
                "VIIRS_SNPP_DayNightBand_At_Sensor_Radiance",
                "500m",
                Integer.toString(zoom),
                Integer.toString(col),
                Integer.toString(row),
                previousDateString,
                "default",
                "image/png"
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    InputStream inputStream = null;
                    ByteArrayOutputStream byteArrayOutputStream = null;
                    try {
                        inputStream = response.body().byteStream();
                        byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, bytesRead);
                        }
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        rawImageBytes[0] = imageBytes;
                        processingIntent.putExtra("image",imageBytes);


                        // Use DIP Operations
                        DipOperation dipOperation = new DipOperation();

                        // Step 1: Apply Median Filter
                        bitmap = dipOperation.applyMedianFilter(bitmap, 3);

                        // Step 2: Apply Gamma Correction
                        bitmap = dipOperation.applyGammaCorrection(bitmap, 0.8);

                        bitmap = dipOperation.applyContrastStretching(bitmap);

                        // Step 3: Apply Unsharp Masking
                        bitmap = dipOperation.applyUnsharpMask(bitmap);

                        // Step 4: Apply Thresholding
                        bitmap = dipOperation.applyThreshold(bitmap, 100, false);

                        // Step 5: Apply Dilation
                        bitmap = dipOperation.applyDilation(bitmap, 3);

                        // Update UI: Show image and hide progress bar
                        satImage.setImageBitmap(bitmap);  // Display the processed image

                        double brightnessLevel = calculateBrightness(bitmap);
                        String level = categorizeBrightness(brightnessLevel);
                        textLevel.setText(level);

                        // Hide the progress bar
                        progressBar.setVisibility(View.GONE);

                    } catch (IOException e) {
                        e.printStackTrace();
                        // Hide progress bar on failure
                        progressBar.setVisibility(View.GONE);
                    } finally {
                        try {
                            if (inputStream != null) inputStream.close();
                            if (byteArrayOutputStream != null) byteArrayOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // Handle error response, hide progress bar
                    progressBar.setVisibility(View.GONE);
                    Log.e("Image Load Error", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, Throwable t) {
                // Handle failure, hide progress bar
                progressBar.setVisibility(View.GONE);
                Log.e("API Call Failure", "Failed to load image", t);
                t.printStackTrace();
            }
        });

        return rawImageBytes[0];

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
        if (brightness < 5) {
            return "Low";
        } else if (brightness < 15) {
            return "Medium";
        } else {
            return "High";
        }
    }

    //Navigates to the other screen
    public void mapBrightSuggestion(View view) {
        Intent intent = new Intent(LightMapActivity.this, LightSuggestionActivity.class);
        startActivity(intent);
    }

    public void goBack(View view) {
        Intent intent = new Intent(LightMapActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void userPROFILE(View view) {
        Intent intent = new Intent(LightMapActivity.this, Profile.class);
        startActivity(intent);

    }

    public void showProcessing(View view) {
        startActivity(processingIntent);
    }
}