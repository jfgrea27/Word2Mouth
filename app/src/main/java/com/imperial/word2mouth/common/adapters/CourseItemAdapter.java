package com.imperial.word2mouth.common.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.common.Categories;
import com.imperial.word2mouth.common.Languages;
import com.imperial.word2mouth.create.CourseSummaryCreateActivity;
import com.imperial.word2mouth.model.CourseItem;

import java.io.File;
import java.util.ArrayList;

public class CourseItemAdapter extends RecyclerView.Adapter<CourseItemAdapter.ViewHolder> {

    private ArrayList<CourseItem> courseItems;
    private MediaPlayer player;
    private Activity activity;

    public CourseItemAdapter(ArrayList<CourseItem> courseItemList, Activity activity) {
        courseItems = courseItemList;
        this.activity = activity;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_view_row_course_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.getCourseName().setText(courseItems.get(position).getCourseName());
        holder.getPhotoThumbnail().setImageURI(Uri.fromFile(new File(courseItems.get(position).getCourseImageThumbnailPath())));
        holder.getLanguageImage().setImageResource(Languages.getLanguageIcon(courseItems.get(position).getCourseLanguage()));
        holder.getCategoryImage().setImageResource(Categories.getCategory(courseItems.get(position).getCourseTopic()));

        holder.getSoundThumbnail().setOnClickListener(v -> {
            Uri soundURI = Uri.fromFile(new File(courseItems.get(position).getCourseAudioThumbnailPath()));
            if (soundURI != null) {
                holder.getSoundThumbnail().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                player = MediaPlayer.create(activity.getApplicationContext(), soundURI);
                if (player != null) {
                    player.start();
                    player.setOnCompletionListener(mp -> holder.getSoundThumbnail().setColorFilter(null));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView courseName;
        private final ImageButton photoThumbnail;
        private final ImageButton soundThumbnail;
        private final ImageView languageImage;
        private final ImageView categoryImage;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            photoThumbnail = v.findViewById(R.id.courseThumbnailButton);
            soundThumbnail = v.findViewById(R.id.courseSoundThumbnailButton);
            languageImage = v.findViewById(R.id.languageImage);
            categoryImage = v.findViewById(R.id.categoryImage);
            courseName = v.findViewById(R.id.courseTitle);

        }


        public TextView getCourseName() {
            return courseName;
        }

        public ImageButton getPhotoThumbnail() {
            return photoThumbnail;
        }

        public ImageButton getSoundThumbnail() {
            return soundThumbnail;
        }

        public ImageView getLanguageImage() {
            return languageImage;
        }

        public ImageView getCategoryImage() {
            return categoryImage;
        }


    }


}
