package com.imperial.word2mouth.common.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.common.collect.Lists;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.common.adapters.CategoryAdapter;
import com.imperial.word2mouth.create.CreateContentActivity;
import com.imperial.word2mouth.model.Categories;

import java.util.ArrayList;

public class CategoryDialog extends AppCompatDialogFragment {

    private final AppCompatActivity activity;

    public CategoryDialog(AppCompatActivity activity) {
        super();
        this.activity = activity;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.category_selection);

        ListView categoryListView = new ListView(getActivity());

        ArrayList<String> categories = Lists.newArrayList(Categories.categoryIconMap.keySet());
        categoryListView.setAdapter(new CategoryAdapter(getActivity(), R.layout.list_categories, categories));

        builder.setView(categoryListView);

        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CreateContentActivity createContentActivity = (CreateContentActivity) CategoryDialog.this.activity;

                createContentActivity.setCourseTopic(categories.get(position));
                CategoryDialog.this.dismiss();

                createContentActivity.createCourse();
            }
        });

        return builder.create();
    }
}
