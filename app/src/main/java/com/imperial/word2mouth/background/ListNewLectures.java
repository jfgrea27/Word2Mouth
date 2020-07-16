package com.imperial.word2mouth.background;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Map;

public class ListNewLectures implements Parcelable {

    public String courseUID;
    public ArrayList<String> lectures;

    public ListNewLectures(Map.Entry<String, ArrayList<String>> entry) {
        courseUID = entry.getKey();
        lectures = entry.getValue();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.courseUID);
        dest.writeStringList(this.lectures);
    }

    protected ListNewLectures(Parcel in) {
        this.courseUID = in.readString();
        this.lectures = in.createStringArrayList();
    }

    public static final Parcelable.Creator<ListNewLectures> CREATOR = new Parcelable.Creator<ListNewLectures>() {
        @Override
        public ListNewLectures createFromParcel(Parcel source) {
            return new ListNewLectures(source);
        }

        @Override
        public ListNewLectures[] newArray(int size) {
            return new ListNewLectures[size];
        }
    };
}
