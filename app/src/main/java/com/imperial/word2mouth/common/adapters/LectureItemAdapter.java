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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.create.CourseSummaryCreateActivity;
import com.imperial.word2mouth.helpers.FileSystemHelper;
import com.imperial.word2mouth.model.LectureItem;

import java.io.File;
import java.util.ArrayList;

public class LectureItemAdapter  extends RecyclerView.Adapter<LectureItemAdapter.ViewHolder>  {



    private ArrayList<LectureItem> lectureItems;
    private MediaPlayer player;
    private Activity activity;

    public LectureItemAdapter(ArrayList<LectureItem> lectureItemList, Activity activity) {
        lectureItems = lectureItemList;
        this.activity = activity;
    }



    @NonNull
    @Override
    public LectureItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_view_row_lecture_item, parent, false);

        return new LectureItemAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LectureItemAdapter.ViewHolder holder, int position) {

        holder.getLectureName().setText(lectureItems.get(position).getLectureName());
        holder.getPhotoThumbnail().setImageURI(Uri.fromFile(new File(lectureItems.get(position).getLectureImageThumbnailPath())));

        holder.getSoundThumbnail().setOnClickListener(v -> {
            Uri soundURI = Uri.fromFile(new File(lectureItems.get(position).getLectureAudioThumbnailPath()));
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
                File courseItem = new File(lectureItems.get(position).getLecturePath());
                FileSystemHelper.deleteRecursive(courseItem);
                lectureItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(newPosition, lectureItems.size());
            }
        });

    }

    @Override
    public int getItemCount() {
        return lectureItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView lectureName;
        private final ImageButton photoThumbnail;
        private final ImageButton soundThumbnail;
        private final ImageButton deleteCourseItem;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CourseSummaryCreateActivity activity = (CourseSummaryCreateActivity) LectureItemAdapter.this.activity;
                    activity.enterLecture(lectureItems.get(getAdapterPosition()));
                }
            });
            photoThumbnail = v.findViewById(R.id.courseThumbnailButton);
            soundThumbnail = v.findViewById(R.id.courseSoundThumbnailButton);
            lectureName = v.findViewById(R.id.slideTitle);
            deleteCourseItem = v.findViewById(R.id.delete_button);
        }


        public TextView getLectureName() {
            return lectureName;
        }

        public ImageButton getPhotoThumbnail() {
            return photoThumbnail;
        }

        public ImageButton getSoundThumbnail() {
            return soundThumbnail;
        }

        public ImageButton getDeleteCourseItem() { return  deleteCourseItem; }

    }
}
