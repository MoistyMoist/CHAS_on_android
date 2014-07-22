package com.sla.codurs.chas.utils;

import android.util.Log;

import com.sla.codurs.chas.activity.BaseActivity;
import com.sla.codurs.chas.model.Address;
import com.sla.codurs.chas.model.Chas;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by COUDRS LLP on 9/7/14.
 */
public class JSONExtractor {

    //THIS METHOD EXRTRACTS THE ADDRESS SEARCH RESULT AND FORMATS IT
    public void ExtractAddressSearchResult(HttpResponse data,boolean continueStream)throws IllegalStateException, IOException, JSONException {
        HttpEntity entity = data.getEntity();
        InputStream instream = entity.getContent();
        String result= convertStreamToString(instream);
        JSONObject json = null;
        json = new JSONObject(result);
       // Log.i("Raw",""+json.toString());
        JSONArray RawData= json.getJSONArray("SearchResults");
        Log.i("count", RawData.length()+"");
        ArrayList<Address> returningData= BaseActivity.addresses;

        if(returningData==null||returningData.size()==0)
            returningData= new ArrayList<Address>();

        if(RawData.length()>1){
            for(int i=1;i<RawData.length();i++){
                Address address= new Address();
                JSONObject row=RawData.getJSONObject(i);

                Log.i("raw", row.toString());

                address.setTitle(row.getString("SEARCHVAL"));
                address.setX(row.getDouble("X"));
                address.setY(row.getDouble("Y"));

                returningData.add(address);
            }
            BaseActivity.addresses=returningData;
        }
        else{
            //BaseActivity.addresses=null;
            BaseActivity.addressEnd=true;
        }


    }

    //THIS METHOD EXRTRACTS THE CHAS SEARCH RESULT AND FORMATS IT
    public void ExtractChasSearchResult(HttpResponse data) throws IllegalStateException, IOException, JSONException{

        HttpEntity entity = data.getEntity();
        InputStream instream = entity.getContent();
        String result= convertStreamToString(instream);
        JSONObject json = null;
        json = new JSONObject(result);
        JSONArray RawData= json.getJSONArray("SrchResults");
        Log.i("raw1", RawData.toString());
        ArrayList<Chas> returningData= new ArrayList<Chas>();

        if(RawData.length()>2){
            for(int i=2;i<RawData.length();i++){
                Chas chas= new Chas();
                JSONObject row=RawData.getJSONObject(i);

                Log.i("raw", row.toString());

                String[] separated = row.getString("XY").split(",");


                chas.setTitle(row.getString("NAME"));
                chas.setIconURL(row.getString("ICON_NAME"));
                chas.setX(Double.parseDouble(separated[0]));
                chas.setY(Double.parseDouble(separated[1]));

                returningData.add(chas);
            }
            BaseActivity.chases=returningData;
        }
        else{
            BaseActivity.chases=null;
        }


    }

    //THIS METHOD EXRTRACTS THE BREAST SEARCH RESULT AND FORMATS IT
    public void ExtractBreastSearchResult(HttpResponse data) throws IllegalStateException, IOException, JSONException{

    }
    //THIS METHOD EXRTRACTS THE CERVICAL SEARCH RESULT AND FORMATS IT
    public void ExtractCervicalSearchResult(HttpResponse data) throws IllegalStateException, IOException, JSONException{

    }

    //THIS METHOD EXRTRACTS THE QUIT SEARCH RESULT AND FORMATS IT
    public void ExtractQuitSearchResult(HttpResponse data) throws IllegalStateException, IOException, JSONException{

    }

    //THIS METHOD EXRTRACTS THE RETAIL SEARCH RESULT AND FORMATS IT
    public void ExtractRetailSearchResult(HttpResponse data) throws IllegalStateException, IOException, JSONException{

    }









    //THIS METHOD CONVERTS THE HTTP RESPONSE TO JSON.
    //DO NOT EDIT OR REMOVE THIS METHOD
    private static String convertStreamToString(InputStream is)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }



}
