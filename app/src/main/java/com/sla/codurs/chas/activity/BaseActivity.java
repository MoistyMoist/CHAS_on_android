package com.sla.codurs.chas.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnPinchListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.popup.PopupContainer;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.sla.codurs.chas.HTTP.GetAddressSearchRequest;
import com.sla.codurs.chas.HTTP.GetChasRequest;
import com.sla.codurs.chas.HTTP.GetDirectionRequest;
import com.sla.codurs.chas.R;
import com.sla.codurs.chas.model.Address;
import com.sla.codurs.chas.model.Chas;
import com.sla.codurs.chas.model.Direction;
import com.sla.codurs.chas.utils.AddressAdapter;

import java.util.ArrayList;


public class BaseActivity extends Activity {
    MapView mMapView;
    LocationDisplayManager ls;
    int addressSet = 1;
    ListView result;
    TextView message;
    GraphicsLayer graphicsLayer;

    public static boolean addressEnd = false;

    public static ArrayList<Chas> chases = null;
    public static ArrayList<Address> addresses = null;
    public static ArrayList<Direction> directions = null;

    String searchQuery = "";
    // The basemap switching menu items.
    MenuItem selectAll = null;
    MenuItem selectNorth = null;
    MenuItem selectSouth = null;
    MenuItem selectEast = null;
    MenuItem selectWest = null;

    private String wholeXMin = "-27328.317339767982";
    private String wholeYMin = "24037.412581491837";
    private String wholeXMax = "82741.27879942424";
    private String wholeYMax = "57975.53805774277";

    private String northXMin = "12743.895004656708";
    private String northYMin = "42974.73372280078";
    private String northXMax = "40261.294039454726";
    private String northYMax = "51459.265091863504";

    private String southXMin = "14673.934798069631";
    private String southYMin = "26903.808314283306";
    private String southXMax = "42191.33383286765";
    private String southYMax = "36534.89797646261";

    private String eastXMin = "24992.95943611888";
    private String eastYMin = "32254.413682160706";
    private String eastXMax = "52510.35847091689";
    private String eastYMax = "43089.38955211242";

    private String westXMin = "-33022.89019558045";
    private String westYMin = "29598.22030310727";
    private String westXMax = "22011.90787401574";
    private String WestYMax = "49204.367115400906";


    final MapOptions mOceansBasemap = new MapOptions(MapOptions.MapType.OCEANS);
    // The current map extent, use to set the extent of the map after switching basemaps.
    Polygon mCurrentMapExtent = null;
    private ViewGroup resultFrame;
    private Callout _callout;

