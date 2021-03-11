package com.einkaufsheld.help2buy.CustomerApp.cart;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.einkaufsheld.help2buy.CustomerApp.ItemDetails;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CusCurrentOrderActivity extends AppCompatActivity {

    FirebaseUser user;
    ShoppingOrderDetails currentOrder;
    LinearLayout linearLayout, cusCurrentOrderEmptyView, shoppingList, priceApproveLinearLayout, priceEnterLinearLayout;
    ExtendedFloatingActionButton extendedFloatingActionButton;
    TextView fromSupermarket, supermarketAddress, customerAddress, suppliersPrice;
    Button direction, yesButton, noButton, confirmPrice;
    EditText customersPrice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cus_activity_current_order);
        Toolbar toolbar = findViewById(R.id.cus_current_order_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        String orderID = intent.getStringExtra("OrderId");
        user = FirebaseAuth.getInstance().getCurrentUser();
        extendedFloatingActionButton = findViewById(R.id.cus_activity_current_order_fab);
        cusCurrentOrderEmptyView = findViewById(R.id.cus_activity_current_order_fragment_empty_view);
        linearLayout = findViewById(R.id.cus_activity_current_order_linear_layout);
        fromSupermarket = findViewById(R.id.cus_activity_current_order_from_supermarket);
        supermarketAddress = findViewById(R.id.cus_activity_current_order_from_address);
        customerAddress = findViewById(R.id.cus_activity_current_order_to_address);
        shoppingList = findViewById(R.id.cus_activity_current_order_list_view_container);
        direction = findViewById(R.id.cus_activity_current_order_directions_button);
        priceApproveLinearLayout = findViewById(R.id.cus_activity_current_order_shopping_list_price_approve_linear_layout);
        suppliersPrice = findViewById(R.id.cus_activity_current_order_shopping_list_suppliers_price_text);
        noButton = findViewById(R.id.cus_activity_current_order_no);
        yesButton = findViewById(R.id.cus_activity_current_order_yes);
        priceEnterLinearLayout = findViewById(R.id.cus_activity_current_order_shopping_list_price_enter_linear_layout);
        customersPrice = findViewById(R.id.cus_activity_current_order_shopping_list_customer_price_text);
        confirmPrice = findViewById(R.id.cus_activity_current_order_confirm_price_button);
        SupCurrentOrderInactiveView();


        FirebaseDatabase.getInstance().getReference().child(getString(R.string.ongoing_orders))
                .orderByChild("orderID")
                .equalTo(orderID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.exists()) {
                            FirebaseDatabase.getInstance().getReference().child(getString(R.string.finished_orders)).child(getString(R.string.pending_payments))
                                    .orderByChild("orderID")
                                    .equalTo(orderID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if (!dataSnapshot.exists()) {
                                                SupCurrentOrderInactiveView();
                                            } else {
                                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                                    currentOrder = childSnapshot.getValue(ShoppingOrderDetails.class);

                                                    updateUI();
                                                    priceApproveLinearLayout.setVisibility(View.VISIBLE);
                                                    suppliersPrice.setText(currentOrder.getShoppingListSuppliersPrice());
                                                    yesButton.setOnClickListener(view -> {

                                                        currentOrder.setShoppingListCustomersPrice(currentOrder.getShoppingListSuppliersPrice());
                                                        currentOrder.setOrderStatus(getString(R.string.successful_order));
                                                        childSnapshot.getRef().removeValue();
                                                        FirebaseDatabase.getInstance().getReference().child(getString(R.string.finished_orders)).child(getString(R.string.successful_orders)).push().setValue(currentOrder);
                                                        SupCurrentOrderInactiveView();

                                                    });

                                                    noButton.setOnClickListener(view -> {
                                                        priceEnterLinearLayout.setVisibility(View.VISIBLE);
                                                        confirmPrice.setVisibility(View.VISIBLE);

                                                        confirmPrice.setOnClickListener(view1 -> {
                                                            currentOrder.setShoppingListCustomersPrice(customersPrice.getText().toString());
                                                            if (customersPrice.getText().toString().equals(currentOrder.getShoppingListSuppliersPrice())) {
                                                                currentOrder.setOrderStatus(getString(R.string.successful_order));
                                                                childSnapshot.getRef().removeValue();
                                                                SupCurrentOrderInactiveView();
                                                                FirebaseDatabase.getInstance().getReference().child(getString(R.string.finished_orders)).child(getString(R.string.successful_orders)).push().setValue(currentOrder);

                                                            } else {
                                                                currentOrder.setOrderStatus(getString(R.string.finished_orders_but_different_prices_inserted));
                                                                childSnapshot.getRef().removeValue();
                                                                SupCurrentOrderInactiveView();
                                                                FirebaseDatabase.getInstance().getReference().child(getString(R.string.finished_orders)).child(getString(R.string.different_prices_inserted)).push().setValue(currentOrder);
                                                            }

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

                        } else {
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                currentOrder = childSnapshot.getValue(ShoppingOrderDetails.class);

                                updateUI();

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        SupCurrentOrderInactiveView();
                    }
                });


    }

    private void updateUI() {

        SupCurrentOrderActiveView();

        StringBuilder supermarketNameBuilder = new StringBuilder().append("From: ").append(currentOrder.getSupermarketName());
        fromSupermarket.setText(supermarketNameBuilder);
        fromSupermarket.setTypeface(Typeface.DEFAULT_BOLD);

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
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
            View vi = LayoutInflater.from(getApplicationContext()).inflate(R.layout.cus_cart_fragment_single_shopping_list_item, null);
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
            DocumentReference docRef = db.collection("users").document(currentOrder.getSupplierUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String supplierPhoneNumber = document.get("PhoneNumber").toString();

                    try {
                        if (Build.VERSION.SDK_INT > 22) {
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                                ActivityCompat.requestPermissions(CusCurrentOrderActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 101);

                                return;
                            }

                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + supplierPhoneNumber));
                            startActivity(callIntent);

                        } else {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + supplierPhoneNumber));
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
    }


    public void SupCurrentOrderActiveView() {
        linearLayout.setVisibility(View.VISIBLE);
        cusCurrentOrderEmptyView.setVisibility(View.GONE);
        extendedFloatingActionButton.setVisibility(View.VISIBLE);

    }

    public void SupCurrentOrderInactiveView() {
        linearLayout.setVisibility(View.GONE);
        cusCurrentOrderEmptyView.setVisibility(View.VISIBLE);
        extendedFloatingActionButton.setVisibility(View.GONE);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}