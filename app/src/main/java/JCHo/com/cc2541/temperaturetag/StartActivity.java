package JCHo.com.cc2541.temperaturetag;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import JCHo.com.cc2541.temperaturetag.data.GlobalData;

public class StartActivity extends Activity {

    private BluetoothAdapter bluetoothAdapter;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_COARSE_LOCATION = 2;

    private Activity myActivity = StartActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "您的手機並不支援BLE", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "您的手機並沒有藍牙設備", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        checkLocationPermission();
    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    //--------------------------------↓ 檢查允許存取裝置位置資訊 ↓----------------------------------

    @TargetApi(Build.VERSION_CODES.M)
    private void checkLocationPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(GlobalData.tag,"no access coarse location permission");
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_COARSE_LOCATION);
        } else {
            checkBluetoothPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_PERMISSION_COARSE_LOCATION :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //獲得權限
                    checkBluetoothPermission(); //繼續檢查下一個權限
                } else { //拒絕權限
                    MyToast(getString(R.string.need_permission_access_coarse_location));
                    finish();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //--------------------------------↑ 檢查允許存取裝置位置資訊 ↑----------------------------------

    //--------------------------------↓ 檢查藍牙是否開啟 ↓----------------------------------

    private void checkBluetoothPermission(){
        if (!bluetoothAdapter.isEnabled()) {//isEnabled()判斷藍牙是否打開，未打開則開啟詢問視窗
            Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);//連結onActivityResult
        }
        else{
            GoToScanActivity();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                checkBluetoothPermission();
            } else {
                GoToScanActivity();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //--------------------------------↑ 檢查藍牙是否開啟 ↑----------------------------------

    private void GoToScanActivity(){
        Handler delayHandler = new Handler();//---延遲過0.5秒
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setClass(StartActivity.this, ScanActivity.class);
                startActivity(intent);
                finish();
            }
        }, 500);//---------延遲0.5秒
    }

    private void MyToast(String string) {
        Toast.makeText(myActivity, string, Toast.LENGTH_SHORT).show();
    }
}
