package JCHo.com.cc2541.temperaturetag.function;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.lang.reflect.Method;

import JCHo.com.cc2541.temperaturetag.data.GlobalData;
import JCHo.com.cc2541.temperaturetag.service.BluetoothService;
import JCHo.com.cc2541.temperaturetag.service.GlobalService;

public class Function {

    //－－－－－－－－－－－－－↓<Notify Write Read>↓－－－－－－－－－－－－

    /*public static void sendReadRequest(){
        final BluetoothGattCharacteristic Characteristic = BluetoothService.getCharacteristicByUUID(GlobalData.HRS_UUID,GlobalData.BSL_UUID);
        if(Characteristic != null){
            final int charaProp = Characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                GlobalService.BleService.readCharacteristic(Characteristic);
            }
        }
    }*/ //nRF UART 沒有read

    public static void sendWriteRequest(String writeData){
        final BluetoothGattCharacteristic Characteristic = BluetoothService.getCharacteristicByUUID(GlobalData.SERVICE_UUID,GlobalData.TX_UUID);
        if(Characteristic != null){
            final int charaProp = Characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                Characteristic.setValue(writeData);
                Characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                GlobalService.BleService.writeCharacteristic(Characteristic);
            }
        }
    }

    public static void enableNotify(){
        Log.i(GlobalData.tag,"enable notification");
        final BluetoothGattCharacteristic Characteristic = BluetoothService.getCharacteristicByUUID(GlobalData.SERVICE_UUID,GlobalData.TX_UUID);
        if(Characteristic != null){
            final int charaProp = Characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                GlobalService.BleService.setCharacteristicNotification(Characteristic, true);
            }
        }
    }

    //---------------------------↓ <Byte String Text 轉換> ↓---------------------------

    public static byte[] HexToBytes(String hexString) {//將16進位字串轉成byte[]
        char[] hex = hexString.toCharArray();
        //轉rawData長度減半
        int length = hex.length / 2;
        byte[] rawData = new byte[length];
        for (int i = 0; i < length; i++) {
            //先將hex資料轉10進位數值
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            //將第一個值的二進位值左平移4位,ex: 00001000 => 10000000 (8=>128)
            //然後與第二個值的二進位值作聯集ex: 10000000 | 00001100 => 10001100 (137)
            int value = (high << 4) | low;
            //與FFFFFFFF作補集
            if (value > 127)
                value -= 256;
            //最後轉回byte就OK
            rawData [i] = (byte) value;
        }
        return rawData ;
    }

    public static String ByteToHex(byte[] b) { //byte[]轉成16進位字串
        String hs = "";
        String stmp;
        for (byte aB : b) {
            stmp = (Integer.toHexString(aB & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    public static String HexToString(String hex){//將16進位字串轉換為對應文字
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i+=2) {
            String str = hex.substring(i, i+2);
            output.append((char)Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public static String ByteArrayToString(byte[] Value){
        final String hex_string = ByteToHex(Value);//將byte[]格式的value轉成16進位字串
        return HexToString(hex_string);
    }

    //---------------------------↓ <藍牙配對> ↓---------------------------
    public static void pairDevice(BluetoothDevice device) {//建立綁定藍牙裝置
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unPairDevice(BluetoothDevice device) {//解除綁定藍牙裝置
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
