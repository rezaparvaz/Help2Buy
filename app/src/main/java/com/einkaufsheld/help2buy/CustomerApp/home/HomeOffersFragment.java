package com.einkaufsheld.help2buy.CustomerApp.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.einkaufsheld.help2buy.R;

public class HomeOffersFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.cus_fragment_home_offers, container, false);

        final TextView textView = root.findViewById(R.id.text_home_items);
        textView.setText(getString(R.string.Offers));
        return root;
    }

}

