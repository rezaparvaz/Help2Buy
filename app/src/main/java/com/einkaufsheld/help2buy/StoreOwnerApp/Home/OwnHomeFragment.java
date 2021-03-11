package com.einkaufsheld.help2buy.StoreOwnerApp.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.einkaufsheld.help2buy.R;
import com.einkaufsheld.help2buy.StoreOwnerApp.OwnSupermarketDetailsActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class OwnHomeFragment extends Fragment {

    ConstraintLayout emptyViewConstraintLayout;
    ExtendedFloatingActionButton addSupermarketEFab;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.own_fragment_home, container, false);
        emptyViewConstraintLayout = root.findViewById(R.id.own_home_empty_view);
        addSupermarketEFab = root.findViewById(R.id.own_home_add_supermarket);

        addSupermarketEFab.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), OwnSupermarketDetailsActivity.class);
            startActivity(intent);
        });

        return root;
    }

    public void whichViewIsActive(){

    }

}
