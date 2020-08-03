package com.imperial.word2mouth.shared;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.imperial.word2mouth.learn.main.offline.tracker.LectureTracker;
import com.imperial.word2mouth.learn.main.offline.tracker.SlideTracker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Scanner;

public class FileHandler {


    public static final int TITLE = 100;
    public static final int VIDEO = 101;
    public static final int INSTRUCTIONS = 102;
    public static final int AUDIO = 103;
    public static final int IMAGE = 104;
    public static final int META = 105;
    public static final int SLIDES = 106;
    public static final int ONLINE_COURSE_IDENTIFICATION = 107;
    public static final int LANGUAGE_SELECTION = 108;
    public static final int CATEGORY_SELECTION = 109;
    public static final int AUTHOR = 110;
    public static final int LECTURES_DIRECTORY = 111;
    public static final int COURSE_LECTURE_DISTINGUISHING = 112;
    public static final int BLUETOOTH_UUID_COURSE = 113;
    public static final int ONLINE_LECTURE_IDENTIFICATION = 114;
    public static final int LECTURE_UUID_BLUETOOTH = 115;
    public static final int VERSION = 116;
    public static final int LECTURE_TRACKING = 117;


    public static File createDirectoryForCourseAndReturnIt(String courseName, Context context) {
        File file = new File(context.getExternalFilesDir(null), DirectoryConstants.offline + courseName);

        if (file.exists()) {
            int directoryNumber = 0;
            while (file.exists()) {
                directoryNumber++;
                file = new File(context.getExternalFilesDir(null), DirectoryConstants.offline + courseName + " (" + directoryNumber + ")");
            }
        }
        file.mkdirs();
        return file;
    }

    public static File createDirectoryForSlideAndReturnIt(String coursePath, int slideNumber) {
        File file = new File(coursePath, DirectoryConstants.slides+ slideNumber);
        file.mkdirs();
        return file;
    }

    public static File createDirectoryAndReturnIt(String coursePath, int type) {
        File file = null;
        switch (type) {
            case META:
                file = new File(coursePath, DirectoryConstants.meta);
                break;
            case SLIDES:
                file = new File(coursePath, DirectoryConstants.slides);
                break;
            case LECTURES_DIRECTORY:
                file = new File(coursePath, DirectoryConstants.lectures);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
        file.mkdirs();
        return file;
    }

    public static File retrieveSlideDirectoryByNumber(String coursePath, int slideNumber) {
        File slideDirectory = new File(coursePath, DirectoryConstants.slides + slideNumber);
        if (slideDirectory.exists()) {
            return slideDirectory;
        } else {
            return null;
        }
    }

    public static File createFileForSlideContentAndReturnIt(String slidePath, @Nullable Uri originalUriPath, ContentResolver content, @Nullable String script, int requestCode) {
        String outputAddress = slidePath;
        switch (requestCode) {
            case TITLE:
                outputAddress += "/title.txt";
                return copyTextToFile(outputAddress, script);
            case INSTRUCTIONS:
                outputAddress += "/instructions.txt";
                return copyTextToFile(outputAddress, script);
            case AUDIO:
                outputAddress += "/audio.3gp";
                return new File(outputAddress);
            case VIDEO:
                outputAddress += "/video.3gp";
                return copyVideoToFile(outputAddress, originalUriPath, content);
            case IMAGE:
                outputAddress += "/thumbnail.jpg";
                return copyVideoToFile(outputAddress, originalUriPath, content);
            case ONLINE_COURSE_IDENTIFICATION:
                outputAddress += DirectoryConstants.identification;
                return copyTextToFile(outputAddress, script);
            case LANGUAGE_SELECTION:
                outputAddress += DirectoryConstants.language;
                return copyTextToFile(outputAddress, script);
            case CATEGORY_SELECTION:
                outputAddress += DirectoryConstants.category;
                return copyTextToFile(outputAddress, script);
            case AUTHOR:
                outputAddress += DirectoryConstants.author;
                return copyTextToFile(outputAddress, script);
            case COURSE_LECTURE_DISTINGUISHING:
                outputAddress += DirectoryConstants.type;
                return copyTextToFile(outputAddress, script);
            case BLUETOOTH_UUID_COURSE:
                outputAddress += DirectoryConstants.courseBluetooth;
                return copyTextToFile(outputAddress, script);
            case ONLINE_LECTURE_IDENTIFICATION:
                outputAddress += DirectoryConstants.lectureIdentifcation;
                return copyTextToFile(outputAddress, script);
            case LECTURE_UUID_BLUETOOTH:
                outputAddress += DirectoryConstants.lectureBluetooth;
                return copyTextToFile(outputAddress, script);
            case VERSION:
                outputAddress += DirectoryConstants.versionLecture;
                return copyTextToFile(outputAddress, script);
            case LECTURE_TRACKING:
                outputAddress += DirectoryConstants.lectureTracking;
                return copyTextToFile(outputAddress, script);
            default:
                return null;
        }
    }

//    public static File createFileForTrackingData(String lectureTrackerPath, ContentResolver content, LectureTracker tracker) {
//        File f = new File(lectureTrackerPath);
//
//        FileWriter fw;
//        try {
//            fw = new FileWriter("tmp");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        BufferedReader br = null;
//        try {
//            br = new BufferedReader(new FileReader(f));
//
//            br.readLine();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
////
////        while (scanner.hasNextLine()) {
////
////            updateCurrentl
////            System.out.println(scanner.nextLine());
////        }
////
////        for (SlideTracker s : tracker.slides) {
////
////        }
////        return updateTrackingFile(versionUID, slidetracker)
//    }

    // Text

    private static File copyTextToFile(String outputPath, String script) {
        File textFile = new File(outputPath);
        try {
            FileWriter writer = new FileWriter(textFile);
            writer.write(script);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textFile;
    }

    // Video

    private static File copyVideoToFile(String outputAddress, Uri originalUri, ContentResolver content) {
        InputStream in = null;
        try {
            in = content.openInputStream(originalUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        try {
            return copyFile(in, outputAddress);
        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }
    }

    public static File copyFile(InputStream in, String desiredLocation) throws IOException {
        File outputFile = new File(desiredLocation);

        FileOutputStream fos = new FileOutputStream(outputFile);

        copyStream(in, fos);

        return outputFile;
    }

    private static void copyStream(InputStream input, OutputStream output)
            throws IOException {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            } else {
                boolean newDirectoryName = false;
                int counter = 1;
                while (!newDirectoryName) {
                    String newLocation = targetLocation.getPath() + " " + counter;
                    File newLocationFile = new File(newLocation);
                    if (newLocationFile.exists()) {
                        counter++;
                    } else {
                        targetLocation = newLocationFile;
                        newDirectoryName = true;
                    }
                }
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);

            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

    }


    public static void addTree(File file, Collection<File> all) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                all.add(child);
                addTree(child, all);
            }
        }
    }



