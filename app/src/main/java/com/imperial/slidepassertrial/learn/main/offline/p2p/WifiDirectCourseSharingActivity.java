package com.imperial.slidepassertrial.learn.main.offline.p2p;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.ListFragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.imperial.slidepassertrial.R;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class WifiDirectCourseSharingActivity extends AppCompatActivity{

    // Permission
    private boolean hasWifiDirectPermissions = false;
    private boolean hasReadWriteStorageAccess = false;
    private int READ_WRITE_PERMISSION = 2;
    private static final int WIFI_DIRECT_PERMISSIONS = 1;


    // UI Stuff
    private ImageButton button = null;
    private ListView devicesList = null;


    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private WifiP2pDevice device;

    // Wifi Stuff
    // Intent Filter
    private final IntentFilter intentFilter = new IntentFilter();

    // Connection
    private WifiP2pManager wifiManager = null;
    private WifiP2pManager.Channel wifiChannel = null;
    private WiFiDirectBroadcastReceiver receiver;
    private boolean wifiP2PEnabled = false;


    private String[] deviceNameArray;
    private WifiP2pDevice[] deviceArray;

    // Course Info
    private String courseName = null;
    private String coursePath = null;

    private WifiP2pConfig config;

    public void setIsWifiP2pEnabled(boolean state) {
        wifiP2PEnabled = state;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2p_discover_devices);

        // getIntent
        courseName = (String) getIntent().getExtras().get("courseName");
        coursePath = (String) getIntent().getExtras().get("coursePath");


        getPermissions();

        if (hasNecessaryPermissions()) {
            turnWifiOn();
            configureListView();
            configureWifiManager();
            getPeers();
        }

    }

    private void turnWifiOn() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {

            Toast.makeText(this, "Please Turn on Wifi", Toast.LENGTH_SHORT).show();
            Intent turnWifiOn = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(turnWifiOn);

        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////

    // Permissions
    private void getPermissions() {

        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Please allow access to Storage", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE_PERMISSION);
        } else {
            hasReadWriteStorageAccess = true;
        }
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, WIFI_DIRECT_PERMISSIONS);
        } else {
            hasWifiDirectPermissions = true;
        }

    }

    private boolean hasNecessaryPermissions() {
        return hasWifiDirectPermissions && hasWifiDirectPermissions;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WIFI_DIRECT_PERMISSIONS) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && (grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                    permissions[1].equals(Manifest.permission.ACCESS_WIFI_STATE) && (grantResults[1] == PackageManager.PERMISSION_GRANTED) &&
                    permissions[2].equals(Manifest.permission.CHANGE_WIFI_STATE) && (grantResults[2] == PackageManager.PERMISSION_GRANTED) &&
                    permissions[3].equals(Manifest.permission.INTERNET) && (grantResults[3] == PackageManager.PERMISSION_GRANTED) &&
                    permissions[4].equals(Manifest.permission.ACCESS_NETWORK_STATE) && (grantResults[4] == PackageManager.PERMISSION_GRANTED) &&
                    permissions[5].equals(Manifest.permission.ACCESS_COARSE_LOCATION) && (grantResults[5] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Access Granted", Toast.LENGTH_SHORT).show();

                hasWifiDirectPermissions = true;
            }
        }
        if (requestCode == READ_WRITE_PERMISSION) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    permissions[1].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasReadWriteStorageAccess = true;
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void configureListView() {
        devicesList = findViewById(R.id.list_devices);



        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connectToPeer(position);

            }

            private void connectToPeer(int position) {
                config = new WifiP2pConfig();
                final WifiP2pDevice peerDevice = peers.get(position);

                config.deviceAddress = peerDevice.deviceAddress;
                config.wps.setup = WpsInfo.PBC;

                if (ActivityCompat.checkSelfPermission(WifiDirectCourseSharingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    wifiManager.connect(wifiChannel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(WifiDirectCourseSharingActivity.this, "Successfully Connected to " + peerDevice.deviceName, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(WifiDirectCourseSharingActivity.this, "Connect failed. Retry.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private ArrayAdapterWifiP2PDevices getListAdapter() {
        return getListAdapter();
    }

    private void configureWifiManager() {

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifiChannel = wifiManager.initialize(this, getMainLooper(), null);

    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(wifiManager, wifiChannel, this, peerListListener);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregisterReceiver(receiver);
    }

    private void getPeers() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            wifiManager.discoverPeers(wifiChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(WifiDirectCourseSharingActivity.this, "Success", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(WifiDirectCourseSharingActivity.this, "Failure Error", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }


    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            peers.clear();
            peers.addAll(peerList.getDeviceList());

            devicesList.setAdapter(new ArrayAdapterWifiP2PDevices(WifiDirectCourseSharingActivity.this, R.layout.row_device, peers));
           if (peers.size() == 0) {
               Toast.makeText(WifiDirectCourseSharingActivity.this, "No Device Found", Toast.LENGTH_SHORT).show();
           } else {
           }

        }
    };

    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;
    }

    private String connectionStatus = new String();

    private WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            // InetAddress from WifiP2pInfo struct.
            String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

            // After the group negotiation, we can determine the group owner.
            if (info.groupFormed && info.isGroupOwner) {
                Toast.makeText(WifiDirectCourseSharingActivity.this, "Host", Toast.LENGTH_SHORT).show();
            } else if (info.groupFormed) {
                Toast.makeText(WifiDirectCourseSharingActivity.this, "Client", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public WifiP2pManager.ConnectionInfoListener getConnectionInfoListener() {
        return connectionInfoListener;
    }


}