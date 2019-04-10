package com.example.locationalarm_apiupto_26.Service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.locationalarm_apiupto_26.Database.MyDatabase;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class LocationService extends Service {

    LocationManager locationManager;
    LocationListener locationListener;
    MyDatabase myDatabase;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Method Call :","onCreate");

        myDatabase=new MyDatabase(LocationService.this);
        locationManager= (LocationManager) LocationService.this.getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                checker(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Method Call :","onStartCommand");


        if (ContextCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void checker(Location currentLocation) {

        Log.e("Method Call :","checker");


        ArrayList<LatLng> setLocation=new ArrayList<>();
        ArrayList<Integer> id=new ArrayList<>();
        ArrayList<Integer> serviceId=new ArrayList<>();

        SQLiteDatabase db=myDatabase.getReadableDatabase();
        Cursor cursor=db.rawQuery("Select "+myDatabase.getID()+","+myDatabase.getLatitude()+","+myDatabase.getLongitude()+","+myDatabase.getServiceID()+" From "+myDatabase.getTableName()+"",null);
        while (cursor.moveToNext()){

            id.add(cursor.getInt(0));
            LatLng lng=new LatLng(cursor.getDouble(1),cursor.getDouble(2));
            setLocation.add(lng);
            serviceId.add(cursor.getInt(3));
        }
        db.close();

        //Log.e("Cursor:",cursor.getCount()+"" );
        if (cursor.getCount()==0){
            stopSelf();
        }

        for (int i=0;i<setLocation.size();i++) {

            Double setLatitude = setLocation.get(i).latitude;
            Double setLongitude = setLocation.get(i).longitude;

            if (setLatitude != null && setLongitude != null) {
                 Log.e("Data:", "Current Location :" + String.valueOf(currentLocation.getLatitude()) + " " + String.valueOf(currentLocation.getLongitude()));
                Log.e("Data:", "Set Location :" + setLatitude + " " + setLongitude);

                if (currentLocation.getLatitude() == setLatitude && currentLocation.getLongitude() == setLongitude) {
                    Log.e("MatchResult :", "Match");
                    stopSelf(serviceId.get(i));
                    long result = myDatabase.deleteService(id.get(i));
                    if (result == -1) {
                        // Log.e("Delete Result :", "Delete Failed");

                    } else {
                        //Log.e("Delete Result :", "Delete");

                    }

                } else {
                    Log.e("MatchResult :", "Not Match");
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.e("onDestroy :", "onDestroy");
        super.onDestroy();
        if (locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }
}
