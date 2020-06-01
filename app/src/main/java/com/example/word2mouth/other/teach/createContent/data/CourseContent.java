package com.example.word2mouth.other.teach.createContent.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class CourseContent implements Parcelable {

    ArrayList<SlideContent> slideContents = new ArrayList<>();

    private final String coursePath;

    public String getCoursePath() {
        return coursePath;
    }

    public CourseContent(String coursePath) {
        this.coursePath = coursePath;
    }

    public int getNumberSlides() {
        return slideContents.size();
    }


    public void addSlide(SlideContent slideContent) {
        slideContents.add(slideContent);
    }

    public boolean isLastSlide(SlideContent slideContent) {
        if(slideContents.get(slideContents.size() - 1).getSlidePath() == slideContent.getSlidePath()) {
            return true;
        }
        return false;
    }

    // Parceable
    protected CourseContent(Parcel in) {
        this.slideContents = new ArrayList<SlideContent>();
        in.readList(this.slideContents, SlideContent.class.getClassLoader());
        this.coursePath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.slideContents);
        dest.writeString(this.coursePath);
    }

    public static final Parcelable.Creator<CourseContent> CREATOR = new Parcelable.Creator<CourseContent>() {
        @Override
        public CourseContent createFromParcel(Parcel source) {
            return new CourseContent(source);
        }

        @Override
        public CourseContent[] newArray(int size) {
            return new CourseContent[size];
        }
    };
}
