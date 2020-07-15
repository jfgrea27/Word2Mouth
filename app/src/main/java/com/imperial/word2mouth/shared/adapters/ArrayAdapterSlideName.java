package com.imperial.word2mouth.shared.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.imperial.word2mouth.R;

import java.io.File;
import java.util.ArrayList;

public class ArrayAdapterSlideName extends ArrayAdapter<String> {

    private static ArrayList<String> slideNames;
    private ViewHolder holder = null;
    private int layout;
    private Context context;
    public ArrayAdapterSlideName(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);

        layout = resource;
        slideNames = objects;
        this.context = context;
    }



    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        ArrayAdapterSlideName.ViewHolder mainViewHolder = null;

        File thumbnailFile;
        final File audioFile;
        Uri audioUri = null;


        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);

            holder = new ArrayAdapterSlideName.ViewHolder();

            holder.slideName = convertView.findViewById(R.id.list_item_text);


            convertView.setTag(holder);
        } else {
            holder = (ArrayAdapterSlideName.ViewHolder) convertView.getTag();
        }

        holder.slideName.setText(position + 1 + ". " + slideNames.get(position));

        return convertView;
    }


    @Override
    public int getViewTypeCount() {

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    public class ViewHolder {
        TextView slideName;
    }

}
