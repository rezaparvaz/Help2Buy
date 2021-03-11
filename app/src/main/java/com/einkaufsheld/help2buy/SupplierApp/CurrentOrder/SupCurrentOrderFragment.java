package com.einkaufsheld.help2buy.SupplierApp.CurrentOrder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.einkaufsheld.help2buy.CustomerApp.ItemDetails;
import com.einkaufsheld.help2buy.LocationService;
import com.einkaufsheld.help2buy.R;
import com.einkaufsheld.help2buy.ShoppingOrderDetails;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SupCurrentOrderFragment extends Fragment {

    public FirebaseUser user;
    public ShoppingOrderDetails currentOrder;
    public LinearLayout linearLayout, supCurrentOrderEmptyView;
    public ExtendedFloatingActionButton extendedFloatingActionButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sup_fragment_current_order, container, false);
        user = FirebaseAuth.getInstance().getCurrentUser();
        Toolbar toolbar = root.findViewById(R.id.sup_current_order_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);// set drawable icon
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        setHasOptionsMenu(true);
        extendedFloatingActionButton = root.findViewById(R.id.sup_current_order_fab);
        supCurrentOrderEmptyView = root.findViewById(R.id.sup_current_order_fragment_empty_view);
        linearLayout = root.findViewById(R.id.sup_current_order_linear_layout);
        TextView fromSupermarket = root.findViewById(R.id.sup_current_order_from_supermarket);
        TextView supermarketAddress = root.findViewById(R.id.sup_current_order_from_address);
        TextView customerAddress = root.findViewById(R.id.sup_current_order_to_address);
        LinearLayout shoppingList = root.findViewById(R.id.sup_current_order_list_view_container);
        LinearLayout priceLinearLayout = root.findViewById(R.id.sup_current_order_shopping_list_price_linear_layout);
        Button direction = root.findViewById(R.id.sup_current_order_directions_button);
        Button deliveredButton = root.findViewById(R.id.sup_current_order_delivered_button);
        EditText priceEditText = root.findViewById(R.id.sup_current_order_shopping_list_price_edit_text);
        deliveredButton.setText("I have delivered the order");
        SupCurrentOrderInactiveView();


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference().child("Ongoing Orders")
                .orderByChild("supplierUid")
                .equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.exists()) {
                            SupCurrentOrderInactiveView();
                        } else {
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                currentOrder = childSnapshot.getValue(ShoppingOrderDetails.class);

                                SupCurrentOrderActiveView();

                                StringBuilder supermarketNameBuilder = new StringBuilder().append("From: ").append(currentOrder.getSupermarketName());
                                fromSupermarket.setText(supermarketNameBuilder);
                                fromSupermarket.setTypeface(Typeface.DEFAULT_BOLD);

                                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                                try {
                                    if (!(currentOrder.getSupermarketLat() == 0.0 && currentOrder.getSupermarketLng() == 0.0)) {
                                        List<Address> supermarketAddressList = geocoder.getFromLocation(currentOrder.getSupermarketLat(), currentOrder.getSupermarketLng(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                        StringBuilder supermarketAddressBuilder = new StringBuilder().append("Address: ").append(supermarketAddressList.get(0).getAddressLine(0));
                                        supermarketAddress.setText(supermarketAddressBuilder);
                                    } else {
                                        supermarketAddress.setText(R.string.address_not_specified);
                                    }
                                    supermarketAddress.setTypeface(Typeface.DEFAULT_BOLD);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    List<Address> customerAddressList = geocoder.getFromLocation(currentOrder.getCustomerLat(), currentOrder.getCustomerLng(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                    StringBuilder customerAddressBuilder = new StringBuilder().append("To: ").append(customerAddressList.get(0).getAddressLine(0));
                                    customerAddress.setText(customerAddressBuilder);
                                    customerAddress.setTypeface(Typeface.DEFAULT_BOLD);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                shoppingList.removeAllViews();
                                for (int i = 0; i < currentOrder.getShoppingList().size(); i++) {
                                    ItemDetails itemDetails = currentOrder.getShoppingList().get(i);
                                    View vi = LayoutInflater.from(getContext()).inflate(R.layout.cus_cart_fragment_single_shopping_list_item, null);
                                    TextView itemNumber = vi.findViewById(R.id.cart_fragment_shopping_list_item_number);
                                    TextView mainItemQuantityDetail = vi.findViewById(R.id.cart_fragment_shopping_list_item_main_quantity_detail);
                                    TextView optionalItemQuantityDetail = vi.findViewById(R.id.cart_fragment_shopping_list_item_optional_quantity_detail);
                                    TextView bioOrCheapestOrNone = vi.findViewById(R.id.cart_fragment_shopping_list_item_bio_or_cheapest_or_none);
                                    itemNumber.setText(String.valueOf(i + 1));
                                    String concatenateMain = itemDetails.getQuantityMain() + " " + itemDetails.getPieceOrKgMain() + " " + itemDetails.getDetailsMain();
                                    mainItemQuantityDetail.setText(concatenateMain);
                                    String concatenateOptional = itemDetails.getQuantityOptional() + " " + itemDetails.getPieceOrKgOptional() + " " + itemDetails.getDetailsOptional();
                                    optionalItemQuantityDetail.setText(concatenateOptional);
                                    bioOrCheapestOrNone.setText(itemDetails.getBioOrCheapestOrNone());
                                    shoppingList.addView(vi);
                                }

                                extendedFloatingActionButton.setOnClickListener(view -> {
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    DocumentReference docRef = db.collection("users").document(currentOrder.getCustomerUid());
                                    docRef.get().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            String customerPhoneNumber = document.get("PhoneNumber").toString();

                                            try {
                                                if (Build.VERSION.SDK_INT > 22) {
                                                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                                                        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CALL_PHONE}, 101);

                                                        return;
                                                    }

                                                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                                                    callIntent.setData(Uri.parse("tel:" + customerPhoneNumber));
                                                    startActivity(callIntent);

                                                } else {
                                                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                                                    callIntent.setData(Uri.parse("tel:" + customerPhoneNumber));
                                                    startActivity(callIntent);
                                                }
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    });

                                });
                                if (!(currentOrder.getSupermarketLat() == 0.0 && currentOrder.getSupermarketLng() == 0.0)) {
                                    direction.setOnClickListener(view -> {

                                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + currentOrder.getCustomerLat() + "," +
                                                        currentOrder.getCustomerLng() + "&waypoints=" + currentOrder.getSupermarketLat() + "," +
                                                        currentOrder.getSupermarketLng() + "&mode=bicycling"));
                                        startActivity(intent);
                                    });
                                } else {
                                    direction.setVisibility(View.GONE);
                                }

                                deliveredButton.setOnClickListener(view -> {

                                    priceLinearLayout.setVisibility(View.VISIBLE);
                                    deliveredButton.setText("Confirm");
                                    deliveredButton.setOnClickListener(view1 -> {

                                        FirebaseDatabase.getInstance().getReference().child(getString(R.string.ongoing_orders))
                                                .orderByChild("supplierUid")
                                                .equalTo(user.getUid())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                                            childSnapshot.getRef().removeValue();
                                                            try {
                                                                FirebaseFunctions.getInstance().getHttpsCallable("getTime")
                                                                        .call().addOnSuccessListener(httpsCallableResult -> {
                                                                    long timestamp = (long) httpsCallableResult.getData();
                                                                    currentOrder.setOrderStatus(getString(R.string.pending_payment));
                                                                    currentOrder.setOrderEndTime(Long.toString(timestamp));
                                                                    currentOrder.setShoppingListSuppliersPrice(priceEditText.getText().toString());
                                                                    FirebaseDatabase.getInstance().getReference().child(getString(R.string.finished_orders)).child(getString(R.string.pending_payments)).push().setValue(currentOrder);
                                                                    requireActivity().stopService(new Intent(getContext(), LocationService.class));


                                                                });


                                                            } catch (Exception ex) {
                                                                Log.d("TAG", "onAcceptingOrder: " + ex);
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                    });

                                });

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        SupCurrentOrderInactiveView();
                    }
                });


        return root;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setMessage("Are you sure that you want to cancel this order?");


                alertDialog.setPositiveButton("Yes",
                        (dialog, which) -> {

                            FirebaseDatabase.getInstance().getReference().child(getString(R.string.ongoing_orders))
                                    .orderByChild("supplierUid")
                                    .equalTo(user.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                                childSnapshot.getRef().removeValue();

                                                try {
                                                    FirebaseFunctions.getInstance().getHttpsCallable("getTime")
                                                            .call().addOnSuccessListener(httpsCallableResult -> {
                                                        long timestamp = (long) httpsCallableResult.getData();
                                                        currentOrder.setOrderStatus("Supplier Cancelled The Order");
                                                        currentOrder.setOrderEndTime(Long.toString(timestamp));
                                                        FirebaseDatabase.getInstance().getReference().child(getString(R.string.finished_orders)).child(getString(R.string.cancelled_orders)).push().setValue(currentOrder);
                                                        requireActivity().stopService(new Intent(getContext(), LocationService.class));


                                                    });

                                                } catch (Exception ex) {
                                                    Log.d("TAG", "onAcceptingOrder: " + ex);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });


                        });

                alertDialog.setNegativeButton("No",
                        (dialog, which) -> dialog.cancel());

                alertDialog.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void SupCurrentOrderActiveView() {
        linearLayout.setVisibility(View.VISIBLE);
        supCurrentOrderEmptyView.setVisibility(View.GONE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        extendedFloatingActionButton.setVisibility(View.VISIBLE);

    }

    public void SupCurrentOrderInactiveView() {
        linearLayout.setVisibility(View.GONE);
        supCurrentOrderEmptyView.setVisibility(View.VISIBLE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        extendedFloatingActionButton.setVisibility(View.GONE);

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