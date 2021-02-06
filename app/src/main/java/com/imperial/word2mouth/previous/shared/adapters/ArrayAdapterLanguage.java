package com.imperial.word2mouth.previous.shared.adapters;

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
import com.imperial.word2mouth.previous.shared.Languages;

import java.util.ArrayList;

public class ArrayAdapterLanguage extends ArrayAdapter<String> {

    private Languages languagesData = new Languages();
    private static ArrayList<String> languages;
    private ViewHolder holder = null;
    private int layout;
    private Context context;

    public ArrayAdapterLanguage(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);

        layout = resource;
        languages = objects;
        this.context = context;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        ViewHolder mainViewHolder = null;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);

            holder = new ViewHolder();

            holder.language = convertView.findViewById(R.id.list_item_text);
            holder.languageThumbnail = convertView.findViewById(R.id.list_item_thumbnail);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.language.setText(languages.get(position));

        holder.languageThumbnail.setImageResource(languagesData.languageIconMap.get(languages.get(position)));
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
        TextView language;
        ImageButton languageThumbnail;
    }

}
