package JCHo.com.cc2541.temperaturetag.service;

import android.util.Log;

/**
 * Created by 10411024 on 2017/01/18 (018).
 */

public class ADCConverter {
    public static double Vs0 = 1098.611;
    public static double Vs10 = 1047.245;
    public static double Vs20 = 995.461;
    public static double Vs30 = 943.499;
    public static double Vs40 = 891.330;
    public static double Vs50 = 838.883;
    public static double Vs60 = 786.189;
    public static double Vs70 = 733.243;
    public static double Vs80 = 680.065;
    public static double Vs90 = 626.646;
    public static double Vs100 = 572.940;

    public static double ADCToDegree(double adc, double k){
        double degree, b, m, v1=0.0, v2 = 0.0;
        int tMin = 0;

        if (adc >= Vs0){ //0度以下
            tMin = 0;
            v1 = Vs0;
            v2 = Vs10;
        }
        else if (adc >= Vs10) {
            tMin = 10;
            v1 = Vs10;
            v2 = Vs20;
        }
        else if (adc >= Vs20) {
            tMin = 20;
            v1 = Vs20;
            v2 = Vs30;
        }
        else if (adc >= Vs30) {
            tMin = 30;
            v1 = Vs30;
            v2 = Vs40;
        }
        else if (adc >= Vs40) {
            tMin = 40;
            v1 = Vs40;
            v2 = Vs50;
        }
        else if (adc >= Vs50) {
            tMin = 50;
            v1 = Vs50;
            v2 = Vs60;
        }
        else if (adc >= Vs60) {
            tMin = 60;
            v1 = Vs60;
            v2 = Vs70;
        }
        else if (adc >= Vs70) {
            tMin = 70;
            v1 = Vs70;
            v2 = Vs80;
        }
        else if (adc >= Vs80) {
            tMin = 80;
            v1 = Vs80;
            v2 = Vs90;
        }
        else{
            tMin = 90;
            v1 = Vs90;
            v2 = Vs100;
        }

        m = 10 / (v1-v2);
        b = tMin + (m * v1);
        degree = (b - m*adc)*k; // k is adjusting
        Log.e("ADCToDegree", "adc ="+adc);
        Log.e("ADCToDegree", "V1,V2 =" + v1 + "," + v2);
        Log.e("ADCToDegree", "b - m * Vtao =" + b + "-" + m + "*" + adc);
        Log.e("ADCToDegree", "degree ="+degree);
        return degree;
    }
}


