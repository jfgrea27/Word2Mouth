package com.imperial.word2mouth.shared;

import android.widget.Toast;

import com.imperial.word2mouth.learn.main.offline.share.bluetooth.ShareBluetoothActivity;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class UnzipFile {

    public static void unzipFile(String zipFilePath, String outputPath) {
        ZipFile zipFile = new ZipFile(zipFilePath);
        try {
            zipFile.extractAll(outputPath);
        } catch (ZipException e) {
            e.printStackTrace();
        }

        if (checkUnzipCorrectly(zipFilePath)) {
            removeZipFile(zipFilePath);
        }

    }


    public static boolean checkUnzipCorrectly(String path) {
        File f = new File(path);
        if (f.exists()) {
            return true;
        }
        return false;
    }


    public static void removeZipFile(String path) {
        File zipped = new File(path);
        zipped.delete();
    }

}
