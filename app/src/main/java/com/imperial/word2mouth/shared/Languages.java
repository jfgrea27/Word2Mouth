package com.imperial.word2mouth.shared;

import com.imperial.word2mouth.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Languages {


    public ArrayList<String> languages = new ArrayList<>(Arrays.asList("English", "Francais", "Maures", "Swahili"));


    public HashMap<String, Integer> languageIconMap = new HashMap<String, Integer>(){{
        put("English", R.drawable.flag_uk);
        put("Francais", R.drawable.flag_france);
        put("Maures", R.drawable.flag_burkina);
        put("Swahili", R.drawable.flag_kenya);
    }};

    public String get(int position) {
        return languages.get(position);
    }

}
