package JCHo.com.cc2541.temperaturetag;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import JCHo.com.cc2541.temperaturetag.adapters.BleDevicesAdapter;
import JCHo.com.cc2541.temperaturetag.data.GlobalData;

public class ScanActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private BleDevicesAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeScanner mLEScanner;
    private Boolean Scanning = false;
    private static BluetoothDevice deviceSelected;

    private static SwipeRefreshLayout swipeRefreshLayout;
    private static Boolean SwipeIsOn = false;
    private static final long SCAN_PERIOD = 2000;

    private ScanSettings settings;
    private List<ScanFilter> filters;

    private ListView Device_listView;

    @Override
    public void onRefresh() {
        if (!SwipeIsOn) {
            SwipeIsOn = true;
            settingFilters();
            scanLeDevice(true);
        }
    }

    private AbsListView.OnScrollListener onListScroll = new AbsListView.OnScrollListener() {
        //監聽list是否已經滑至頂端
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0) {//如果已經滑至頂端，則可開啟刷新滾輪
                swipeRefreshLayout.setEnabled(true);
            } else {
                swipeRefreshLayout.setEnabled(false);
            }
        }
    };

    private AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            deviceSelected = mLeDeviceListAdapter.getDevice(position);
            if (deviceSelected == null)
                return;
            MyToast("你選擇的是 "+deviceSelected.getName());

            GlobalData.deviceAddress = deviceSelected.getAddress();
            GlobalData.deviceName = deviceSelected.getName();

            final Intent intent2 = new Intent(ScanActivity.this, MainActivity.class);
            startActivity(intent2);
            finish();

            //Function.pairDevice(deviceSelected);//綁定
        }
    };

    private View.OnTouchListener onListTouch = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                scanLeDevice(false);
                return true;
            }
            return false;
        }
    };

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Log.i(GlobalData.tag,"BroadcastReceiver : bonded "+deviceSelected.getAddress());

                    GlobalData.deviceAddress = deviceSelected.getAddress();
                    GlobalData.deviceName = deviceSelected.getName();

                    final Intent intent2 = new Intent(ScanActivity.this, MainActivity.class);
                    startActivity(intent2);
                    finish();
                }
            }
        }
    };

    @Override
    public void onBackPressed() {//返回鍵事件
        if (SwipeIsOn) {
            swipeRefreshLayout.setRefreshing(false);
            SwipeIsOn = false;
            scanLeDevice(false);
        } else {
            ScanActivity.this.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        setTitle("綁定您的Q Body");
        findViews();
        setListener();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);//動態檢查藍芽狀態
        registerReceiver(mReceiver, filter);//動態檢查藍芽狀態

        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//監聽綁定狀態
        registerReceiver(mPairReceiver, intent);

        swipeRefreshLayout.post(new Runnable() {//進入頁面自動跳出更新提示滾輪
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                SwipeIsOn = true;
                settingFilters();
                scanLeDevice(true);
            }
        });
    }


    private void findViews() {
        Device_listView = (ListView) findViewById(R.id.device_listView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        assert swipeRefreshLayout != null;
        swipeRefreshLayout.setColorSchemeResources(//更新滾輪顏色
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light,
                android.R.color.holo_purple);
        swipeRefreshLayout.setSize(0);//0為大尺寸滾輪
    }

    private void setListener() {
        Device_listView.setOnScrollListener(onListScroll);//判定是否以滑到List的頂端或末端
        Device_listView.setOnItemClickListener(onItemClick);
        Device_listView.setOnTouchListener(onListTouch);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);//動態檢查藍芽狀態
        unregisterReceiver(mPairReceiver);//動態檢查藍芽狀態
    }



    private void setAdapter() {
        if (mLeDeviceListAdapter != null) {
            Device_listView.setAdapter(mLeDeviceListAdapter);
        } else {
            List<String> data = new ArrayList<>();
            data.add("您的附近搜尋不到Smart In Bag\n請確認您的Q Body是否開啟\n並重新掃描");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ScanActivity.this, R.layout.no_data_listview_item, data);
            Device_listView.setAdapter(adapter);
        }
    }

    //---------------------------------↓BLE API21 Scan↓-----------------------------------------

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if(Scanning){
                        mLEScanner.stopScan(mScanCallback);
                        Scanning = false;
                        swipeRefreshLayout.setRefreshing(false);
                        SwipeIsOn = false;
                        setAdapter();
                    }
                }
            }, SCAN_PERIOD);

            mLeDeviceListAdapter = new BleDevicesAdapter(getBaseContext());
            mLEScanner.startScan(filters, settings, mScanCallback);
            Scanning = true;

        } else {
            mLEScanner.stopScan(mScanCallback);
            Scanning = false;
            swipeRefreshLayout.setRefreshing(false);
            SwipeIsOn = false;
            setAdapter();
        }
    }

    private void settingFilters() {
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        filters = new ArrayList<>();
        ParcelUuid HRS = new ParcelUuid(GlobalData.SERVICE_UUID);//UUID改這裡!!!!
        filters.add(new ScanFilter.Builder().setServiceUuid(HRS).build());//設定過濾條件
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            mLeDeviceListAdapter.addDevice(result.getDevice(), result.getRssi());
            setAdapter();//掃描時，動態顯示Rssi
            mLeDeviceListAdapter.notifyDataSetChanged();
            //BluetoothDevice device = result.getDevice();
            //Log.i(GlobalData.tag, "*********************************");
            //Log.i(GlobalData.tag, "Device name: " + device.getName());
            //Log.i(GlobalData.tag, "Device address: " + device.getAddress());
            //Log.i(GlobalData.tag, "Device service UUIDs: " + device.getUuids());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    //--------------------------------↓進入頁面檢查藍芽↓-----------------------------------------
    private void checkBluetooth(){
        if (!mBluetoothAdapter.isEnabled()) {//isEnabled()判斷藍牙是否打開，未打開則開啟詢問視窗
            Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);//連結onActivityResult
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode != MainActivity.RESULT_OK) {//如果按的不是"是"，持續檢查藍牙是否開啟
                    MyToast(getString(R.string.need_bluetooth));
                    checkBluetooth();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
    //--------------------------------↑進入頁面檢查藍芽↑-----------------------------------------

    //----------------------------------↓監聽藍芽狀態↓-------------------------------------------
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        MyToast(getString(R.string.bluetooth_off));
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //MyToast(getString(R.string.bluetooth_turn_off));
                        checkBluetooth();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        MyToast(getString(R.string.bluetooth_on));
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        //MyToast(getString(R.string.bluetooth_turn_on));
                        break;
                }
            }
        }
    };
    //----------------------------------↑監聽藍芽狀態↑-------------------------------------------

    private void MyToast(String string) {
        Toast.makeText(ScanActivity.this, string, Toast.LENGTH_SHORT).show();
    }
}
