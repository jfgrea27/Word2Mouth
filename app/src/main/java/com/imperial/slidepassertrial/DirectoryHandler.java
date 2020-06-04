package com.imperial.slidepassertrial;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DirectoryHandler {



    public static final int VIDEO = 10;
    public static final int AUDIO = 11;
    public static final int TEXT = 12;

    public static File createDirectoryForCourseAndReturnIt(String courseName, Context context) {
        File file = new File(context.getExternalFilesDir(null), courseName);

        if (file.exists()) {
            int directoryNumber = 0;
            while (file.exists()) {
                directoryNumber++;
                file = new File(context.getExternalFilesDir(null), "/" + courseName + " (" + directoryNumber + ")");
            }
        }
        file.mkdirs();
        return file;
    }

    public static File createDirectoryForSlideAndReturnIt(String coursePath, int slideNumber, String slideName, Context context) {
        File file = new File(coursePath, "/" + slideNumber+ " " + slideName) ;

        int directoryNumber = 0;
        while (file.exists()) {
            directoryNumber++;
            file = new File(coursePath, "/" + slideNumber +  " " + slideName + " (" + directoryNumber + ")");
        }
        file.mkdirs();
        return file;
    }

    public static File createVideoFileAndReturnIt(String slidePath, Uri originalPath, ContentResolver context) throws IOException {
        InputStream in = context.openInputStream(originalPath);

        String outputAddress = slidePath + "/video.3gp";

        return copyFile(in, outputAddress);
    }

    public static File createInstructionFileAndReturnIt(String slidePath, String instructions) {
        String outputAddress = slidePath + "/instructions.txt";

        File file = new File(outputAddress);
        try {
            FileWriter writer = new FileWriter(file);
            writer.append(instructions);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
    public static File retrieveSlideDirectoryByNumber(String coursePath, int slideNumber, String slideName) {
        File slideDirectory = new File(coursePath, "/" + slideNumber +  " " + slideName);

        if (slideDirectory.exists()) {
            return slideDirectory;
        } else {
            return null;
        }
    }

    private static File copyFile(InputStream in, String desiredLocation) throws IOException {
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

}