    private boolean forDirection=false;
    private double directionXTo;
    private double directionYTo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        result = (ListView) findViewById(R.id.addressResultList);
        resultFrame = (ViewGroup) findViewById(R.id.resultFrame);
        message = (TextView) findViewById(R.id.message);

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
                mMapView.zoomTo(new Point(addresses.get(position).getX(), addresses.get(position).getY()), 1);
                getLayersAroundSelectedLocation(addresses.get(position).getX(), addresses.get(position).getY(), position);
            }
        });
        initMapListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items from the Menu XML to the action bar, if present.
        getMenuInflater().inflate(R.menu.base, menu);

        selectAll = menu.getItem(0).getSubMenu().getItem(0);
        selectNorth = menu.getItem(0).getSubMenu().getItem(1);
        selectSouth = menu.getItem(0).getSubMenu().getItem(2);
        selectEast = menu.getItem(0).getSubMenu().getItem(3);
        selectWest = menu.getItem(0).getSubMenu().getItem(4);

        selectAll.setChecked(true);

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
                forDirection=false;
                BaseActivity.addresses = null;
                addressSet = 1;
                searchQuery = query;
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
            case R.id.menu_all:
                if (selectAll.isChecked())
                    selectAll.setChecked(false);
                else {
                    if (graphicsLayer != null)
                        graphicsLayer.removeAll();
                    GetChasRequest searchRequest = new GetChasRequest(wholeXMin, wholeYMin, wholeXMax, wholeYMax);
                    new GetLayersBackgroundTask().execute(searchRequest, searchRequest);
                }
                return true;
            case R.id.menu_east:
                if (selectEast.isChecked())
                    selectEast.setChecked(false);
                else {
                    if (graphicsLayer != null)
                        graphicsLayer.removeAll();
                    selectAll.setChecked(false);
                    selectEast.setChecked(true);
                    GetChasRequest searchRequest = new GetChasRequest(eastXMin, eastYMin, eastXMax, eastYMax);
                    new GetLayersBackgroundTask().execute(searchRequest, searchRequest);
                }

                return true;
            case R.id.menu_north:
                if (selectNorth.isChecked())
                    selectNorth.setChecked(false);
                else {
                    if (graphicsLayer != null)
                        graphicsLayer.removeAll();
                    selectAll.setChecked(false);
                    selectNorth.setChecked(true);
                    GetChasRequest searchRequest = new GetChasRequest(northXMin, northYMin, northXMax, northYMax);
                    new GetLayersBackgroundTask().execute(searchRequest, searchRequest);
                }

                return true;
            case R.id.menu_south:
                if (selectSouth.isChecked())
                    selectSouth.setChecked(false);
                else {
                    if (graphicsLayer != null)
                        graphicsLayer.removeAll();
                    selectAll.setChecked(false);
                    selectSouth.setChecked(true);
                    GetChasRequest searchRequest = new GetChasRequest(southXMin, southYMin, southXMax, southYMax);
                    new GetLayersBackgroundTask().execute(searchRequest, searchRequest);
                }

                return true;
            case R.id.menu_west:
                if (selectWest.isChecked())
                    selectWest.setChecked(false);
                else {
                    if (graphicsLayer != null)
                        graphicsLayer.removeAll();
                    selectAll.setChecked(false);
                    selectWest.setChecked(true);
                    GetChasRequest searchRequest = new GetChasRequest(westXMin, westYMin, westXMax, WestYMax);
                    new GetLayersBackgroundTask().execute(searchRequest, searchRequest);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Displays the resut of the address search
    public void displayResult() {
        findViewById(R.id.direction_frame).setVisibility(View.GONE);
        resultFrame.setVisibility(View.VISIBLE);
        AddressAdapter adapter = new AddressAdapter(getBaseContext(), R.layout.address_list_layout, BaseActivity.addresses);
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        result.setAdapter(adapter);
        if(forDirection)
        {
            result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    getDirection(addresses.get(position));
                }
            });
        }
        else
        {
            result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mMapView.zoomTo(new Point(addresses.get(position).getX(), addresses.get(position).getY()), 1);
                    getLayersAroundSelectedLocation(addresses.get(position).getX(), addresses.get(position).getY(), position);
                }
            });
        }

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
            public void onLocationChanged(Location location) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        });

        mMapView.setOnLongPressListener(new OnLongPressListener() {
            @Override
            public boolean onLongPress(float v, float v2) {
                Point p = mMapView.toMapPoint(new Point(v, v2));
                PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.location_icon));
                Graphic graphic = new Graphic(mMapView.toMapPoint(new Point(v, v2)), icon);
                Log.i("ON TOUCH", Double.toString(p.getX()));

                getLayersAroundSelectedLocation(p.getX(), p.getY(), -1);
                if (graphicsLayer == null) graphicsLayer = new GraphicsLayer();
                graphicsLayer.addGraphic(graphic);
//                mMapView.addLayer(graphicsLayer);
                return false;
            }
        });
        mMapView.setOnPinchListener(new OnPinchListener() {
            @Override
            public void prePointersMove(float v, float v2, float v3, float v4, double v5) {
            }

            @Override
            public void postPointersMove(float v, float v2, float v3, float v4, double v5) {
            }

            @Override
            public void prePointersDown(float v, float v2, float v3, float v4, double v5) {
            }

            @Override
            public void postPointersDown(float v, float v2, float v3, float v4, double v5) {
            }

            @Override
            public void prePointersUp(float v, float v2, float v3, float v4, double v5) {
            }

            @Override
            public void postPointersUp(float v, float v2, float v3, float v4, double v5) {
            }
        });


        mMapView.setOnSingleTapListener(new OnSingleTapListener() {

            public void onSingleTap(float x, float y) {
                if (graphicsLayer != null) {
                    if (_callout != null) {
                        _callout.hide();
                    } else {
                        _callout = mMapView.getCallout();
                        _callout.setStyle(R.xml.callout_style);
                    }

                    Graphic graphic = findClosestGraphic(x, y, graphicsLayer, 25);
                    if (graphic == null) {
                    } else {
                        String message = "";
                        double xLocation = 0.0;
                        double yLocation = 0.0;
                        for (int i = 0; i < BaseActivity.chases.size(); i++) {
                            try {
                                if ((((Point) graphic.getGeometry()).hashCode()) == (BaseActivity.chases.get(i).hashCode)) {
                                    message = BaseActivity.chases.get(i).getTitle() + "\n" + BaseActivity.chases.get(i).getDescription() + "\n" + BaseActivity.chases.get(i).getAddress();
                                    xLocation = BaseActivity.chases.get(i).getX();
                                    yLocation = BaseActivity.chases.get(i).getY();
                                }
                                Point location = (Point) graphic.getGeometry();

                                _callout.setOffset(0, -15);

                                _callout.setMaxWidth(1000);
                                _callout.setMaxHeight(500);
                                _callout.show(location, message(message, xLocation, yLocation));
                            }
                            catch (Exception e){}


                        }



                    }
                }
            }
        });


    }

    private TextView message(String text, final double x, double y) {
        final TextView msg = new TextView(this);
        directionXTo = x;
        directionYTo = y;

        msg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                FrameLayout myRoot = (FrameLayout) findViewById(R.id.direction_frame);
                View itemView = inflater.inflate(R.layout.layout_direction, myRoot);
                findViewById(R.id.direction_frame).setVisibility(View.VISIBLE);
                return false;
            }
        });
        msg.setText(text + "\n" + "Tap to get direction.");
        msg.setTextSize(12);
        msg.setTextColor(Color.BLACK);
        return msg;
    }

    public Graphic findClosestGraphic(float x, float y, GraphicsLayer graphicsLayer, int tolerance) {
        Graphic graphic = null;
        int[] graphicIDs = graphicsLayer.getGraphicIDs(x, y, tolerance);
        if (graphicIDs != null && graphicIDs.length > 0) {
            graphic = graphicsLayer.getGraphic(graphicIDs[0]);
        }
        return graphic;
    }

    public double[] getExtent(double x, double y) {
        Envelope env = new Envelope(mMapView.toMapPoint((float) x, (float) y), 100 * mMapView.getResolution(), 100 * mMapView.getResolution());
        double[] extent = new double[4];
        extent[0] = x - 1000;
        extent[1] = y - 1000;
        extent[2] = x + 1000;
        extent[3] = y + 1000;

        return extent;
    }

    public void getLayersAroundSelectedLocation(double x, double y, int i) {
        double[] extent = getExtent(x, y);
        if (graphicsLayer != null)
            graphicsLayer.removeAll();
        plotLocation(x, y, i);
        GetChasRequest searchRequest = new GetChasRequest(Double.toString(extent[0]), Double.toString(extent[1]), Double.toString(extent[2]), Double.toString(extent[3]));
        new GetLayersBackgroundTask().execute(searchRequest, searchRequest);
    }

    public boolean isGPSoN() {
        LocationManager lm = null;
        boolean gps_enabled = false;
        if (lm == null)
            lm = (LocationManager) getBaseContext().getSystemService(getBaseContext().LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            gps_enabled = false;
        }
        if (!gps_enabled) {
            return true;
        } else
            return false;
    }

    public void getLayersAroundGPS(View v) {
        if (graphicsLayer != null)
            graphicsLayer.removeAll();
        if (isGPSoN()) {
            Toast.makeText(getBaseContext(), "Please on Location Service GPS", Toast.LENGTH_SHORT).show();
        } else {
            double[] extent = getExtent(ls.getPoint().getX(), ls.getPoint().getY());
            GetChasRequest searchRequest = new GetChasRequest(Double.toString(extent[0]), Double.toString(extent[1]), Double.toString(extent[2]), Double.toString(extent[3]));
            new GetLayersBackgroundTask().execute(searchRequest, searchRequest);
        }


    }

    //Plot the selected location
    public void plotLocation(double x, double y, int i) {
        if (graphicsLayer == null) graphicsLayer = new GraphicsLayer();
        PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.location_icon));
        PopupContainer popupContainer = new PopupContainer(mMapView);
        Graphic graphic = new Graphic(new Point(x, y), icon);
        if (i != -1)
            BaseActivity.addresses.get(i).hashCode = ((Point) graphic.getGeometry()).hashCode();
        graphicsLayer.addGraphic(graphic);
        mMapView.addLayer(graphicsLayer);
    }

    //Plots chas layers on map
    public void plotChasCentres() {

        if (BaseActivity.chases != null) {
            if (graphicsLayer == null) graphicsLayer = new GraphicsLayer();
            if (BaseActivity.chases.size() == 0 || BaseActivity.chases == null) {
                Toast.makeText(getBaseContext(), "No results found", Toast.LENGTH_SHORT).show();
            }
            for (int i = 0; i < BaseActivity.chases.size(); i++) {
                PictureMarkerSymbol icon = new PictureMarkerSymbol(getBaseContext(), getResources().getDrawable(R.drawable.chas_logo));
                PopupContainer popupContainer = new PopupContainer(mMapView);
                Point p = new Point();

                Graphic graphic = new Graphic(new Point(BaseActivity.chases.get(i).getX(), BaseActivity.chases.get(i).getY()), icon);

                BaseActivity.chases.get(i).hashCode = ((Point) graphic.getGeometry()).hashCode();
                //Popup popup = graphicsLayer.createPopup(mMapView, 0, graphic);
                //popupContainer.addPopup(popup);
                graphicsLayer.addGraphic(graphic);
            }
            mMapView.addLayer(graphicsLayer);
        }


    }

    public void ploDirectionPoints() {
        Log.i("plotting direction","dsads");
        if (BaseActivity.directions != null) {
            if (graphicsLayer == null) graphicsLayer = new GraphicsLayer();
            Polyline line = new Polyline();

            line.startPath(BaseActivity.directions.get(0).getX(), BaseActivity.directions.get(0).getY());
            for (int i = 1; i < BaseActivity.directions.size(); i++) {
//                Log.i("ploting x"+i,BaseActivity.directions.get(i).getX()+"");
//                Log.i("ploting Y"+i,BaseActivity.directions.get(i).getY()+"");
                line.lineTo(BaseActivity.directions.get(i).getX(), BaseActivity.directions.get(i).getY());
            }
            graphicsLayer.addGraphic(new Graphic(line, new SimpleFillSymbol(Color.RED)));
            mMapView.addLayer(graphicsLayer);
        }
    }

    public void changeTab2(View v) {
        findViewById(R.id.tab1).setVisibility(View.GONE);
        findViewById(R.id.tab2).setVisibility(View.VISIBLE);
    }

    public void changeTab1(View v) {
        findViewById(R.id.tab1).setVisibility(View.VISIBLE);
        findViewById(R.id.tab2).setVisibility(View.GONE);

    }


    public void findStartPoint(View v) {
        forDirection=true;
        BaseActivity.addresses = null;
        addressSet = 1;
        searchQuery = ((EditText) findViewById(R.id.direction_query)).getText().toString();
        GetAddressSearchRequest searchRequest = new GetAddressSearchRequest(searchQuery, addressSet);
        new BackgroundTask().execute(searchRequest,searchRequest);
    }



    public void getDirection(Address address) {

        findViewById(R.id.resultFrame).setVisibility(View.GONE);

        EditText starttingPointET = (EditText) findViewById(R.id.direction_query);
        String query = starttingPointET.getText().toString();

        RadioGroup selectedVehicleMode = (RadioGroup) findViewById(R.id.transport_type);
        RadioGroup selectedWay = (RadioGroup) findViewById(R.id.selected_way);
        RadioGroup selectedWay2 = (RadioGroup) findViewById(R.id.selected_way2);

        GetDirectionRequest request;

        if (findViewById(R.id.tab1).getVisibility() == View.VISIBLE) {
            Toast.makeText(getBaseContext(),"Getting transport direction..",Toast.LENGTH_LONG).show();

            int selectedVehicle = selectedVehicleMode.getCheckedRadioButtonId();
            RadioButton temp = (RadioButton) findViewById(selectedVehicle);


            int selectedType = selectedWay.getCheckedRadioButtonId();
            RadioButton temp2 = (RadioButton) findViewById(selectedType);

            request= new GetDirectionRequest(address.getX(),address.getY(),directionXTo,directionYTo,temp.getTag().toString(),0,temp2.getTag().toString());

        } else {
            Toast.makeText(getBaseContext(),"Getting driving directions..",Toast.LENGTH_LONG).show();

            CheckBox box = (CheckBox) findViewById(R.id.avoid_erp);
            int avoidERP=-1;
            if(box.isChecked())
                avoidERP=0;
            else
                avoidERP=1;

            int selectedType = selectedWay2.getCheckedRadioButtonId();
            RadioButton temp = (RadioButton) findViewById(selectedType);

            request= new GetDirectionRequest(address.getX(),address.getY(),directionXTo,directionYTo,"DRIVE",avoidERP,temp.getTag().toString());
        }


        new GetDirectionBackgroundTask().execute(request,request);


    }


    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }

    //use for get chas api
    private class GetLayersBackgroundTask extends AsyncTask<Runnable, Integer, Long> {
        @Override
        protected void onPostExecute(Long result) {
            resultFrame.setVisibility(View.GONE);
            plotChasCentres();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getBaseContext(), "Retrieving a CHAS near you.", Toast.LENGTH_LONG);
        }

        @Override
        protected Long doInBackground(Runnable... task) {
            task[0].run();
            return null;
        }
    }

    //used for get direction api
    private class GetDirectionBackgroundTask extends AsyncTask<Runnable, Integer, Long> {
        @Override
        protected void onPostExecute(Long result) {
            ploDirectionPoints();
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

    // use for address search api
    private class BackgroundTask extends AsyncTask<Runnable, Integer, Long> {
        @Override
        protected void onPostExecute(Long result) {
            if (!BaseActivity.addressEnd) {
                addressSet++;
                Log.i("address search", "still searching");
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

}
