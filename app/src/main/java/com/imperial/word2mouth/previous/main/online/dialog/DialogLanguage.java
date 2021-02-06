package com.imperial.word2mouth.previous.main.online.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.previous.main.online.LearnSearchFingerCourseFragment;
import com.imperial.word2mouth.previous.shared.Languages;
import com.imperial.word2mouth.previous.teach.offline.TeachOfflineMainFragment;
import com.imperial.word2mouth.previous.shared.adapters.ArrayAdapterLanguage;

public class DialogLanguage {

    public static final int QUERY_FINGER = 0;
    public static final int CREATE = 1;
    public static final int QUERY_SPEAK = 2;

    private final View view;
    private Fragment fragment;

    private Languages languages = new Languages();

    public DialogLanguage(View view, Fragment fragment) {
        this.view = view;
        this.fragment = fragment;
    }




    public void buildDialog(int c) {
        AlertDialog.Builder builder;
        ListView listLanguages;
        switch (c) {
            case QUERY_FINGER:
                 builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Language Selection");

                listLanguages = new ListView(view.getContext());

                listLanguages.setAdapter(new ArrayAdapterLanguage(view.getContext(), R.layout.list_language, languages.languages));

                builder.setView(listLanguages);


                listLanguages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        LearnSearchFingerCourseFragment f = (LearnSearchFingerCourseFragment) fragment;
                        f.setLanguageTo(languages.get(position));
                    }
                });



                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                break;
            case CREATE:
                builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Language Selection");

                listLanguages = new ListView(view.getContext());

                listLanguages.setAdapter(new ArrayAdapterLanguage(view.getContext(), R.layout.list_language, languages.languages));

                builder.setView(listLanguages);

                listLanguages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TeachOfflineMainFragment f = (TeachOfflineMainFragment) fragment;

                        f.setLanguageSelected(languages.get(position));
                    }
                });

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TeachOfflineMainFragment f = (TeachOfflineMainFragment) fragment;

                        if (f.getLanguageSelected() != null) {
                            f.dialogCategorySelection();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                break;
            }

        }

}
