package com.imperial.word2mouth.common;

import com.imperial.word2mouth.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Categories {


    public static final LinkedHashMap<String, Integer> categoryIconMap= new LinkedHashMap<String, Integer>() {{
        put("Health", R.drawable.category_health);
        put("Mechanical", R.drawable.category_mechanical);
        put("Agriculture", R.drawable.category_agriculture);
        put("Academic", R.drawable.category_academic);
    }};

    public static Integer getCategory(String category) {
        return categoryIconMap.get(category);
    }

}
