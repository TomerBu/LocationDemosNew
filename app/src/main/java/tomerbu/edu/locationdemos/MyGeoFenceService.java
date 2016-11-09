package tomerbu.edu.locationdemos;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

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
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()){
            Log.d(Constants.TAG, String.format("Error code: %d", geofencingEvent.getErrorCode()));
            return;
        }

        Location location = geofencingEvent.getTriggeringLocation();
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        switch (geofenceTransition){
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d(Constants.TAG, "Exit" + location.toString());
                break;
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.d(Constants.TAG, "Enter" + location.toString());
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(Constants.TAG, "Dwell" + location.toString());
                break;
        }
    }
}
