package com.einkaufsheld.help2buy.CustomerApp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.einkaufsheld.help2buy.R;
import com.einkaufsheld.help2buy.ShoppingOrderDetails;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.appbar.CollapsingToolbarLayout;
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

import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.RETURN_EDITED_CART_ITEM;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.RETURN_SELECT_FROM_FAVOURITES;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.START_ADD_SUPERMARKET;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.START_EDIT_CART_ITEM;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.START_SELECT_FROM_FAVOURITES;


public class ShoppingListActivity extends AppCompatActivity {

    final static int START_EDIT_ITEM_IN_SHOPPING_LIST_ACTIVITY = 0x04;
    final static int START_ADD_ITEM_IN_SHOPPING_LIST_ACTIVITY = 0x07;
    final static int RETURN_FROM_ADD_OR_EDIT_ITEM_IN_SHOPPING_LIST_ACTIVITY = 0x08;
    public ShoppingListViewAdapter adapter;
    public ArrayList<ItemDetails> itemList = new ArrayList<>();
    public Context context;
    public String supermarketName;
    public String supermarketAddress;
    public ShoppingOrderDetails shoppingOrderDetails;
    public int requestCode;
    public Intent cartToShoppingListIntent;
    private TextView emptyView;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        setContentView(R.layout.cus_activity_shopping_list);
        Toolbar toolbar = findViewById(R.id.shopping_list_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ImageView imageView = findViewById(R.id.shopping_list_image);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.shopping_list_collapsing_toolbar);
        TextView address = findViewById(R.id.supermarket_address_shopping_list);
        TextView distance = findViewById(R.id.supermarket_distance_shopping_list);
        TextView openingHours = findViewById(R.id.supermarket_opening_hours_shopping_list);
        RatingBar ratingBar = findViewById(R.id.ratingBar_shopping_list);
        emptyView = findViewById(R.id.shopping_list_empty_list_view);
        recyclerView = findViewById(R.id.shopping_list_recycler_view);

        cartToShoppingListIntent = getIntent();

        requestCode = cartToShoppingListIntent.getIntExtra("requestCode", 0);
        shoppingOrderDetails = cartToShoppingListIntent.getParcelableExtra("Shopping Details");

        supermarketName = shoppingOrderDetails.getSupermarketName();
        supermarketAddress = shoppingOrderDetails.getSupermarketAddress();

