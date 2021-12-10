package com.tom.mycamera;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.objects.DetectedObject;
import java.util.ArrayList;
import java.util.List;

public class FaceDetectionImageAnalysis implements ImageAnalysis.Analyzer {
    private final FaceDetector mFaceDetector;

    private onFacesDetectedListener listener;

    private int previewWidth;
    private int previewHeight;

    private float topFraction;
    private float botFraction;


    public static final int FOR_OTHERS = 1;
    public static final int FOR_BATMAN = 2;
    private static final String TAG = "ObjectDetectionIA";

    public interface onFacesDetectedListener {
        void onFacesDetected(List<RectF> list);
        void onError(Exception e);
    }

    public void setOnFacesDetectedListener(onFacesDetectedListener listener) {
        this.listener = listener;
    }

    public FaceDetectionImageAnalysis(int previewWidth, int previewHeight) {
        FaceDetectorOptions faceDetectorOptions = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .build();

        mFaceDetector = FaceDetection.getClient(faceDetectorOptions);

        this.previewHeight = previewHeight;
        this.previewWidth = previewWidth;
    }

    @Override
    public void analyze(ImageProxy image) {
        @SuppressLint("UnsafeOptInUsageError") Image mediaImage = image.getImage();
        if (mediaImage != null) {
            int rotation = image.getImageInfo().getRotationDegrees();
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, rotation);
            mFaceDetector.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                        @Override
                        public void onSuccess(List<Face> faces) {
                            ArrayList<RectF> list = new ArrayList<>();
                            for (Face detectedObject : faces) {
                                if (rotation == 90 || rotation == 270) {
                                    list.add(transform(image.getHeight(), image.getWidth(), detectedObject.getBoundingBox()));
                                } else {
                                    list.add(transform(image.getWidth(), image.getHeight(), detectedObject.getBoundingBox()));
                                }
                            }
                            listener.onFacesDetected(list);
                            image.close();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            e.printStackTrace();
                            image.close();
                        }
                    });
        }
    }

    private RectF transform(int width, int height, Rect rect) {

        float scaleX = previewWidth / (float) width;
        float scaleY = previewHeight / (float) height;

        float flippedLeft = width - rect.right;
        float flippedRight = width - rect.left;

        float scaledLeft = scaleX * flippedLeft;
        float scaledTop = scaleY * rect.top - (topFraction * rect.height() * scaleY);
        float scaledRight = scaleX * flippedRight;
        float scaledBottom = scaleY * rect.bottom + (botFraction * rect.height() * scaleY);

        return new RectF(scaledLeft, scaledTop, scaledRight, scaledBottom);
    }

    public void setFraction (int face) {
        if (face == FOR_OTHERS) {
            topFraction = (float)1/5;
            botFraction = (float)1/6;
        } else if (face == FOR_BATMAN) {
            topFraction = (float)2/7;
            botFraction = (float)1/6;
        }
    }

}
