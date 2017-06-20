package JCHo.com.cc2541.temperaturetag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import JCHo.com.cc2541.temperaturetag.data.GlobalData;
import JCHo.com.cc2541.temperaturetag.database.MyDBHelper;
import JCHo.com.cc2541.temperaturetag.database.Spot;
import JCHo.com.cc2541.temperaturetag.service.BluetoothService;
import JCHo.com.cc2541.temperaturetag.service.GlobalService;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;
import static JCHo.com.cc2541.temperaturetag.data.GlobalData.alertString;
import static JCHo.com.cc2541.temperaturetag.data.GlobalData.alertSwitch;
import static JCHo.com.cc2541.temperaturetag.data.GlobalData.betteryLevel;
import static JCHo.com.cc2541.temperaturetag.data.GlobalData.notification;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static int counter = 0;

    private TextView Message;
    private TextView Distance;
    private TextView TempText;
    private TextView AlertData;
    private Button AlertEdit;
    private ToggleButton AlertSwitch;

    private MyDBHelper helper;
    private static final int REQUEST_ENABLE_BT = 1;
    private List<Spot> spotList;
    private int index;
    double alertValue = 0;
    BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Activity myActivity = MainActivity.this;
    List<double[]> x = new ArrayList<double[]>(); // 點的x坐標
    List<double[]> y = new ArrayList<double[]>(); // 點的y坐標
    double[] xaxi = {};
    double[] yaxi = {};
    double tempmax, tempmin, countmax,countmin;

    XYMultipleSeriesDataset dataset;

    //----------------------------↓動態由Service傳值回Activity↓------------------------------
    private void setHandler(){

        GlobalService.handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg){
                super.handleMessage(msg);
                if(msg.what==1){
                    Bundle data = msg.getData();
//                    notification = data.getString("Notification");
                    String byteNotify;
                    byteNotify = data.getString("Notification");
                    String s = "" + byteNotify;
//                    String s = Byte.toString(byteNotify);
//                    int i = 0;
//                    for (byte b : byteNotify)
//                        i = byteNotify & 0xff;
//                    System.out.printf("################################ %d",i);
                   //String value = new String(byteNotify);
//                    String s = String.valueOf(i);
//                    String value = new String(byteNotify, "UTF-8");
                    notification = s;
//                    Log.e("##############",notification);
                    double newNotification;
                    try {
                        newNotification = Double.parseDouble(notification);
                        newNotification = newNotification;
                    }catch (Exception e){newNotification = 0.0;}
//                    MyToast("Get notification : "+notification+ "°C");
                    //if(newNotification>20) {
                        TempText.setText(notification + "°C");
                    if (newNotification < 15){
                        TempText.setTextColor(Color.parseColor("#0072E3"));
                    }else if (newNotification <30){
                        TempText.setTextColor(Color.parseColor("#73BF00"));
                    }else if (newNotification <37){
                        TempText.setTextColor(Color.parseColor("#000000"));
                    }else{
                        TempText.setTextColor(Color.parseColor("#EA0000"));
                    }
                        // 數值X,Y坐標值輸入
                        xaxi = insertElement(xaxi, xaxi.length, xaxi.length);
//                        double xaxi_duble = xaxi.length;
                        x.clear();
                        x.add(xaxi);
                        yaxi = insertElement(yaxi, newNotification, yaxi.length);
                        yaxi = average(yaxi);
                        y.clear();
                        y.add(yaxi);

                        NumberFormat nf = new DecimalFormat("0.#");
                        System.out.println("x");
                        for (double speed : xaxi) {
                            System.out.printf(nf.format(speed)+" ");
                        }
                        System.out.println("y");
                        for (double speed : yaxi) {
                            System.out.printf(nf.format(speed)+" ");
                        }
                        drawPicture();
                        saveData(notification);
                    //}else{
                    //    TempText.setText("Remeasuring ...");
                    //}
                    BluetoothService.startReadRssi(true);



                    if (alertValue != 0 && alertSwitch ==1){
                        if(newNotification > alertValue ){
                            MyToast("發燒了");
                            alertNotification();
                    }

                }}
                if(msg.what==2){
                    Bundle data = msg.getData();
                    String read_result = data.getString("ReadResult");
                    MyToast("Get read result : "+read_result);
                }
                if(msg.what==3){
                    Bundle data = msg.getData();
                    int rssi = data.getInt("Rssi");
                    String t = String.valueOf(RssiToDistance(-58,rssi));
                    t = t.substring(0, t.indexOf(".") + 3);
                    String string = "距離 : "+ t + "m" + "   "+ "電量：" + betteryLevel + "%";
                    Distance.setText(string);
                }

            }
        };
    }
    //----------------------------↑動態由Service傳值回Activity↑------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        drawPicture();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        checkBluetooth();
        findView();

        Message.setText(GlobalData.deviceName + " 已連線");
        if(helper == null){
            helper = new MyDBHelper((this));
            MyToast("Create SQLite");
        }
    }

    private void findView() {
        Message = (TextView) findViewById(R.id.main_message);
        Distance = (TextView) findViewById(R.id.rssi_dis);
        AlertData = (TextView) findViewById(R.id.alert_data);
        AlertEdit = (Button) findViewById(R.id.alert_edit);
        AlertSwitch = (ToggleButton) findViewById(R.id.alert_switch);
        TempText = (TextView) findViewById(R.id.temp_text);

    }

    private void setListener(){
        //ReadTest.setOnClickListener(onReadClickListener);
        AlertEdit.setOnClickListener(onAlertClickListener);
//        NotifyTest.setOnClickListener(onNotifyClickListener);
        TempText.setOnClickListener(onHistoryClickListener);
        AlertSwitch.setOnClickListener(onAlertSwitchClickListener);
    }

    private View.OnClickListener onReadClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            counter++;
            if(counter%2==1){
                BluetoothService.startReadRssi(true);
            } else {
                BluetoothService.startReadRssi(false);
            }
        }
    };

    private View.OnClickListener onAlertClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