        final String placeId = shoppingOrderDetails.getPlaceID();

// Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
        final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

// Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);
        PlacesClient placesClient = Places.createClient(this);
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
                imageView.setImageBitmap(bitmap);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    final ApiException apiException = (ApiException) exception;
                    Log.e("TAG", "Place not found: " + exception.getMessage());
                    final int statusCode = apiException.getStatusCode();

                }
            });
        }).addOnFailureListener((exception) -> {
            imageView.setImageResource(R.drawable.supermarket_photo);

        });


        collapsingToolbarLayout.setTitle(supermarketName);

        address.setText(shoppingOrderDetails.getSupermarketAddress());
        distance.setText(shoppingOrderDetails.getSupermarketDistance());
        String openNow = shoppingOrderDetails.getSupermarketOpenNow();
        if (openNow.equals("Open")) {
            openingHours.setText(R.string.open_now);
            openingHours.setTextColor(Color.GREEN);
        } else if (openNow.equals("Closed")) {
            openingHours.setText(R.string.closed);
            openingHours.setTextColor(Color.RED);
        } else {
            openingHours.setText(R.string.unavailableOpeningHours);
            openingHours.setTextColor(Color.DKGRAY);
        }
        ratingBar.setRating(shoppingOrderDetails.getSupermarketRating().floatValue());
        itemList = shoppingOrderDetails.getShoppingList();

        adapter = new ShoppingListViewAdapter(itemList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        ItemViewsCheck();

        FloatingActionButton fabAddANewItem = findViewById(R.id.fab_add_a_new_item);
        fabAddANewItem.setOnClickListener(view -> {

            Intent addNewItemIntent = new Intent(this, AddNewOrEditItemActivity.class);
            addNewItemIntent.putExtra("requestCode", START_ADD_ITEM_IN_SHOPPING_LIST_ACTIVITY);
            startActivityForResult(addNewItemIntent, START_ADD_ITEM_IN_SHOPPING_LIST_ACTIVITY);

        });

        FloatingActionButton selectFromFavouritesFab = findViewById(R.id.fab_select_from_favourites);
        selectFromFavouritesFab.setOnClickListener(view -> {

            Intent selectFromFavouritesIntent = new Intent(this, CusSelectFromFavouritesListActivity.class);
            selectFromFavouritesIntent.putExtra("requestCode", START_SELECT_FROM_FAVOURITES);
            startActivityForResult(selectFromFavouritesIntent, START_SELECT_FROM_FAVOURITES);

        });
    }

    public void ItemViewsCheck() {
        if (itemList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shopping_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == R.id.menu_shopping_list) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (requestCode == START_ADD_SUPERMARKET) {
                try {
                    FirebaseFunctions.getInstance().getHttpsCallable("getTime")
                            .call().addOnSuccessListener(httpsCallableResult -> {
                        long timestamp = (long) httpsCallableResult.getData();
                        StringBuilder orderID = new StringBuilder().append(timestamp).append(user.getUid());
                        shoppingOrderDetails.setInCartTime(Long.toString(timestamp));
                        shoppingOrderDetails.setOrderID(orderID.toString());
                        shoppingOrderDetails.setShoppingList(itemList);
                        shoppingOrderDetails.setOrderStatus(getString(R.string.undone_order));
                        FirebaseDatabase.getInstance().getReference().child(getString(R.string.undone_orders)).push().setValue(shoppingOrderDetails);

                    });

                } catch (Exception ex) {
                    Log.d("TAG", "onOptionsItemSelected: Exceptions" + ex);
                }
            } else if (requestCode == START_EDIT_CART_ITEM) {
                String prevOrderID = cartToShoppingListIntent.getStringExtra("orderID");

                FirebaseDatabase.getInstance().getReference().child(getString(R.string.undone_orders))
                        .orderByChild("orderID")
                        .equalTo(prevOrderID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    childSnapshot.getRef().removeValue();
                                    shoppingOrderDetails.setOrderID(prevOrderID);
                                    shoppingOrderDetails.setShoppingList(itemList);
                                    FirebaseDatabase.getInstance().getReference().child(getString(R.string.undone_orders)).push().setValue(shoppingOrderDetails);
                                    setResult(RETURN_EDITED_CART_ITEM, intent);
                                    finish();

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("TAG", "33333333333333333333333333: ");

                            }
                        });


            }


//                customerOrderDetailsArrayList.add(new ShoppingOrderDetails(shoppingOrderDetails.getSupermarketLat(), shoppingOrderDetails.getSupermarketLng(),
//                        shoppingOrderDetails.getSupermarketName(), shoppingOrderDetails.getSupermarketRating(), shoppingOrderDetails.getSupermarketOpenNow(),
//                        shoppingOrderDetails.getSupermarketDistance(), shoppingOrderDetails.getSupermarketAddress(), shoppingOrderDetails.getSupermarketImage(),
//                        itemList));

            finish();

        }
        return super.

                onOptionsItemSelected(item);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FloatingActionMenu fabContainer = findViewById(R.id.shopping_list_fab_container);
        if (resultCode == RETURN_FROM_ADD_OR_EDIT_ITEM_IN_SHOPPING_LIST_ACTIVITY) {
            fabContainer.close(true);
            Bundle bundle = data != null ? data.getExtras() : null;
            if (bundle != null) {
                ItemDetails itemDetails = bundle.getParcelable("Item Details");

                if (requestCode == START_ADD_ITEM_IN_SHOPPING_LIST_ACTIVITY) {
                    itemList.add(itemDetails);
                    adapter.notifyDataSetChanged();
                    ItemViewsCheck();
                } else if (requestCode == START_EDIT_ITEM_IN_SHOPPING_LIST_ACTIVITY) {
                    int listPosition = bundle.getInt("listPosition");
                    itemList.set(listPosition, itemDetails);
                    adapter.notifyItemChanged(listPosition);
                }
            }

        } else if (requestCode == START_SELECT_FROM_FAVOURITES && resultCode == RETURN_SELECT_FROM_FAVOURITES) {
            Bundle bundle = data != null ? data.getExtras() : null;
            fabContainer.close(true);
            if (bundle != null) {
                for (int index = 0; index < bundle.getInt("Number of Items"); index++) {
                    itemList.add(bundle.getParcelable(String.valueOf(index)));
                    adapter.notifyDataSetChanged();
                    ItemViewsCheck();
                }
            }
        }
    }
}

