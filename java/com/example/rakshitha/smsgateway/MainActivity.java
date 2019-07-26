package com.example.rakshitha.smsgateway;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.VoiceInteractor;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity  {

    private static MainActivity inst;
    Button sender;
    ArrayList<String> smsMessagesList = new ArrayList<String>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;
    LocationManager locationManager;
    LocationListener locationListener;
    String phone_number,symptoms,number,msgsymp,latutide,longitude,msgsymp1;
    Double Lat,Lng;

    private FusedLocationProviderClient client;
    String url="https://32utybyjof.execute-api.us-west-2.amazonaws.com/test/offline";
    public static final ArrayList<String> sms_num = new ArrayList<String>();
    public static final ArrayList<String> sms_body = new ArrayList<String>();


    public static MainActivity instance() {
        return inst;
    }
    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        requestPermission();
    //    Log.d("On Create====","Created");


        //Location Permission
  /*      if (ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }

        client= LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Lat=location.getLatitude();
                Lng=location.getLongitude();
                Log.d("Locations",String.valueOf(Lat));
            }
        });*/
        smsListView = findViewById(R.id.SMSList);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        // SMS Read Permision At Runtime
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            refreshSmsInbox();
        } else {
            final int REQUEST_CODE_ASK_PERMISSIONS = 123;
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
        }

    /*    try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
            locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        }
        catch(Exception e) {
            e.printStackTrace();
        }*/

        RequestQueue queue = Volley.newRequestQueue(this);  // this = context
        url = "https://32utybyjof.execute-api.us-west-2.amazonaws.com/test/offline";

        JSONObject jsonObject=new JSONObject();

        msgsymp = new String();

        try {
            jsonObject.put("latitude",latutide);
            jsonObject.put("longitude",longitude);
            jsonObject.put("phone_number",number);
            jsonObject.put("symptoms",msgsymp1);

        } catch (JSONException e) {
            e.printStackTrace(); }


            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                    }
                });

                queue.add(jsonRequest);

        //post request
        Log.d("Location",String.valueOf(Lat));


    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},1);
    }


    public void refreshSmsInbox() {
        String[] symptom_list={"pain in chest","dizziness","fatigue","shortness of breath","impending doom","cough","wheezing","frequent respiratory infections","sweating","palpitations","tachycardia","severe vomiting","loss of appetite","abdominal discomfort","blurring of vision","fast heart rate","weight loss","excessive sleepiness","bleeding","accident"};
        String[] phone_nos=new String[100000];
            ContentResolver contentResolver = getContentResolver();
            Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
            try{
                int indexBody = smsInboxCursor.getColumnIndex("body");
            int indexAddress = smsInboxCursor.getColumnIndex("address");
            int j=0;
            if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
            arrayAdapter.clear();
            do {
                String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                        "\n" + smsInboxCursor.getString(indexBody) + "\n";
               phone_number=smsInboxCursor.getString(indexAddress);
               phone_nos[j] = phone_number;
               j++;
             //   Log.d("Phone no",phone_number);
                arrayAdapter.add(str);
            } while (smsInboxCursor.moveToNext());
            String txtsymp[]=new String[10000];
            int k = 0;
            number=phone_nos[0];
            Log.d("Phone 1",number);

            smsInboxCursor.moveToFirst();
            String message = smsInboxCursor.getString(indexBody);
            Log.d("mes",message);
            //Extract latitude and longitude from message
                Matcher m = Patterns.WEB_URL.matcher(message);
                String url_location="";
                while (m.find()) {
                    url_location = m.group();
                    Log.d("URL", "URL extracted: " + url_location);
                }
                String extract[]=url_location.split("=");
                Log.d("Latlng Extracted",extract.toString());
                String latlng=extract[1];

                Log.d("Text URL",url_location);


                String latlng1[]=latlng.split(",");

                latutide=latlng1[0];
                longitude=latlng1[1];
                Log.d("Latitude extracted",latutide);

                for( int i = 0; i < symptom_list.length; i++ ){
                    if( message.contains(symptom_list[i])){
                        msgsymp += symptom_list[i] + " ";
                        k++;
                    }
                }
                msgsymp1=msgsymp.substring(4);

                Log.d("Symps",msgsymp);

        }catch (CursorIndexOutOfBoundsException e){
            Log.d("Cursor",e.toString());
        } finally {
            smsInboxCursor.close();
        }

    }


    public void updateList(final String smsMessage) {
        Toast.makeText(MainActivity.this,"New Mesg",Toast.LENGTH_SHORT).show();

        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            String address = smsMessages[0];
            String smsMessage = "";
            for (int i = 1; i < smsMessages.length; ++i) {
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = address + "\n";
            smsMessageStr += smsMessage;
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