//            Function.sendWriteRequest(WriteData.getText().toString());
            setAlert();
//
//            try {
//
//            }catch (Exception e){MyToast("請輸入正確溫度 (如27.8度)");}
//            drawPicture();
        }
    };

    private View.OnClickListener onAlertSwitchClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (AlertSwitch.isChecked()) {
                alertSwitch = 1;
                if (alertString == null){
                    Toast.makeText(MainActivity.this, "請先設定警示溫度", Toast.LENGTH_SHORT).show();
                    AlertSwitch.setChecked(false);
                    alertSwitch = 0;
                }else {
                    Toast.makeText(MainActivity.this, "開啟警示功能", Toast.LENGTH_SHORT).show();
                    AlertData.setText("警示 : " + alertString + "°C");
                }
            }
            // 當按鈕再次被點擊時候響應的事件
            else {
                Toast.makeText(MainActivity.this, "關閉警示功能", Toast.LENGTH_SHORT).show();
                alertSwitch = 0;
                AlertData.setText("未設定警示溫度");
            }
        }
    };

//    private View.OnClickListener onNotifyClickListener = new View.OnClickListener(){
//        @Override
//        public void onClick(View v) {
//            //Function.enableNotify();
//        }
//    };

    private View.OnClickListener onHistoryClickListener = new View.OnClickListener(){
        @Override
        public  void onClick(View v){
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        }
    };


    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());//監聽 BLE GATT 狀態
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);//監聽藍芽狀態
        registerReceiver(mReceiver, filter);//監聽藍芽狀態

        setListener();
        setHandler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(gattUpdateReceiver);//監聽 BLE GATT 狀態
        unregisterReceiver(mReceiver);//監聽藍芽狀態

        BluetoothService.startReadRssi(false);

        GlobalService.doUnbindService(myActivity);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void saveData(String tempture){
        String time = getTime();
        String temp = tempture;

        Spot spot = new Spot(time, temp);
        long rowId = helper.insert(spot);
        if(rowId != -1){
//            MyToast("InsertSuccess");
        }else{
            MyToast("InsertFail");
        }
        helper.close();
    }

    public String getTime(){
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        String date = sDateFormat.format(new java.util.Date());
        Log.e("date = ", date);
        return date;
    }
    public void drawPicture(){

        // 畫曲線圖
        LinearLayout drawLayout = (LinearLayout)findViewById(R.id.gridLinearLayout);
        //drawLayout.removeAllViews();
        String[] titles = new String[] { "溫度曲線"}; // 定義折線的名稱

        NumberFormat nf = new DecimalFormat("0.#");
        System.out.println("x");
        for (double speed : xaxi) {
            System.out.printf(nf.format(speed)+" ");
        }
        System.out.println("y");
        for (double speed : yaxi) {
            System.out.printf(nf.format(speed)+" ");
        }

//       dataset = buildDatset(titles,x,y);
//        dataset.clear();
        dataset = buildDatset(titles, x, y); // 儲存座標值;
        int[] colors = new int[] { Color.BLACK};// 折線的顏色
        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE}; // 折線點的形狀
        XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles, true);


        tempmax = largest(yaxi)*1.1;
        tempmin = miniest(yaxi)*0.9;
        countmax = xaxi.length;
        countmin = xaxi.length - 5;
        if(countmin<5){
            countmin = 0;
        }
        setChartSettings(renderer, "", "", "", countmin, countmax, tempmin, tempmax, Color.BLACK);// 定義折線圖
        View chart = ChartFactory.getLineChartView(this, dataset, renderer);
        chart.refreshDrawableState();
