package com.imperial.slidepassertrial;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class VideoDialog extends DialogFragment {

    public interface OnInputListener {
        void sendInput(int choice);
    }

    public OnInputListener onInputListener;
    // widgets
    private Button gallery;
    private Button roll;


    private final int MAX_DURATION_VIDEO = 10;

    // Intent Results
    public final int CAMERA_ROLL_SELECTION = 0;
    public final int GALLERY_SELECTION = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_video_chooser, container, false);

        gallery = view.findViewById(R.id.d_gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(view.getContext(), "Fetching from the gallery", Toast.LENGTH_SHORT).show();

                onInputListener.sendInput(GALLERY_SELECTION);
                getDialog().dismiss();

//                Intent galleryIntent = new Intent();
//                galleryIntent.setType("video/*");
//                galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//                galleryIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, MAX_DURATION_VIDEO);
//                startActivityForResult(Intent.createChooser(galleryIntent,"Select Video"), GALLERY_SELECTION);
            }
        });

        roll = view.findViewById(R.id.d_roll);
        roll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(view.getContext(),"Fetching from the camera roll", Toast.LENGTH_SHORT).show();

                onInputListener.sendInput(CAMERA_ROLL_SELECTION);
                getDialog().dismiss();
//                Intent rollIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//                rollIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, MAX_DURATION_VIDEO);
//                startActivityForResult(rollIntent, CAMERA_ROLL_SELECTION);
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            onInputListener = (OnInputListener) getActivity();
        } catch(ClassCastException e) {

        }
    }
}
