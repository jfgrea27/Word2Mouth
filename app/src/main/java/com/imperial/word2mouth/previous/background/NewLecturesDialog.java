package com.imperial.word2mouth.previous.background;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.imperial.word2mouth.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class NewLecturesDialog extends DialogFragment {


    private final Map<String, ArrayList<String>> newLectures;

    // Widgets
    private ImageButton cancel;
    private ImageButton checkOutNewContentButton;

    public NewLecturesDialog(Map<String, ArrayList<String>> newLectures) {
        this.newLectures = newLectures;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        final View view = inflater.inflate(R.layout.dialog_new_content, container, false);


        cancel = view.findViewById(R.id.dismiss);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(view.getContext(), getString(R.string.dimissingNotification), Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
            }
        });


        checkOutNewContentButton = view.findViewById(R.id.getContent);
        checkOutNewContentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(view.getContext(), getString(R.string.retrieveContentFirebase), Toast.LENGTH_SHORT).show();
                getDialog().dismiss();

                FragmentManager manager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                LearnOnlineNewLecturesSelectionFragment frag;
                ArrayList<ListNewLectures> temp = getParceableLectures(newLectures);
                frag = LearnOnlineNewLecturesSelectionFragment.newInstance(temp);
                manager.beginTransaction().replace( R.id.fragment_chooser_query, frag).addToBackStack(null).commit();

            }
        });
        return view;

    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }


    private ArrayList<ListNewLectures> getParceableLectures(Map<String, ArrayList<String>> newLectures) {
        ArrayList<ListNewLectures> temp = new ArrayList<>();

        for (Map.Entry<String, ArrayList<String>> entry : newLectures.entrySet()) {
            temp.add(new ListNewLectures(entry));
        }

        return temp;
    }

}