//        refresh(titles, x, y, chart);
        drawLayout.removeAllViews();
        drawLayout.addView(chart,new LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
//        setContentView(chart);

        //畫曲線圖 end
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private double[] average(double[] array){
        double sumoften = 0;
        int length = array.length;
        if (length > 5){
            length = 5;
        }

        for (int x = 0 ; x < length ; x++){
//            array[length-1] = (array[length-1]+array[x])*0.5;
//            把全部溫度的最後一個做平均
            sumoften += array[(array.length-1)-x];
        }
        array[array.length-1] = sumoften/length;
        DecimalFormat df = new DecimalFormat("0.00");
        String str = df.format(array[array.length-1]);
        array[array.length-1] = Double.parseDouble(str);

        return array;
    }

    //---------------------------------↓開啟程式檢查藍芽↓-----------------------------------------
    private void checkBluetooth(){
        if (!myBluetoothAdapter.isEnabled()) {//isEnabled()判斷藍牙是否打開，未打開則開啟詢問視窗
            Intent intentBtEnabled = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intentBtEnabled, REQUEST_ENABLE_BT);//連結onActivityResult
        }
        else{
            GlobalService.doBindService(MainActivity.this);//已打開則連結service
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == MainActivity.RESULT_OK) {
                    GlobalService.doBindService(MainActivity.this);
                } else {//如果按"否"，持續檢查藍牙是否開啟
                    MyToast(getString(R.string.need_bluetooth));
                    checkBluetooth();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
    //---------------------------------↑開啟程式檢查藍芽↑-----------------------------------------

    //--------------------------------↓動態檢查藍芽狀態↓-------------------------------------------
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
                        GlobalService.doUnbindService(MainActivity.this);
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
    //--------------------------------↑動態檢查藍芽狀態↑-------------------------------------------
    private void MyToast(String string){
        Toast.makeText(MainActivity.this,string,Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothService.ACTION_GATT_CONNECTED.equals(action)) {
                MyToast("建立連結");
            } else if (BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) {
                MyToast("中斷連結");
                showDisconnectDialog(myActivity);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }

    public static int[] insertElement(int original[],int element, int index) {
        int length = original.length;
        int destination[] = new int[length + 1];
        System.arraycopy(original, 0, destination, 0, index);
        destination[index] = element;
        System.arraycopy(original, index, destination, index
                + 1, length - index);
        return destination;
    }

    public static double[] insertElement(double original[],double element, int index) {
        int length = original.length;
        double destination[] = new double[length + 1];
        System.arraycopy(original, 0, destination, 0, index);
        destination[index] = element;
        System.arraycopy(original, index, destination, index
                + 1, length - index);
        return destination;
    }

    private void connectDevice(){
        GlobalService.doBindService(myActivity);
    }

    private void disconnectDevice(){
        GlobalService.BleService.disconnect();
        GlobalService.doUnbindService(myActivity);
    }

    //－－－－－－－－－－－－－↓<GATT斷線警告>↓－－－－－－－－－－－－

    public void showDisconnectDialog(final Activity activity){
        GlobalService.doUnbindService(myActivity);
        //產生一個Builder物件
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.disconnect_title);
        builder.setMessage(R.string.disconnect_hint);
        builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                connectDevice();
                MyToast("連線中...");
            }
        });
        builder.setNegativeButton(R.string.end, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                activity.finish();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    // 定義折線圖名稱
    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
                                    String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor) {
        renderer.setBackgroundColor(Color.parseColor("#efefef"));//背景 灰白色
        renderer.setApplyBackgroundColor(true);// 设置背景颜色生效
        renderer.setMarginsColor(Color.parseColor("#efefef"));// // 边框外侧颜色
        renderer.setMargins(new int[] { 30, 20, 10, 20 });// 图形4边距
        renderer.setXLabels(3);
        renderer.setShowGrid(true); // 设置网格显示
        renderer.setGridColor(Color.parseColor("#eeeeee"));

        renderer.setChartTitle(title); // 折線圖名稱
        renderer.setChartTitleTextSize(100); // 折線圖名稱字形大小
        renderer.setXTitle(xTitle); // X軸名稱
        renderer.setYTitle(yTitle); // Y軸名稱
        renderer.setXAxisMin(xMin); // X軸顯示最小值
        renderer.setXAxisMax(xMax); // X軸顯示最大值
        renderer.setXLabelsColor(Color.BLACK); // X軸線顏色
        renderer.setYAxisMin(yMin); // Y軸顯示最小值
        renderer.setYAxisMax(yMax); // Y軸顯示最大值
        renderer.setAxesColor(axesColor); // 設定坐標軸顏色
        renderer.setYLabelsColor(0, Color.BLACK); // Y軸線顏色
        renderer.setLabelsColor(Color.BLACK); // 設定標籤顏色
        renderer.setLegendTextSize(30);//图例文字大小
        renderer.setLabelsTextSize(30);//设置刻度显示文字的大小(XY轴都会被设置)
        renderer.setMarginsColor(Color.WHITE); // 設定背景顏色
        renderer.setShowGrid(true); // 設定格線
        renderer.setAxisTitleTextSize(100);//軸標籤大小
        renderer.setPointSize(10);
        renderer.setChartTitleTextSize(100);//軸字體大小
        renderer.setZoomButtonsVisible(true);//是否显示放大缩小按钮
//        renderer.setGridLineWidth(50);
//        renderer.setShowGrid(true);
//        renderer.setGridColor(Color.YELLOW);

    }

    // 定義折線圖的格式
    private XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles, boolean fill) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        int length = colors.length;
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(colors[i]);
            r.setPointStyle(styles[i]);
            r.setFillPoints(fill);
            r.setLineWidth(10);
            r.setDisplayChartValues(true);
            r.setChartValuesTextSize(50);
