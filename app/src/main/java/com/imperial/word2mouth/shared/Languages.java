package com.imperial.word2mouth.shared;

import java.util.ArrayList;
import java.util.Arrays;

public class Languages {


    public static ArrayList<String> languages = new ArrayList<>(Arrays.asList("English", "Francais", "Deutsch", "Swahili"));

    public static String get(int position) {
        return languages.get(position);
    }

}
