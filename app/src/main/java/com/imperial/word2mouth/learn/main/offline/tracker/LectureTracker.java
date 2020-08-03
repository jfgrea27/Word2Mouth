package com.imperial.word2mouth.learn.main.offline.tracker;

import java.util.ArrayList;

public class LectureTracker {

    public ArrayList<SlideTracker> slides = new ArrayList<>();
    private final String versionUID;

    public LectureTracker(String versionUID, int totalNumberSlides) {
        this.versionUID = versionUID;

        for (int counter = 0 ; counter < totalNumberSlides; counter++) {
            slides.add(new SlideTracker());
        }
    }

    public SlideTracker getSlideTracker(int slideCounter) {
        return slides.get(slideCounter);
    }
}
