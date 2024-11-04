package com.rusher.lightly;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class DipOperation {

    // Helper method to convert Bitmap to grayscale Mat if required
    private Mat toGrayscaleIfNeeded(Bitmap bitmap, boolean toGray) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        if (toGray) {
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        }
        return mat;
    }

    // Contrast Stretching
    public Bitmap applyContrastStretching(Bitmap bitmap) {
        Mat mat = toGrayscaleIfNeeded(bitmap, true);  // Convert to grayscale
        Core.MinMaxLocResult minMax = Core.minMaxLoc(mat);
        Core.normalize(mat, mat, 0, 255, Core.NORM_MINMAX);
        Bitmap resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);
        return resultBitmap;
    }

    // Gamma Correction
    public Bitmap applyGammaCorrection(Bitmap bitmap, double gamma) {
        Mat mat = toGrayscaleIfNeeded(bitmap, false);
        Mat lut = new Mat(1, 256, CvType.CV_8UC1);
        for (int i = 0; i < 256; i++) {
            lut.put(0, i, Math.pow(i / 255.0, gamma) * 255.0);
        }
        Core.LUT(mat, lut, mat);
        Bitmap resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);
        return resultBitmap;
    }

    // Unsharp Masking
    public Bitmap applyUnsharpMask(Bitmap bitmap) {
        Mat mat = toGrayscaleIfNeeded(bitmap, false);
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(mat, blurred, new Size(5, 5), 0);
        Core.addWeighted(mat, 1.5, blurred, -0.5, 0, mat);
        Bitmap resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);
        return resultBitmap;
    }

    // High Pass Filter
    public Bitmap applyHighPassFilter(Bitmap bitmap) {
        Mat mat = toGrayscaleIfNeeded(bitmap, false);
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(mat, blurred, new Size(9, 9), 0);
        Core.subtract(mat, blurred, mat);
        Bitmap resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);
        return resultBitmap;
    }

    // Median Filter
    public Bitmap applyMedianFilter(Bitmap bitmap, int kernelSize) {
        Mat mat = toGrayscaleIfNeeded(bitmap, false);
        Imgproc.medianBlur(mat, mat, kernelSize);
        Bitmap resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);
        return resultBitmap;
    }

    // Thresholding (Binary)
    public Bitmap applyThreshold(Bitmap bitmap, double thresholdValue, boolean useAdaptiveThreshold) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY); // Convert to grayscale

        if (useAdaptiveThreshold) {
            // Adaptive thresholding (mean or Gaussian method can be chosen)
            Imgproc.adaptiveThreshold(
                    mat, mat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2
            );
        } else {
            // Simple binary thresholding
            Imgproc.threshold(mat, mat, thresholdValue, 255, Imgproc.THRESH_BINARY);
        }

        Bitmap thresholdBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, thresholdBitmap);

        return thresholdBitmap;
    }


    // Dilation
    public Bitmap applyDilation(Bitmap bitmap, int kernelSize) {
        Mat mat = toGrayscaleIfNeeded(bitmap, false);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(kernelSize, kernelSize));
        Imgproc.dilate(mat, mat, kernel);
        Bitmap resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);
        return resultBitmap;
    }

    // Categorize Brightness
    public String calculateAndCategorizeBrightness(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int categoryLow = 0, categoryMedium = 0, categoryHigh = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = bitmap.getPixel(x, y);
                int intensity = (int) (0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel));
                if (intensity < 85) categoryLow++;
                else if (intensity < 170) categoryMedium++;
                else categoryHigh++;
            }
        }

        return (categoryLow > categoryMedium && categoryLow > categoryHigh) ? "Low" :
                (categoryMedium > categoryLow && categoryMedium > categoryHigh) ? "Medium" : "High";
    }
}