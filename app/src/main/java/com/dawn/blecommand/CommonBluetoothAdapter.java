package com.dawn.blecommand;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

public class CommonBluetoothAdapter extends ArrayAdapter<BluetoothDevice> {
    private CallBackListener mListener;
    private List<BluetoothDevice> mData = new ArrayList<>();
    private LayoutInflater inflater;
    public CommonBluetoothAdapter(Context context,  @Nullable List<BluetoothDevice> data, CallBackListener listener) {
        super(context, R.layout.item_bluetooth);
        mData.addAll(data);
        this.mListener = listener;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return this.mData.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final BluetoothDevice device = this.mData.get(position); // 获取当前项的Fruit实例
        View view = inflater.inflate(R.layout.item_bluetooth, null);//实例化一个对象
        TextView tvName = view.findViewById(R.id.tv_name);
        tvName.setText(device.getName());
        view.findViewById(R.id.item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null)
                    mListener.clickItem(device);
            }
        });
        return view;
    }

    public void refreshData(List<BluetoothDevice> devices){
        this.mData.clear();
        this.mData.addAll(devices);
        notifyDataSetChanged();
    }

    public interface CallBackListener{
        void clickItem(BluetoothDevice device);
    }

}
