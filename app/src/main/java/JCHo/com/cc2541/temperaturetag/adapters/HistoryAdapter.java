package JCHo.com.cc2541.temperaturetag.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

/**
 * Created by 10411024 on 2017/01/20 (020).
 */

public class HistoryAdapter extends SimpleAdapter {
    private int[] colors = new int[]{
            Color.parseColor("#B9B9FF"),
            Color.parseColor("#CECEFF"),
            Color.parseColor("#DDDDFF"),
            Color.parseColor("#ECECFF"),
            Color.parseColor("#FBFBFF")
    };
    public HistoryAdapter(Context context, List<? extends Map<String, ?>> data,
                          int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        // TODO Auto-generated constructor stub
    }
    /* (non-Javadoc)
     * @see android.widget.SimpleAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view = super.getView(position, convertView, parent);
        int colorPos = position%colors.length;
        view.setBackgroundColor(colors[colorPos]);
        return view;
    }
}