    public static void saveUriFileToDestination(Uri sourceuri, String destinationPath)
    {
        String sourceFilename= sourceuri.getPath();
        String destinationFilename = destinationPath;

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createFileForLectureTracking(LectureItem lectureItem, Activity activity) {

        File lectureDirectory = new File(lectureItem.getLecturePath() + DirectoryConstants.slides);
        int numberSlides = lectureDirectory.listFiles().length;


        File trackerSlides = new File(activity.getExternalFilesDir(null).getPath() + DirectoryConstants.cache + lectureItem.getVersion() + ".txt");
        if (!trackerSlides.exists()) {
            try {
                trackerSlides.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(trackerSlides);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        try {
            bw.write(lectureItem.getVersion());
            bw.newLine();

            for (int i = 0; i < numberSlides; i++) {
                bw.write("0, 0, 0");
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void createFileForLectureTracking(String version, int numberSlides, Activity activity) {

        File trackerSlides = new File(activity.getExternalFilesDir(null).getPath() + DirectoryConstants.cache + version + ".txt");
        if (!trackerSlides.exists()) {
            try {
                trackerSlides.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(trackerSlides);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        try {
            bw.write(version);
            bw.newLine();
            for (int i = 0; i < numberSlides; i++) {
                bw.write("0,0,0");
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void updateTracker(LectureTracker lectureTracker, Activity activity, String lecturePath) {
        int counter = 0;

        String version = FileReaderHelper.readTextFromFile(lecturePath + DirectoryConstants.meta + DirectoryConstants.versionLecture);
        File lectureTrackerFile = new File(activity.getExternalFilesDir(null).getPath() + DirectoryConstants.cache + version + ".txt");

        if (lectureTrackerFile.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(lectureTrackerFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // Retrieve data and Update Lecture Tracker
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            try {
                version = br.readLine();
                String entry = br.readLine();
                while (entry != null && !entry.isEmpty()) {
                    // Update within the LectureTracker
                    String[] data = entry.split("\\s*,\\s*");
                    long time = Long.parseLong(data[0]);
                    int videoCounter = Integer.parseInt(data[1]);
                    int soundCounter = Integer.parseInt(data[2]);

                    lectureTracker.slides.get(counter).updateEntries(time, videoCounter, soundCounter);

                    counter++;
                    entry = br.readLine();
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            // Store LectureTracker Inside file
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(lectureTrackerFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            try {
                bw.write(version);
                bw.newLine();

                for (SlideTracker s : lectureTracker.slides) {
                    String data = s.getTimeSpent() + "," + s.getVideoCounter() + "," + s.getSoundCounter();
                    bw.write(data);
                    bw.newLine();
                }
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}

