package com.sla.codurs.chas.HTTP;

import android.net.Uri;
import android.util.Log;
import com.sla.codurs.chas.utils.JSONExtractor;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.IOException;

public class GetDirectionRequest implements Runnable{

    private double fromX;
    private double fromY;
    private double toX;
    private double toY;
    private String routeMode;
    private int avoidERP=0;
    private String routeOption;

    private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

    public GetDirectionRequest(double fromX, double fromY, double toX, double toY, String routeMode, int avoidERP, String routeOption){
        this.fromX=fromX;
        this.fromY=fromY;
        this.toX=toX;
        this.toY=toY;
        this.routeMode=routeMode;
        this.routeOption=routeOption;
        if(avoidERP!=-1)
            this.avoidERP=avoidERP;

    }

    @Override
    public void run() {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = null;


        String url = "http://www.onemap.sg/API/services.svc/route/solve?token=qo/s2TnSUmfLz+32CvLC4RMVkzEFYjxqyti1KhByvEacEdMWBpCuSSQ+IFRT84QjGPBCuz/cBom8PfSm3GjEsGc8PkdEEOEr&routeStops="+fromX+","+fromY+";"+toX+","+toY+"&routemode="+routeMode+"&avoidERP="+avoidERP+"&routeOption="+routeOption;
        Log.i("URL", url);
        String urlEncoded = Uri.encode(url, ALLOWED_URI_CHARS);

        httpget = new HttpGet(urlEncoded);


        //EXCUTE REQUEST
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);

            //PRINT OUT THE RESPONSE
            Log.i("RETRIEVE Direction RESPONSE STATUS:", response.getStatusLine().toString());
            JSONExtractor extractor= new JSONExtractor();
            extractor.ExtractDirectionSearchResult(response);


        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
