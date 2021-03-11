package com.einkaufsheld.help2buy.StoreOwnerApp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.einkaufsheld.help2buy.R;
import com.einkaufsheld.help2buy.StoreOwnerApp.Account.OwnAccountFragment;
import com.einkaufsheld.help2buy.StoreOwnerApp.Home.OwnHomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

public class OwnerMainActivity extends AppCompatActivity {


    public static int SUPERMARKET_PHOTO_GALLERY = 22141;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.own_activity_main);


        if (savedInstanceState == null) {
            OwnFragmentChoose(new OwnHomeFragment(), getString(R.string.owner_home));
        }


        BottomNavigationView bottomNavigationView = findViewById(R.id.own_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.own_home_menu:
                    OwnFragmentChoose(new OwnHomeFragment(), getString(R.string.owner_home));
                    break;
                case R.id.own_account_menu:
                    OwnFragmentChoose(new OwnAccountFragment(), getString(R.string.owner_account));
                    break;
            }
            return true;
        });
    }


    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    public void OwnFragmentChoose(Fragment fragmentName, String Tag) {

        getSupportFragmentManager().beginTransaction().replace(R.id.own_nav_host_fragment, fragmentName, Tag).addToBackStack(null)
                .commit();
    }

}
