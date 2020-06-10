package com.imperial.slidepassertrial.teach.offline.create.video;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.imperial.slidepassertrial.R;

public class ImageDialog extends DialogFragment {

    public interface OnInputListener {
        void sendInput(int choice);
    }
    public OnInputListener onInputListener;

    // Widgets
    private ImageButton gallery;
    private ImageButton roll;


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
            }
        });

        roll = view.findViewById(R.id.d_roll);
        roll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(view.getContext(),"Fetching from the camera roll", Toast.LENGTH_SHORT).show();

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
