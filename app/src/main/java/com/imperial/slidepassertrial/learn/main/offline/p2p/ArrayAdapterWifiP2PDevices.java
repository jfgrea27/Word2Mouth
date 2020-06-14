package com.imperial.slidepassertrial.learn.main.offline.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.imperial.slidepassertrial.R;

import java.util.List;

public class ArrayAdapterWifiP2PDevices extends ArrayAdapter<WifiP2pDevice> {


    private Context context = null;
    private int layout = 0;
    private List<WifiP2pDevice> devices = null;

    private ViewHolder holder;

    public ArrayAdapterWifiP2PDevices(@NonNull Context context, int resource, @NonNull List<WifiP2pDevice> objects) {
        super(context, resource, objects);

        this.context = context;
        this.layout = resource;
        this.devices = objects;
    }



    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);

            holder = new ArrayAdapterWifiP2PDevices.ViewHolder();

            holder.deviceNames = convertView.findViewById(R.id.device_name);

            convertView.setTag(holder);
        } else {
            holder = (ArrayAdapterWifiP2PDevices.ViewHolder) convertView.getTag();
        }


        holder.deviceNames.setText(devices.get(position).deviceName);


        return convertView;
    }


    @Override
    public int getViewTypeCount() {

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    public class ViewHolder {
        TextView deviceNames;

    }
}
