package com.imperial.word2mouth;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.imperial.word2mouth.background.ConnectivityReceiver;

public class Word2Mouth extends Application {


    private static Word2Mouth mInstance;
    private ConnectivityReceiver connectivityReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        mInstance = this;
    }

    public static synchronized Word2Mouth getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

}