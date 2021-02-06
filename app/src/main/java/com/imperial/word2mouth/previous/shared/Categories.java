package com.imperial.word2mouth.previous.shared;

import com.imperial.word2mouth.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Categories {

    public static final ArrayList<String> categories = new ArrayList<>(Arrays.asList("Health", "Mechanical", "Agriculture", "Academic"));




    public static final HashMap<String, Integer> categoryIconMap= new HashMap<String, Integer>(){{
        put("Health", R.drawable.category_health);
        put("Mechanical", R.drawable.category_mechanical);
        put("Agriculture", R.drawable.category_agriculture);
        put("Academic", R.drawable.category_academic);
    }};

    public static String get(int position) {
        return categories.get(position);
    }

}
