package com.imperial.word2mouth.learn.main.online;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.imperial.word2mouth.R;

public class TeacherDialog extends DialogFragment{

    public interface OnInputListener {
        void sendInput(int choice);
    }
    public OnInputListener onInputListener;

    // Widgets
    private ImageButton search;
    private ImageButton contacts;


    // Intent Results
    public static final int FOLLOWING = 0;
    public static final int TEACHER_SEARCH = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_contacts, container, false);

        search = view.findViewById(R.id.d_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInputListener.sendInput(TEACHER_SEARCH);
                getDialog().dismiss();
            }
        });

        contacts = view.findViewById(R.id.d_following);
        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInputListener.sendInput(FOLLOWING);
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            onInputListener = (OnInputListener) getActivity().getSupportFragmentManager().getFragments().get(0);
        } catch(ClassCastException e) {

        }
    }

}
