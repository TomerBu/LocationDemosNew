package tomerbu.edu.locationdemos;

import android.app.IntentService;
import android.content.Intent;

public class MyGeoFenceService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p>
     * name Used to name the worker thread, important only for debugging.
     */
    public MyGeoFenceService() {
        super(MyGeoFenceService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
