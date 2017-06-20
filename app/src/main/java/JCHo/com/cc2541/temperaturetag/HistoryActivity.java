package JCHo.com.cc2541.temperaturetag;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import JCHo.com.cc2541.temperaturetag.adapters.HistoryAdapter;
import JCHo.com.cc2541.temperaturetag.database.MyDBHelper;
import JCHo.com.cc2541.temperaturetag.database.Spot;

/**
 * Created by 10411024 on 2016/11/02 (002).
 */

public class HistoryActivity extends AppCompatActivity {

    private MyDBHelper helper;
    private List<Spot> spotList;
    private SimpleAdapter simpleAdapter;
    private HistoryAdapter historyAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_main);

        if (helper == null) {
            helper = new MyDBHelper(this);
        }


    }

    @Override
    public void onStart(){
        super.onStart();
        spotList = helper.getAllSpots();
        showSpots();
    }

    public void showSpots(){
        ListView list = (ListView) findViewById(R.id.list);

        int index = spotList.size();
        Log.e("spotSize","value = " + index);
        String[] dateArray = new String[index];
        String[] infoArray = new String[index];

//        for (int x = 0; x<dateArray.length  ; x++ ) {
//            Spot spot = spotList.get(x);
//            dateArray[dateArray.length - x - 1] = spot.getTime(); //倒過來存, 最新的才會在上面
//            infoArray[infoArray.length - x - 1] = spot.getInfo();
//            Log.e("x","value = " + x);
//            Log.e("getTime",dateArray[x]);
//        }

        List<Map<String,String>> items = new ArrayList<Map<String, String >>();
        for(int x=0; x<index ; x++){
            Map<String,String> item = new HashMap<String, String>();
            Spot spot = spotList.get((index-1) - x);
            item.put("date",spot.getTime());
            item.put("info",spot.getInfo());
            items.add(item);
        }
        historyAdapter = new HistoryAdapter(this, items, R.layout.simplp_adapter, new String[]{"date","info"},
                new int[]{R.id.textDate,R.id.textInfo});
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,dateArray);
        list.setAdapter(historyAdapter);

//        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
//                android.R.layout.simple_expandable_list_item_2,
//                c,
//                new String[] {"date", "info"},
//                new int[] {android.R.id.text1, android.R.id.text2},
//                0);
//        list.setAdapter(adapter);
    }
}
