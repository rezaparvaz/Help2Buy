package com.einkaufsheld.help2buy.SupplierApp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.einkaufsheld.help2buy.R;
import com.einkaufsheld.help2buy.SupplierApp.Account.SupAccountFragment;
import com.einkaufsheld.help2buy.SupplierApp.CurrentOrder.SupCurrentOrderFragment;
import com.einkaufsheld.help2buy.SupplierApp.Home.SupHomeFragment;
import com.einkaufsheld.help2buy.SupplierApp.Map.SupMapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

public class SupplierMainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sup_activity_main);


        if (savedInstanceState == null) {
            SupFragmentChoose(new SupHomeFragment(), getString(R.string.supplier_home));
        }


        BottomNavigationView bottomNavigationView = findViewById(R.id.sup_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.sup_home_menu:
                    SupFragmentChoose(new SupHomeFragment(), getString(R.string.supplier_home));
                    break;
                case R.id.sup_map_menu:
                    SupFragmentChoose(new SupMapFragment(), getString(R.string.supplier_map));
                    break;
                case R.id.sup_current_order:
                    SupFragmentChoose(new SupCurrentOrderFragment(), getString(R.string.supplier_current_order));
                    break;
                case R.id.sup_account_menu:
                    SupFragmentChoose(new SupAccountFragment(), getString(R.string.supplier_account));
                    break;
            }
            return true;
        });
    }


    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    public void SupFragmentChoose(Fragment fragmentName, String Tag) {

        getSupportFragmentManager().beginTransaction().replace(R.id.sup_nav_host_fragment, fragmentName, Tag).addToBackStack(null)
                .commit();
    }

}
