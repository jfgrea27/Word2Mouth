package com.imperial.word2mouth.teach.online;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.imperial.word2mouth.teach.online.courseData.CourseTrackingData;
import com.imperial.word2mouth.teach.online.lectureData.LectureTrackingData;
import com.imperial.word2mouth.teach.online.lectureData.TimeStampLectureData;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DataExtractor
{

    public static final int MOST = 0;
    public static final int LEAST = 1;

    private LectureTrackingData ltd;
    private  CourseTrackingData ctd;

    public DataExtractor(LectureTrackingData ltd) {
        this.ltd = ltd;
    }

    public DataExtractor(CourseTrackingData ctd) {this.ctd = ctd;}

    ///////////////////////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////////////////////////

    // Lecture
    public LineGraphSeries<DataPoint> getTimeSeries() {
        ArrayList<DataPoint> data = new ArrayList<>();

        ArrayList<TimeStampLectureData> dataTemp = ltd.getData();
        ArrayList<TimeStampLectureData> increasingData = new ArrayList<>();

        int sizeData = ltd.getData().size();
        for (int i = 0; i < sizeData; i++) {
            int index = i;
            long minCurPos = Long.valueOf(dataTemp.get(i).getTimeStamp());
            for (int j = i + 1; j < sizeData; j++) {
                long temp = Long.valueOf(dataTemp.get(j).getTimeStamp());
                if (minCurPos > temp ) {
                    index = j;
                    minCurPos = temp;
                }
            }
            increasingData.add(dataTemp.get(index));
        }

        ltd.setData(increasingData);

        for (TimeStampLectureData stamp : ltd.getData()) {

            Date date=new Date(Long.parseLong(stamp.getTimeStamp()));

            long totalTimeSpent = totalTime(stamp.getTimeSpent());


            data.add(new DataPoint(date, totalTimeSpent));
        }

        // Arrange in Increasing x Order
        DataPoint[] dp = new DataPoint[data.size()];

        int i = 0;
        for (DataPoint d : data) {
            dp[i] = d;
            i++;
        }




        LineGraphSeries<DataPoint> result = new LineGraphSeries<DataPoint>(dp);

        return result;
    }


    public LineGraphSeries<DataPoint> getTimeSeries(LectureTrackingData t) {
        ArrayList<DataPoint> data = new ArrayList<>();

        ArrayList<TimeStampLectureData> dataTemp = t.getData();
        ArrayList<TimeStampLectureData> increasingData = new ArrayList<>();

        int sizeData = t.getData().size();
        for (int i = 0; i < sizeData; i++) {
            int index = i;
            long minCurPos = Long.valueOf(dataTemp.get(i).getTimeStamp());
            for (int j = i + 1; j < sizeData; j++) {
                long temp = Long.valueOf(dataTemp.get(j).getTimeStamp());
                if (minCurPos > temp ) {
                    index = j;
                    minCurPos = temp;
                }
            }
            increasingData.add(dataTemp.get(index));
        }

        t.setData(increasingData);

        for (TimeStampLectureData stamp : t.getData()) {

            Date date=new Date(Long.parseLong(stamp.getTimeStamp()));

            long totalTimeSpent = totalTime(stamp.getTimeSpent());


            data.add(new DataPoint(date, totalTimeSpent));
        }

        DataPoint[] dp = new DataPoint[data.size()];

        int i = 0;
        for (DataPoint d : data) {
            dp[i] = d;
            i++;
        }

        LineGraphSeries<DataPoint> result = new LineGraphSeries<DataPoint>(dp);

        return result;
    }



    private long totalTime(ArrayList<Long> timeSpent) {
        long total = 0;
        for (Long i : timeSpent) {
            total += i;
        }
        return total / 1000;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getPopularSlideNumber(int p) {
        int size = ltd.getData().get(0).size();

        ArrayList<Long> totalTime = new ArrayList<>(Collections.nCopies(size, (long) 0));
        for (TimeStampLectureData stamp : ltd.getData()) {
            int counter = 0;
            for (Long time : stamp.getTimeSpent()) {
                totalTime.set(counter, totalTime.get(counter) + time);
                counter++;
            }
        }

        switch (p) {
            case MOST:
                return String.valueOf(indexOfHighest(totalTime));
            case LEAST:
                return String.valueOf(indexOfLowest(totalTime));
            default:
                return String.valueOf(-1);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public LineGraphSeries<DataPoint> get7DayRollingAverage(@Nullable LectureTrackingData t) {
        ArrayList<ArrayList<DataPoint>> data = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            data.add(new ArrayList<DataPoint>());
        }

        LocalDate currentTime = LocalDate.now();
        LocalDate nextTimeDay = currentTime.plusDays(1);

        for (int i = 0; i < 7; i++) {
            LocalDate sevenDayPrevious = currentTime.minusDays(7);
            Date sevenDayPreviousDate = Date.from(sevenDayPrevious.atStartOfDay(ZoneId.systemDefault()).toInstant());
            if (t != null) {
                for (TimeStampLectureData stamp : t.getData()) {
                    Date date=new Date(Long.parseLong(stamp.getTimeStamp()));
                    long totalTimeSpent = totalTime(stamp.getTimeSpent());

                    if (date.after(sevenDayPreviousDate) && date.before(Date.from(nextTimeDay.atStartOfDay(ZoneId.systemDefault()).toInstant()))) {
                        data.get(i).add(new DataPoint(date, totalTimeSpent));
                    }
                }
            } else {
                for (TimeStampLectureData stamp : ltd.getData()) {
                    Date date=new Date(Long.parseLong(stamp.getTimeStamp()));
                    long totalTimeSpent = totalTime(stamp.getTimeSpent());

                    if (date.after(sevenDayPreviousDate) && date.before(Date.from(nextTimeDay.atStartOfDay(ZoneId.systemDefault()).toInstant()))) {
                        data.get(i).add(new DataPoint(date, totalTimeSpent));
                    }
                }
            }

            currentTime = currentTime.minusDays(1);
            nextTimeDay = nextTimeDay.minusDays(1);
        }

        ArrayList<DataPoint> resultAverage = new ArrayList<>();

        LocalDate tempTime = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            int timePerDay = 0;
            int numberElements = data.get(i).size();
            for (DataPoint d : data.get(i)) {
                timePerDay += d.getY();
            }
            if (numberElements > 0) {
                resultAverage.add(new DataPoint(Date.from(tempTime.atStartOfDay(ZoneId.systemDefault()).toInstant()),  (float) timePerDay / (float)numberElements));

            } else {
                resultAverage.add(new DataPoint(Date.from(tempTime.atStartOfDay(ZoneId.systemDefault()).toInstant()), (long) 0));
            }
            tempTime = tempTime.minusDays(1);

        }

        // Reverse The List
        Collections.reverse(resultAverage);


        DataPoint[] dp = new DataPoint[resultAverage.size()];

        int i = 0;
        for (DataPoint d : resultAverage) {
            dp[i] = d;
            i++;
        }

        LineGraphSeries<DataPoint> result = new LineGraphSeries<DataPoint>(dp);

        return result;
    }

    private int indexOfHighest(ArrayList<Long> totalTime) {
        int index = 0;
        Long temp = totalTime.get(0);
        for (int j = 0; j < totalTime.size(); j++) {
            if (temp < totalTime.get(j)) {
                index = j;
            }
        }
        return index + 1;
    }

    private int indexOfLowest(ArrayList<Long> totalTime) {
        int index = 0;
        Long temp = totalTime.get(0);
        for (int j = 0; j < totalTime.size(); j++) {
            if (temp > totalTime.get(j)) {
                index = j;
            }
        }
        return index + 1;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    // Audio

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarGraphSeries<DataPoint> getAudioSeries() {
        int size = ltd.getData().get(0).size();

        ArrayList<Long> totalAudioCounter = new ArrayList<>(Collections.nCopies(size, (long) 0));
        List<DataPoint> data = Arrays.asList(new DataPoint[size]);
        for (TimeStampLectureData stamp : ltd.getData()) {
            int counter = 0;
            for (Long time : stamp.getAudio()) {
                totalAudioCounter.set(counter, totalAudioCounter.get(counter) + time);
                counter++;
            }
        }
        int counter = 0;
        for (Long i : totalAudioCounter) {
            data.set(counter, new DataPoint(counter + 1  , i));
            counter++;
        }

        DataPoint[] dp = new DataPoint[totalAudioCounter.size()];

        int i = 0;
        for (DataPoint d : data) {
            dp[i] = d;
            i++;
        }

        BarGraphSeries<DataPoint> result = new BarGraphSeries<>(dp);

        return result;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    // Video

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarGraphSeries<DataPoint> getVideoSeries() {
        int size = ltd.getData().get(0).size();

        ArrayList<Long> totalVideoCounter = new ArrayList<>(Collections.nCopies(size, (long) 0));
        List<DataPoint> data =  Arrays.asList(new DataPoint[size]);
        for (TimeStampLectureData stamp : ltd.getData()) {
            int counter = 0;
            for (Long time : stamp.getVideo()) {
                totalVideoCounter.set(counter, totalVideoCounter.get(counter) + time);
                counter++;
            }
        }
        int counter = 0;
        for (Long i : totalVideoCounter) {
            data.set(counter, new DataPoint(counter + 1, i));
            counter++;
        }

        DataPoint[] dp = new DataPoint[totalVideoCounter.size()];

        int i = 0;
        for (DataPoint d : data) {
            dp[i] = d;
            i++;
        }

        BarGraphSeries<DataPoint> result = new BarGraphSeries<>(dp);

        return result;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////

    // Course

    public ArrayList<LineGraphSeries<DataPoint>> getCourseTotalTime() {
        ArrayList<LineGraphSeries<DataPoint>> data = new ArrayList<>();
        for (LectureTrackingData l : ctd.getLectureGeneralData()) {
            data.add(getTimeSeries(l));
        }
        return data;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<LineGraphSeries<DataPoint>> getCourseSevenRollingAverageTime() {
        ArrayList<LineGraphSeries<DataPoint>> data = new ArrayList<>();

        for (LectureTrackingData l : ctd.getLectureGeneralData()) {
            data.add(get7DayRollingAverage(l));
        }
        return data;
    }


    public int indexOfMostPopularCourse() {

        int counter = 0;
        int maxCounter = 0;
        long maxTimeSpent = 0;
        for (LectureTrackingData l : ctd.getLectureGeneralData()) {
            long tempTimeSpent = 0;
            ArrayList<TimeStampLectureData> d = l.getData();
            for (TimeStampLectureData t : d) {
                for (Long lg : t.getTimeSpent()) {
                    tempTimeSpent += lg;
                }
            }

            if (maxTimeSpent < tempTimeSpent) {
                maxCounter = counter;
                maxTimeSpent = tempTimeSpent;
            }
            counter++;
        }
        return  maxCounter;
    }

    public int indexOfLeastPopularCourse() {
        int counter = 0;
        int minCounter = 0;
        long minTimeSpent = 0;
        for (LectureTrackingData l : ctd.getLectureGeneralData()) {
            long tempTimeSpent = 0;
            ArrayList<TimeStampLectureData> d = l.getData();
            for (TimeStampLectureData t : d) {
                for (Long lg : t.getTimeSpent()) {
                    tempTimeSpent += lg;
                }
            }

            if (counter == 0) {
                minTimeSpent = tempTimeSpent;
                minCounter = counter;
            } else {
                if (minTimeSpent > tempTimeSpent) {
                    minCounter = counter;
                    minTimeSpent = tempTimeSpent;
                }
            }

            counter++;
        }
        return  minCounter;
    }

}
