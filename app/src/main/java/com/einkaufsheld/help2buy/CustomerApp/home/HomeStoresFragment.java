package com.einkaufsheld.help2buy.CustomerApp.home;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.einkaufsheld.help2buy.CustomerApp.MapsActivity;
import com.einkaufsheld.help2buy.CustomerApp.ShoppingListActivity;
import com.einkaufsheld.help2buy.CustomerApp.home.model.ReceivedPack;
import com.einkaufsheld.help2buy.CustomerApp.home.model.Result;
import com.einkaufsheld.help2buy.CustomerApp.home.model.Route;
import com.einkaufsheld.help2buy.CustomerApp.home.model.RouteReceived;
import com.einkaufsheld.help2buy.CustomerApp.home.model.Step;
import com.einkaufsheld.help2buy.MyLocation;
import com.einkaufsheld.help2buy.R;
import com.einkaufsheld.help2buy.ShoppingOrderDetails;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.CUS_RETURN_FROM_MAP;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.CUS_START_MAP;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.START_ADD_SUPERMARKET;
import static com.einkaufsheld.help2buy.CustomerApp.home.HomeStoresViewAdapter.ROUTE_MATRIX;
import static com.einkaufsheld.help2buy.MainActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.einkaufsheld.help2buy.MainActivity.lastKnownLocation;
import static com.einkaufsheld.help2buy.MainActivity.locationPermissionGranted;

public class HomeStoresFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, HomeStoresViewAdapter.EventListener {

