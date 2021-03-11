package com.einkaufsheld.help2buy.CustomerApp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.einkaufsheld.help2buy.CustomerApp.account.AccountFragment;
import com.einkaufsheld.help2buy.CustomerApp.cart.CartFragment;
import com.einkaufsheld.help2buy.CustomerApp.favourites.FavouritesFragment;
import com.einkaufsheld.help2buy.CustomerApp.home.HomeOffersFragment;
import com.einkaufsheld.help2buy.CustomerApp.home.HomeStoresFragment;
import com.einkaufsheld.help2buy.CustomerApp.map.MapFragment;
import com.einkaufsheld.help2buy.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class CustomerMainActivity extends AppCompatActivity {

    public View buttonContainer;
    public static int START_EDIT_CART_ITEM = 320;
    public static int START_ADD_SUPERMARKET = 321;
    public static int RETURN_EDITED_CART_ITEM = 420;
    public static int RETURN_ADDED_CART_ITEM = 421;
    public static int CUS_START_MAP = 32;
    public static int CUS_RETURN_FROM_MAP = 32;
    public static int START_ADD_FAVOURITE_ITEM = 303;
    public static int START_EDIT_FAVOURITE_ITEM = 304;
    public static int RETURN_ADDED_FAVOURITE_ITEM = 313;
    public static int RETURN_EDITED_FAVOURITE_ITEM = 314;
    public static int START_SELECT_FROM_FAVOURITES = 85;
    public static int RETURN_SELECT_FROM_FAVOURITES = 86;

    public static ArrayList<ItemDetails> favouriteList = new ArrayList<>();
    public HomeStoresFragment homeStoresFragment = new HomeStoresFragment();
    private ArrayList<String> fragmentTags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cus_activity_main);

        Toolbar toolbar = findViewById(R.id.cus_main_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (fragmentTags.isEmpty()){
            fragmentTags.add("STORES");
            fragmentTags.add("MAP");
            fragmentTags.add("FAVOURITES");
            fragmentTags.add("CART");
            fragmentTags.add("ACCOUNT");
        }
        buttonContainer = findViewById(R.id.cus_main_activity_button_container);
        Button storesButton = findViewById(R.id.cus_main_activity_stores_button);
        Button offersButton = findViewById(R.id.cus_main_activity_items_button);
        storesButton.setOnClickListener(v -> {
            if (!storesButton.isSelected()) {
                FragmentChoose(new HomeStoresFragment(), getString(R.string.stores));
                storesButton.setSelected(true);
                offersButton.setSelected(false);
            }
        });
        offersButton.setOnClickListener(v -> {
            if (!offersButton.isSelected()) {
                FragmentChoose(new HomeOffersFragment(), getString(R.string.offers));
                storesButton.setSelected(false);
                offersButton.setSelected(true);
            }
        });

        if (savedInstanceState == null) {
            FragmentChoose(new HomeStoresFragment(), getString(R.string.stores));
            storesButton.setSelected(true);
            offersButton.setSelected(false);
        }else {
//            homeStoresFragment = (HomeStoresFragment) getSupportFragmentManager().getFragment(savedInstanceState, "savedHomeStoresFragment");

        }

//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        toggle.syncState();
//
//        NavigationView navigationView = findViewById(R.id.nav_view_drawer);
//        navigationView.setNavigationItemSelectedListener(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.cus_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home_menu:
                    FragmentChoose(new HomeStoresFragment(),  getString(R.string.stores));
                    break;
                case R.id.map_menu:
                    FragmentChoose(new MapFragment(), "MAP");
                    break;
                case R.id.favorites_menu:
                    FragmentChoose(new FavouritesFragment(), "FAVOURITES");
                    break;
                case R.id.shopping_cart_menu:
                    FragmentChoose(new CartFragment(), "CART");
                    break;
                case R.id.account_menu:
                    FragmentChoose(new AccountFragment(), "ACCOUNT");
                    break;
            }
            return true;
        });
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        homeStoresFragment = (HomeStoresFragment) getSupportFragmentManager().getFragment(savedInstanceState, getString(R.string.stores));
        if (homeStoresFragment != null){
            Log.d("TAG", "onRestoreInstanceState: haaaaaaaaaaaaaaaaaaaaaaaaaaaast");
        } else {
            Log.d("TAG", "onRestoreInstanceState: niiiiiiiiiiiiiiiiiiiiiiiiiiist");

        }
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        homeStoresFragment = new HomeStoresFragment();
        if (homeStoresFragment.isAdded()){
            getSupportFragmentManager().putFragment(outState,  getString(R.string.stores), homeStoresFragment);

        }

    }

    public void FragmentChoose(Fragment fragmentName, String Tag) {
        if (Arrays.asList(getString(R.string.stores), getString(R.string.offers)).contains(Tag)){
            buttonContainer.setVisibility(View.VISIBLE);
        } else {
            buttonContainer.setVisibility(View.GONE);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.cus_nav_host_fragment, fragmentName, Tag).addToBackStack(null)
                .commit();
    }

}
