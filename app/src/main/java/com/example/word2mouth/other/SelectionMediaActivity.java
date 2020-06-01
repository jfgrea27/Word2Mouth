package com.example.word2mouth.other;

import android.os.Bundle;
import android.widget.Button;

public abstract class SelectionMediaActivity extends OtherActivity {

    protected Button galleryButton;
    protected Button captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected abstract void configureCaptureButton();
    protected abstract void configureGalleryButton();
}
