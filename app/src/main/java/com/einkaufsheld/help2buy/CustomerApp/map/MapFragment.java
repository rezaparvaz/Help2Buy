package com.einkaufsheld.help2buy.CustomerApp.map;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.einkaufsheld.help2buy.R;
import com.einkaufsheld.help2buy.ShoppingOrderDetails;
import com.einkaufsheld.help2buy.SupplierApp.Home.SupActiveSupplier;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static com.einkaufsheld.help2buy.CustomerApp.MapsActivity.DEFAULT_ZOOM;
import static com.einkaufsheld.help2buy.MainActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {

    private GoogleMap mMap;
    private boolean locationPermissionGranted;
    private Geocoder geocoder;
    public FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng mDefaultLocation;
    private View mapView;
    private Location mLastKnownLocation;
    public Marker mapMarker, supplierMapMarker, customerMapMarker, supermarketMapMarker;
    private Polyline mPolyline;
    public FirebaseUser user;
    public LatLng currentLocation;
    public ArrayList<MapMarker> suppliersPositionsMarkersList = new ArrayList<>();
    int indicator = 5000;

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
        mMap = googleMap;

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
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.ongoing_orders))
                .orderByChild("customerUid")
                .equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                ShoppingOrderDetails currentOrder = snapshot.getValue(ShoppingOrderDetails.class);

                                try {
                                    List<Address> customerAddress = geocoder.getFromLocation(currentOrder.getCustomerLat(), currentOrder.getCustomerLng(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                    StringBuilder customerAddressBuilder = new StringBuilder().append(customerAddress.get(0).getAddressLine(0));

                                    if (!(currentOrder.getSupermarketLat() == 0.0 && currentOrder.getSupermarketLng() == 0.0)){
                                        List<Address> supermarketAddress = geocoder.getFromLocation(currentOrder.getSupermarketLat(), currentOrder.getSupermarketLng(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                        StringBuilder supermarketAddressBuilder = new StringBuilder().append(supermarketAddress.get(0).getAddressLine(0));

                                        BitmapDrawable supermarketBitMapDraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.supermarket_marker);
                                        Bitmap supermarketBitmap = supermarketBitMapDraw.getBitmap();
                                        Bitmap smallSupermarketMarker = Bitmap.createScaledBitmap(supermarketBitmap, 100, 100, false);

                                        supermarketMapMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentOrder.getSupermarketLat(), currentOrder.getSupermarketLng()))
                                                .title(currentOrder.getSupermarketName())
                                                .snippet(String.valueOf(supermarketAddressBuilder))
                                                .icon(BitmapDescriptorFactory.fromBitmap(smallSupermarketMarker)));

                                    }



                                    BitmapDrawable customerBitMapDraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.home_marker);
                                    Bitmap customerBitmap = customerBitMapDraw.getBitmap();
                                    Bitmap smallCustomerMarker = Bitmap.createScaledBitmap(customerBitmap, 102, 122, false);

                                    BitmapDrawable supplierBitMapDraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.delivery_icon);
                                    Bitmap supplierBitmap = supplierBitMapDraw.getBitmap();
                                    Bitmap smallSupplierMarker = Bitmap.createScaledBitmap(supplierBitmap, 102, 122, false);
                                    customerMapMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentOrder.getCustomerLat(), currentOrder.getCustomerLng()))
                                            .title("Your Address")
                                            .snippet(String.valueOf(customerAddressBuilder))
                                            .icon(BitmapDescriptorFactory.fromBitmap(smallCustomerMarker)));

                                    for (int index = 0; index < suppliersPositionsMarkersList.size(); index++) {
                                        String orderID = suppliersPositionsMarkersList.get(index).getOrderID();

                                        if (currentOrder.getOrderID().equals(orderID)) {
                                            indicator = index;
                                        }
                                    }

                                    if (indicator == 5000){
                                        supplierMapMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentOrder.getSupplierLat(), currentOrder.getSupplierLng()))
                                                .icon(BitmapDescriptorFactory.fromBitmap(smallSupplierMarker)));
                                        suppliersPositionsMarkersList.add(new MapMarker(currentOrder.getOrderID(), supplierMapMarker));
                                    } else {
                                        supplierMapMarker = suppliersPositionsMarkersList.get(indicator).getMarker();
                                        supplierMapMarker.setPosition(new LatLng(currentOrder.getSupplierLat(), currentOrder.getSupplierLng()));
                                        suppliersPositionsMarkersList.set(indicator, new MapMarker(currentOrder.getOrderID(),supplierMapMarker));

                                    }



                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                        } else {


                            ChildEventListener mChildEventListener = new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                    SupActiveSupplier supActiveSupplier = snapshot.getValue(SupActiveSupplier.class);

                                    mapMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(supActiveSupplier.getSupplierLat(), supActiveSupplier.getSupplierLng())));


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
                            FirebaseDatabase.getInstance().getReference().child("Available Suppliers").addChildEventListener(mChildEventListener);
                        }

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


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
