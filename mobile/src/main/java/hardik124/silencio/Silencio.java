package hardik124.silencio;

import android.app.Application;
import android.content.Intent;

/**
 * Created by f390 on 8/8/17.
 */

public class Silencio extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this,StarterService.class));
    }
}
