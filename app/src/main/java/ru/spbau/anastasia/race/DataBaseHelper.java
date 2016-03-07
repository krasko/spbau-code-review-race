package ru.spbau.anastasia.race;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * The DataBaseHelper class provides creating and updating the data base table with the best score.
 */
public class DataBaseHelper extends SQLiteOpenHelper implements BaseColumns {

    /**
     * Column of the data base table with the best score.
     */
    public static final String SCORE_COLUMN = "score";

    /**
     * Name of the database table.
     */
    private static final String DATABASE_TABLE = "Scores";

    /**
     * Describes creating of database table Scores.
     */
    private static final String DATABASE_CREATE_SCRIPT = "create table "
            + DATABASE_TABLE + " (" + BaseColumns._ID + " integer primary key autoincrement, "
            + SCORE_COLUMN + " integer);";

    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }
}
