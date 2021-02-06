package com.imperial.word2mouth.previous.main.offline.tracker;

public class SlideTracker {

    private long timeSpent = 0;
    private int soundCounter;
    private int videoCounter;



    public long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent + this.timeSpent;
    }

    public int getSoundCounter() {
        return soundCounter;
    }

    public void setSoundCounter(int soundCounter) {
        this.soundCounter = soundCounter + this.soundCounter;
    }

    public int getVideoCounter() {
        return videoCounter;
    }

    public void setVideoCounter(int videoCounter) {
        this.videoCounter = videoCounter + this.videoCounter;
    }

    public void updateEntries(long time, int videoCounter, int soundCounter) {
        timeSpent += time;
        this.soundCounter += soundCounter;
        this.videoCounter += videoCounter;
    }
}
