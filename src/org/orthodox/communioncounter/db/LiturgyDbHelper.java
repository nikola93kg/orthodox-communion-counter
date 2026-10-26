package org.orthodox.communioncounter.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.orthodox.communioncounter.model.LiturgyRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper for the Holy Trinity Church Communion Chronicle.
 */
public class LiturgyDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "orthodox_records.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_LITURGIES = "liturgy_records";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_DATE_FORMATTED = "date_formatted";
    public static final String COLUMN_DAY_NAME = "day_name";
    public static final String COLUMN_FEAST_NAME = "feast_name";
    public static final String COLUMN_COUNT = "count";
    public static final String COLUMN_NOTES = "notes";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_LITURGIES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                    COLUMN_DATE_FORMATTED + " TEXT NOT NULL, " +
                    COLUMN_DAY_NAME + " TEXT, " +
                    COLUMN_FEAST_NAME + " TEXT NOT NULL, " +
                    COLUMN_COUNT + " INTEGER NOT NULL, " +
                    COLUMN_NOTES + " TEXT" +
                    ");";

    public LiturgyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Future migrations can be handled cleanly here
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LITURGIES);
        onCreate(db);
    }

    /**
     * Inserts a new liturgy communion entry into the database.
     * @return the row ID of the newly inserted row, or -1 on error
     */
    public long insertRecord(LiturgyRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, record.getTimestamp());
        values.put(COLUMN_DATE_FORMATTED, record.getDateFormatted());
        values.put(COLUMN_DAY_NAME, record.getDayName());
        values.put(COLUMN_FEAST_NAME, record.getFeastName());
        values.put(COLUMN_COUNT, record.getCount());
        values.put(COLUMN_NOTES, record.getNotes());

        return db.insert(TABLE_LITURGIES, null, values);
    }

    /**
     * Retrieves all records ordered chronologically (newest first).
     */
    public List<LiturgyRecord> getAllRecords() {
        List<LiturgyRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_LITURGIES + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                String dateFormatted = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_FORMATTED));
                String dayName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY_NAME));
                String feastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FEAST_NAME));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COUNT));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES));

                records.add(new LiturgyRecord(id, timestamp, dateFormatted, dayName, feastName, count, notes));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return records;
    }

    /**
     * Deletes a record by its row ID.
     */
    public boolean deleteRecord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_LITURGIES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    /**
     * Statistics container for total summary figures.
     */
    public static class SummaryStats {
        public final int totalCommunicants;
        public final int totalLiturgies;
        public final double averagePerLiturgy;

        public SummaryStats(int totalCommunicants, int totalLiturgies, double averagePerLiturgy) {
            this.totalCommunicants = totalCommunicants;
            this.totalLiturgies = totalLiturgies;
            this.averagePerLiturgy = averagePerLiturgy;
        }
    }

    /**
     * Computes the aggregated statistics across all liturgies.
     */
    public SummaryStats getSummaryStats() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*), SUM(" + COLUMN_COUNT + ") FROM " + TABLE_LITURGIES;

        int totalLiturgies = 0;
        int totalCommunicants = 0;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            totalLiturgies = cursor.getInt(0);
            totalCommunicants = cursor.getInt(1);
            cursor.close();
        }

        double average = totalLiturgies > 0 ? ((double) totalCommunicants / totalLiturgies) : 0.0;
        return new SummaryStats(totalCommunicants, totalLiturgies, average);
    }
}
