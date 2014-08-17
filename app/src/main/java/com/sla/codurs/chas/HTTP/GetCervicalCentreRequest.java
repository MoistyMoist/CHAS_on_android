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

/**
 * Created by Moistyburger on 21/7/14.
 */
public class GetCervicalCentreRequest implements Runnable{

    private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

    private String xMin;
    private String xMax;
    private String yMin;
    private String yMax;

    public GetCervicalCentreRequest(String xMin,String yMin, String xMax, String yMax)
    {
        this.xMax=xMax;
        this.xMin=xMin;
        this.yMax=yMax;
        this.yMin=yMin;
    }

    public GetCervicalCentreRequest(){
        xMin="-24270.84806959612";
        yMin="20406.65967741936";
        xMax="85798.7480695961";
        yMax="49529.240322580634";
    }

    @Override
    public void run() {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = null;

        String url = "http://uat.onemap.sg/API/services.svc/mashupData?token=6XuW0BLzyfHZw4NbhtP9MlZ8h3mtyKpe6/uzOuPLzxC+/G53S9qpjw0raKnthwFBwTIsqH3Yv6No5+lLK57wKdlmO3c1K37l|mv73ZvjFcSo=&themeName=CERVICALSCREEN&extents="+xMin+","+yMin+","+xMax+","+yMax;
        //Log.i("EVENT",url);
        String urlEncoded = Uri.encode(url, ALLOWED_URI_CHARS);

        httpget = new HttpGet(urlEncoded);


        //EXCUTE REQUEST
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);

            //PRINT OUT THE RESPONSE
            Log.i("RETRIEVE CERVICAL RESPONSE STATUS:", response.getStatusLine().toString());
            JSONExtractor extractor= new JSONExtractor();
            extractor.ExtractCervicalSearchResult(response);


        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
