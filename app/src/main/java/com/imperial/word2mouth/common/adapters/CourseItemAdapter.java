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
import com.imperial.word2mouth.create.CreateContentActivity;
import com.imperial.word2mouth.model.Categories;
import com.imperial.word2mouth.model.Languages;
import com.imperial.word2mouth.helpers.FileSystemHelper;
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

        holder.getDeleteCourseItem().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newPosition = holder.getAdapterPosition();
                File courseItem = new File(courseItems.get(position).getCoursePath());
                FileSystemHelper.deleteRecursive(courseItem);
                courseItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(newPosition, courseItems.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView courseName;
        private final ImageButton photoThumbnail;
        private final ImageButton soundThumbnail;
        private final ImageView languageImage;
        private final ImageView categoryImage;
        private final ImageButton deleteCourseItem;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CreateContentActivity activity = (CreateContentActivity) CourseItemAdapter.this.activity;
                    activity.enterCourse(courseItems.get(getAdapterPosition()));
                }
            });
            photoThumbnail = v.findViewById(R.id.courseThumbnailButton);
            soundThumbnail = v.findViewById(R.id.courseSoundThumbnailButton);
            languageImage = v.findViewById(R.id.languageImage);
            categoryImage = v.findViewById(R.id.categoryImage);
            courseName = v.findViewById(R.id.courseTitle);
            deleteCourseItem = v.findViewById(R.id.delete_button);
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

        public ImageButton getDeleteCourseItem() { return  deleteCourseItem; }

    }


}
