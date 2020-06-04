package com.imperial.slidepassertrial;

public class Slide implements SlideInterface {


    private final String courseName;
    private final String slideName;
    private final int slideNumber;


    public Slide(String courseName, String slideName, int slideNumber) {
        this.courseName = courseName;
        this.slideName = slideName;
        this.slideNumber = slideNumber;
    }

    @Override
    public String getSlideName() {
        return slideName;
    }


    @Override
    public int getSlideNumber() {
        return slideNumber;
    }

}
