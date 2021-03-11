package com.einkaufsheld.help2buy.SupplierApp.Home;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bitvale.switcher.SwitcherX;
import com.einkaufsheld.help2buy.LocationService;
import com.einkaufsheld.help2buy.R;
import com.einkaufsheld.help2buy.ShoppingOrderDetails;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.einkaufsheld.help2buy.MainActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.einkaufsheld.help2buy.MainActivity.lastKnownLocation;
import static com.einkaufsheld.help2buy.MainActivity.locationPermissionGranted;


public class SupHomeFragment extends Fragment {

    public FirebaseUser user;
    private SupHomeViewAdapter mSupHomeViewAdapter;
    private ArrayList<ShoppingOrderDetails> allSurroundingOrders = new ArrayList<>();
    private RecyclerView supHomeRecyclerView;
    private LinearLayout supHomeEmptyView;
    private FrameLayout activeView;
    private TextView inactiveTextView;
    public SwitcherX switcherX;
    public ConstraintSet constraintSet = new ConstraintSet();
    public ConstraintLayout constraintLayout;
    private ArrayList<ShoppingOrderDetails> toBeRemovedOrders = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sup_fragment_home, container, false);
        getLocationPermission();
        user = FirebaseAuth.getInstance().getCurrentUser();
        inactiveTextView = root.findViewById(R.id.sup_home_fragment_inactive_text);
        activeView = root.findViewById(R.id.sup_home_fragment_active_view);
        switcherX = root.findViewById(R.id.sup_home_fragment_switcher);
        supHomeEmptyView = root.findViewById(R.id.sup_home_fragment_empty_list);
        supHomeRecyclerView = root.findViewById(R.id.sup_home_fragment_recyclerview);

        constraintLayout = root.findViewById(R.id.sup_home_fragment_inactive_view);
        constraintSet.clone(constraintLayout);

        activeView.setVisibility(View.GONE);
        inactiveTextView.setVisibility(View.VISIBLE);
        inactiveTextView.setText("Go active and pick up orders!");




        switcherX.setOnCheckedChangeListener(checked -> {
            if (checked) {
                CheckSupplierAcceptedAnOrder();






            } else {

                requireActivity().stopService(new Intent(getContext(), LocationService.class));

                constraintSet.connect(R.id.sup_home_fragment_switcher, ConstraintSet.TOP, R.id.sup_home_fragment_inactive_view, ConstraintSet.TOP, 0);
                constraintSet.applyTo(constraintLayout);

                FirebaseDatabase.getInstance().getReference().child("Available Suppliers")
                        .orderByChild("supplierUid")
                        .equalTo(user.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    childSnapshot.getRef().removeValue();

                                }
                                activeView.setVisibility(View.GONE);
                                inactiveTextView.setVisibility(View.VISIBLE);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


            }
            return null;
        });


        return root;


    }

    private void StartFetchingOrders() {
        activeView.setVisibility(View.VISIBLE);

        mSupHomeViewAdapter = new SupHomeViewAdapter(allSurroundingOrders, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        supHomeRecyclerView.setLayoutManager(linearLayoutManager);
        supHomeRecyclerView.setItemAnimator(new DefaultItemAnimator());
        supHomeRecyclerView.setAdapter(mSupHomeViewAdapter);
        allSurroundingOrders.clear();
        ChildEventListener mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ShoppingOrderDetails newOrder = snapshot.getValue(ShoppingOrderDetails.class);
                allSurroundingOrders.add(newOrder);
                mSupHomeViewAdapter.notifyDataSetChanged();
                SupHomeViewsCheck();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = 10000;
                ShoppingOrderDetails removedOrder = snapshot.getValue(ShoppingOrderDetails.class);
                for (ShoppingOrderDetails individualOrder: allSurroundingOrders){
                    if(individualOrder.getOrderID().equals(removedOrder.getOrderID())){
                        index = allSurroundingOrders.indexOf(individualOrder);

                    }
                }
                if (index != 10000){
                    allSurroundingOrders.remove(index);
                    mSupHomeViewAdapter.notifyDataSetChanged();
                    SupHomeViewsCheck();
                }

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

    public void SupHomeViewsCheck() {
        if (allSurroundingOrders.isEmpty()) {
            supHomeRecyclerView.setVisibility(View.GONE);
            supHomeEmptyView.setVisibility(View.VISIBLE);
        } else {
            supHomeRecyclerView.setVisibility(View.VISIBLE);
            supHomeEmptyView.setVisibility(View.GONE);
        }
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
            FirebaseDatabase.getInstance().getReference().child("Available Suppliers")
                    .orderByChild("supplierUid")
                    .equalTo(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                requireActivity().startService(new Intent(getContext(), LocationService.class));
                                switcherX.setChecked(true, true);
                            } else {
                                CheckSupplierAcceptedAnOrder();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void getLocationAndFetchOrders() {

        try {

            if (locationPermissionGranted) {

                FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity());
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this.requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.

                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {

                            FirebaseDatabase.getInstance().getReference().child("Available Suppliers")
                                    .orderByChild("supplierUid")
                                    .equalTo(user.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.exists()) {
                                                FirebaseDatabase.getInstance().getReference().child("Available Suppliers").push().setValue(new SupActiveSupplier(user.getUid(),
                                                        lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                                                requireActivity().startService(new Intent(getContext(), LocationService.class));

                                            }
                                            StartFetchingOrders();


                                        }


                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                        }

                    } else {
                        Log.d(Constraints.TAG, "Current location is null. Using defaults.");
                        Log.e(Constraints.TAG, "Exception: %s", task.getException());
//                            map.moveCamera(CameraUpdateFactory
//                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
//                            map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    public void CheckSupplierAcceptedAnOrder(){
        FirebaseDatabase.getInstance().getReference().child("Ongoing Orders")
                .orderByChild("supplierUid")
                .equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            switcherX.setChecked(false,true);
                            inactiveTextView.setText("You already accepted an order");
                            requireActivity().startService(new Intent(getContext(), LocationService.class));

                        }
                        if (switcherX.isChecked()){
                            getLocationAndFetchOrders();
                            constraintSet.clear(R.id.sup_home_fragment_switcher, ConstraintSet.TOP);
                            constraintSet.applyTo(constraintLayout);
                            inactiveTextView.setVisibility(View.GONE);
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FirebaseDatabase.getInstance().getReference().child("Available Suppliers")
                .orderByChild("supplierUid")
                .equalTo(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            requireActivity().startService(new Intent(getContext(), LocationService.class));
                            switcherX.setChecked(true, true);
                        } else {
                            CheckSupplierAcceptedAnOrder();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //    public static SupHomeFragment newInstance(String param1, String param2) {
//        SupHomeFragment fragment = new SupHomeFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }


}