    private ArrayList<ResultsWithPhoto> SUPERMARKETS_LIST = new ArrayList<>();
    private HomeStoresViewAdapter adapter;
    private int requestCount = 0;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    public LinearLayoutManager linearLayoutManager;
    public FusedLocationProviderClient fusedLocationProviderClient;
    public ProgressBar progressBar;


    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.cus_fragment_home_stores, container, false);
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), this.getString(R.string.google_maps_key));
        }
        progressBar = root.findViewById(R.id.cus_fragment_home_stores_progress_bar);
        mRecyclerView = root.findViewById(R.id.stores_home_recyclerview);
        TextView anySupermarket = root.findViewById(R.id.cus_home_stores_any_supermarket);
        swipeRefreshLayout = root.findViewById(R.id.home_store_fragment_pull_to_refresh);
        FloatingActionButton floatingActionButton = root.findViewById(R.id.cus_home_stores_fab);

        getLocationPermission();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        adapter = new HomeStoresViewAdapter(SUPERMARKETS_LIST, this, this::getLocation);
        linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(adapter);

        anySupermarket.setOnClickListener(view -> {

            StringBuilder distanceBuilder = new StringBuilder().append(this.getContext().getString(R.string.distance)).append(": ").append(this.getContext().getString(R.string.unavailable));
            StringBuilder addressBuilder = new StringBuilder().append(this.getContext().getString(R.string.address)).append(": ").append(this.getContext().getString(R.string.unavailable));
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            Intent shoppingListIntent = new Intent(this.getContext(), ShoppingListActivity.class);
            ShoppingOrderDetails shoppingOrderDetails = new ShoppingOrderDetails(0.0, 0.0, "0", "0", user.getUid(),
                    0.0, 0.0, 0.0, 0.0,
                    this.getContext().getString(R.string.unspecified_supermarket), "",
                    "0", "0", "0", "0", "0.00", "0.00", "0.00", new ArrayList<>(),
                    0.0, "No Info", distanceBuilder.toString(), addressBuilder.toString(), "0");

            shoppingListIntent.putExtra("Shopping Details", shoppingOrderDetails);
            shoppingListIntent.putExtra("requestCode", START_ADD_SUPERMARKET);

            startActivity(shoppingListIntent);
        });


        swipeRefreshLayout.setOnRefreshListener(this);

        floatingActionButton.setOnClickListener(view -> {

            Intent mapsIntent = new Intent(this.getContext(), MapsActivity.class);
            startActivityForResult(mapsIntent, CUS_START_MAP);

        });



        return root;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CUS_START_MAP && resultCode == CUS_RETURN_FROM_MAP) {

            Bundle bundle = data != null ? data.getExtras() : null;
            if (bundle != null) {
                lastKnownLocation.setLatitude(bundle.getDouble("Last Known Location Lat"));
                lastKnownLocation.setLongitude(bundle.getDouble("Last Known Location Lng"));
                ResetList();
                RequestSingleton.getInstance(getContext()).addToRequestQueue(findSupermarketRequest(lastKnownLocation));
            }

        }

    }

    private JsonObjectRequest findSupermarketRequest(final Location location) {
        return new JsonObjectRequest(Request.Method.GET, getFindSupermarketUrl(location), null, response -> {
            ReceivedPack receivedPack = new Gson().fromJson(new JsonParser().parse(response.toString()), ReceivedPack.class);
            for (Result it : receivedPack.getResults()) {

                if (!Places.isInitialized()) {
                    Places.initialize(requireContext(), this.getString(R.string.google_maps_key));
                }
                // Define a Place ID.


                final String placeId = it.getReference();

// Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
                final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

// Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
                final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);
                PlacesClient placesClient = Places.createClient(requireContext());
                placesClient.fetchPlace(placeRequest).addOnSuccessListener((photoResponse) -> {
                    final Place place = photoResponse.getPlace();

                    // Get the photo metadata.
                    final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
                    if (metadata == null || metadata.isEmpty()) {
                        Log.w(Constraints.TAG, "No photo metadata.");
                        return;
                    }
                    final PhotoMetadata photoMetadata = metadata.get(0);

                    // Get the attribution text.
//                    final String attributions = photoMetadata.getAttributions();

                    // Create a FetchPhotoRequest.
                    final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
//                            .setMaxWidth(500) // Optional.
                            .setMaxHeight(300) // Optional.
                            .build();
                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                        Bitmap bitmap = fetchPhotoResponse.getBitmap();
                        RequestSingleton.getInstance(getContext()).addToRequestQueue(getRouteRequest(location, new ResultsWithPhoto(it, bitmap)));
                        if (!SUPERMARKETS_LIST.contains(new ResultsWithPhoto(it, bitmap))) {
                            SUPERMARKETS_LIST.add(new ResultsWithPhoto(it, bitmap));
                        }
                    }).addOnFailureListener((exception) -> {
                        Log.d(TAG, "Error Message: " + exception);
                    });
                });
            }

            Log.i(TAG, "onSupermarketResponse: " + response.toString());
        }, error -> Log.e(TAG, "onErrorResponse: ", error));
    }

    private String getFindSupermarketUrl(Location location) {
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + "location=" + location.getLatitude() + "," + location.getLongitude() +
                "&radius=" + 15000 +
                "&keyword=" + "supermarket" +
                "&key=" + getString(R.string.google_maps_key);
    }

    private JsonObjectRequest getRouteRequest(final Location origin, final ResultsWithPhoto dest) {
        return new JsonObjectRequest(Request.Method.GET, getRouteUrl(origin, dest.getResult()), null, response -> {
            RouteReceived routeReceived = new Gson().fromJson(new JsonParser().parse(response.toString()), RouteReceived.class);
            List<Route> routes = routeReceived.getRoutes();

            if (routes.size() > 0) {
                ROUTE_MATRIX.put(new Pair<>(origin, dest.getResult()), routes.get(0));
            } else {
                ROUTE_MATRIX.put(new Pair<>(origin, dest.getResult()), null);
            }

            int idx = SUPERMARKETS_LIST.indexOf(dest);
            adapter.notifyItemChanged(idx);
            requestCount++;

            if (requestCount == SUPERMARKETS_LIST.size()) {
                progressBar.setVisibility(View.GONE);
                transferSort();
                adapter.notifyDataSetChanged();
            }
            Log.i(VolleyLog.TAG, "onRouteResponse: " + response.toString());
        }, error -> Log.e(VolleyLog.TAG, "onErrorResponse: ", error));
    }

    private String getRouteUrl(Location origin, Result dest) {
        return "https://maps.googleapis.com/maps/api/directions/json?" + "origin=" + origin.getLatitude() + "," + origin.getLongitude() +
                "&destination=" + dest.getGeometry().getLocation().getLat() + "," + dest.getGeometry().getLocation().getLng() +
                "&mode=" + "walking" +
                "&key=" + this.getString(R.string.google_maps_key);
    }

    private void transferSort() {

        Collections.sort(SUPERMARKETS_LIST, (o1, o2) -> {
            int transfer1 = 0, transfer2 = 0, notGo1 = 0, notGo2 = 0, sum1 = 0, sum2 = 0;
            Route route1 = ROUTE_MATRIX.get(Pair.create(lastKnownLocation, o1.getResult()));
            Route route2 = ROUTE_MATRIX.get(Pair.create(lastKnownLocation, o2.getResult()));
            if (route1 == null) {
                notGo1++;
            } else {
                sum1 += route1.getLegs().get(0).getDistance().getValue();
                for (Step step : route1.getLegs().get(0).getSteps()) {
                    if ("walking".equals(step.getTravelMode())) {
                        transfer1 += 1;
                    }

                }
                if (route2 == null) {
                    notGo2++;
                } else {
                    sum2 += route2.getLegs().get(0).getDistance().getValue();
                    for (Step step : route2.getLegs().get(0).getSteps()) {
                        if ("walking".equals(step.getTravelMode())) {
                            transfer2 += 1;
                        }
                    }
                }

            }
            if (notGo1 != notGo2) {
                return notGo1 - notGo2;
            } else if (transfer1 != transfer2) {
                return transfer1 - transfer2;
            } else {
                return sum1 - sum2;
            }
        });
    }


    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this.requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        if (locationPermissionGranted){
            getSurroundingSupermarkets();
        }
    }

    public void getSurroundingSupermarkets() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                lastKnownLocation = location;
                RequestSingleton.getInstance(getContext()).addToRequestQueue(findSupermarketRequest(lastKnownLocation));

            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(getContext(), locationResult);



