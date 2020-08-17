package com.dawn.blelibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.List;
import java.util.UUID;

public class BLEManage {
    private Context mContext;
    private OnBLEListener mListener;
    private BluetoothAdapter mBluetoothAdapter;//获取系统蓝牙适配器管理类
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothGatt mBluetoothGatt;
    private TypeTransport typeReceiver = TypeTransport.hex;//接收类型
    private TypeTransport typeSend = TypeTransport.hex;//发送类型
    public enum TypeTransport{
        hex, ascii
    }
    private String receiverStr = "";
    private String headStr;//接收字符串的头信息
    private String endStr;//接收字符串的尾信息
    private final static int h_ble_connect = 0x101;
    private final static int d_ble_connect = 100;//先停止搜索，再连接
    private final static int h_ble_stop_search = 0x102;
    private final static int d_ble_stop_search = 10 * 1000;//搜索10秒后自动停止
    private final static int h_join_str = 0x103;//字符串拼接
    private final static int d_join_str = 20;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case h_ble_connect:
                    BluetoothDevice device = (BluetoothDevice) msg.obj;
                    connectGatt(device);
                    break;
                case h_ble_stop_search:
                    stopSearchBluetooth();
                    break;
                case h_join_str:
                    if(receiverStr.length() > 0){
                        operateReceiverStr();
                    }
                    break;
            }
        }
    };
    private static BLEManage bleManage;
    private BLEManage(Context context){
        this.mContext = context;
    }
    public static BLEManage singleInstance(Context context){
        if(bleManage == null)
            bleManage = new BLEManage(context);
        return bleManage;
    }

    public void setListener(OnBLEListener listener){
        this.mListener = listener;
    }

    /**
     * 搜索蓝牙的回调
     */
    private BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int arg1, byte[] arg2) {
            if(mListener != null)
                mListener.deviceFind(device);
        }
    };

    /**
     * 设置返回类型
     * @param receiverType 返回类型
     */
    public void setReceiverType(TypeTransport receiverType){
        this.typeReceiver = receiverType;
    }

    /**
     * 设置发送类型
     * @param sendType 发送类型
     */
    public void setSendType(TypeTransport sendType){
        this.typeSend = sendType;
    }

    /**
     * 设置接收字符串的头尾数据
     * @param headStr 头字符串
     * @param endStr 尾字符串
     */
    public void setHeadEndStr(String headStr, String endStr){
        this.headStr = headStr;
        this.endStr = endStr;
    }

    /**
     * ble的初始化
     * @return 是否初始化成功
     */
    public boolean initBLE(){
        BluetoothManager mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            return false;
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        return mBluetoothAdapter != null;
    }
    /**
     * 搜索设备
     */
    public void startSearchBLE(){
        mHandler.sendEmptyMessageDelayed(h_ble_stop_search, d_ble_stop_search);//默认10秒后停止搜索
        if(mBluetoothAdapter != null)
            mBluetoothAdapter.startLeScan(callback);
    }
    /**
     * 停止搜索设备
     */
    public void stopSearchBluetooth(){
        if(mBluetoothAdapter != null)
            mBluetoothAdapter.stopLeScan(callback);
    }
    /**
     * 设备连接，对外开放，先停止搜索功能，再进行设备连接
     * @param device 需要连接的设备
     */
    public synchronized boolean connectBLE(BluetoothDevice device){
        if(device == null)
            return false;
        stopSearchBluetooth();
        mHandler.removeMessages(h_ble_connect);
        Message msg = new Message();
        msg.what = h_ble_connect;
        msg.obj = device;
        mHandler.sendMessageDelayed(msg, d_ble_connect);
        return true;
    }

    /**
     * ble关闭连接
     */
    public void disconnectBLE(){
        if(mBluetoothGatt != null){
            mBluetoothGatt.close();
            if(mListener != null)
                mListener.printDeviceState("disconnect ble");
        }
    }

    /**
     * 设备连接
     * @param device 要连接的设备
     */
    private void connectGatt(BluetoothDevice device){
        disconnectBLE();
        mBluetoothGatt = device.connectGatt(mContext, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if(newState == BluetoothGatt.STATE_CONNECTED){
                    if(mListener != null){
                        mListener.deviceState(true);
                        mListener.printDeviceState("on connect state change connected");
                    }
                    gatt.discoverServices();
                }else if(newState == BluetoothGatt.STATE_DISCONNECTED){
                    if(mListener != null){
                        mListener.deviceState(false);
                        mListener.printDeviceState("on connect state change disconnected");
                    }
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if(status == BluetoothGatt.GATT_SUCCESS){
                    if(mListener != null)
                        mListener.printDeviceState("on services discovered");
                    // 默认先使用 B-0006/TL8266 服务发现
                    List<BluetoothGattService> services = gatt.getServices();
                    UUID serviceUuid = null;
                    BluetoothGattService service = null;
                    for(int i = 0; i < services.size(); i ++){
                        String serviceUuidStr = services.get(i).getUuid().toString();
                        if(serviceUuidStr.contains("fee0") || serviceUuidStr.contains("FEE0")){
                            serviceUuid = services.get(i).getUuid();
                            break;
                        }
                    }
                    if(serviceUuid != null){
                        service = gatt.getService(serviceUuid);
                    }
                    if (service!=null){
                        //找到服务，继续查找特征值
                        UUID characteristicUuid = null;
                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                        for(int i = 0;i < characteristics.size(); i ++){
                            String characteristicsUuidStr = characteristics.get(i).getUuid().toString();
                            if(characteristicsUuidStr.contains("fee1") || characteristicsUuidStr.contains("FEE1")){
                                characteristicUuid = characteristics.get(i).getUuid();
                                break;
                            }
                        }
                        if(characteristicUuid != null){
                            mNotifyCharacteristic = service.getCharacteristic(characteristicUuid);
                            mWriteCharacteristic = service.getCharacteristic(characteristicUuid);
                            if(mNotifyCharacteristic != null)
                                setCharacteristicNotification(mNotifyCharacteristic);
                        }else{
                            if(mListener != null)
                                mListener.deviceConnectError("没有获取到对应的特征值");
                        }
                    }else{
                        if(mListener != null)
                            mListener.deviceConnectError("没有获取到对应服务信息");
                    }

                }else{
                    if(mListener != null)
                        mListener.deviceConnectError("没有发现服务");
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if(mListener != null)
                        mListener.printDeviceState("on characteristic write success");
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

                byte[] bytes = characteristic.getValue();
                String receiverStr = null;
                switch (typeReceiver){
                    case hex:
                        receiverStr = BLEUtil.toHexString(bytes);
                        break;
                    case ascii:
                        receiverStr = new String(bytes);
                        break;
                }
                getReceiverStr(receiverStr);
                mListener.printDeviceState("on characteristics changed " + receiverStr);
            }
        });
    }
    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     */
    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            if(mListener != null)
                mListener.deviceConnectError("BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(BleSppGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    /**
     * 获取到接收字符串
     * @param receiverStr 接收的字符串
     */
    private void getReceiverStr(String receiverStr){
        mHandler.removeMessages(h_join_str);
        this.receiverStr += receiverStr;
        mHandler.sendEmptyMessageDelayed(h_join_str, d_join_str);
    }
    /**
     * 处理接收字符串
     * 时间间隔接收数据，然后处理
     */
    private void operateReceiverStr(){
        if(headStr == null && endStr == null){
            if(mListener != null)
                mListener.getDeviceContent(receiverStr);
            receiverStr = "";
        }else{
            if(headStr == null){
                if(mListener != null)
                    mListener.getDeviceContent(receiverStr);
                receiverStr = "";
                return;
            }
            int headIndex = receiverStr.indexOf(headStr);
            if(headIndex != -1){
                if(endStr == null){
                    if(mListener != null)
                        mListener.getDeviceContent(receiverStr.substring(headIndex));
                    receiverStr = "";
                }else{
                    int endIndex = receiverStr.lastIndexOf(endStr);
                    if(endIndex != -1 && headIndex < endIndex){//当前字符串有结尾
                        if(mListener != null)
                            mListener.getDeviceContent(receiverStr.substring(headIndex, endIndex) + endStr);
                        receiverStr = receiverStr.substring(endIndex + endStr.length());
                    }
                }

            }

        }
    }

    /**
     * 发送消息
     * @param dataStr 发送的内容
     */
    public void writeData(String dataStr, TypeTransport typeSend) {
        if(dataStr == null || dataStr.trim().length() == 0)
            return;
        byte[] data = null;
        switch (typeSend){
            case ascii:
                data = dataStr.getBytes();
                break;
            case hex:
                data = BLEUtil.toByteArray(dataStr);
                break;
        }
        if(mBluetoothGatt == null){
            if(mListener != null)
                mListener.printDeviceState("bluetooth gatt is null");
            return;
        }
        if ( mWriteCharacteristic != null) {
            mWriteCharacteristic.setValue(data);
            mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
        }

    }

    /**
     * 发送信息
     * @param dataStr 发送的内容
     */
    public void writeData(String dataStr){
        this.writeData(dataStr, typeSend);
    }

    /**
     * 蓝牙相关的回调函数
     */
    public interface OnBLEListener{
        void deviceFind(BluetoothDevice device);
        void deviceState(boolean isConnect);
        void deviceConnectError(String msg);
        void getDeviceContent(String content);
        void printDeviceState(String stateMsg);
    }
}
