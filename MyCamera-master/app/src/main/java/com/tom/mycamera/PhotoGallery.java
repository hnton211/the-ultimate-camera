package com.tom.mycamera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

//import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
//import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.google.android.material.appbar.AppBarLayout;

import java.io.InputStream;

public class PhotoGallery extends AppCompatActivity {
    private ImageView imageView;
    private Intent getImage;
    private String image_path;
    private Uri fileUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        imageView = findViewById(R.id.imageView);
        getImage = getIntent();
        image_path = getIntent().getStringExtra("imagePath");
        fileUri = Uri.parse(image_path);
        imageView.setImageURI(fileUri);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_trial();
            }
        });

    }
    public void edit_trial() {
        Intent dsPhotoEditorIntent = new Intent(this, DsPhotoEditorActivity.class);
        dsPhotoEditorIntent.setData(fileUri);
        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY,
                "Ultimate Photo Directory");
        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_TOOL_BAR_BACKGROUND_COLOR,
                Color.parseColor("#39477a"));

        int[] toolsToHide = {DsPhotoEditorActivity.TOOL_ORIENTATION, DsPhotoEditorActivity.TOOL_CROP};

        startActivityForResult(dsPhotoEditorIntent, 2);
    }

}