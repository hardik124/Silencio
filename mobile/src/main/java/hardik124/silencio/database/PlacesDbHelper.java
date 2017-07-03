package hardik124.silencio.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlacesDbHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "places.db";
    private static final int DATABSE_VERSION = 1;

    public PlacesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABSE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String PLACES_SQL_TABLE = "CREATE TABLE "+
                DB_Contract.PlacesTable.TABLE_NAME+" ("+
                DB_Contract.PlacesTable._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                DB_Contract.PlacesTable.COLOUMN_PLACE_ID+" TEXT NOT NULL,"+
                DB_Contract.PlacesTable.COLOUMN_PLACE_Name+" TEXT,"+
                DB_Contract.PlacesTable.COLOUMN_PLACE_Address+" TEXT NOT NULL,"+
                "UNIQUE ("+ DB_Contract.PlacesTable.COLOUMN_PLACE_ID+") ON CONFLICT REPLACE"+
                ")";
        sqLiteDatabase.execSQL(PLACES_SQL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ DB_Contract.PlacesTable.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
