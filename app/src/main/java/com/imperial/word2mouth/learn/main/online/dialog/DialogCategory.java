package com.imperial.word2mouth.learn.main.online.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.learn.main.online.LearnSearchFingerCourseFragment;
import com.imperial.word2mouth.shared.Categories;
import com.imperial.word2mouth.teach.offline.TeachOfflineMainFragment;
import com.imperial.word2mouth.shared.adapters.ArrayAdapterCategories;

public class DialogCategory {


    public static final int QUERY = 0;
    public static final int CREATE = 1;


    private final View view;
    private Fragment fragment;

    public DialogCategory(View view, Fragment fragment) {
        this.view = view;
        this.fragment = fragment;
    }



    public void buildDialog(int c) {


        AlertDialog.Builder builder;
        final ListView categoryListView = new ListView(view.getContext());

        switch (c) {
            case QUERY:
                builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Category Selection");

                categoryListView.setAdapter(new ArrayAdapterCategories(view.getContext(), R.layout.list_categories, Categories.categories));

                builder.setView(categoryListView);

                categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        LearnSearchFingerCourseFragment f = (LearnSearchFingerCourseFragment) fragment;
                        f.setCategory(Categories.get(position));
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
                builder.setTitle("Category Selection");


                categoryListView.setAdapter(new ArrayAdapterCategories(view.getContext(), R.layout.list_categories, Categories.categories));

                builder.setView(categoryListView);

                categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TeachOfflineMainFragment f = (TeachOfflineMainFragment) fragment;

                        f.setSelectedCategory(Categories.get(position));
                    }
                });

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TeachOfflineMainFragment f = (TeachOfflineMainFragment) fragment;

                        if ( f.getCategorySelected() != null) {
                            f.createCourse();

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
