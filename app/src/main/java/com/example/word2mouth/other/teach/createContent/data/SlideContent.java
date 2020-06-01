package com.example.word2mouth.other.teach.createContent.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.word2mouth.utilities.video.VideoHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SlideContent implements Parcelable {

    // Data parameters

    private String videoFilePath;

    // Address parameters
    private String slidePath;
    private int slideNumber;
    private final String coursePath;
    // constuctor

    public SlideContent(String cPath, int num, String sPath) {
        coursePath = cPath;
        slideNumber = num;
        slidePath = "/" + slideNumber + ". " + sPath;
        videoFilePath = coursePath + slidePath + "/video.3gp";
    }

    // getters

    public String getSlidePath() {
        return slidePath;
    }


    // Video Handling

    public void setVideoFilePath(InputStream in ) throws IOException {
        VideoHandler videoHandler = new VideoHandler(videoFilePath);
        videoHandler.copyVideo(in);
    }
    public String getVideoFilePath() {
        return videoFilePath;
    }

    // Audio Handling

    // Text Handling


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.videoFilePath);
        dest.writeString(this.slidePath);
        dest.writeInt(this.slideNumber);
        dest.writeString(this.coursePath);
    }

    protected SlideContent(Parcel in) {
        this.videoFilePath = in.readString();
        this.slidePath = in.readString();
        this.slideNumber = in.readInt();
        this.coursePath = in.readString();
    }

    public static final Creator<SlideContent> CREATOR = new Creator<SlideContent>() {
        @Override
        public SlideContent createFromParcel(Parcel source) {
            return new SlideContent(source);
        }

        @Override
        public SlideContent[] newArray(int size) {
            return new SlideContent[size];
        }
    };
}
