package com.dawn.blecommand;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dawn.blelibrary.BLEManage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BLEManage bleManage;
    private ListView listView;
    private CommonBluetoothAdapter mAdapter;
    private List<BluetoothDevice> devices = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.list_view);
        mAdapter = new CommonBluetoothAdapter(this, devices, new CommonBluetoothAdapter.CallBackListener() {
            @Override
            public void clickItem(BluetoothDevice device) {
                bleManage.connectBLE(device);
            }
        });
        listView.setAdapter(mAdapter);
        bleManage = new BLEManage(this, new BLEManage.OnBLEListener() {
            @Override
            public void deviceFind(BluetoothDevice device) {
                if(device.getName() != null && device.getName().trim().length() > 0 ){
                    addDevice(device);
                }
            }

            @Override
            public void deviceState(boolean isConnect) {
                Log.i("dawn", "device state : " + isConnect);
            }

            @Override
            public void deviceConnectError(String msg) {
                Log.e("dawn", "device connect error");
            }

            @Override
            public void getDeviceContent(String content) {
                Log.i("dawn", "get device content : " + content);
            }

            @Override
            public void printDeviceState(String stateMsg) {
                Log.i("dawn", "print device state : " + stateMsg);
            }
        });
        boolean getBleState = bleManage.initBLE();
        if(getBleState){
            bleManage.startSearchBLE();
        }else{
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        findViewById(R.id.tv_temperature).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleManage.writeData("|T|0|");
            }
        });
    }
    /**
     * 添加设备到列表中
     * @param device 获取设备
     */
    private synchronized void addDevice(BluetoothDevice device){
        boolean hasDevice = false;
        for(int i = 0; i < devices.size(); i ++){
            if(device.getAddress() != null && device.getAddress().equals(devices.get(i).getAddress())){
                hasDevice = true;
                break;
            }
        }
        if(!hasDevice && device.getAddress() != null){
            Log.i("dawn", "搜索到新设备:" + device.getName());
            devices.add(device);
            if(mAdapter != null){
                mAdapter.refreshData(devices);
            }
        }
    }
}
