package com.einkaufsheld.help2buy;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocationService extends Service {

    private static final String TAG = "RealTimeLocation";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 5000;
    private static final float LOCATION_DISTANCE = 10f;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;


        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "gooooooooooooooooooooooooooooooooz: " + location);
            FirebaseApp.initializeApp(getApplicationContext());
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseDatabase.getInstance().getReference().child("Available Suppliers")
                    .orderByChild("supplierUid")
                    .equalTo(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                FirebaseDatabase.getInstance().getReference().child("Ongoing Orders")
                                        .orderByChild("supplierUid")
                                        .equalTo(user.getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot availableSupplierSnapshot : dataSnapshot.getChildren()) {
                                                    availableSupplierSnapshot.getRef().child("supplierLat").setValue(location.getLatitude());
                                                    availableSupplierSnapshot.getRef().child("supplierLng").setValue(location.getLongitude());


                                                }

                                            }


                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            } else {
                                for (DataSnapshot availableSupplierSnapshot : dataSnapshot.getChildren()) {
                                    availableSupplierSnapshot.getRef().child("supplierLat").setValue(location.getLatitude());
                                    availableSupplierSnapshot.getRef().child("supplierLng").setValue(location.getLongitude());


                                }

                            }
                        }

                            @Override
                            public void onCancelled (@NonNull DatabaseError error){

                            }
                        });
            mLastLocation.set(location);
                    }


            @Override
            public void onProviderDisabled (String provider){
                Log.e(TAG, "onProviderDisabled: " + provider);
            }

            @Override
            public void onProviderEnabled (String provider){
                Log.e(TAG, "onProviderEnabled: " + provider);
            }

            @Override
            public void onStatusChanged (String provider,int status, Bundle extras){
                Log.e(TAG, "onStatusChanged: " + provider);
            }
        }

        LocationListener[] mLocationListeners = new LocationListener[]{
                new LocationListener(LocationManager.GPS_PROVIDER),
                new LocationListener(LocationManager.NETWORK_PROVIDER)
        };

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.e(TAG, "onStartCommand");
            super.onStartCommand(intent, flags, startId);
            return START_STICKY;
        }

        @Override
        public void onCreate() {
            Log.e(TAG, "onCreate");
            initializeLocationManager();
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[1]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "network provider does not exist, " + ex.getMessage());
            }
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "gps provider does not exist " + ex.getMessage());
            }
        }

        @Override
        public void onDestroy() {
            Log.e(TAG, "onDestroy");
            super.onDestroy();
            if (mLocationManager != null) {
                for (int i = 0; i < mLocationListeners.length; i++) {
                    try {
                        mLocationManager.removeUpdates(mLocationListeners[i]);
                    } catch (Exception ex) {
                        Log.i(TAG, "fail to remove location listners, ignore", ex);
                    }
                }
            }
        }

        private void initializeLocationManager() {
            Log.e(TAG, "initializeLocationManager");
            if (mLocationManager == null) {
                mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            }
        }
    }