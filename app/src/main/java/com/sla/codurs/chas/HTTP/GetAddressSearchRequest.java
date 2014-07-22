package com.sla.codurs.chas.HTTP;

import android.net.Uri;
import android.util.Log;

import com.sla.codurs.chas.activity.BaseActivity;
import com.sla.codurs.chas.utils.JSONExtractor;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.IOException;

/**
 * Created by Moistyburger on 12/7/14.
 */
public class GetAddressSearchRequest implements Runnable{

    private String searchQuery;
    private String addressSet;
    private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

    public GetAddressSearchRequest(String searchQuery,int addressSet){
        this.searchQuery=searchQuery;
        this.addressSet=Integer.toString(addressSet);
    }

    @Override
    public void run() {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = null;


        String url = "http://www.onemap.sg/API/services.svc/basicSearch?token=qo/s2TnSUmfLz+32CvLC4RMVkzEFYjxqyti1KhByvEacEdMWBpCuSSQ+IFRT84QjGPBCuz/cBom8PfSm3GjEsGc8PkdEEOEr&searchVal="+searchQuery+"&otptFlds=SEARCHVAL,CATEGORY&returnGeom=0&rset="+addressSet;
        Log.i("URL",url);
        String urlEncoded = Uri.encode(url, ALLOWED_URI_CHARS);

        httpget = new HttpGet(urlEncoded);


        //EXCUTE REQUEST
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);

            //PRINT OUT THE RESPONSE
            Log.i("RETRIEVE Addreess RESPONSE STATUS:", response.getStatusLine().toString());
            JSONExtractor extractor= new JSONExtractor();
            extractor.ExtractAddressSearchResult(response, BaseActivity.addressEnd);


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
