package com.example.word2mouth.utilities.video;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VideoHandler {
    private final String outputFilePath;
    public VideoHandler(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public void copyVideo(InputStream in) throws IOException {
        File outputFile = new File(outputFilePath);

        FileOutputStream fos = new FileOutputStream(outputFile);

        copyStream(in, fos);
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
