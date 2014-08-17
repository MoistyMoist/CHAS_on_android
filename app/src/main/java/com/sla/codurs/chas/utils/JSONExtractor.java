package com.sla.codurs.chas.utils;

import android.util.Log;

import com.sla.codurs.chas.activity.BaseActivity;
import com.sla.codurs.chas.model.Address;
import com.sla.codurs.chas.model.BreastScreeningCentre;
import com.sla.codurs.chas.model.CervicalScreeningCentre;
import com.sla.codurs.chas.model.Chas;
import com.sla.codurs.chas.model.Direction;
import com.sla.codurs.chas.model.QuitCentre;
import com.sla.codurs.chas.model.RetailPharmacy;

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
            BaseActivity.addressEnd=false;
        }
        else{
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
                chas.setURL(row.getString("URL"));
                chas.setAddress(row.getString("ADDRESS"));
                chas.setDescription(row.getString("DESCRIPTION"));
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
        HttpEntity entity = data.getEntity();
        InputStream instream = entity.getContent();
        String result= convertStreamToString(instream);
        JSONObject json = null;
        json = new JSONObject(result);
        JSONArray RawData= json.getJSONArray("SrchResults");
        Log.i("raw1", RawData.toString());
        ArrayList<BreastScreeningCentre> returningData= new ArrayList<BreastScreeningCentre>();

        if(RawData.length()>2){
            for(int i=2;i<RawData.length();i++){
                BreastScreeningCentre breast= new BreastScreeningCentre();
                JSONObject row=RawData.getJSONObject(i);

                Log.i("raw", row.toString());

                String[] separated = row.getString("XY").split(",");


                breast.setName(row.getString("NAME"));
                breast.setIconURL(row.getString("ICON_NAME"));
                breast.setX(Double.parseDouble(separated[0]));
                breast.setY(Double.parseDouble(separated[1]));

                returningData.add(breast);
            }
            BaseActivity.brestCentres=returningData;
        }
        else{
            BaseActivity.brestCentres=null;
        }
    }

    //THIS METHOD EXRTRACTS THE CERVICAL SEARCH RESULT AND FORMATS IT
    public void ExtractCervicalSearchResult(HttpResponse data) throws IllegalStateException, IOException, JSONException{
        HttpEntity entity = data.getEntity();
        InputStream instream = entity.getContent();
        String result= convertStreamToString(instream);
        JSONObject json = null;
        json = new JSONObject(result);
        JSONArray RawData= json.getJSONArray("SrchResults");
        Log.i("raw1", RawData.toString());
        ArrayList<CervicalScreeningCentre> returningData= new ArrayList<CervicalScreeningCentre>();

        if(RawData.length()>2){
            for(int i=2;i<RawData.length();i++){
                CervicalScreeningCentre cervical= new CervicalScreeningCentre();
                JSONObject row=RawData.getJSONObject(i);

                Log.i("raw", row.toString());

                String[] separated = row.getString("XY").split(",");


                cervical.setName(row.getString("NAME"));
                cervical.setIconURL(row.getString("ICON_NAME"));
                cervical.setX(Double.parseDouble(separated[0]));
                cervical.setY(Double.parseDouble(separated[1]));

                returningData.add(cervical);
            }
            BaseActivity.cervicalCentres=returningData;
        }
        else{
            BaseActivity.cervicalCentres=null;
        }
    }

    //THIS METHOD EXRTRACTS THE QUIT SEARCH RESULT AND FORMATS IT
    public void ExtractQuitSearchResult(HttpResponse data) throws IllegalStateException, IOException, JSONException{
        HttpEntity entity = data.getEntity();
        InputStream instream = entity.getContent();
        String result= convertStreamToString(instream);
        JSONObject json = null;
        json = new JSONObject(result);
        JSONArray RawData= json.getJSONArray("SrchResults");
        Log.i("raw1", RawData.toString());
        ArrayList<QuitCentre> returningData= new ArrayList<QuitCentre>();

        if(RawData.length()>2){
            for(int i=2;i<RawData.length();i++){
                QuitCentre quit= new QuitCentre();
                JSONObject row=RawData.getJSONObject(i);

                Log.i("raw", row.toString());

                String[] separated = row.getString("XY").split(",");


                quit.setName(row.getString("NAME"));
                quit.setIconURL(row.getString("ICON_NAME"));
                quit.setX(Double.parseDouble(separated[0]));
                quit.setY(Double.parseDouble(separated[1]));

                returningData.add(quit);
            }
            BaseActivity.quitCentres=returningData;
        }
        else{
            BaseActivity.quitCentres=null;
        }
    }

    //THIS METHOD EXRTRACTS THE RETAIL SEARCH RESULT AND FORMATS IT
    public void ExtractRetailSearchResult(HttpResponse data) throws IllegalStateException, IOException, JSONException{
        HttpEntity entity = data.getEntity();
        InputStream instream = entity.getContent();
        String result= convertStreamToString(instream);
        JSONObject json = null;
        json = new JSONObject(result);
        JSONArray RawData= json.getJSONArray("SrchResults");
        Log.i("raw1", RawData.toString());
        ArrayList<RetailPharmacy> returningData= new ArrayList<RetailPharmacy>();

        if(RawData.length()>2){
            for(int i=2;i<RawData.length();i++){
                RetailPharmacy retail= new RetailPharmacy();
                JSONObject row=RawData.getJSONObject(i);

                Log.i("raw", row.toString());

                String[] separated = row.getString("XY").split(",");


                retail.setName(row.getString("NAME"));
                retail.setIconURL(row.getString("ICON_NAME"));
                retail.setX(Double.parseDouble(separated[0]));
                retail.setY(Double.parseDouble(separated[1]));

                returningData.add(retail);
            }
            BaseActivity.retailPharmacies=returningData;
        }
        else{
            BaseActivity.retailPharmacies=null;
        }
    }


    public void ExtractDirectionSearchResult(HttpResponse data) throws IllegalStateException, IOException, JSONException {
        HttpEntity entity = data.getEntity();
        InputStream instream = entity.getContent();
        String result= convertStreamToString(instream);
        JSONObject json = null;
        json = new JSONObject(result);
//        Log.i("raw1", json.toString());
        JSONObject features= json.getJSONObject("routes");
//        Log.i("raw2", json.toString());
        JSONArray array= features.getJSONArray("features");
//        Log.i("raw2", array.toString());
        JSONObject gemometry= array.getJSONObject(0);
//        Log.i("raw3", gemometry.toString());
        JSONObject paths= gemometry.getJSONObject("geometry");
//        Log.i("raw4", paths.toString());
        JSONArray points= paths.getJSONArray("paths");
//        Log.i("raw5", points.toString());
        JSONArray points2= points.getJSONArray(0);
//        Log.i("raw6", points2.toString());

        BaseActivity.directions=new ArrayList<Direction>();


        for(int i=0;i<points2.length();i++){
            Direction direction= new Direction();
            Log.i("point"+i,points2.getJSONArray(i).toString());

            direction.setX(points2.getJSONArray(i).getDouble(0));
            direction.setY(points2.getJSONArray(i).getDouble(1));
//                Log.i("pointX:"+i,points2.getJSONArray(i).getString(0));
//                Log.i("pointY:"+i,points2.getJSONArray(i).getString(1));
            BaseActivity.directions.add(direction);

        }
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
