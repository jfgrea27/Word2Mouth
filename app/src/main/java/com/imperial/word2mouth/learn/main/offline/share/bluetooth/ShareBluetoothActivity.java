package com.imperial.word2mouth.learn.main.offline.share.bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.FileHandler;
import com.imperial.word2mouth.shared.IntentNames;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class ShareBluetoothActivity extends AppCompatActivity {

    private static final int BLUETOOTH_PERSMISSIONS = 1;
    private ImageButton share;
    private ListView listDiscoverableDevices;
    private ListView listPairedDevices;
    private ProgressBar progress;

    // Bluetooth
    private java.util.UUID UUID = java.util.UUID.fromString("2f9bda9c-2e2c-4636-8bd5-f88da6d49538");
    private BluetoothDevice selectedDevice;


    // Part 1: Connecting Bluetooth
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;

    // Part 2: Making Device Discoverable

    // Part 3: Listing Discoverable Devices
    private LinkedHashSet<BluetoothDevice> discoverableDevices = new LinkedHashSet<>();
    private ArrayAdapterBluetooth discoverableAdapter;

    // Part 4: Pairing Devices
    private LinkedHashSet<BluetoothDevice> pairedDevices = new LinkedHashSet<>();
    private ArrayAdapterBluetooth pairedAdapter;


    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_RECEIVING_CONTENT = 5;
    static final int STATE_CONTENT_RECEIVED = 6;
    static final int STATE_SENDING_CONTENT = 7;
    private static final int STATE_CONTENT_SENT = 8;


    private ImageButton speakBluetooth;
    // Send
    private String lecturePath;
    private String lectureName;
    private String zipSendLectureZipPath = null;

    // Receive
    private String zipReceiveLecturePath = null;

    private SendReceive sendReceive;

    private boolean selectedCourse = false;
    private TextToSpeech textToSpeech;

    private ImageView discoverableDevicesTitle;
    private ImageView pairedDevicesTitle;
    private boolean hasPermissions = false;
    private int step = 0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_sharing_bluetooth);

        getIntents();

        getPermissions();

        if (hasPermissions) {
            // Section 0: General UI & Set Up
            // Part 0: Set Up UI
            findViewsByIds();

            if (selectedCourse) {
                createSendZipFile();
            } else {
                createReceiveZipFile();
            }


            configureUI();
            configureTextToSpeech();

            configureSpeakHelp();
            configureOnLongClicks();
            // Section 1: Connectivity
            // Part 1: Connecting Bluetooth
            setUpBluetooth();

            // Part 2: Making Device Discoverable
            makeThisDeviceDiscoverable();

            // Part 3: Discovering nearby devices
            discover();

            // Part 4: Connecting Device To Paired Device
            configureConnection();
            configureConnectedDevices();

            // Section 2: File Sharing
        } else {
            finish();
        }

    }

    private void configureSpeakHelp() {
        speakBluetooth = findViewById(R.id.speakBluetooth);

        speakBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (step) {
                    case 0:
                        speak(getString(R.string.discoverableDeviceSelect));
                        break;
                    case 1:
                        speak(getString(R.string.pairedDeviceSelect) + selectedDevice.getName());
                        break;
                }
            }
        });
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getPermissions() {

        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, BLUETOOTH_PERSMISSIONS);
        } else {
            hasPermissions = true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == BLUETOOTH_PERSMISSIONS) {
            if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    permissions[1].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasPermissions = true;
            }
        }
    }



    private void configureUI() {
        discoverableDevicesTitle = findViewById(R.id.discoverableDevices);
        pairedDevicesTitle = findViewById(R.id.pairedDevices);


    }

    private void configureOnLongClicks() {
        share.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.share));
                return false;
            }
        });

        discoverableDevicesTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        pairedDevicesTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.pairedDevices));
                return false;
            }
        });

        speakBluetooth.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.bluetoothSpeakInstructions));
                return false;
            }
        });


    }

    @Override
    public void onDestroy() {
        try{
            if(discoverReceiver !=null)
                unregisterReceiver(discoverReceiver);

        }catch(Exception e)
        {
        }
        
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        tidyAnyExtaFiles();

        finish();
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void getIntents() {
        boolean hasCourse = false;
        boolean hasPath = false;
        Intent intent = getIntent();


        if (intent.hasExtra(IntentNames.COURSE_NAME)) {
            lectureName = (String) getIntent().getExtras().get(IntentNames.COURSE_NAME);
            hasCourse = true;

        } else {
            lectureName = null;
        }

        if (intent.hasExtra(IntentNames.COURSE_PATH)) {
            lecturePath = (String) getIntent().getExtras().get(IntentNames.COURSE_PATH);
            hasPath = true;
        } else {
            lecturePath = null;
        }
        if (hasCourse && hasPath) {
            selectedCourse = true;
        }
    }

    ///////// RECEIVE
    private void createReceiveZipFile() {
        zipReceiveLecturePath = getApplicationContext().getExternalFilesDir(null).getPath() +
                DirectoryConstants.zip + "ReceivedLecture.zip";

    }

    /////////// SEND

    private void createSendZipFile() {

        zipSendLectureZipPath = getApplicationContext().getExternalFilesDir(null).getPath() +
                DirectoryConstants.zip + lectureName + ".zip";

        File zipFile = new File(zipSendLectureZipPath);
        try {
            zipFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileZip.zipFileAtPath(lecturePath, zipSendLectureZipPath);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    // UI
    private void findViewsByIds() {
        listDiscoverableDevices = findViewById(R.id.list_discoverable__devices);
        listPairedDevices = findViewById(R.id.list_paired_devices);
        share = findViewById(R.id.share);

        share.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);


        progress = findViewById(R.id.progress_bar);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Part 1: Connecting Bluetooth

    private void setUpBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(ShareBluetoothActivity.this, getString(R.string.deviceNotBluetoothSuported), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(ShareBluetoothActivity.this, getString(R.string.onBluetooth), Toast.LENGTH_SHORT).show();

                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(ShareBluetoothActivity.this, getString(R.string.btON),
                        Toast.LENGTH_SHORT).show();

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(ShareBluetoothActivity.this,
                        getString(R.string.needBTOn), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Part 2: Making This Device Discoverable


    private void makeThisDeviceDiscoverable() {

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Part 3: Connecting Bluetooth

    private void discover() {
        mBluetoothAdapter.startDiscovery();

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        registerReceiver(discoverReceiver, intentFilter);

        List<BluetoothDevice> list = new ArrayList<>(discoverableDevices);

        discoverableAdapter = new ArrayAdapterBluetooth(getApplicationContext(),
                R.layout.row_device_discoring, list);

        Server server = new Server();
        server.start();
    }

    BroadcastReceiver discoverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (discoverableAdapter.getPosition(device) == -1 && device.getName() != null) {
                    discoverableDevices.add(device);
                    if (!discoverableAdapter.contains(device)) {
                        discoverableAdapter.add(device);
                    }

                    if (listDiscoverableDevices.getAdapter() == null) {
                        listDiscoverableDevices.setAdapter(discoverableAdapter);
                    }
                    discoverableAdapter.notifyDataSetChanged();
                }
            }
        }
    };


    private void configureConnectedDevices() {
        List<BluetoothDevice> list = new ArrayList<>(pairedDevices);

        pairedAdapter = new ArrayAdapterBluetooth(getApplicationContext(), R.layout.row_device_pairing, list);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Part 4: Connection to Other Device


    public void configureConnection() {
        configureDeviceSelection();
        configureShareButton();
    }

    Handler connectionHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                case STATE_LISTENING:
                    Toast.makeText(getBaseContext(), getString(R.string.listeningBT), Toast.LENGTH_SHORT).show();
                    break;
                case STATE_CONNECTED:
                    Toast.makeText(getBaseContext(),getString(R.string.connectedBT), Toast.LENGTH_SHORT).show();
                    mBluetoothAdapter.cancelDiscovery();
                    if (selectedDevice != null) {

                        if (pairedAdapter.getPosition(selectedDevice) == -1 && selectedDevice.getName() != null) {
                            pairedAdapter.add(selectedDevice);
                            if (!pairedAdapter.contains(selectedDevice)) {
                                pairedAdapter.add(selectedDevice);
                            }

                            if (listPairedDevices.getAdapter() == null) {
                                listPairedDevices.setAdapter(pairedAdapter);
                            }
                            step = 1;
                            pairedAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case STATE_CONNECTION_FAILED:
                    Toast.makeText(getBaseContext(), getString(R.string.connectionFailBT), Toast.LENGTH_SHORT).show();
                    break;
                case STATE_CONTENT_RECEIVED:
                    if (progress.getVisibility() == View.VISIBLE) {
                        progress.setVisibility(View.INVISIBLE);
                    }
                    unzipFile(zipReceiveLecturePath);
                    break;
                case STATE_RECEIVING_CONTENT:
                    if (progress.getVisibility() == View.INVISIBLE) {
                        progress.setVisibility(View.VISIBLE);
                    } else {
                        float prog = (float) msg.obj;
                        progress.setProgress((int) (prog * 100));
                    }
                    break;
                case STATE_SENDING_CONTENT:
                    if (progress.getVisibility() == View.INVISIBLE) {
                        progress.setVisibility(View.VISIBLE);
                    } else {
                        float prog = (float) msg.obj;
                        if ((int) (prog * 100) == 100) {
                            progress.setVisibility(View.INVISIBLE);
                        }
                        progress.setProgress((int) (prog * 100));
                    }
                    break;

                case STATE_CONTENT_SENT:
                    if (progress.getVisibility() == View.VISIBLE) {
                        progress.setVisibility(View.INVISIBLE);
                    }
                    tidyZippedFile();
                    Toast.makeText(getBaseContext(), getString(R.string.contentSentBT), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });


    private void configureDeviceSelection() {
        listDiscoverableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedDevice = nthElement(discoverableDevices, position);
                share.setColorFilter(null);


                Client client = new Client(selectedDevice);
                client.start();
            }
        });

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Part 5: Sending Item to Other Device

    private void configureShareButton() {
        share.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (selectedCourse) {
                    sendReceive.write(zipSendLectureZipPath);
                }
            }
        });


        if (!selectedCourse) {
            share.setVisibility(View.INVISIBLE);
        }
    }



    //////////////////// SERVER ////////////////////

    private class Server extends Thread {
        private BluetoothServerSocket serverSocket;

        public Server() {
            try {
                serverSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("Word2Mouth", UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            BluetoothSocket socket = null;

            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_LISTENING;
                    connectionHandler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    continue;
                }

                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    connectionHandler.sendMessage(message);
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                    break;
                }
            }
        }
    }


    //////////////////// CLIENT ////////////////////
    private class Client extends Thread {
        private BluetoothSocket socket;
        private BluetoothDevice device;

        public Client(BluetoothDevice device) {
            this.device = device;
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                socket.connect();

                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                connectionHandler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                connectionHandler.sendMessage(message);
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

    }

    //////////////////// SEND/RECEIVE ////////////////////
    public class SendReceive extends Thread {
        private final BluetoothSocket socket;
        private InputStream in;
        private OutputStream out;


        public SendReceive(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
            in = tempIn;
            out = tempOut;
        }


        public void run() {

            FileOutputStream fos = null;
            byte[] buffer = new byte[1024];
            int numberBytes = 0;
            int count = 0;
            float tenPercent;
            int i = 1;
            // progress bar
            int counter = 0;

            boolean running = true;

            while (running) {
                try {

                    byte[] temp = new byte[4];
                    in.read(temp);
                    numberBytes = ByteBuffer.wrap(temp).getInt();

                    tenPercent = (float) numberBytes / 10;

                    Message message = Message.obtain();
                    message.what = STATE_RECEIVING_CONTENT;
                    connectionHandler.sendMessage(message);

                    while ((count = in.read(buffer)) != -1) {
                        counter += count;
                        if (fos == null) {
                            fos = new FileOutputStream(new File(zipReceiveLecturePath));

                        }

                        fos.write(buffer, 0, count);

                        if (counter > tenPercent * i) {
                            // progress Bar
                            Message msg = Message.obtain();
                            msg.what = STATE_RECEIVING_CONTENT;
                            msg.obj = (float) counter / numberBytes;
                            connectionHandler.sendMessage(msg);
                            i++;
                        }

                    }


                    } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();


                    if (fos != null) {
                        Message message1 = Message.obtain();
                        message1.what = STATE_CONTENT_RECEIVED;
                        connectionHandler.sendMessage(message1);

                    }
                    try {
                        in.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;

                }

            }//                        try {
//                            fos.close();
//                        } catch (IOException ex) {
//                            ex.printStackTrace();
//                        }

        }


        @RequiresApi(api = Build.VERSION_CODES.O)
        public void write(String zipFilePath) {
            try {

                byte[] fileBytes = Files.readAllBytes(Paths.get(zipFilePath));

                int numberBytes = fileBytes.length;
                int subArraySize = 1024;

                float tenPercent = (float) numberBytes/ 10;
                int j = 1;

                out.write(ByteBuffer.allocate(4).putInt(numberBytes).array());

                Message message = Message.obtain();
                message.what = STATE_SENDING_CONTENT;
                connectionHandler.sendMessage(message);


                for (int i = 0; i < fileBytes.length; i += subArraySize) {
                    byte[] tempArray;
                    tempArray = Arrays.copyOfRange(fileBytes, i, Math.min(numberBytes, i + subArraySize));
                    out.write(tempArray);
                    out.flush();
                    if (i > tenPercent * j) {
                        // progress Bar
                        Message msg = Message.obtain();
                        msg.what = STATE_SENDING_CONTENT;
                        msg.obj = (float) i / numberBytes;
                        connectionHandler.sendMessage(msg);
                        j++;
                    }
                }
                out.close();

                Message msg = Message.obtain();
                msg.what = STATE_CONTENT_SENT;
                connectionHandler.sendMessage(msg);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

        ///////////////////////////////////////////////////////////////////////////////////////////////

        // After File Has Been Received

        private void unzipFile(String zipFilePath) {
            ZipFile zipFile = new ZipFile(zipFilePath);
            String path = null;
            try {
                path = new String(getApplicationContext().getExternalFilesDir(null).getAbsolutePath() +
                        DirectoryConstants.zip + "Unzipped");
                zipFile.extractAll(path);
            } catch (ZipException e) {
                e.printStackTrace();
            }

            if (checkUnzipCorrectly(path)) {
                copyFileToOfflineDirectory(path);
                removeZipFile(path);
            }

            Toast.makeText(ShareBluetoothActivity.this, getString(R.string.checkOffline), Toast.LENGTH_SHORT).show();
        }



    private boolean checkUnzipCorrectly(String path) {
        File f = new File(path);
        if (f.exists()) {
            return true;
        }
        return false;
    }

    private void copyFileToOfflineDirectory(String path) {
        File parent = new File(path);

        Collection<File> paths = new ArrayList<File>();

        FileHandler.addTree(new File(path), paths);

        File file = ((ArrayList<File>) paths).get(0);

        String courseName =file.getName();

        try {
            FileHandler.copyDirectoryOneLocationToAnotherLocation(file, new File(getApplicationContext().getExternalFilesDir(null) + DirectoryConstants.offline + courseName));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ShareBluetoothActivity.this, getString(R.string.notreceivedproperly), Toast.LENGTH_SHORT).show();
        }

    }

    private void removeZipFile(String path) {
        File zipped = new File(zipReceiveLecturePath);
        zipped.delete();

        File unzipped = new File(path);
        if (unzipped.exists()) {
            FileHandler.deleteRecursive(unzipped);
        }
    }


    // After Sending the file


    private void tidyZippedFile() {
        File f = new File(zipSendLectureZipPath);
        if (f.exists()) {
            FileHandler.deleteRecursive(f);
        }
    }


    private void tidyAnyExtaFiles() {
        File f;
        if (zipSendLectureZipPath != null) {
            f = new File(zipSendLectureZipPath);
            if (f.exists()) {
                FileHandler.deleteRecursive(f);
            }
        }


        if (zipReceiveLecturePath != null) {
            f = new File(zipReceiveLecturePath);

            if (f.exists()) {
                FileHandler.deleteRecursive(f);
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////




    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Utilities

    public static final <T> T nthElement(Iterable<T> data, int n) {
        int index = 0;
        for (T element : data) {
            if (index == n) {
                return element;
            }
            index++;
        }
        return null;
    }



    public void speak(String string) {
        textToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void configureTextToSpeech() {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(ShareBluetoothActivity.this, getString(R.string.languageNotSupported), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ShareBluetoothActivity.this, getString(R.string.initializationFailedSST), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}