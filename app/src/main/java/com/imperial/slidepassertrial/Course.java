package com.imperial.slidepassertrial;

import java.util.ArrayList;

public class Course {

    private String courseName;

    private ArrayList<SlideInterface> slides = new ArrayList<>();

    public Course(String courseName) {
        this.courseName = courseName; }

    public String getCourseName() {
        return courseName;
    }

    public int size() {
        return slides.size();
    }

    public void addSlide(SlideInterface slide) {
        if (!(slide.getSlideNumber() < slides.size())) {
            slides.add(slide);
        }

    }

    public SlideInterface retrieveByPosition(int pos) {
        try {
            return slides.get(pos);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

}
