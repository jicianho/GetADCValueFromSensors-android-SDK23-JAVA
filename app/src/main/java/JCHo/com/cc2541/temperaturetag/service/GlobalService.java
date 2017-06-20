package JCHo.com.cc2541.temperaturetag.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import JCHo.com.cc2541.temperaturetag.data.GlobalData;

public class GlobalService {
    public static BluetoothService BleService;
    public static Handler handler;
    public static boolean isBound;

    public static ServiceConnection BlueConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BleService = ((BluetoothService.ServiceBinder)service).getService();

            if (!BleService.initialize())
            {
                Log.e(GlobalData.tag, "Unable to initialize Bluetooth");
            }
            BleService.connect(GlobalData.deviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            BleService = null;
        }
    };
    public static void doBindService(Activity activity){
        if(!GlobalService.isBound){
            Intent intent = new Intent(activity, BluetoothService.class);
            activity.bindService(intent, GlobalService.BlueConnection, Context.BIND_AUTO_CREATE);
            GlobalService.isBound = true;
            Log.i(GlobalData.tag,"doBindService");
        }
    }
    public static void doUnbindService(Activity activity){
        if(GlobalService.isBound){
            activity.unbindService(GlobalService.BlueConnection);
            GlobalService.isBound = false;
            Log.i(GlobalData.tag,"doUnBindService");
        }
    }
}