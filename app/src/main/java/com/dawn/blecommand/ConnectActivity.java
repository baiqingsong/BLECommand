package com.dawn.blecommand;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.dawn.blelibrary.BLEManage;

public class ConnectActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        BLEManage.singleInstance(this).setListener(new BLEManage.OnBLEListener() {
            @Override
            public void deviceFind(BluetoothDevice device) {
                Log.i("dawn", "device find");
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

        findViewById(R.id.tv_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BLEManage.singleInstance(ConnectActivity.this).writeData("AA4000BB");
            }
        });
        BLEManage.singleInstance(this).setHeadEndStr("AA", "BB");
//        findViewById(R.id.tv_test).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bleManage.writeData("|T|0|");
//            }
//        });
//        bleManage.setSendType(BLEManage.TypeTransport.ascii);
//        bleManage.setReceiverType(BLEManage.TypeTransport.ascii);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BLEManage.singleInstance(this).disconnectBLE();
    }
}
