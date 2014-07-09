package com.sla.codurs.chas.activity;

import android.app.Activity;
import android.os.Bundle;

import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.sla.codurs.chas.R;


public class BaseActivity extends Activity {
    MapView mMapView;

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.addLayer(new ArcGISTiledMapServiceLayer("http://www.onemap.sg/ArcGIS/rest/services/BASEMAP/MapServer"));

        LocationDisplayManager ls = mMapView.getLocationDisplayManager();
        ls.start();


    }

    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }
}
