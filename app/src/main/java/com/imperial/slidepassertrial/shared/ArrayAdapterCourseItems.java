package com.imperial.slidepassertrial.shared;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.imperial.slidepassertrial.R;

import java.util.List;

public class ArrayAdapterCourseItems extends ArrayAdapter<String> {

    private List<String> courseItems;

    private int layout;
    public ArrayAdapterCourseItems(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);

        layout = resource;
        courseItems = objects;
    }




    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder mainViewHolder = null;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);

            ViewHolder viewhHolder = new ViewHolder();

            viewhHolder.thumbnail = convertView.findViewById(R.id.list_item_thumbnail);
            viewhHolder.title = convertView.findViewById(R.id.list_item_text);
            viewhHolder.audio = convertView.findViewById(R.id.list_item_button);
            convertView.setTag(viewhHolder);
        }
        mainViewHolder = (ViewHolder) convertView.getTag();
        mainViewHolder.audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Audio for the Course", Toast.LENGTH_SHORT).show();
            }
        });
        mainViewHolder.title.setText(getItem(position));
        return convertView;

    }


    public class ViewHolder {
        ImageView thumbnail;
        TextView title;
        ImageButton audio;

    }

}
