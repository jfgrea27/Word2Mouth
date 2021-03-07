package com.imperial.word2mouth.common.dialog;



import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.common.collect.Lists;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.model.Languages;
import com.imperial.word2mouth.common.adapters.LanguageAdapter;
import com.imperial.word2mouth.create.CreateContentActivity;

import java.util.ArrayList;

public class LanguageDialog extends AppCompatDialogFragment {

    private final AppCompatActivity activity;

    public LanguageDialog(AppCompatActivity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.course_name);
        final EditText input = new EditText(getActivity());
        input.setHint(R.string.type_course_name);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        builder.setView(input);

        ListView listLanguages = new ListView(getActivity());

        ArrayList<String> languages = Lists.newArrayList(Languages.languageIconMap.keySet());

        listLanguages.setAdapter(new LanguageAdapter(getActivity(), R.layout.list_language, languages));

        builder.setView(listLanguages);

        listLanguages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CreateContentActivity createContentActivity = (CreateContentActivity) LanguageDialog.this.activity;

                createContentActivity.setCourseLanguage(languages.get(position));
                CategoryDialog categoryDialog = new CategoryDialog(LanguageDialog.this.activity);
                categoryDialog.show(LanguageDialog.this.activity.getSupportFragmentManager(), getString(R.string.category_selection));
                LanguageDialog.this.dismiss();
            }
        });

        return builder.create();
    }
}
