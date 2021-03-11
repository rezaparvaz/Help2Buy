package com.einkaufsheld.help2buy.SupplierApp.Home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.einkaufsheld.help2buy.CustomerApp.ItemDetails;
import com.einkaufsheld.help2buy.R;
import com.einkaufsheld.help2buy.ShoppingOrderDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SupHomeViewAdapter extends RecyclerView.Adapter<SupHomeViewAdapter.OrderViewHolder> {
    private ArrayList<ShoppingOrderDetails> dataSet;
    private SupHomeFragment mSupHomeFragment;
    private ProgressDialog progress;
    private Geocoder geocoder;


    public SupHomeViewAdapter(ArrayList<ShoppingOrderDetails> dataset, SupHomeFragment supHomeFragment) {
        this.dataSet = dataset;
        this.mSupHomeFragment = supHomeFragment;
    }


    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sup_single_home_item, parent, false);
        return new OrderViewHolder(view);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NotNull final OrderViewHolder holder, final int listPosition) {

        ShoppingOrderDetails object = dataSet.get(listPosition);
        if (object != null) {

            StringBuilder supermarketNameBuilder = new StringBuilder().append("From: ").append(object.getSupermarketName());
            holder.supermarketName.setText(supermarketNameBuilder);

            geocoder = new Geocoder(mSupHomeFragment.getContext(), Locale.getDefault());
            try {
                if (!(object.getSupermarketLat() == 0.0 && object.getSupermarketLng() == 0.0)) {
                    List<Address> supermarketAddressList = geocoder.getFromLocation(object.getSupermarketLat(), object.getSupermarketLng(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    StringBuilder supermarketAddressBuilder = new StringBuilder().append("Address: ").append(supermarketAddressList.get(0).getAddressLine(0));
                    holder.supermarketAddress.setText(supermarketAddressBuilder);
                } else {
                    holder.supermarketAddress.setText(R.string.address_not_specified);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            StringBuilder numberOfShoppingListItemsString = new StringBuilder().append(object.getShoppingList().size()).append(" Items");
            holder.numberOfShoppingListItems.setText(numberOfShoppingListItemsString);

            try {
                List<Address> customerAddress = geocoder.getFromLocation(object.getCustomerLat(), object.getCustomerLng(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                StringBuilder customerAddressBuilder = new StringBuilder().append("To: ").append(customerAddress.get(0).getAddressLine(0));
                holder.customerAddress.setText(customerAddressBuilder);
            } catch (IOException e) {
                e.printStackTrace();
            }


            holder.arrowButton.setOnClickListener(v -> {
                if (holder.expandableView.getVisibility() == View.GONE) {
                    TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                    holder.expandableView.setVisibility(View.VISIBLE);
                    holder.arrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                } else {
                    TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                    holder.expandableView.setVisibility(View.GONE);
                    holder.arrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }
            });

            holder.shoppingList.removeAllViews();
            for (int i = 0; i < object.getShoppingList().size(); i++) {
                ItemDetails itemDetails = object.getShoppingList().get(i);
                View vi = LayoutInflater.from(mSupHomeFragment.getContext()).inflate(R.layout.cus_cart_fragment_single_shopping_list_item, null);
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
                holder.shoppingList.addView(vi);
            }

            holder.acceptOrderButton.setOnClickListener(v -> {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mSupHomeFragment.getContext());
                alertDialog.setMessage("Are you sure that you want to accept this order?");


                alertDialog.setPositiveButton("Yes",
                        (dialog, which) -> {
                            ShoppingOrderDetails shoppingOrderDetails = dataSet.get(listPosition);
                            FirebaseDatabase.getInstance().getReference().child("Pending Orders")
                                    .orderByChild("orderID")
                                    .equalTo(dataSet.get(listPosition).getOrderID())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                childSnapshot.getRef().removeValue();
                                                try {
                                                    FirebaseFunctions.getInstance().getHttpsCallable("getTime")
                                                            .call().addOnSuccessListener(httpsCallableResult -> {
                                                                long timestamp = (long) httpsCallableResult.getData();
                                                                shoppingOrderDetails.setSupplierUid(user.getUid());
                                                                shoppingOrderDetails.setOrderStatus(mSupHomeFragment.getString(R.string.ongoing_order));
                                                                shoppingOrderDetails.setOrderAcceptTime(Long.toString(timestamp));
                                                                FirebaseDatabase.getInstance().getReference().child(mSupHomeFragment.getString(R.string.ongoing_orders)).push().setValue(shoppingOrderDetails);


                                                            });


                                                } catch (Exception ex) {
                                                    Log.d("TAG", "onAcceptingOrder: "+ ex);
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

            });
            if (!(object.getSupermarketLat() == 0.0 && object.getSupermarketLng() == 0.0)) {

                holder.direction.setOnClickListener(view -> {

                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + dataSet.get(listPosition).getCustomerLat() + "," +
                                    dataSet.get(listPosition).getCustomerLng() + "&waypoints=" + dataSet.get(listPosition).getSupermarketLat() + "," +
                                    dataSet.get(listPosition).getSupermarketLng() + "&mode=bicycling"));
                    mSupHomeFragment.startActivity(intent);

                });
            } else {
                holder.direction.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        CardView cardView;
        TextView supermarketName;
        LinearLayout shoppingList;
        TextView supermarketAddress;
        TextView numberOfShoppingListItems;
        TextView customerAddress;
        ConstraintLayout expandableView;
        Button arrowButton;
        Button acceptOrderButton;
        Button direction;

        OrderViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.supermarketName = itemView.findViewById(R.id.sup_single_home_item_from_supermarket);
            this.supermarketAddress = itemView.findViewById(R.id.sup_single_home_item_from_address);
            this.numberOfShoppingListItems = itemView.findViewById(R.id.sup_single_home_item_number_of_items);
            this.customerAddress = itemView.findViewById(R.id.sup_single_home_item_to_address);
            this.expandableView = itemView.findViewById(R.id.sup_single_home_item_expandable_view);
            this.arrowButton = itemView.findViewById(R.id.sup_single_home_item_arrow_button);
            this.acceptOrderButton = itemView.findViewById(R.id.sup_single_home_item_accept_order_button);
            this.cardView = itemView.findViewById(R.id.sup_single_home_item_card_view);
            this.shoppingList = itemView.findViewById(R.id.sup_single_home_list_view_container);
            this.direction = itemView.findViewById(R.id.sup_single_home_item_directions_button);
        }

    }

//
//    public class Order extends AsyncTask<String, String, String>
//    {
//
//
//        String z="";
//        boolean isSuccess=false;
//
//        @Override
//        protected void onPreExecute() {
//            progress.setMessage("Loading...");
//            progress.show();
//
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//
//
//            try {
//
//                mCurrentOrdersDatabaseReference.push().setValue(new CartViewModel(object.getSupermarketName(),object.getShoppingList()));
//
////                connectionClass = new ConnectionClass();
////                Connection con = connectionClass.CONN();
////
////
////
////                if (con == null) {
////                        z = "Please check your internet connection";
////                    } else {
////
////                        String query="insert into demoregister values('"+listToText+"','"+nameOfSupermarket+"','"+"cccccc"+"')";
////
////                        Statement stmt = con.createStatement();
////                        stmt.executeUpdate(query);
////
////                        z = "Register successfull";
////                        isSuccess=true;
////
////
////                    }
//            }
//            catch (Exception ex)
//            {
//                isSuccess = false;
//                z = "Exceptions"+ex;
//            }
//
//            return z;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//
////            Toast.makeText(getApplicationContext,""+z,Toast.LENGTH_LONG).show();
//
//
//            if(isSuccess) {
////                startActivity(new Intent(MainActivity.this,Main2Activity.class));
//
//
//            }
//
//
//            progress.hide();
//        }
//    }


}
