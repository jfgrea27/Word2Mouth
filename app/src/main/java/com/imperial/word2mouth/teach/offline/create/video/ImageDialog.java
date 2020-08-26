package com.imperial.word2mouth.teach.offline.create.video;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.imperial.word2mouth.R;

public class ImageDialog extends DialogFragment {


    public static final int PROFILE = 0;
    public static final int SLIDE = 1;
    public static final int THUMBNAIL =2;

    private final int code;

    public interface OnInputListener {
        void sendInput(int choice);
    }

    public OnInputListener onInputListener;



    public ImageDialog(int code) {
        this.code = code;
    }
    // Widgets
    private ImageButton gallery;
    private ImageButton roll;
    private TextView title;

    // Intent Results
    public final int CAMERA_ROLL_SELECTION = 0;
    public final int GALLERY_SELECTION = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        final View view = inflater.inflate(R.layout.dialog_video_chooser, container, false);
        title = view.findViewById(R.id.title);
        switch (code) {
            case PROFILE:
                title.setText(getString(R.string.profilePictureSelection));
                break;
            case THUMBNAIL:
                title.setText(R.string.thumbnailSelection);
                break;
            case SLIDE:
                title.setText(R.string.videoSelection);
                break;
        }

        gallery = view.findViewById(R.id.d_gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(view.getContext(), R.string.fetchingGallery, Toast.LENGTH_SHORT).show();

                onInputListener.sendInput(GALLERY_SELECTION);
                getDialog().dismiss();
            }
        });

        roll = view.findViewById(R.id.finger_chooser);
        roll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(view.getContext(), R.string.fetchingCameraRoll, Toast.LENGTH_SHORT).show();

                onInputListener.sendInput(CAMERA_ROLL_SELECTION);
                getDialog().dismiss();
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
