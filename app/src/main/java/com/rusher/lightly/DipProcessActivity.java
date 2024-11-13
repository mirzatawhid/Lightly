package com.rusher.lightly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DipProcessActivity extends AppCompatActivity {

    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dip_process);

        ImageView medianImageView = findViewById(R.id.median_filtering_iv);
        ImageView gammaImageView = findViewById(R.id.gamma_correction_iv);
        ImageView contrastImageView = findViewById(R.id.contrast_stretching_iv);
        ImageView unsharpImageView = findViewById(R.id.unsharp_masking_iv);
        ImageView thresholdImageView = findViewById(R.id.thresholding_iv);
        ImageView dilationImageView = findViewById(R.id.dilation_iv);

        byte[] byteArray = getIntent().getByteArrayExtra("image");
        if (byteArray != null) {
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        }

        // Use DIP Operations
        DipOperation dipOperation = new DipOperation();

        // Step 1: Apply Median Filter
        bitmap = dipOperation.applyMedianFilter(bitmap, 3);
        medianImageView.setImageBitmap(bitmap);

        // Step 2: Apply Gamma Correction
        bitmap = dipOperation.applyGammaCorrection(bitmap, 0.8);
        gammaImageView.setImageBitmap(bitmap);

        // Step 3: Apply Contrast Stretching
        bitmap = dipOperation.applyContrastStretching(bitmap);
        contrastImageView.setImageBitmap(bitmap);

        // Step 4: Apply Unsharp Masking
        bitmap = dipOperation.applyUnsharpMask(bitmap);
        unsharpImageView.setImageBitmap(bitmap);

        // Step 5: Apply Thresholding
        bitmap = dipOperation.applyThreshold(bitmap, 100, false);
        thresholdImageView.setImageBitmap(bitmap);

        // Step 6: Apply Dilation
        bitmap = dipOperation.applyDilation(bitmap, 3);
        dilationImageView.setImageBitmap(bitmap);

    }

    public void goBack(View view) {
        Intent intent = new Intent(DipProcessActivity.this, LightMapActivity.class);
        startActivity(intent);
        finish();
    }
}