package com.sla.codurs.chas.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.sla.codurs.chas.HTTP.GetAddressSearchRequest;
import com.sla.codurs.chas.HTTP.GetBreastCentre;
import com.sla.codurs.chas.HTTP.GetCervicalCentreRequest;
import com.sla.codurs.chas.HTTP.GetChasRequest;
import com.sla.codurs.chas.HTTP.GetQuitCenterRequest;
import com.sla.codurs.chas.HTTP.GetRetailPharmacyRequest;
import com.sla.codurs.chas.R;
import com.sla.codurs.chas.model.Address;
import com.sla.codurs.chas.model.BreastScreeningCentre;
import com.sla.codurs.chas.model.CervicalScreeningCentre;
import com.sla.codurs.chas.model.Chas;
import com.sla.codurs.chas.model.QuitCentre;
import com.sla.codurs.chas.model.RetailPharmacy;
import com.sla.codurs.chas.utils.AddressAdapter;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class BaseActivity extends Activity {
    MapView mMapView;
    LocationDisplayManager ls;
    int addressSet = 1;
    ListView result;
    ProgressDialog dialog;
    GraphicsLayer graphicsLayer;

    GraphicsLayer chasGraphicLayer;

    public static boolean addressEnd = false;

    public static ArrayList<Chas> chases = null;
    public static ArrayList<Address> addresses = null;
    public static ArrayList<BreastScreeningCentre> brestCentres = null;
    public static ArrayList<CervicalScreeningCentre> cervicalCentres = null;
    public static ArrayList<QuitCentre> quitCentres = null;
    public static ArrayList<RetailPharmacy> retailPharmacies = null;

    // The basemap switching menu items.
    MenuItem chasMenuItem = null;
    MenuItem breastScreeningMenuItem = null;
    MenuItem quiteCentresMenuItem = null;
    MenuItem cervicalScreeningMenuItem = null;
    MenuItem retailPharmaciesMenuItem = null;



    final MapOptions mOceansBasemap = new MapOptions(MapOptions.MapType.OCEANS);
    // The current map extent, use to set the extent of the map after switching basemaps.
    Polygon mCurrentMapExtent = null;
    private ViewGroup resultFrame;
    private Callout _callout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        result = (ListView) findViewById(R.id.addressResultList);
        resultFrame = (ViewGroup) findViewById(R.id.resultFrame);

        mMapView = (MapView) findViewById(R.id.map);
        ArcGISRuntime.setClientId("j9r0J2JIy8FFFfB8");
        mMapView.addLayer(new ArcGISTiledMapServiceLayer("http://www.onemap.sg/ArcGIS/rest/services/BASEMAP/MapServer"));

        // Set a listener for map status changes; this will be called when switching basemaps.
        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onStatusChanged(Object source, STATUS status) {
                if (STATUS.LAYER_LOADED == status) {
                    mMapView.setExtent(mCurrentMapExtent);
                }
            }
        });


        ls = mMapView.getLocationDisplayManager();
        ls.start();

        result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getLayersAroundSelectedLocation(addresses.get(position).getX(),addresses.get(position).getY());
            }
        });


        initMapListeners();
        //TODO do a dialog to open location service if not on
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items from the Menu XML to the action bar, if present.
        getMenuInflater().inflate(R.menu.base, menu);

        chasMenuItem = menu.getItem(0).getSubMenu().getItem(0);
        breastScreeningMenuItem = menu.getItem(0).getSubMenu().getItem(1);
        quiteCentresMenuItem = menu.getItem(0).getSubMenu().getItem(2);
        cervicalScreeningMenuItem = menu.getItem(0).getSubMenu().getItem(3);
        retailPharmaciesMenuItem = menu.getItem(0).getSubMenu().getItem(4);

        chasMenuItem.setChecked(true);

        //Create the search view
        final SearchView searchView = new SearchView(getActionBar().getThemedContext());
        searchView.setQueryHint("Search");


        menu.add(Menu.NONE, Menu.NONE, 1, "Search")
                .setIcon(R.drawable.search)
                .setActionView(searchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0) {

                } else {
                    // Do something when there's no input
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {

//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

//                setProgressBarIndeterminateVisibility(true);
                BaseActivity.addresses = null;
                addressSet=1;

                GetAddressSearchRequest searchRequest = new GetAddressSearchRequest(query, addressSet);
                new BackgroundTask().execute(searchRequest, searchRequest);

//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    public void run() {
//                        setProgressBarIndeterminateVisibility(false);
//                    }
//                }, 2000);

                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                resultFrame.setVisibility(View.GONE);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Save the current extent of the map before changing the map.
        mCurrentMapExtent = mMapView.getExtent();

        // Handle menu item selection.
        switch (item.getItemId()) {
            case R.id.chas_centers:
                chasMenuItem.setChecked(true);
                return true;
            case R.id.cervical_screening_centre:
                breastScreeningMenuItem.setChecked(true);
                return true;
            case R.id.breast_screening_centres:
                quiteCentresMenuItem.setChecked(true);
                return true;
            case R.id.quit_centres:
                cervicalScreeningMenuItem.setChecked(true);
                return true;
            case R.id.retail_pharmacy:
                retailPharmaciesMenuItem.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Displays the resut of the address search
    public void displayResult() {
        resultFrame.setVisibility(View.VISIBLE);
        AddressAdapter adapter = new AddressAdapter(getBaseContext(), R.layout.address_list_layout, BaseActivity.addresses);
        result.setAdapter(adapter);
        findViewById(R.id.resultFrame).setVisibility(View.VISIBLE);

    }

    //Hides the listview if address search is empty
    public void displayNoResult() {
        resultFrame.setVisibility(View.GONE);
        result.setAdapter(null);
        findViewById(R.id.resultFrame).setVisibility(View.GONE);
    }


    //Map listeners for GPS
    public void initMapListeners() {
        ls.setLocationListener(new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
             //  mMapView.zoomTo(ls.getPoint(), 12);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(0));
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(1));
            }

            @Override
            public void onProviderEnabled(String provider) {
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(0));
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(1));
            }

            @Override
            public void onProviderDisabled(String provider) {
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(0));
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(1));
            }
        });

        mMapView.setOnSingleTapListener(new OnSingleTapListener() {

            public void onSingleTap(float x, float y) {
                if(_callout != null){
                    _callout.hide();
                }
                else
                {
                    _callout = mMapView.getCallout();
                    _callout.setStyle(R.xml.callout_style);
                    //_callout.setContent(loadView(countyName,countyPop));
                }

                Graphic graphic = findClosestGraphic(x, y, graphicsLayer ,25);
                if(graphic == null){
                    //_quickStartLib.findAddressByXY(x, y);
                }
                else{
                    String message = "Lorem ipsum";
                    Map<String,Object> atts = graphic.getAttributes();
                    for(Map.Entry<String,Object> entry : atts.entrySet()){
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        message = message + key + ": " + value +"\n";
                    }
                    Point location = (Point) graphic.getGeometry();

                    _callout.setOffset(0, -15);
                    _callout.show(location, message(message));
                }

            }
        });
    }

    /*
	 * Customize the map Callout text
	 */
    private TextView message(String text) {

        final TextView msg = new TextView(this);
        msg.setText(text);
        msg.setTextSize(12);
        msg.setTextColor(Color.BLACK);
        return msg;

    }

    public Graphic findClosestGraphic(float x, float y, GraphicsLayer graphicsLayer,int tolerance){
        Graphic graphic = null;
        int[] graphicIDs = graphicsLayer.getGraphicIDs(x, y, tolerance);
        if (graphicIDs != null && graphicIDs.length > 0) {
            graphic = graphicsLayer.getGraphic(graphicIDs[0]);
        }

        return graphic;
    }



    public void zoomToGps(View v) {
        mMapView.zoomTo(ls.getPoint(), 14);

        int tolerance = 20;
    }

    public double[] getExtent(double x, double y)
    {
        Envelope env = new Envelope(mMapView.toMapPoint((float) x, (float) y), 100 * mMapView.getResolution(), 100 * mMapView.getResolution());
        double[] extent = new double[4];
        extent[0] = x-1000;
        extent[1] = y-1000;
        extent[2] = x+1000;
        extent[3] = y+1000;

        return extent;
    }
    public void getLayersAroundSelectedLocation(double x, double y) {
        double[] extent = getExtent(x, y);
        plotLocation(x,y);

        GetChasRequest searchRequest = new GetChasRequest(Double.toString(extent[0]), Double.toString(extent[1]), Double.toString(extent[2]), Double.toString(extent[3]));
        new GetLayersBackgroundTask().execute(searchRequest, searchRequest);

        if(breastScreeningMenuItem.isChecked()){
            GetBreastCentre searchRequest2 = new GetBreastCentre(Double.toString(extent[0]), Double.toString(extent[1]), Double.toString(extent[2]), Double.toString(extent[3]));
            new GetLayersBackgroundTask().execute(searchRequest2, searchRequest2);
        }
        if(cervicalScreeningMenuItem.isChecked()){
            GetCervicalCentreRequest searchRequest3 = new GetCervicalCentreRequest(Double.toString(extent[0]), Double.toString(extent[1]), Double.toString(extent[2]), Double.toString(extent[3]));
            new GetLayersBackgroundTask().execute(searchRequest3, searchRequest3);
        }
        if(quiteCentresMenuItem.isChecked()){
            GetQuitCenterRequest searchRequest4 = new GetQuitCenterRequest(Double.toString(extent[0]), Double.toString(extent[1]), Double.toString(extent[2]), Double.toString(extent[3]));
            new GetLayersBackgroundTask().execute(searchRequest4, searchRequest4);
        }

        if(retailPharmaciesMenuItem.isChecked()){
            GetRetailPharmacyRequest searchRequest5 = new GetRetailPharmacyRequest(Double.toString(extent[0]), Double.toString(extent[1]), Double.toString(extent[2]), Double.toString(extent[3]));
            new GetLayersBackgroundTask().execute(searchRequest5, searchRequest);
        }

    }

    public void getLayersAroundGPS(View v) {
        double[] extent = getExtent(ls.getPoint().getX(), ls.getPoint().getY());

        GetChasRequest searchRequest = new GetChasRequest(Double.toString(extent[0]), Double.toString(extent[1]), Double.toString(extent[2]), Double.toString(extent[3]));
        new GetLayersBackgroundTask().execute(searchRequest, searchRequest);

        if(breastScreeningMenuItem.isChecked()){
            GetBreastCentre searchRequest2 = new GetBreastCentre(Double.toString(extent[0]), Double.toString(extent[1]), Double.toString(extent[2]), Double.toString(extent[3]));
            new GetLayersBackgroundTask().execute(searchRequest2, searchRequest2);
        }
        if(cervicalScreeningMenuItem.isChecked()){
            GetCervicalCentreRequest searchRequest3 = new GetCervicalCentreRequest(Double.toString(extent[0]), Double.toString(extent[1]), Double.toString(extent[2]), Double.toString(extent[3]));
            new GetLayersBackgroundTask().execute(searchRequest3, searchRequest3);
        }
        if(quiteCentresMenuItem.isChecked()){
            GetQuitCenterRequest searchRequest4 = new GetQuitCenterRequest(Double.toString(extent[0]), Double.toString(extent[1]), Double.toString(extent[2]), Double.toString(extent[3]));
            new GetLayersBackgroundTask().execute(searchRequest4, searchRequest4);
        }

        if(retailPharmaciesMenuItem.isChecked()){
            GetRetailPharmacyRequest searchRequest5 = new GetRetailPharmacyRequest(Double.toString(extent[0]), Double.toString(extent[1]), Double.toString(extent[2]), Double.toString(extent[3]));
            new GetLayersBackgroundTask().execute(searchRequest5, searchRequest);
        }

    }

    //Plot the selected location
    public void plotLocation(double x, double y){
        if(graphicsLayer==null)graphicsLayer= new GraphicsLayer();
        PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.dots));
        PopupContainer popupContainer = new PopupContainer(mMapView);
        Graphic graphic = new Graphic(new Point(x,y), icon);
        //Popup popup = graphicsLayer.createPopup(mMapView, 0, graphic);
        //popupContainer.addPopup(popup);
        graphicsLayer.addGraphic(graphic);
        mMapView.addLayer(graphicsLayer);
    }
    //Plots chas layers on map
    public void plotChasCentres() {

        if(BaseActivity.chases!=null){
            if(graphicsLayer==null)graphicsLayer= new GraphicsLayer();
            for (int i = 0; i < BaseActivity.chases.size(); i++) {
                PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.chas_logo));
                PopupContainer popupContainer = new PopupContainer(mMapView);
                Graphic graphic = new Graphic(new Point(BaseActivity.chases.get(i).getX(), BaseActivity.chases.get(i).getY()), icon);
                //Popup popup = graphicsLayer.createPopup(mMapView, 0, graphic);
                //popupContainer.addPopup(popup);
                graphicsLayer.addGraphic(graphic);
            }
            mMapView.addLayer(graphicsLayer);
        }


    }

    //Plot Breast on Map
    public void plotBreastCentres() {
        if(BaseActivity.brestCentres!=null){
            if(graphicsLayer==null)graphicsLayer= new GraphicsLayer();
            for (int i = 0; i < BaseActivity.brestCentres.size(); i++) {
                PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.chas_logo));
                PopupContainer popupContainer = new PopupContainer(mMapView);
                Graphic graphic = new Graphic(new Point(BaseActivity.brestCentres.get(i).getX(), BaseActivity.brestCentres.get(i).getY()), icon);
                //Popup popup = graphicsLayer.createPopup(mMapView, 0, graphic);
                //popupContainer.addPopup(popup);
                graphicsLayer.addGraphic(graphic);
            }
            mMapView.addLayer(graphicsLayer);
        }
    }

    //Plot cervical centres on Map
    public void plotCervicalCentres() {
        if(BaseActivity.cervicalCentres!=null){
            if(graphicsLayer==null)graphicsLayer= new GraphicsLayer();
            for (int i = 0; i < BaseActivity.cervicalCentres.size(); i++) {
                PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.chas_logo));
                PopupContainer popupContainer = new PopupContainer(mMapView);
                Graphic graphic = new Graphic(new Point(BaseActivity.cervicalCentres.get(i).getX(), BaseActivity.cervicalCentres.get(i).getY()), icon);
                //Popup popup = graphicsLayer.createPopup(mMapView, 0, graphic);
                //popupContainer.addPopup(popup);
                graphicsLayer.addGraphic(graphic);
            }
            mMapView.addLayer(graphicsLayer);
        }
    }

    //Plot Quit centre on Map
    public void plotQuitCentres() {
        if(BaseActivity.quitCentres!=null){
            if(graphicsLayer==null)graphicsLayer= new GraphicsLayer();
            for (int i = 0; i < BaseActivity.quitCentres.size(); i++) {
                PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.chas_logo));
                PopupContainer popupContainer = new PopupContainer(mMapView);
                Graphic graphic = new Graphic(new Point(BaseActivity.quitCentres.get(i).getX(), BaseActivity.quitCentres.get(i).getY()), icon);
                //Popup popup = graphicsLayer.createPopup(mMapView, 0, graphic);
                //popupContainer.addPopup(popup);
                graphicsLayer.addGraphic(graphic);
            }
            mMapView.addLayer(graphicsLayer);
        }
    }

    //plot RetailPharmacyCentres on map
    public void plotRetailPharmacyCentres() {
        if(BaseActivity.retailPharmacies!=null){
            if(graphicsLayer==null)graphicsLayer= new GraphicsLayer();
            for (int i = 0; i < BaseActivity.retailPharmacies.size(); i++) {
                PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.chas_logo));
                PopupContainer popupContainer = new PopupContainer(mMapView);
                Graphic graphic = new Graphic(new Point(BaseActivity.retailPharmacies.get(i).getX(), BaseActivity.retailPharmacies.get(i).getY()), icon);
                //Popup popup = graphicsLayer.createPopup(mMapView, 0, graphic);
                //popupContainer.addPopup(popup);
                graphicsLayer.addGraphic(graphic);
            }
            mMapView.addLayer(graphicsLayer);
        }
    }

    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }


    private class GetLayersBackgroundTask extends AsyncTask<Runnable, Integer, Long> {

        @Override
        protected void onPostExecute(Long result) {
            resultFrame.setVisibility(View.GONE);
            plotChasCentres();
            if(breastScreeningMenuItem.isChecked())
                plotBreastCentres();
            if(cervicalScreeningMenuItem.isChecked())
                plotCervicalCentres();
            if(quiteCentresMenuItem.isChecked())
                plotQuitCentres();
            if(retailPharmaciesMenuItem.isChecked())
                plotRetailPharmacyCentres();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Long doInBackground(Runnable... task) {


                task[0].run();


            return null;
        }
    }


    // used for async task for address search only!!
    private class BackgroundTask extends AsyncTask<Runnable, Integer, Long> {

        @Override
        protected void onPostExecute(Long result) {
//            dialog.dismiss();
            if (!BaseActivity.addressEnd) {
                addressSet++;
            } else {
                BaseActivity.addressEnd = true;
                addressSet = 1;
                if (BaseActivity.addresses == null) {
                    Toast.makeText(getBaseContext(), "No result found", Toast.LENGTH_SHORT).show();
                    displayNoResult();
                } else {
                    displayResult();
                }
            }

        }

        @Override
        protected void onPreExecute() {
//            final ProgressDialog progDailog = ProgressDialog.show(getBaseContext(), "Progress_bar or give anything you want",
//                    "Give message like ....please wait....", true);
            //Toast.makeText(getBaseContext(), "Loading...", Toast.LENGTH_LONG).show();
            super.onPreExecute();
        }

        @Override
        protected Long doInBackground(Runnable... task) {
            task[0].run();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        findViewById(R.id.resultFrame).setVisibility(View.GONE);
    }
}
