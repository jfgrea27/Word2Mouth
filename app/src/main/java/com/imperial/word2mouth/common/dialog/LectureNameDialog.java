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
import com.imperial.word2mouth.create.CourseSummaryCreateActivity;

public class LectureNameDialog extends AppCompatDialogFragment {
    private final AppCompatActivity activity;

    private String lectureName;
    public LectureNameDialog(AppCompatActivity activity) {
        super();
        this.activity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.lecture_name);
        final EditText input = new EditText(getActivity());
        input.setHint(R.string.type_course_name);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        builder.setView(input);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                lectureName = input.getText().toString();
                if (lectureName.isEmpty()) {
                    lectureName = "Untitled Lecture";
                }


                CourseSummaryCreateActivity courseSummaryCreateActivity = (CourseSummaryCreateActivity) LectureNameDialog.this.activity;
                courseSummaryCreateActivity.setLectureName(lectureName);
                courseSummaryCreateActivity.createLecture();

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

