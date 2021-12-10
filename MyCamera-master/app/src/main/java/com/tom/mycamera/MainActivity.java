package com.tom.mycamera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.print.PrintAttributes;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.camera.core.*;
import androidx.lifecycle.LifecycleOwner;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    private ImageCapture mImageCapture;
    private ImageAnalysis mImageAnalysis;

    private Button mButton;
    private PreviewView mPreviewView;
    private GraphicOverlayPreview mGraphicOverlayPreview;
    private ImageButton imageButton;
    private RadioGroup radioGroup;

    private ExecutorService cameraExecutor;

    private FaceDetectionImageAnalysis faceDetectionImageAnalysis;

    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] REQUIRED_PERMISSIONS = new String[] {"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static final String TAG = "CameraX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getSupportActionBar().hide();

        mPreviewView = (PreviewView) findViewById(R.id.viewFinder);
        mGraphicOverlayPreview = (GraphicOverlayPreview) findViewById(R.id.rect_overlay);
        imageButton = (ImageButton) findViewById((R.id.imageButton));
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);

        if (allPermissionGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        mButton = (Button) findViewById(R.id.camera_capture_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        cameraExecutor = Executors.newSingleThreadExecutor();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               selectImage();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int maskChosen;
                switch (checkedId) {
                    default:
                    case R.id.no_mask:
                        maskChosen = GraphicOverlayPreview.NO_MASK;
                        break;
                    case R.id.money_heist_mask:
                        maskChosen = GraphicOverlayPreview.MONEY_HEIST_MASK;
                        break;
                    case R.id.batman_mask:
                        maskChosen = GraphicOverlayPreview.BATMAN_MASK;
                        break;
                    case R.id.shrek_face:
                        maskChosen = GraphicOverlayPreview.SHREK_FACE;
                        break;
                    case R.id.deadpool_mask:
                        maskChosen = GraphicOverlayPreview.DEADPOOL_MASK;
                        break;
                }
                mGraphicOverlayPreview.setBitmap(maskChosen);
                if (maskChosen == GraphicOverlayPreview.BATMAN_MASK) {
                    faceDetectionImageAnalysis.setFraction(FaceDetectionImageAnalysis.FOR_BATMAN);
                } else {
                    faceDetectionImageAnalysis.setFraction(FaceDetectionImageAnalysis.FOR_OTHERS);
                }
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null) {
                Uri selectedImageUri = data.getData();
                Intent i = new Intent(this,PhotoGallery.class);
                i.putExtra("imagePath", selectedImageUri.toString());
                startActivity(i);
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    Preview preview = new Preview.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                            .build();
                    preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

                    CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                    mImageCapture = new ImageCapture.Builder().build();

                    mImageAnalysis = new ImageAnalysis.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();

                    mImageAnalysis.setAnalyzer(cameraExecutor, createFaceDetectionImageAnalysis());
//                    mImageAnalysis.setAnalyzer(cameraExecutor, new FaceDetectionImageAnalysis());

                    try {
                        cameraProvider.unbindAll();
                        cameraProvider.bindToLifecycle(((LifecycleOwner) MainActivity.this), cameraSelector, preview, mImageCapture, mImageAnalysis);
                    } catch (Exception e) {
                        Log.d(TAG, "Use case binding failed");
                    }

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void takePhoto() {
//        // Save the picture here: Environment.getExternalStoragePublicDirectory("DIRECTORY_PICTURES")

        ArrayList<RectF> arrayList = new ArrayList<>(mGraphicOverlayPreview.getList());
        Bitmap bitmap = mPreviewView.getBitmap();

        int x = mGraphicOverlayPreview.getWhichBitmap();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Date now = new Date();
                android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
                String fileName = now + ".jpg";

                OutputStream fos = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    ContentResolver contentResolver = getContentResolver();

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                    Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                    try {
                        fos = contentResolver.openOutputStream(imageUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    File imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    File image = new File (imageDirectory, fileName);
                    try {
                        fos = new FileOutputStream(image);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                Canvas canvas = new Canvas(bitmap);
                GraphicOverlayPicture graphicOverlayPicture
                        = new GraphicOverlayPicture(arrayList, x, MainActivity.this);
                graphicOverlayPicture.draw(canvas);

                Matrix matrix = new Matrix();
                matrix.postScale(-1, 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
                Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                newBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        Toast.makeText(getBaseContext(), "Photo Captured!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdownNow();
    }

    private FaceDetectionImageAnalysis createFaceDetectionImageAnalysis() {

        int height = mPreviewView.getHeight();
        int width = mPreviewView.getWidth();

        faceDetectionImageAnalysis =
                new FaceDetectionImageAnalysis(width, height);
        faceDetectionImageAnalysis.setOnFacesDetectedListener(new FaceDetectionImageAnalysis.onFacesDetectedListener() {
            @Override
            public void onFacesDetected(List<RectF> list) {
                mGraphicOverlayPreview.postRect(list);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });

        return faceDetectionImageAnalysis;
    }
}