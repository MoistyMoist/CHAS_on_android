package com.sla.codurs.chas.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Transformation2D;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.ags.na.Route;
import com.sla.codurs.chas.HTTP.GetAddressSearchRequest;
import com.sla.codurs.chas.HTTP.GetBreastCentre;
import com.sla.codurs.chas.HTTP.GetCervicalCentreRequest;
import com.sla.codurs.chas.HTTP.GetChasRequest;
import com.sla.codurs.chas.HTTP.GetDirectionRequest;
import com.sla.codurs.chas.HTTP.GetQuitCenterRequest;
import com.sla.codurs.chas.HTTP.GetRetailPharmacyRequest;
import com.sla.codurs.chas.R;
import com.sla.codurs.chas.model.Address;
import com.sla.codurs.chas.model.BreastScreeningCentre;
import com.sla.codurs.chas.model.CervicalScreeningCentre;
import com.sla.codurs.chas.model.Chas;
import com.sla.codurs.chas.model.Direction;
import com.sla.codurs.chas.model.QuitCentre;
import com.sla.codurs.chas.model.RetailPharmacy;
import com.sla.codurs.chas.utils.AddressAdapter;

import org.codehaus.jackson.map.deser.ValueInstantiators;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class BaseActivity extends Activity {
    MapView mMapView;
    LocationDisplayManager ls;
    int addressSet = 1;
    ListView result;
    GraphicsLayer graphicsLayer;

    public static boolean addressEnd = false;

    public static ArrayList<Chas> chases = null;
    public static ArrayList<Address> addresses = null;
    public static ArrayList<BreastScreeningCentre> brestCentres = null;
    public static ArrayList<CervicalScreeningCentre> cervicalCentres = null;
    public static ArrayList<QuitCentre> quitCentres = null;
    public static ArrayList<RetailPharmacy> retailPharmacies = null;
    public static ArrayList<Direction> directions=null;

    String searchQuery="";
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
                if (STATUS.LAYER_LOADED == status) {mMapView.setExtent(mCurrentMapExtent);}}});
        ls = mMapView.getLocationDisplayManager();
        ls.start();
        result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getLayersAroundSelectedLocation(addresses.get(position).getX(),addresses.get(position).getY(),position);
            }
        });
        initMapListeners();
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
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {

                BaseActivity.addresses = null;
                addressSet=1;
                searchQuery=query;
                GetAddressSearchRequest searchRequest = new GetAddressSearchRequest(searchQuery, addressSet);
                new BackgroundTask().execute(searchRequest, searchRequest);
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
        mCurrentMapExtent = mMapView.getExtent();
        switch (item.getItemId()) {
            case R.id.chas_centers:
                chasMenuItem.setChecked(true);
                return true;
            case R.id.cervical_screening_centre:
                if(cervicalScreeningMenuItem.isChecked())
                    cervicalScreeningMenuItem.setChecked(false);
                else
                    cervicalScreeningMenuItem.setChecked(true);
                return true;
            case R.id.breast_screening_centres:
                if(breastScreeningMenuItem.isChecked())
                    breastScreeningMenuItem.setChecked(false);
                else
                    breastScreeningMenuItem.setChecked(true);
                return true;
            case R.id.quit_centres:
                if(quiteCentresMenuItem.isChecked())
                    quiteCentresMenuItem.setChecked(false);
                else
                    quiteCentresMenuItem.setChecked(true);
                return true;
            case R.id.retail_pharmacy:
                if(retailPharmaciesMenuItem.isChecked())
                    retailPharmaciesMenuItem.setChecked(false);
                else
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

    public void initMapListeners() {
        ls.setLocationListener(new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {}
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        });

        mMapView.setOnSingleTapListener(new OnSingleTapListener() {

            public void onSingleTap(float x, float y) {
                if(graphicsLayer!=null){
                    if(_callout != null){
                        _callout.hide();
                    }
                    else
                    {
                        _callout = mMapView.getCallout();
                        _callout.setStyle(R.xml.callout_style);
                    }

                    Graphic graphic = findClosestGraphic(x, y, graphicsLayer ,25);
                    if(graphic == null){}
                    else{
                        String message = "";
                        double xLocation=0.0;
                        double yLocation=0.0;
                        for(int i=0;i<BaseActivity.chases.size();i++){
                            if((((Point)graphic.getGeometry()).hashCode())==(BaseActivity.chases.get(i).hashCode)) {
                                message = BaseActivity.chases.get(i).getTitle() + "\n" + BaseActivity.chases.get(i).getDescription() + "\n" + BaseActivity.chases.get(i).getAddress();
                                xLocation=BaseActivity.chases.get(i).getX();
                                yLocation=BaseActivity.chases.get(i).getY();
                            }
                        }
                        if(breastScreeningMenuItem.isChecked()){
                            if(BaseActivity.brestCentres!=null){
                                for(int i=0;i<BaseActivity.brestCentres.size();i++){
                                    if((((Point)graphic.getGeometry()).hashCode())==(BaseActivity.brestCentres.get(i).hashCode)) {
                                        message = BaseActivity.brestCentres.get(i).getName();
                                        xLocation=BaseActivity.brestCentres.get(i).getX();
                                        yLocation=BaseActivity.brestCentres.get(i).getY();
                                    }
                                }
                            }
                        }
                        if(cervicalScreeningMenuItem.isChecked()){
                            if(BaseActivity.cervicalCentres!=null){
                                for(int i=0;i<BaseActivity.cervicalCentres.size();i++){
                                    if((((Point)graphic.getGeometry()).hashCode())==(BaseActivity.cervicalCentres.get(i).hashCode)) {
                                        message = BaseActivity.cervicalCentres.get(i).getName();
                                        xLocation=BaseActivity.cervicalCentres.get(i).getX();
                                        yLocation=BaseActivity.cervicalCentres.get(i).getY();
                                    }
                                }
                            }
                        }

                        if(quiteCentresMenuItem.isChecked()){
                            if(BaseActivity.quitCentres!=null){
                                for(int i=0;i<BaseActivity.quitCentres.size();i++){
                                    if((((Point)graphic.getGeometry()).hashCode())==(BaseActivity.quitCentres.get(i).hashCode)) {
                                        message = BaseActivity.quitCentres.get(i).getName();
                                        xLocation=BaseActivity.quitCentres.get(i).getX();
                                        yLocation=BaseActivity.quitCentres.get(i).getY();
                                    }
                                }
                            }
                        }
                        if(addresses!=null){
                            for(int i=0;i<BaseActivity.addresses.size();i++){
                                if((((Point)graphic.getGeometry()).hashCode())==(BaseActivity.addresses.get(i).hashCode)) {
                                    message = BaseActivity.addresses.get(i).getTitle();
                                    xLocation=BaseActivity.addresses.get(i).getX();
                                    yLocation=BaseActivity.addresses.get(i).getY();
                                }
                            }
                        }


                        Point location = (Point) graphic.getGeometry();

                        _callout.setOffset(0, -15);

                        _callout.setMaxWidth(1000);
                        _callout.setMaxHeight(500);
                        _callout.show(location, message(message,xLocation,yLocation));
                    }
                }
            }
        });
    }

    private TextView message(String text, final double x, double y) {
        final TextView msg = new TextView(this);
        final double xLocation =x;
        final double yLocation =y;

        msg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getBaseContext(),"Finding direction"+ xLocation+","+yLocation+" : "+ls.getPoint().getX()+" , "+ls.getPoint().getY(),Toast.LENGTH_LONG).show();
                if(isGPSoN())
                { Toast.makeText(getBaseContext(),"Please on Location Service GPS",Toast.LENGTH_SHORT).show();}
                else{
                    GetDirectionRequest directionRequest = new GetDirectionRequest(ls.getPoint().getX(), ls.getPoint().getY(),xLocation,yLocation);
                    new GetDirectionBackgroundTask().execute(directionRequest, directionRequest);
                }
                return false;
            }
        });
        msg.setText(text + "\n" + "Tap to get direction.");
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

    public void getLayersAroundSelectedLocation(double x, double y,int i) {
        double[] extent = getExtent(x, y);
        if(graphicsLayer!=null)
            graphicsLayer.removeAll();
        plotLocation(x,y,i);


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

    public boolean isGPSoN(){
        LocationManager lm = null;
        boolean gps_enabled=false;
        if(lm==null)
            lm = (LocationManager) getBaseContext().getSystemService(getBaseContext().LOCATION_SERVICE);
        try{
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){}
        if(!gps_enabled){
            return true;
        }
        else
            return false;
    }

    public void getLayersAroundGPS(View v) {
        if(graphicsLayer!=null)
            graphicsLayer.removeAll();
        if(isGPSoN()){
            Toast.makeText(getBaseContext(),"Please on Location Service GPS",Toast.LENGTH_SHORT).show();
        }
        else{
//            Toast.makeText(getBaseContext(),"Cuurrent location"+ ls.getPoint().getX()+" , "+ ls.getPoint().getY(),Toast.LENGTH_LONG).show();

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
                new GetLayersBackgroundTask().execute(searchRequest5, searchRequest5);
            }
        }


    }

    //Plot the selected location
    public void plotLocation(double x, double y,int i){
        if(graphicsLayer==null)graphicsLayer= new GraphicsLayer();
        PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.location_icon));
        PopupContainer popupContainer = new PopupContainer(mMapView);
        Graphic graphic = new Graphic(new Point(x,y), icon);
        BaseActivity.addresses.get(i).hashCode=((Point)graphic.getGeometry()).hashCode();
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
                Point p= new Point();

                Graphic graphic = new Graphic(new Point(BaseActivity.chases.get(i).getX(), BaseActivity.chases.get(i).getY()), icon);

                BaseActivity.chases.get(i).hashCode=((Point)graphic.getGeometry()).hashCode();
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
                PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.breastscreen_icon));
                PopupContainer popupContainer = new PopupContainer(mMapView);
                Graphic graphic = new Graphic(new Point(BaseActivity.brestCentres.get(i).getX(), BaseActivity.brestCentres.get(i).getY()), icon);
                //Popup popup = graphicsLayer.createPopup(mMapView, 0, graphic);
                //popupContainer.addPopup(popup);
                BaseActivity.brestCentres.get(i).hashCode=((Point)graphic.getGeometry()).hashCode();
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
                PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.cervical_icon));
                PopupContainer popupContainer = new PopupContainer(mMapView);
                Graphic graphic = new Graphic(new Point(BaseActivity.cervicalCentres.get(i).getX(), BaseActivity.cervicalCentres.get(i).getY()), icon);
                //Popup popup = graphicsLayer.createPopup(mMapView, 0, graphic);
                //popupContainer.addPopup(popup);
                BaseActivity.cervicalCentres.get(i).hashCode=((Point)graphic.getGeometry()).hashCode();
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
                PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.quitcentre_icon));
                PopupContainer popupContainer = new PopupContainer(mMapView);
                Graphic graphic = new Graphic(new Point(BaseActivity.quitCentres.get(i).getX(), BaseActivity.quitCentres.get(i).getY()), icon);
                //Popup popup = graphicsLayer.createPopup(mMapView, 0, graphic);
                //popupContainer.addPopup(popup);
                BaseActivity.quitCentres.get(i).hashCode=((Point)graphic.getGeometry()).hashCode();
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
                PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.singhealth_polyclinics_logo));
                PopupContainer popupContainer = new PopupContainer(mMapView);
                Graphic graphic = new Graphic(new Point(BaseActivity.retailPharmacies.get(i).getX(), BaseActivity.retailPharmacies.get(i).getY()), icon);
                BaseActivity.retailPharmacies.get(i).hashCode=((Point)graphic.getGeometry()).hashCode();
                graphicsLayer.addGraphic(graphic);
            }
            mMapView.addLayer(graphicsLayer);
        }
    }

    public void ploDirectionPoints() {
        if(BaseActivity.directions!=null){
            if(graphicsLayer==null)graphicsLayer= new GraphicsLayer();
            Polyline line= new Polyline();

            line.startPath(BaseActivity.directions.get(0).getX(),BaseActivity.directions.get(0).getY());
            for (int i = 1; i < BaseActivity.directions.size(); i++) {
//                Log.i("ploting x"+i,BaseActivity.directions.get(i).getX()+"");
//                Log.i("ploting Y"+i,BaseActivity.directions.get(i).getY()+"");
                line.lineTo(BaseActivity.directions.get(i).getX(), BaseActivity.directions.get(i).getY());
            }
            graphicsLayer.addGraphic(new Graphic(line,new SimpleFillSymbol(Color.RED)));
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

    private class GetDirectionBackgroundTask extends AsyncTask<Runnable, Integer, Long> {
        @Override
        protected void onPostExecute(Long result) {ploDirectionPoints();}
        @Override
        protected void onPreExecute() {super.onPreExecute();}
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
            if (!BaseActivity.addressEnd) {
                addressSet++;
                Log.i("address search","still searching");
                GetAddressSearchRequest searchRequest = new GetAddressSearchRequest(searchQuery, addressSet);
                new BackgroundTask().execute(searchRequest, searchRequest);
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
