package com.imperial.word2mouth.shared;

import java.util.ArrayList;
import java.util.Arrays;

public class Categories {

    public static ArrayList<String> categories = new ArrayList<>(Arrays.asList("Health", "Mechanical", "Academic"));

    public static String get(int position) {
        return categories.get(position);
    }
}
