package hardik124.silencio.database;

import android.net.Uri;
import android.provider.BaseColumns;

public class DB_Contract {
    public static final String PACKAGE = "hardik124.silencio";
    public static final String PATH_PLACES = "places";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + PACKAGE);

    private DB_Contract() {
    }

    public static final class PlacesTable implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLACES).build();
        public static final String COLOUMN_PLACE_ID = "place_ID";
        public static final String COLOUMN_PLACE_Name = "place_NAME";
        public static final String COLOUMN_PLACE_Address = "place_ADDRESS";

        public static String TABLE_NAME = "places";
    }
}

