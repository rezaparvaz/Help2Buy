package com.einkaufsheld.help2buy.CustomerApp.favourites;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.einkaufsheld.help2buy.CustomerApp.AddNewOrEditItemActivity;
import com.einkaufsheld.help2buy.CustomerApp.ShoppingListViewAdapter;
import com.einkaufsheld.help2buy.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.RETURN_ADDED_FAVOURITE_ITEM;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.RETURN_EDITED_FAVOURITE_ITEM;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.START_ADD_FAVOURITE_ITEM;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.START_EDIT_FAVOURITE_ITEM;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.favouriteList;


public class FavouritesFragment extends Fragment implements ShoppingListViewAdapter.EventListener {

    private LinearLayout favouritesEmptyView;
    private FavouritesViewAdapter favouritesViewAdapter;
    private RecyclerView favouritesRecyclerView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.cus_fragment_favourites, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        favouritesEmptyView = root.findViewById(R.id.cus_favourites_fragment_empty_list);
        favouritesViewAdapter = new FavouritesViewAdapter(favouriteList, this, this::FavouritesItemViewsCheck);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        favouritesRecyclerView = root.findViewById(R.id.cus_favourites_recyclerview);
        favouritesRecyclerView.setLayoutManager(linearLayoutManager);
        favouritesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        favouritesRecyclerView.setAdapter(favouritesViewAdapter);
        FavouritesItemViewsCheck();

        ExtendedFloatingActionButton fabAddANewItem = root.findViewById(R.id.cus_favourites_fab);
        fabAddANewItem.setOnClickListener(view -> {

            Intent addNewItemIntent = new Intent(this.getContext(), AddNewOrEditItemActivity.class);
            addNewItemIntent.putExtra("requestCode", START_ADD_FAVOURITE_ITEM);
            startActivityForResult(addNewItemIntent, START_ADD_FAVOURITE_ITEM);

        });
        return root;


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        Bundle bundle = data != null ? data.getExtras() : null;
        if (bundle != null) {
            if (requestCode == START_ADD_FAVOURITE_ITEM && resultCode == RETURN_ADDED_FAVOURITE_ITEM) {
                favouriteList.add(bundle.getParcelable("Item Details"));
                favouritesViewAdapter.notifyDataSetChanged();
                FavouritesItemViewsCheck();

            } else if (requestCode == START_EDIT_FAVOURITE_ITEM && resultCode == RETURN_EDITED_FAVOURITE_ITEM) {
                favouriteList.set(bundle.getInt("listPosition",0), bundle.getParcelable("Item Details"));
                favouritesViewAdapter.notifyDataSetChanged();
                FavouritesItemViewsCheck();

            }
        }


    }
    public void FavouritesItemViewsCheck() {
        if (favouriteList.isEmpty()) {
            favouritesRecyclerView.setVisibility(View.GONE);
            favouritesEmptyView.setVisibility(View.VISIBLE);
        } else {
            favouritesRecyclerView.setVisibility(View.VISIBLE);
            favouritesEmptyView.setVisibility(View.GONE);
        }
    }


}
