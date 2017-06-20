package JCHo.com.cc2541.temperaturetag.database;

import java.io.Serializable;

/**
 * Created by 10411024 on 2016/11/01 (001).
 */

public class Spot implements Serializable{
    private int id;
    private String time;
    private String info;

    public Spot(String time, String info){
        this(0, time, info);
    }

    public  Spot (int id, String time, String info){
        this.id = id;
        this.time = time;
        this.info = info;
    }

    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return id;
    }

    public void setTime(String time){
        this.time = time;
    }
    public String getTime(){
        return time;
    }

    public void setInfo(String info){
        this.info = info;
    }
    public String getInfo(){
        return info;
    }
}
