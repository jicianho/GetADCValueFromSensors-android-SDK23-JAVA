package JCHo.com.cc2541.temperaturetag.data;

import java.util.UUID;

public class GlobalData {
//89CA3486-B541-DD5A-2C16-714E343F0259
    //public static final UUID BM71_SERVICE_UUID = UUID.fromString("49535343-FE7D-4AE5-8FA9-9FAFD205E455");   //BM71 SERVICE UUID
    //public static final UUID BM71_TX_UUID = UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616");        //BM71 CHARACTERISTIC UUID (notify)

    public static final UUID SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");   //CC2541 SERVICE UUID
    public static final UUID TX_UUID = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");        //CC2541 CHARACTERISTIC UUID (notify)
    public static final UUID BET_UUID = UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb");        //CC2541 CHARACTERISTIC UUID (notify)
//    public static final UUID UART_SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");   //nRF UART SERVICE UUID
//    public static final UUID UART_TX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");        //nRF TX CHARACTERISTIC UUID (notify)
//    public static final UUID UART_RX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");        //nRF RX CHARACTERISTIC UUID (write)

    //public static final UUID HRS_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");  //HEART RATE SERVICE UUID
    //public static final UUID HRM_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");  //HEART RATE MEASUREMENT UUID (notify)
    //public static final UUID BSL_UUID = UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb");  //BODY SENSOR LOCATION UUID (read)
    //public static final UUID HRCP_UUID = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb"); //HEART RATE CONTROL POINT UUID (write)

    //public static final UUID BS_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb"); //BATTERY SERVICE UUID
    //public static final UUID BL_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"); //BATTERY LEVEL UUID

    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static String tag = "JC_BLE_App";

    public static String deviceAddress;
    public static String deviceName;
    public static String notification;
    public static String getString;
    public static int notifyCount = 0;
    public static String notifyTemp;
    public static int alertSwitch = 0;
    public static String alertString;
    public static int betteryLevel = 0;
}
