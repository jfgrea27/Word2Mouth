package com.example.word2mouth.other.teach.createContent.video;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.word2mouth.R;
import com.example.word2mouth.other.OtherActivity;
import com.example.word2mouth.other.SelectionMediaActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.BitSet;

public class VideoSelectionActivity extends SelectionMediaActivity {

    private static final int RESULT_CHOOSE_VIDEO_GALLERY = 1;
    private static final int RESULT_TAKE_VIDEO_CAMERA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_selection);
        configureBackButton();

        configureCaptureButton();
        configureGalleryButton();
    }

    @Override
    protected void configureCaptureButton() {
        captureButton = findViewById(R.id.button_camera);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraRollIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                cameraRollIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                startActivityForResult(cameraRollIntent, RESULT_TAKE_VIDEO_CAMERA);
            }
        });

    }

    @Override
    protected void configureGalleryButton() {
        galleryButton = findViewById(R.id.button_gallery);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("video/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Video"),RESULT_CHOOSE_VIDEO_GALLERY);
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Intent intentSelectedImage = new Intent();
            switch (requestCode) {
                case (RESULT_TAKE_VIDEO_CAMERA):
                case (RESULT_CHOOSE_VIDEO_GALLERY):
                    intentSelectedImage.putExtra("video", data.getData());
                    setResult(RESULT_OK, intentSelectedImage);
                    finish();
                    break;

            }
        }
    }

}
