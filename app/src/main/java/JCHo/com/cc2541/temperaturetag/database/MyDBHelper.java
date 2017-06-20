package JCHo.com.cc2541.temperaturetag.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10411024 on 2016/11/01 (001).
 */
public class MyDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "TempRecoder";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "Spot";
    private static final String COL_id = "id";
    private static final String COL_DATE = "date";
    private static final String COL_INFO = "info";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " ( " +
                    COL_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_DATE + " TEXT NOT NULL, " +
                    COL_INFO + " TEXT );";

    public MyDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public List<Spot> getAllSpots(){
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = {
                COL_id, COL_DATE, COL_INFO
        };

        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);

        List<Spot> spotList = new ArrayList<>();

        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String date = cursor.getString(1);
            String info = cursor.getString(2);

            Spot spot = new Spot(id, date, info);
            spotList.add(spot);
        }

        cursor.close();
        return spotList;
    }

    public Spot findByid(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] columns = {
                COL_DATE, COL_INFO
        };
        String selection = COL_id + " = ?;";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);

        Spot spot = null;

        if (cursor.moveToNext()) {
            String date = cursor.getString(0);
            String info = cursor.getString(1);

            spot = new Spot(id, date, info);
        }

        cursor.close();
        return spot;
    }
        public long insert(Spot spot){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, spot.getTime());
        values.put(COL_INFO, spot.getInfo());
        return db.insert(TABLE_NAME, null, values);
    }

    public int update(Spot spot){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, spot.getTime());
        values.put(COL_INFO, spot.getInfo());
        String whereClause = COL_id + " = ?;";
        String[] whereArgs = {Integer.toString(spot.getId())};

        return db.update(TABLE_NAME, values, whereClause, whereArgs);
    }

    public int deleteById(int id){
        SQLiteDatabase db = getWritableDatabase();

        String whereClause = COL_id + " = ?;";
        String[] whereArgs = {String.valueOf(id)};

        return db.delete(TABLE_NAME, whereClause, whereArgs);
    }
}
