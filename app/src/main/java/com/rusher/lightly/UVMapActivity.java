package com.rusher.lightly;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

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

public class UVMapActivity extends AppCompatActivity {

    public double lat=23.8103; // Latitude in degrees
    public double lon=90.4125;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uvmap);

        ImageView sat_image = (ImageView) findViewById(R.id.sat_image);
        TextView textLevel = findViewById(R.id.text_level);


        int zoom = 4;

        int row = (int) (((90 - lat) * Math.pow(2, zoom)) / 288);
        int col = (int) (((180 + lon) * Math.pow(2, zoom)) / 288);

        Date currentDate = new Date();

        // Create a Calendar instance and set it to the current date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Subtract one day from the current date
        calendar.add(Calendar.DAY_OF_YEAR, -3);

        // Get the previous date as a Date object
        Date previousDate = calendar.getTime();

        // Format the previous date as a string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String previousDateString = dateFormat.format(previousDate);


        //API Calling
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://gibs.earthdata.nasa.gov/wmts/epsg4326/").addConverterFactory(GsonConverterFactory.create()).build();
        GIBSService services = retrofit.create(GIBSService.class);

        Call<ResponseBody> call = services.getImageData(
                "WMTS",
                "GetTile",
                "1.0.0",
                "MLS_O3_46hPa_Day",
                "2km",
                Integer.toString(zoom),
                Integer.toString(col),
                Integer.toString(row),
                previousDateString,
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
//                            double brightnessLevel = calculateBrightness(bitmap);
//                            String level = categorizeBrightness(brightnessLevel);
//                            textLevel.setText(level);

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
}