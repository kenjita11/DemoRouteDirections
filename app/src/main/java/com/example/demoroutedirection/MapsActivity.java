package com.example.demoroutedirection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private double min = 9999999999.99;
    private GoogleMap mMap;
    private Spinner choseLocation;
    private Button btnNearest;
    private PolylineOptions lineOptions = null;
    private ArrayList<LatLng> points = new ArrayList<>();
    private ArrayList<Double> Distance = new ArrayList<>();
    private ArrayList<LatLng> store = new ArrayList<>();
    private int positionNearest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        final LatLng SSKCompany = new LatLng(10.729454, 106.721913);
//        mMap.addMarker(new MarkerOptions().position(SSKCompany).title("Paragon Building"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(SSKCompany));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 92);
        }
        mMap.setMyLocationEnabled(true);

        Object_Maps Hospital = new Object_Maps(10.732955, 106.719018); // Benh vien tim tam duc
        Object_Maps CrescentMall = new Object_Maps(10.728559, 106.719030);// Trung tam mua sam CrescentMall
        Object_Maps CoffeePower = new Object_Maps(10.729259, 106.727104); // Coffee Power
        Object_Maps WaterfrontPresident = new Object_Maps(10.725592, 106.725340);// Waterfront President
        Object_Maps Sacombank = new Object_Maps(10.725832, 106.720471); // Sacombank

        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.locate_flag);

        final LatLng SubAddress1 = new LatLng(Hospital.Latitue,Hospital.Longtitue);
        mMap.addMarker(new MarkerOptions().position(SubAddress1).icon(icon).title("Bệnh viện tim Tâm Đức"));

        final LatLng SubAddress2 = new LatLng(CrescentMall.Latitue,CrescentMall.Longtitue);
        mMap.addMarker(new MarkerOptions().position(SubAddress2).icon(icon).title("Trung tâm mua sắm CrescentMall"));

        final LatLng SubAddress3 = new LatLng(CoffeePower.Latitue,CoffeePower.Longtitue);
        mMap.addMarker(new MarkerOptions().position(SubAddress3).icon(icon).title("Coffee Power"));

        final LatLng SubAddress4 = new LatLng(WaterfrontPresident.Latitue,WaterfrontPresident.Longtitue);
        mMap.addMarker(new MarkerOptions().position(SubAddress4).icon(icon).title("Waterfront President"));

        final LatLng SubAddress5 = new LatLng(Sacombank.Latitue,Sacombank.Longtitue);
        mMap.addMarker(new MarkerOptions().position(SubAddress5).icon(icon).title("PGD Sacombank"));

        store.add(SubAddress1);
        store.add(SubAddress2);
        store.add(SubAddress3);
        store.add(SubAddress4);
        store.add(SubAddress5);

        Distance.add(CalculationByDistance(SSKCompany,SubAddress1));
        Distance.add(CalculationByDistance(SSKCompany,SubAddress2));
        Distance.add(CalculationByDistance(SSKCompany,SubAddress3));
        Distance.add(CalculationByDistance(SSKCompany,SubAddress4));
        Distance.add(CalculationByDistance(SSKCompany,SubAddress5));

        choseLocation = (Spinner) findViewById(R.id.chose_location);
        btnNearest = (Button) findViewById(R.id.btnNearest);
        btnNearest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < Distance.size(); i++)
                {
                    if(Distance.get(i) < min) {
                        min = Distance.get(i);
                        positionNearest = i;
                    }
                }
                String url = getRequestUrl(SSKCompany,store.get(positionNearest));
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url);
                choseLocation.setSelection(positionNearest);
            }
        });
        final String[] location = {"Bệnh viện Tâm Đức", "Trung tâm mua sắm CrescentMall","Coffee Power","Waterfront President","Phòng giao dịch Sacombank"};
        ArrayAdapter<String> locationList = new ArrayAdapter(this, android.R.layout.simple_spinner_item,location);
        choseLocation.setAdapter(locationList);
        locationList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        class activity implements android.widget.AdapterView.OnItemSelectedListener {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                String text = choseLocation.getSelectedItem().toString();
                if(text.compareTo(location[0])==0)
                {
                    String url = getRequestUrl(SSKCompany,SubAddress1);
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                }
                if(text.compareTo(location[1])==0)
                {
                    String url = getRequestUrl(SSKCompany,SubAddress2);
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                }
                if(text.compareTo(location[2])==0)
                {
                    String url = getRequestUrl(SSKCompany,SubAddress3);
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                }
                if(text.compareTo(location[3])==0)
                {
                    String url = getRequestUrl(SSKCompany,SubAddress4);
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                }
                if(text.compareTo(location[4])==0)
                {
                    String url = getRequestUrl(SSKCompany,SubAddress5);
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        }
        choseLocation.setOnItemSelectedListener(new activity());

    }

    private String getRequestUrl(LatLng sourceLocation, LatLng desLocation) {
        // Origin of route
        points.removeAll(points);
        mMap.clear();

        Object_Maps Hospital = new Object_Maps(10.732955, 106.719018); // Benh vien tim tam duc
        Object_Maps CrescentMall = new Object_Maps(10.728559, 106.719030);// Trung tam mua sam CrescentMall
        Object_Maps CoffeePower = new Object_Maps(10.729259, 106.727104); // Coffee Power
        Object_Maps WaterfrontPresident = new Object_Maps(10.725592, 106.725340);// Waterfront President
        Object_Maps Sacombank = new Object_Maps(10.725832, 106.720471); // Sacombank
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.locate_icon);

        final LatLng SubAddress1 = new LatLng(Hospital.Latitue,Hospital.Longtitue);
        mMap.addMarker(new MarkerOptions().position(SubAddress1).icon(icon).title("Bệnh viện tim Tâm Đức"));

        final LatLng SubAddress2 = new LatLng(CrescentMall.Latitue,CrescentMall.Longtitue);
        mMap.addMarker(new MarkerOptions().position(SubAddress2).icon(icon).title("Trung tâm mua sắm CrescentMall"));

        final LatLng SubAddress3 = new LatLng(CoffeePower.Latitue,CoffeePower.Longtitue);
        mMap.addMarker(new MarkerOptions().position(SubAddress3).icon(icon).title("Coffee Power"));

        final LatLng SubAddress4 = new LatLng(WaterfrontPresident.Latitue,WaterfrontPresident.Longtitue);
        mMap.addMarker(new MarkerOptions().position(SubAddress4).icon(icon).title("Waterfront President"));

        final LatLng SubAddress5 = new LatLng(Sacombank.Latitue,Sacombank.Longtitue);
        mMap.addMarker(new MarkerOptions().position(SubAddress5).icon(icon).title("PGD Sacombank"));

        String str_origin = "origin=" + sourceLocation.latitude + "," + sourceLocation.longitude;
        // Destination of route
        String str_dest = "destination=" + desLocation.latitude + "," + desLocation.longitude;
        // Mode
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    private String requestDirection (String requestURL) throws IOException {
        String response = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(requestURL);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //get response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            response = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return response;
    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String response = "";
            try {
                response = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TaskPraser taskPraser = new TaskPraser();
            taskPraser.execute(s);
        }
    }

    public class TaskPraser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DataPraser dataPraser = new DataPraser();
                routes = dataPraser.parse(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {

            // Traversing through all the routes
            for (int i = 0; i < lists.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = lists.get(i);
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(15);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);

            }

            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Toast.makeText(MapsActivity.this,"Not found directions",Toast.LENGTH_SHORT).show();Log.d("mylog", "without Polylines drawn");
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 92 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
        }
        else {
            Toast.makeText(MapsActivity.this,"provide permission please",Toast.LENGTH_SHORT).show();
        }
    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
//        double valueResult = Radius * c;
//        double km = valueResult / 1;
//        DecimalFormat newFormat = new DecimalFormat("####");
//        int kmInDec = Integer.valueOf(newFormat.format(km));
//        double meter = valueResult % 1000;
//        int meterInDec = Integer.valueOf(newFormat.format(meter));
//        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
//                + " Meter   " + meterInDec);

        return Radius * c;
    }

}