//
//        try {
//            if (locationPermissionGranted) {
//
//                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity());
//                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
//                locationResult.addOnCompleteListener(this.requireActivity(), task -> {
//                    if (task.isSuccessful()) {
//                        // Set the map's camera position to the current location of the device.
//                        lastKnownLocation = task.getResult();
//                        Log.d("TAG", "2222222222222222222222222222222222222222");
//
//                        if (lastKnownLocation != null) {
//                            Log.d("TAG", "111111111111111111111111111111111111111111111");
//
//                            RequestSingleton.getInstance(getContext()).addToRequestQueue(findSupermarketRequest(lastKnownLocation));
//
//
//                        }
//                    } else {
//                        Log.d(TAG, "Current location is null. Using defaults.");
//                        Log.e(TAG, "Exception: %s", task.getException());
////                            map.moveCamera(CameraUpdateFactory
////                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
////                            map.getUiSettings().setMyLocationButtonEnabled(false);
//                    }
//                });
//            }
//        } catch (SecurityException e) {
//            Log.e("Exception: %s", e.getMessage(), e);
//        }

    }

    @Override
    public void onResume() {
        super.onResume();



    }

    @Override
    public void onRefresh() {
        if(lastKnownLocation == null) {
            swipeRefreshLayout.setRefreshing(false);
        } else {
            swipeRefreshLayout.setRefreshing(true);
            ResetList();
            RequestSingleton.getInstance(getContext()).addToRequestQueue(findSupermarketRequest(lastKnownLocation));
            if (requestCount == SUPERMARKETS_LIST.size()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }



    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            getLocationPermission();
            ResetList();
            getSurroundingSupermarkets();
        } else {
//            SUPERMARKETS_LIST = savedInstanceState.getParcelableArrayList("Supermarket List");
//            int position = savedInstanceState.getInt("List Position");
//            linearLayoutManager.scrollToPosition(position);
//            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        Log.d("TAG", "onSaveInstanceState: kiiiiiiiiiiiiiiiiiiiiiiiiir       ") ;
//        outState.putParcelableArrayList("Supermarket List", SUPERMARKETS_LIST);
//        outState.putInt("List Position", linearLayoutManager.findFirstVisibleItemPosition());
    }

    public Location getLocation() {
        return lastKnownLocation;
    }

    public void ResetList() {
        SUPERMARKETS_LIST.clear();
        adapter.notifyDataSetChanged();
        requestCount = 0;
        progressBar.setVisibility(View.VISIBLE);

    }
}

