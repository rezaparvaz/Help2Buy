package com.einkaufsheld.help2buy.SupplierApp.Map;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.einkaufsheld.help2buy.ShoppingOrderDetails;
import com.einkaufsheld.help2buy.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static com.einkaufsheld.help2buy.CustomerApp.MapsActivity.DEFAULT_ZOOM;
import static com.einkaufsheld.help2buy.MainActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

public class SupMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {

    private GoogleMap mMap;
    private boolean locationPermissionGranted;
    private Geocoder geocoder;
    public FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng mDefaultLocation;
    private View mapView;
    private Location mLastKnownLocation;
    public Marker mapMarker;
    private Polyline mPolyline;
    public FirebaseUser user;
    public LatLng currentLocation;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.cus_fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        getLocationPermission();
        user = FirebaseAuth.getInstance().getCurrentUser();

        geocoder = new Geocoder(getContext(), Locale.getDefault());


        // Construct a FusedLocationProviderClient.
        mDefaultLocation = new LatLng(-34, 151);

        mapView = mapFragment.getView();


        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            requireContext(), R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        updateLocationUI();
        getDeviceLocation();

        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 150);

        }

        FirebaseDatabase.getInstance().getReference().child(getString(R.string.available_suppliers))
                .orderByChild("supplierUid")
                .equalTo(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            ChildEventListener mChildEventListener = new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                    ShoppingOrderDetails newOrder = snapshot.getValue(ShoppingOrderDetails.class);
                                    BitmapDrawable customerBitMapDraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.home_marker);
                                    Bitmap customerBitmap = customerBitMapDraw.getBitmap();
                                    Bitmap smallCustomerMarker = Bitmap.createScaledBitmap(customerBitmap, 102, 122, false);
                                    mapMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(newOrder.getCustomerLat(), newOrder.getCustomerLng()))
                                            .title("Customer")
                                            .icon(BitmapDescriptorFactory.fromBitmap(smallCustomerMarker)));


                                }

                                @Override
                                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                }

                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                                    ShoppingOrderDetails removedOrder = snapshot.getValue(ShoppingOrderDetails.class);
                                    // Todo : remove marker handle
                                }

                                @Override
                                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            };
                            FirebaseDatabase.getInstance().getReference().child("Pending Orders").addChildEventListener(mChildEventListener);
                        }

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        FirebaseDatabase.getInstance().getReference().child("Ongoing Orders")
                .orderByChild("supplierUid")
                .equalTo(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        {
                            ShoppingOrderDetails currentOrder = snapshot.getValue(ShoppingOrderDetails.class);
                            try {
                                if (!(currentOrder.getSupermarketLat() == 0.0 && currentOrder.getSupermarketLng() == 0.0)){
                                    List<Address> supermarketAddress = geocoder.getFromLocation(currentOrder.getSupermarketLat(), currentOrder.getSupermarketLng(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                    StringBuilder supermarketAddressBuilder = new StringBuilder().append(supermarketAddress.get(0).getAddressLine(0));

                                    BitmapDrawable supermarketBitMapDraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.supermarket_marker);
                                    Bitmap supermarketBitmap = supermarketBitMapDraw.getBitmap();
                                    Bitmap smallSupermarketMarker = Bitmap.createScaledBitmap(supermarketBitmap, 100, 100, false);

                                    mapMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentOrder.getSupermarketLat(), currentOrder.getSupermarketLng()))
                                            .title(currentOrder.getSupermarketName())
                                            .snippet(String.valueOf(supermarketAddressBuilder))
                                            .icon(BitmapDescriptorFactory.fromBitmap(smallSupermarketMarker)));

                                }

                                List<Address> customerAddress = geocoder.getFromLocation(currentOrder.getCustomerLat(), currentOrder.getCustomerLng(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                StringBuilder customerAddressBuilder = new StringBuilder().append(customerAddress.get(0).getAddressLine(0));


                                BitmapDrawable customerBitMapDraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.home_marker);
                                Bitmap customerBitmap = customerBitMapDraw.getBitmap();
                                Bitmap smallCustomerMarker = Bitmap.createScaledBitmap(customerBitmap, 102, 122, false);
                                mapMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentOrder.getCustomerLat(), currentOrder.getCustomerLng()))
                                        .title("Customer Address")
                                        .snippet(String.valueOf(customerAddressBuilder))
                                        .icon(BitmapDescriptorFactory.fromBitmap(smallCustomerMarker)));

                                try {
                                    if (locationPermissionGranted) {
                                        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
                                        Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                                        locationResult.addOnCompleteListener(requireActivity(), task -> {
                                            if (task.isSuccessful()) {
                                                // Set the map's camera position to the current location of the device.
                                                mLastKnownLocation = task.getResult();

                                                if (mLastKnownLocation != null) {
                                                    currentLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                                                    if (!(currentOrder.getSupermarketLat() == 0.0 && currentOrder.getSupermarketLng() == 0.0)) {
                                                        drawRoute(currentLocation, new LatLng(currentOrder.getCustomerLat(), currentOrder.getCustomerLng()),
                                                                new LatLng(currentOrder.getSupermarketLat(), currentOrder.getSupermarketLng()));
                                                    }

                                                } else {

                                                    Log.d("TAG", "Current location is null. Using defaults.");
                                                    Log.e("TAG", "Exception: %s", task.getException());
                                                    mMap.animateCamera(CameraUpdateFactory
                                                            .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                                                }
                                            }
                                        });
                                    }
                                } catch (SecurityException e) {
                                    Log.e("Exception: %s", e.getMessage(), e);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            }

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void drawRoute(LatLng origin , LatLng customerLatLng, LatLng SupermarketLatLng) {

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, customerLatLng, SupermarketLatLng);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin , LatLng customerLatLng, LatLng SupermarketLatLng) {


        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_customerLatLng = "destination=" + customerLatLng.latitude + "," + customerLatLng.longitude;

        String str_SupermarketLatLng = "waypoints=via:" + SupermarketLatLng.latitude + "," + SupermarketLatLng.longitude;

        // Key
        String key = "key=" + getString(R.string.google_maps_key);

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_customerLatLng + "&" + str_SupermarketLatLng + "&" + "mode=walking" +"&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception on download", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * A class to download data from Google Directions URL
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("DownloadTask", "DownloadTask : " + data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Directions in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

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
                lineOptions.width(8);
                lineOptions.color(Color.BLUE);
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                mPolyline = mMap.addPolyline(lineOptions);

            } else
                Toast.makeText(getContext(), "No route is found", Toast.LENGTH_LONG).show();
        }
    }


    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }


    @Override
    public boolean onMyLocationButtonClick() {

        getDeviceLocation();

        return true;
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;

            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();

                        if (mLastKnownLocation != null) {
                            currentLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude()), DEFAULT_ZOOM);
                            mMap.animateCamera(cameraUpdate);


                        } else {
                            currentLocation = mDefaultLocation;
                        }

                    } else {

                        Log.d("TAG", "Current location is null. Using defaults.");
                        Log.e("TAG", "Exception: %s", task.getException());
                        mMap.animateCamera(CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }

                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

}
