package fr.innodev.trd.gpsbasedemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import static java.lang.Math.PI;
import static java.lang.Math.cos;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location myLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private StepCounter stepCounter;
    private LatLng lastLatLng;
    private Circle lastCircle;
    private int seconds = 60;
    ArrayList<LatLng> myWay;
    Marker currMarquer;
    Polyline currLine;
    Polyline dir;

    private MagnetDirection magnetDirection;
    @SuppressLint("MissingPermission")
    @Override

    /*
    Exercice 1 : GPS
        • Récupérer les coordonnées GPS et les afficher.
        • Utiliser les fonctionnalités des listener pour limiter l’accès au GPS toutes les
        15 secondes et mettre à jour la position
        Exercice 2 : Exploitation des données
        • A l’aide des données collectées dans l’exercice 1, estimer la distance parcouru
        entre 2 mesure du GPS.
        • A l’aide des données des autres capteurs, donner une 2ème estimation de la
        distance parcouru
        Exercice 3 : Exploitation des données
        • Étendre le travail de l’exercice 2 pour donner aussi une information de
        direction du déplacement.
        Exercice 4 : Coopération entre capteurs
        • Au lieu de collecter les données GPS toutes les 15 sec, collecter les données
        GPS toutes les 60 sec
        • Continuer à rafraîchir les information de position toutes les 15 sec en utilisant
        les données de l’exercice 3 pour prédire les coordonnées
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounter  = new StepCounter(getApplicationContext(),this, sm);
        magnetDirection  = new MagnetDirection(getApplicationContext(),this, sm);
        while (!permissionGranted()) ;

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            Log.v("INFO", "Location Result" + location.toString());
                            updateMapDisplay(location);
                        }
                    }
                });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    Log.v("INFO", "Location Callback" + location.toString());
                    updateMapDisplay(location);
                }
            }
        };

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(seconds*1000);
        mLocationRequest.setFastestInterval(seconds*1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);

    }

    public  double getDistanceFromLatLonInKm(LatLng latLon1,LatLng lantlon2 ) {
        double lat1 = latLon1.latitude,lon1=latLon1.longitude,lat2 = lantlon2.latitude,lon2 = lantlon2.longitude;
        int  R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        cos(deg2rad(lat1)) * cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    public double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }
    public double rad2deg(double rad) { return rad * (180/Math.PI);
    }

    public LatLng distanceToLatLong(double angle , double distance){
        // distance is meters
        double x = distance * Math.cos(angle);
        double y = distance * Math.sin(angle);

          // convert to long
           x /=111111 ;
           y /=111111/*  * Math.cos(latitud) */ ;

        /*
        quick and dirty estimate that 111,111 meters (111.111 km) in the y direction is 1 degree (of latitude)
         and 111,111 * cos(latitude) meters in the x direction is 1 degree (of longitude).
         */
        return  new LatLng(-y,-x);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }
    public void updateCircle( float distance ){
        if(lastCircle != null){
            lastCircle.setRadius(distance);
        }
        else{
            if(mMap!=null) {
                lastCircle = mMap.addCircle(new CircleOptions()
                        .center(lastLatLng)
                        .radius(distance)
                        .strokeColor(Color.RED)
                        .fillColor(Color.BLUE));

                float direction = magnetDirection.getOrientation()[0];


            }
        }

    }
    public void update1step(){

            if(mMap!=null) {


                double direction = magnetDirection.getOrientation()[0];
                LatLng newPosition = distanceToLatLong(-(direction +(PI/2)),StepCounter.M_PAR_PAS);
                double latToadd = newPosition.latitude + myWay.get(myWay.size()-1).latitude;
                double longToadd = newPosition.longitude + myWay.get(myWay.size()-1).longitude;
                LatLng toAdd = new LatLng(latToadd,longToadd);

                myWay.add(toAdd);

                currLine.setPoints(myWay);


            }


    }
    public void updateDirection( double direction){


        if(mMap!=null) {

            if(dir!=null) dir.remove();
            dir = mMap.addPolyline(new PolylineOptions().color(0xf0ff00ff));

            LatLng newPosition = distanceToLatLong(-(direction+(PI/2)),StepCounter.M_PAR_PAS * 6);
            double latToadd = newPosition.latitude + myWay.get(0).latitude;
            double longToadd = newPosition.longitude + myWay.get(0).longitude;

            ArrayList<LatLng> temp = new ArrayList<>();
             temp.add(myWay.get(0));
             temp.add(new LatLng(latToadd,longToadd));

             dir.setPoints(temp);

        }
    }
    public void updateCircle( LatLng center,float distance ){
        if(lastCircle != null){
            lastCircle.setRadius(distance);
            lastCircle.setCenter(center);
        }
        else{
            lastCircle = mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(distance)
                    .strokeColor(0xaaddddff)
                    .fillColor(0x80ddddff));
        }

    }
    private void updateMapDisplay(Location myLocation) {
        // Add a marker in Sydney and move the camera
        LatLng curPos = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        myWay = new ArrayList<>();
        myWay.add(curPos);
        if(lastLatLng!= null){
            Log.e("sd","km: "+getDistanceFromLatLonInKm(lastLatLng,curPos));
        }

        updateCircle(curPos,0);
        lastLatLng = curPos;

        stepCounter.resetCounter();

        if(currMarquer!=null) currMarquer.remove();
        currMarquer = mMap.addMarker(new MarkerOptions().position(curPos).title("Position courante"));

        if(currLine!=null) currLine.remove();
        currLine = mMap.addPolyline(new PolylineOptions());
        currLine.setPoints(myWay);


        float zoom = mMap.getMaxZoomLevel();

        Log.d("INFO", "Zoom Max = " + zoom);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPos, zoom - 3.0f));
    }

    private boolean permissionGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
