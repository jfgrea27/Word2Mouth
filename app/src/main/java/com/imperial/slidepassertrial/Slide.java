package com.imperial.slidepassertrial;

public class Slide implements SlideInterface {

    private final int slideNumber;


    public Slide(int slideNumber) {
        this.slideNumber = slideNumber;
    }

    @Override
    public int getSlideNumber() {
        return slideNumber;
    }

}
