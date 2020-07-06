package com.imperial.word2mouth.teach.offline.create;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.imperial.word2mouth.R;

import java.util.ArrayList;

public class ArrayAdapterCategories extends ArrayAdapter<String> {
    private static ArrayList<String> categories;
    private ViewHolder holder = null;
    private int layout;
    private Context context;

    public ArrayAdapterCategories(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);

        layout = resource;
        categories = objects;
        this.context = context;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        ArrayAdapterLanguage.ViewHolder mainViewHolder = null;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);

            holder = new ViewHolder();

            holder.category= convertView.findViewById(R.id.list_item_text);



            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.category.setText(categories.get(position));

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
        TextView category;
        ImageButton categoryThumbnail;
    }

}
