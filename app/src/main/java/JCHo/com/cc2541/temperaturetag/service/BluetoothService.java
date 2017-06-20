package JCHo.com.cc2541.temperaturetag.service;

import android.app.Service;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Binder;
import android.os.Message;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import JCHo.com.cc2541.temperaturetag.data.GlobalData;
import JCHo.com.cc2541.temperaturetag.function.Function;

import static JCHo.com.cc2541.temperaturetag.data.GlobalData.betteryLevel;
import static JCHo.com.cc2541.temperaturetag.data.GlobalData.notifyCount;
import static JCHo.com.cc2541.temperaturetag.data.GlobalData.notifyTemp;

public class BluetoothService extends Service {
    private final IBinder binder = new ServiceBinder();

    public static Handler handler;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    public static BluetoothGatt mBluetoothGatt;
    //private int mConnectionState = STATE_DISCONNECTED;

    private static Timer requestRssiTimer;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    @Override
    // 第一次啟動Service時會呼叫onCreate()
    public void onCreate() {
        super.onCreate();
        Log.i(GlobalData.tag,"start provide service");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(GlobalData.tag,"stop provide service");
        //requestRssiTask.stopTask();
        //requestRssiTimer.cancel();

        close();
    }

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        //mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.i(GlobalData.tag, "Connected to GATT server.");
                        Log.i(GlobalData.tag, "Attempting to start service discovery:" + gatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        //mConnectionState = STATE_DISCONNECTED;
                        Log.i(GlobalData.tag, "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.i(GlobalData.tag, "onServicesDiscovered success" );
                        Function.enableNotify();
                    } else {
                        Log.w(GlobalData.tag, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Characteristic notification
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    //super.onCharacteristicChanged(gatt, characteristic);
                    final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(GlobalData.CLIENT_CHARACTERISTIC_CONFIG);
                    final boolean notifications = ( descriptor == null || descriptor.getValue() == null || descriptor.getValue().length != 2 || descriptor.getValue()[0] == 0x01 );

                    if (notifications) {
                        if (notifyCount == 2){
                            betteryLevel = characteristic.getIntValue(0x11, 0);
                            Log.e(GlobalData.tag, "Get betteryLevel : " + betteryLevel); // 正常值
                            //betteryLevel = (betteryLevel)/256*1240;
                            betteryLevel =  (betteryLevel - 145) * 100 / 72;
                            if (betteryLevel > 100){
                                betteryLevel = 100;
                            }else if (betteryLevel <0){
                                betteryLevel = 0;
                            }
                            notifyCount = 0;
                        }
                        else if (notifyCount == 1) {

                            String sum = notifyTemp +  Function.ByteToHex(characteristic.getValue());
                            double sumToDec = Integer.parseInt(sum.trim(), 16 );
                            sumToDec /= 1.022; //adc值修正
                            sumToDec = (sumToDec)/8191*1240;
                            //double degree = 211.1491 - 0.1921*sumToDec;
                            double degree = ADCConverter.ADCToDegree(sumToDec, 0.8);
                            String s = String.valueOf(degree);
                            String t = s.substring(0, s.indexOf(".") + 3);
                            final String notification = t;
//                        final String notification = ""+characteristic.getIntValue(0x11,0);
                            Log.e(GlobalData.tag, "Get notification1 : " + Function.ByteToHex(characteristic.getValue()));
                            Log.e(GlobalData.tag, "Get notification2 : " + characteristic.getIntValue(0x11, 0)); // 正常值
                            Log.e(GlobalData.tag, "Get notification3 : " + characteristic.getIntValue(0x11, 1));
                            Log.e(GlobalData.tag, "Get notification4 : " + Function.ByteArrayToString(characteristic.getValue()));
                            Log.e(GlobalData.tag, "Get notification5 : " + characteristic.getIntValue(0x12, 0));
                            Log.e(GlobalData.tag, "Get notification Temp : " + notifyTemp);
                            Log.e(GlobalData.tag, "Get notification a set - hex : " + notifyTemp +  Function.ByteToHex(characteristic.getValue()));
//                            Log.e(GlobalData.tag, "Get notification a set - dec : " + Integer.parseInt(t.trim(), 16 ));

                            Log.e(GlobalData.tag, "ADC = " + degree);
                            getNotification(notification);
                            notifyCount += 1 ;
                        }else{
                            Log.e(GlobalData.tag, "Get nottify MSB 1 : " + Function.ByteToHex(characteristic.getValue()));
                            Log.e(GlobalData.tag, "Get nottify MSB 2 : " + characteristic.getIntValue(0x11, 0)); // 正常值
                            notifyTemp = Function.ByteToHex(characteristic.getValue());
                            notifyCount += 1;
                        }
                    } else {
                        Log.i(GlobalData.tag,"Get notification error ");
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                    Log.i(GlobalData.tag,"Receive Read Result");

                    //Log.i(GlobalData.tag,"Read Raw data : "+ read_result_hex);
                    Log.i(GlobalData.tag,"Read Result : "+ Function.ByteArrayToString(characteristic.getValue()));
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        getReadResult(Function.ByteArrayToString(characteristic.getValue()));
                    }
                }

                //add from nRF BleManger
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status){
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.i(GlobalData.tag, "Data written to " + characteristic.getUuid()+" success , value: " + Function.ByteArrayToString(characteristic.getValue()));
                        // The value has been written. Notify the manager and proceed with the initialization queue.
                        //onCharacteristicWrite(gatt, characteristic);
                        //nextRequest();
                    } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                        if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
                            Log.i(GlobalData.tag, "Phone has lost bonding information");
                            //mCallbacks.onError(ERROR_AUTH_ERROR_WHILE_BONDED, status);
                        }
                    } else {
                        Log.i(GlobalData.tag, "onCharacteristicWrite error " + status);
                        //onError(ERROR_READ_CHARACTERISTIC, status);
                    }
                }

                @Override
                public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status){
                    super.onReadRemoteRssi(gatt, rssi, status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //获取到RSSI，
                        getRssi(rssi);
                        //通过mBluetoothGatt.readRemoteRssi();来获取
                    }

                }


            };


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public boolean initialize() {
        Log.i(GlobalData.tag,"initialize BluetoothManager and BluetoothAdapter");
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(GlobalData.tag, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(GlobalData.tag, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        Log.i(GlobalData.tag, "prepare to connect "+address);
        if (mBluetoothAdapter == null || address == null) {
            Log.w(GlobalData.tag, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(GlobalData.tag, "Trying to use an existing mBluetoothGatt for connection.");
            return mBluetoothGatt.connect();
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(GlobalData.tag, "Device not found.  Unable to connect.");
            return false;
        }

        /**
         *@param autoConnect Whether to directly connect to the remote device (false)
         *                    or to automatically connect as soon as the remote
         *                    device becomes available (true).
         */

        final boolean autoConnect = false;
        mBluetoothGatt = device.connectGatt(this, autoConnect, mGattCallback);
        Log.d(GlobalData.tag, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        //mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(GlobalData.tag, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();

    }

    //After using a given BLE device, the app must call this method to ensure resources are released properly.
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(GlobalData.tag, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        /*if (GlobalData.HRM_UUID.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(GlobalData.CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
            Log.i(TAG, "Enables notification on HRM");
        }*/
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(GlobalData.CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        //final BluetoothGatt gatt = mBluetoothGatt;
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(GlobalData.tag, "BluetoothAdapter not initialized");
            return;
        }
        Log.i(GlobalData.tag, "Reading characteristic " + characteristic.getUuid());
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    //add from nRF BleManager.java
    public boolean writeCharacteristic(final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(GlobalData.tag, "BluetoothAdapter not initialized");
            return false;
        }
        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0) {
            Log.w(GlobalData.tag, "Characteristic property error");
            return false;
        }

        Log.i(GlobalData.tag, "Writing characteristic " + characteristic.getUuid());
        return gatt.writeCharacteristic(characteristic);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public static List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }

    public static BluetoothGattCharacteristic getCharacteristicByUUID(UUID ServiceUUID, UUID CharacteristicUUID){
        //Log.i(GlobalData.tag,"Find Characteristic By UUID");
        final List<BluetoothGattService> gattServices = getSupportedGattServices();
        if(gattServices != null){
            for (BluetoothGattService gattService : gattServices){
                if(gattService.getUuid().equals(ServiceUUID)){
                    final List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    if(gattCharacteristics != null){
                        for (BluetoothGattCharacteristic Characteristic : gattCharacteristics){
                            if(Characteristic.getUuid().equals(CharacteristicUUID)){
                                return Characteristic;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    //------------------------------↓讀取RSSI數值↓------------------------------------------
    //透過Timer持續送出讀取RSSI請求
    public static void startReadRssi(boolean enable){
        if(enable){
            requestRssiTimer = new Timer(true);
            requestRssiTimer.schedule(new requestRssiTask(), 250, 5000);//0.25秒後開始，且0.25秒執行一次
        } else {
            if(requestRssiTimer != null){
                requestRssiTask.stopTask();
                requestRssiTimer.cancel();
            }
        }
    }

    public static class requestRssiTask extends TimerTask
    {
        public void run()
        {
            mBluetoothGatt.readRemoteRssi();//送出讀取RSSI請求
            //會透過BluetoothGattCallback onReadRemoteRssi回傳資料，最終由handler送出數值，並於activity接收
        }
        public static void stopTask(){//任務完成，終止Timer
            requestRssiTimer.cancel();
        }
    }
    //------------------------------↓讀取RSSI數值↓------------------------------------------


    //----------------------------↓ServiceConnection↓--------------------------------------
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    //service與activity間互動
    public class ServiceBinder extends Binder {
        //呼叫gService()可以取得Service
        //如此client才可以跟Service互動
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }
    @Override
    // 沒有任何client連結Service時會呼叫此方法
    public boolean onUnbind(Intent intent){
        return true;
    }
    //----------------------------↑ServiceConnection↑--------------------------------------


    //-------------------------↓透過handler與activity互動↓----------------------------------
    private void getNotification(String notification){
        Message msg = new Message();
        msg.what = 1;
        Bundle bundle = new Bundle();
        bundle.putString("Notification", notification);
        msg.setData(bundle);
        GlobalService.handler.sendMessage(msg);
    }

    private void getReadResult(String read_result){
        Message msg = new Message();
        msg.what = 2;
        Bundle bundle = new Bundle();
        bundle.putString("ReadResult", read_result);
        msg.setData(bundle);
        GlobalService.handler.sendMessage(msg);
    }

    private void getRssi(int rssi){
        Message msg = new Message();
        msg.what = 3;
        Bundle bundle = new Bundle();
        bundle.putInt("Rssi", rssi);
        msg.setData(bundle);
        GlobalService.handler.sendMessage(msg);
    }
}
