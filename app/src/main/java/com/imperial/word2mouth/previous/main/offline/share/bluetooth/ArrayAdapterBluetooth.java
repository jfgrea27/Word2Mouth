package com.imperial.word2mouth.previous.main.offline.share.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.imperial.word2mouth.R;

import java.util.List;

public class ArrayAdapterBluetooth extends ArrayAdapter<BluetoothDevice> {


    private Context context;
    private int layout;
    private List<BluetoothDevice> devices;

    private ViewHolder holder;

    public ArrayAdapterBluetooth(@NonNull Context context, int resource, @NonNull List<BluetoothDevice> objects) {
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

            holder = new ArrayAdapterBluetooth.ViewHolder();
            holder.deviceNames = convertView.findViewById(R.id.device_name);

            convertView.setTag(holder);

        } else {
            holder = (ArrayAdapterBluetooth.ViewHolder) convertView.getTag();
        }

        holder.deviceNames.setText(devices.get(position).getName());


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

    public int getSize() {
        return devices.size();
    }

    public int getPosition(BluetoothDevice device){
        for (BluetoothDevice d : devices) {
            if (d.getAddress() == device.getAddress()) {
                return devices.indexOf(d);
            }
        }
        return -1;
    }

    public boolean contains(BluetoothDevice device) {
        return devices.contains(device);
    }

    public class ViewHolder {
        TextView deviceNames;

    }


    private abstract class ListDevices<B> implements List<BluetoothDevice> {

        private List<BluetoothDevice> devices;

        protected ListDevices(List<BluetoothDevice> list) {
            devices = list;

        }
        @Override
        public boolean contains(@Nullable Object o) {
            BluetoothDevice d = (BluetoothDevice) o;

            for (BluetoothDevice device : devices) {
                if (device.getAddress() == d.getAddress()) {
                    return false;
                }
            }
            return  true;
        }
    }
}
