package com.imperial.word2mouth.common.adapters;

import android.app.Activity;
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
import com.imperial.word2mouth.model.SlideItem;

import java.io.File;
import java.util.ArrayList;

public class SlideItemAdapter extends RecyclerView.Adapter<SlideItemAdapter.ViewHolder> {



    private final ArrayList<SlideItem> slideItems;
    private final Activity activity;

    public SlideItemAdapter(ArrayList<SlideItem> slideList, Activity activity) {
        slideItems = slideList;
        this.activity = activity;
    }


    @NonNull
    @Override
    public SlideItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_view_row_slide_item, parent, false);

        return new SlideItemAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideItemAdapter.ViewHolder holder, int position) {

        holder.getSlideName().setText(slideItems.get(position).getSlideName());

        holder.getDeleteCourseItem().setOnClickListener(v -> {
            int newPosition = holder.getAdapterPosition();
            File courseItem = new File(slideItems.get(position).getSlideName());
            FileSystemHelper.deleteRecursive(courseItem);
            slideItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(newPosition, slideItems.size());
        });

    }

    @Override
    public int getItemCount() {
        return slideItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView slideName;
        private final ImageButton deleteCourseItem;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(v1 -> {
                CourseSummaryCreateActivity activity = (CourseSummaryCreateActivity) SlideItemAdapter.this.activity;
                activity.enterSlide(slideItems.get(getAdapterPosition()));
            });
            slideName = v.findViewById(R.id.slideTitle);
            deleteCourseItem = v.findViewById(R.id.delete_button);

        }


        public TextView getSlideName() {
            return slideName;
        }
        public ImageButton getDeleteCourseItem() { return  deleteCourseItem; }


    }
}
