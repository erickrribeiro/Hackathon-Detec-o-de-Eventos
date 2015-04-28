package erickribeiro.incidentdetector.databe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import erickribeiro.incidentdetector.R;

/**
 * Created by eribeiro on 25/04/15.
 * email: erick.ribeiro.16@gmail.com
 */

public class HistoryContract {
    public DataBaseDbHelper dataBaseDbHelper;
    public HistoryContract(Context context) {
        this.dataBaseDbHelper = new DataBaseDbHelper(context);
    }

    public static abstract class HistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "histoty";
        public static final String COLUMN_NAME_HISTORY_ID = "historyid";
        public static final String COLUMN_NAME_STATUS_INCIDENT = "status";
        public static final String COLUMN_NAME_DATA = "data";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + HistoryEntry.TABLE_NAME + " (" +
                    HistoryEntry._ID + " INTEGER PRIMARY KEY," +
                    HistoryEntry.COLUMN_NAME_HISTORY_ID + " integer auto increment, "+
                    HistoryEntry.COLUMN_NAME_STATUS_INCIDENT + " boolean ,  " +
                    HistoryEntry.COLUMN_NAME_DATA + " TEXT " +
            " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + HistoryEntry.TABLE_NAME;


    public long insert(boolean statusIncidente){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.dataBaseDbHelper.getWritableDatabase();

        SimpleDateFormat sdfDate = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String stringDate = sdfDate.format(now);

        // Create a new map of values, where column names are the keys
        Log.d("DATE", stringDate);

        ContentValues values = new ContentValues();
        values.put(HistoryEntry.COLUMN_NAME_STATUS_INCIDENT, statusIncidente);
        values.put(HistoryEntry.COLUMN_NAME_DATA, stringDate);

        return db.insert(HistoryEntry.TABLE_NAME, null,values);
    }

    public List<History> query() {
        List<History> histories =  new ArrayList<>();
        SQLiteDatabase db = this.dataBaseDbHelper.getReadableDatabase();
        String selectQuery = "SELECT  * FROM "+HistoryEntry.TABLE_NAME+" ORDER BY "+HistoryEntry.COLUMN_NAME_DATA+" DESC";

        Cursor cursor =  db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            while (cursor.moveToNext()) {

                SimpleDateFormat dateFormat = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss");
                Date convertedDate = new Date();

                try {
                    convertedDate = dateFormat.parse(cursor.getString(cursor.getColumnIndex(HistoryEntry.COLUMN_NAME_DATA)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                History history = new History(
                        R.drawable.checkv,
                        cursor.getInt(cursor.getColumnIndex(HistoryEntry.COLUMN_NAME_STATUS_INCIDENT)) == 1,
                        convertedDate);

                histories.add(history);
            }
        }
        return histories;

    }
}
