package com.imperial.word2mouth.common.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.create.CreateContentActivity;

public class CourseNameDialog extends AppCompatDialogFragment {

    private String courseName ;

    private final AppCompatActivity activity;

    public CourseNameDialog(AppCompatActivity activity) {
        this.activity = activity;
        this.courseName = new String();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.course_name);
        final EditText input = new EditText(getActivity());
        input.setHint(R.string.type_course_name);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        builder.setView(input);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                CourseNameDialog.this.courseName = input.getText().toString();
                if ( CourseNameDialog.this.courseName.isEmpty()) {
                    CourseNameDialog.this.courseName  = "Untitled Course";
                }
                CreateContentActivity createContentActivity = (CreateContentActivity) CourseNameDialog.this.activity;

                createContentActivity.setCourseName(courseName);

                LanguageDialog languageDialog = new LanguageDialog(CourseNameDialog.this.activity);
                languageDialog.show(CourseNameDialog.this.activity.getSupportFragmentManager(), getString(R.string.language_selection));
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

}