//            r.setDisplayChartValues(true);
//            r.setAnnotationsTextSize(20);
            renderer.addSeriesRenderer(r); //將座標變成線加入圖中顯示
        }
        return renderer;
    }

    // 資料處理
    private XYMultipleSeriesDataset buildDatset(String[] titles, List<double[]> xValues,
                                                List<double[]> yValues) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        int length = titles.length; // 折線數量
        for (int i = 0; i < length; i++) {
            // XYseries對象,用於提供繪製的點集合的資料
            XYSeries series = new XYSeries(titles[i]); // 依據每條線的名稱新增
            double[] xV = xValues.get(i); // 獲取第i條線的資料
            double[] yV = yValues.get(i);
            int seriesLength = xV.length; // 有幾個點

            for (int k = 0; k < seriesLength; k++) // 每條線裡有幾個點
            {
                series.add(xV[k], yV[k]);
            }
            dataset.addSeries(series);
        }
        return dataset;
    }

    public void refresh(String[] titles, List<double[]> xValues, List<double[]> yValues, View view){
        cleardata();
        for (int i = 0; i < xValues.size(); i++) {
            XYSeries series= new XYSeries(titles[i]);
            double[] xV = xValues.get(i);
            double[] yV = yValues.get(i);
            int seriesLength = xV.length;
            for (int k = 0; k < seriesLength; k++) {
                series.add(xV[k], yV[k]);
            }
            dataset.addSeries(series);
            view.refreshDrawableState();
        }
//   view.repaint();
    }

    public static double largest(double arr[])
    {
        double max=arr[0];         // <-從此開始
        for(int i=0;i<arr.length;i++)
            if(max<arr[i])
                max=arr[i];
        return max;
    }

    public static double miniest(double arr[])
    {
        double min=arr[0];         // <-從此開始
        for(int i=0;i<arr.length;i++)
            if(min>arr[i])
                min=arr[i];
        return min;
    }

    public void cleardata(){
        while(dataset.getSeries().length > 0){
            XYSeries series= dataset.getSeries()[0];
            dataset.removeSeries(series);
            series.clear();
        }
    }

    public void setAlert(){
        final View item = LayoutInflater.from(MainActivity.this).inflate(R.layout.alert_dialog, null);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("溫度警示")
                .setView(item)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText alertEditText = (EditText) item.findViewById(R.id.alertEditText);
//                        Toast.makeText(getApplicationContext(), alertEditText.getText().toString(), Toast.LENGTH_SHORT).show();
                        alertString = alertEditText.getText().toString();
                        MyToast("設定的警示溫度為" + alertString + "°C");
                        alertValue = Double.parseDouble(alertString);
                    }
                })
                .show();
    }

public void alertNotification(){
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

// 设置通知的基本信息：icon、标题、内容
    builder.setSmallIcon(R.drawable.ic_menu_alert);
    builder.setContentTitle("藍牙溫度計");
    builder.setContentText("目前量測溫度為"+notification+"°C");
// 设置通知的优先级
    builder.setPriority(NotificationCompat.PRIORITY_MAX);
    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
// 设置通知的提示音
    builder.setSound(alarmSound);



// 设置通知的点击行为：这里启动一个 Activity
    Intent intent = new Intent(this, MainActivity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    builder.setContentIntent(pendingIntent);

    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(id, builder.build());

// 发送通知 id 需要在应用内唯一
//    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//    notificationManager.notify(id, builder.build());
}


    protected static double RssiToDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

}
