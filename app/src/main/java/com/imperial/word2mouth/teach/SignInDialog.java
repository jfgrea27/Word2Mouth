package com.imperial.word2mouth.teach;

import android.app.Dialog;
import android.content.Intent;
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
import com.imperial.word2mouth.background.LearnOnlineNewLecturesSelectionFragment;
import com.imperial.word2mouth.background.ListNewLectures;
import com.imperial.word2mouth.teach.online.account.TeachLoginActivity;

import java.util.ArrayList;

public class SignInDialog  extends DialogFragment {

    private ImageButton cancel;
    private ImageButton signIn;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        final View view = inflater.inflate(R.layout.dialog_user_signin, container, false);


        cancel = view.findViewById(R.id.dismiss);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        signIn = view.findViewById(R.id.signIn);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activity = new Intent(getActivity(), TeachLoginActivity.class);
                startActivity(activity);
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


}
