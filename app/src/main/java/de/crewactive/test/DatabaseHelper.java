package de.crewactive.test;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to help make different operations with SQLite DB
 */
public class DatabaseHelper extends SQLiteOpenHelper {


    public static final String PLAYER_TABLE = "PLAYER_TABLE";
    public static final String COLUMN_PLAYER_NAME = "PLAYER_NAME";
    public static final String COLUMN_PLAYER_TIME = "PLAYER_TIME";
    public static final String COLUMN_ID = "ID";

    /**
     * Constructor von DatabaseHelper
     *
     * @param context to initialize Class DatabaseHelper
     */
    public DatabaseHelper(@Nullable Context context) {
        super(context, "player.db", null, 1);
    }

    /**
     * Creates a Table for players
     *
     * @param db android db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + PLAYER_TABLE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_PLAYER_NAME + " TEXT, " + COLUMN_PLAYER_TIME + " TEXT)";
        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Adds one Player on SQLite db and returns boolean, true if Player successful added and false if failure
     *
     * @param playerModel model of Player
     */
    public boolean addOne(PlayerModel playerModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_PLAYER_NAME, playerModel.getId());
        cv.put(COLUMN_PLAYER_NAME, playerModel.getName());
        cv.put(COLUMN_PLAYER_TIME, playerModel.getTime());

        long insert = db.insert(PLAYER_TABLE, null, cv);
        return insert != -1;
    }

    /**
     * Removes one Player from SQLite db
     *
     * @param playerModel model of Player Object
     */
    public boolean deleteOne(PlayerModel playerModel) {
        SQLiteDatabase db = this.getReadableDatabase();
        String queryString = "DELETE FROM " + PLAYER_TABLE + " WHERE " + COLUMN_ID + " = " + playerModel.getId();

        Cursor cursor = db.rawQuery(queryString, null);
        return cursor.moveToFirst();
    }

    /**
     * Gets all players records from db and returns sorted list
     */
    public List<PlayerModel> getEveryOne() {
        List<PlayerModel> returnList = new ArrayList<>();

        String queryString = "SELECT * FROM " + PLAYER_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            // loop throw the results
            do {
                int id = cursor.getInt(0);
                String playerName = cursor.getString(1);
                String playerTime = cursor.getString(2);

                PlayerModel newPlayer = new PlayerModel(id, playerName, playerTime);
                returnList.add(newPlayer);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Collections.sort(returnList);
        return returnList;
    }

}
