package com.imperial.word2mouth.model;

import com.imperial.word2mouth.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Languages {

    public static LinkedHashMap<String, Integer> languageIconMap = new LinkedHashMap<String, Integer>() {{
        put("English", R.drawable.flag_uk);
        put("Francais", R.drawable.flag_france);
        put("Maures", R.drawable.flag_burkina);
        put("Swahili", R.drawable.flag_kenya);
    }};

    public static Integer getLanguageIcon(String language) {
        return languageIconMap.get(language);
    }

}
