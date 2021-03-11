package com.einkaufsheld.help2buy.CustomerApp.cart;

import android.content.Intent;
import android.graphics.Bitmap;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.einkaufsheld.help2buy.CustomerApp.ItemDetails;
import com.einkaufsheld.help2buy.CustomerApp.ShoppingListActivity;
import com.einkaufsheld.help2buy.CustomerApp.home.HomeStoresFragment;
import com.einkaufsheld.help2buy.R;
import com.einkaufsheld.help2buy.ShoppingOrderDetails;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.START_EDIT_CART_ITEM;


public class CartViewAdapter extends RecyclerView.Adapter<CartViewAdapter.ShoppingCartViewHolder> {
    private CartFragment mCartFragment;
    private ArrayList<ShoppingOrderDetails> mDataSet;


    public CartViewAdapter(ArrayList<ShoppingOrderDetails> dataSet, CartFragment cartFragment) {
        this.mDataSet = dataSet;
        this.mCartFragment = cartFragment;
    }


    @NotNull
    @Override
    public ShoppingCartViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cus_single_cart_item, parent, false);
        return new ShoppingCartViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NotNull final ShoppingCartViewHolder holder, final int listPosition) {

        ShoppingOrderDetails object = mDataSet.get(listPosition);
        holder.relativeLayoutStatus.bringToFront();

        holder.supermarketName.setText(object.getSupermarketName());


        holder.supermarketAddress.setText(object.getSupermarketAddress());

        // Define a Place ID.
        final String placeId = object.getPlaceID();

// Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
        final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

// Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);
        PlacesClient placesClient = Places.createClient(mCartFragment.requireContext());
        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            final Place place = response.getPlace();

            // Get the photo metadata.
            final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                Log.w("TAG", "No photo metadata.");
                return;
            }
            final PhotoMetadata photoMetadata = metadata.get(0);

            // Get the attribution text.
            final String attributions = photoMetadata.getAttributions();

            // Create a FetchPhotoRequest.
            final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500) // Optional.
                    .setMaxHeight(300) // Optional.
                    .build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                holder.supermarketPhoto.setImageBitmap(bitmap);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    final ApiException apiException = (ApiException) exception;
                    Log.e("TAG", "Place not found: " + exception.getMessage());
                    final int statusCode = apiException.getStatusCode();
                }
            });
        }).addOnFailureListener((exception) -> {
            holder.supermarketPhoto.setImageResource(R.drawable.supermarket_photo);
        });


        holder.removeButton.setOnClickListener(v -> {

            FirebaseDatabase.getInstance().getReference().child("Undone Orders")
                    .orderByChild("orderID")
                    .equalTo(object.getOrderID())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                childSnapshot.getRef().removeValue();
                                mDataSet.remove(listPosition);
                                notifyDataSetChanged();
                                mCartFragment.CartItemViewsCheck();
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        });

        holder.editButton.setOnClickListener(v -> {
            Intent shoppingListIntent = new Intent(mCartFragment.getActivity(), ShoppingListActivity.class);
            shoppingListIntent.putExtra("Shopping Details", object);
            shoppingListIntent.putExtra("requestCode", START_EDIT_CART_ITEM);
            shoppingListIntent.putExtra("orderID", object.getOrderID());
            mCartFragment.startActivityForResult(shoppingListIntent, START_EDIT_CART_ITEM);


        });

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
            View vi = LayoutInflater.from(mCartFragment.getContext()).inflate(R.layout.cus_cart_fragment_single_shopping_list_item, null);
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

        holder.itemView.setOnLongClickListener(view -> {

            Toast toast = Toast.makeText(mCartFragment.getContext(), object.getSupermarketName(), Toast.LENGTH_SHORT);
            toast.show();
            return false;
        });


        if (object.getOrderStatus().equals(mCartFragment.getString(R.string.undone_order))) {

            holder.relativeLayoutStatus.setVisibility(View.GONE);

            holder.orderButton.setText(R.string.place_order);

            holder.orderButton.setOnClickListener(v -> {

                String prevOrderID = object.getOrderID();
                FirebaseDatabase.getInstance().getReference().child("Undone Orders")
                        .orderByChild("orderID")
                        .equalTo(prevOrderID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    FragmentManager fragmentManager = mCartFragment.getFragmentManager();
                                    HomeStoresFragment homeStoresFragment = (HomeStoresFragment) fragmentManager.findFragmentByTag(mCartFragment.getResources().getString(R.string.stores));
                                    childSnapshot.getRef().removeValue();

                                    try {
                                        FirebaseFunctions.getInstance().getHttpsCallable("getTime")
                                                .call().addOnSuccessListener(httpsCallableResult -> {
                                            long timestamp = (long) httpsCallableResult.getData();
                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                            object.setCustomerUid(user.getUid());
                                            object.setCustomerLat(homeStoresFragment.getLocation().getLatitude());
                                            object.setCustomerLng(homeStoresFragment.getLocation().getLongitude());
                                            object.setOrderStatus(mCartFragment.getString(R.string.pending_order));
                                            object.setOrderPlaceTime(Long.toString(timestamp));
                                            FirebaseDatabase.getInstance().getReference().child(mCartFragment.getString(R.string.pending_orders)).push().setValue(object);

                                        });


                                    } catch (Exception ex) {
                                        Log.d("TAG", "onDataChange: Exception " + ex);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


            });
        } else if (object.getOrderStatus().equals(mCartFragment.getString(R.string.pending_order))) {

            holder.notification.setVisibility(View.GONE);
            holder.detailsButton.setVisibility(View.GONE);
            holder.orderButton.setText(R.string.cancel_order);
            holder.relativeLayoutStatus.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.VISIBLE);

            holder.orderButton.setOnClickListener(v -> {

                String prevOrderID = object.getOrderID();
                FirebaseDatabase.getInstance().getReference().child(mCartFragment.getString(R.string.pending_orders))
                        .orderByChild("orderID")
                        .equalTo(prevOrderID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    childSnapshot.getRef().removeValue();
                                    object.setOrderStatus(mCartFragment.getString(R.string.undone_order));
                                    FirebaseDatabase.getInstance().getReference().child(mCartFragment.getString(R.string.undone_orders)).push().setValue(object);


                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


            });
        } else if (object.getOrderStatus().equals(mCartFragment.getString(R.string.ongoing_order))) {
            holder.orderButton.setText(R.string.cancel_order);
            holder.relativeLayoutStatus.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
            holder.linearLayout.setVisibility(View.VISIBLE);
            holder.acceptedOrderText.setText(mCartFragment.getString(R.string.your_order_is_being_processed));
            holder.detailsButton.setOnClickListener(view -> {
                Intent intent = new Intent(mCartFragment.getContext(), CusCurrentOrderActivity.class);
                intent.putExtra("OrderId", object.getOrderID());
                mCartFragment.startActivity(intent);
            });
            holder.orderButton.setOnClickListener(v -> {

                String prevOrderID = object.getOrderID();
                FirebaseDatabase.getInstance().getReference().child(mCartFragment.getString(R.string.ongoing_orders))
                        .orderByChild("orderID")
                        .equalTo(prevOrderID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    childSnapshot.getRef().removeValue();
                                    mDataSet.remove(listPosition);
                                    notifyDataSetChanged();
                                    mCartFragment.CartItemViewsCheck();
                                    try {
                                        FirebaseFunctions.getInstance().getHttpsCallable("getTime")
                                                .call().addOnSuccessListener(httpsCallableResult -> {
                                            long timestamp = (long) httpsCallableResult.getData();
                                            object.setOrderStatus("Customer Cancelled The Order");
                                            object.setOrderEndTime(Long.toString(timestamp));
                                            FirebaseDatabase.getInstance().getReference().child("Finished Orders").child("Cancelled Orders").push().setValue(object);
                                        });


                                    } catch (Exception ex) {
                                        Log.d("TAG", "onDataChange: Exception " + ex);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


            });
        } else if (object.getOrderStatus().equals(mCartFragment.getString(R.string.pending_payment))) {


            holder.relativeLayoutStatus.setVisibility(View.VISIBLE);
            holder.linearLayout.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
            holder.acceptedOrderText.setText(mCartFragment.getString(R.string.your_order_has_been_delivered));
            holder.notification.setVisibility(View.VISIBLE);
            holder.detailsButton.setVisibility(View.VISIBLE);
            holder.detailsButton.setOnClickListener(view -> {
                Intent intent = new Intent(mCartFragment.getContext(), CusCurrentOrderActivity.class);
                intent.putExtra("OrderId", object.getOrderID());
                mCartFragment.startActivity(intent);
            });

        }


    }


    @Override
    public int getItemCount() {
        return mDataSet.size();
    }


    public static class ShoppingCartViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView supermarketName;
        TextView supermarketAddress;
        LinearLayout shoppingList;
        ImageView supermarketPhoto;
        ConstraintLayout expandableView;
        Button arrowButton;
        CardView cardView;
        Button orderButton;
        ImageView removeButton;
        ImageView editButton;
        RelativeLayout relativeLayoutStatus;
        ProgressBar progressBar;
        LinearLayout linearLayout;
        Button detailsButton;
        TextView acceptedOrderText;
        ImageView notification;
        ShoppingCartViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.supermarketName = itemView.findViewById(R.id.cart_fragment_supermarket_name);
            this.supermarketAddress = itemView.findViewById(R.id.cart_fragment_supermarket_address);
            this.supermarketPhoto = itemView.findViewById(R.id.cart_fragment_supermarket_photo);
            this.shoppingList = itemView.findViewById(R.id.cart_fragment_list_view_container);
            this.expandableView = itemView.findViewById(R.id.cart_fragment_expandable_view);
            this.arrowButton = itemView.findViewById(R.id.cart_fragment_arrow_button);
            this.cardView = itemView.findViewById(R.id.cart_fragment_card_view);
            this.orderButton = itemView.findViewById(R.id.cart_fragment_order_button);
            this.removeButton = itemView.findViewById(R.id.cart_fragment_item_remove_button);
            this.editButton = itemView.findViewById(R.id.cart_fragment_item_edit_button);
            this.relativeLayoutStatus = itemView.findViewById(R.id.cus_single_cart_item_status);
            this.progressBar = itemView.findViewById(R.id.cus_single_cart_item_progress_bar);
            this.linearLayout = itemView.findViewById(R.id.cus_single_cart_item_accepted_order_view);
            this.detailsButton = itemView.findViewById(R.id.cus_single_cart_item_details_button);
            this.acceptedOrderText = itemView.findViewById(R.id.cus_single_cart_item_accepted_order_text_view);
            this.notification = itemView.findViewById(R.id.cus_single_cart_item_notification);


        }
    }


}
