package com.einkaufsheld.help2buy.CustomerApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.einkaufsheld.help2buy.R;

import java.util.ArrayList;


public class CusSelectFromFavouritesViewAdapter extends ArrayAdapter<ItemDetails> {
    private ArrayList<ItemDetails> dataSet;
    private ItemDetails object;
    private Context mContext;


    public CusSelectFromFavouritesViewAdapter(ArrayList<ItemDetails> data, Context context) {
        super(context, 0, data);
        this.dataSet = data;
        this.mContext = context;

    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        object = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cus_shopping_list_item, parent, false);
        }
        TextView mainDetailTextView = convertView.findViewById(R.id.shopping_list_item_main_detail);
        TextView mainQuantityTextView = convertView.findViewById(R.id.shopping_list_item_main_quantity);
        TextView optionalDetailTextView = convertView.findViewById(R.id.shopping_list_item_optional_detail);
        TextView optionalQuantityTextView = convertView.findViewById(R.id.shopping_list_item_optional_quantity);
        TextView bioOrCheapestOrNone = convertView.findViewById(R.id.shopping_list_item_bio_or_cheapest_or_none);
        ImageView removeButton = convertView.findViewById(R.id.shopping_list_item_remove_button);
        mainDetailTextView.setText(object.getDetailsMain());
        String quantityMain = object.getQuantityMain() + " " + object.getPieceOrKgMain();
        mainQuantityTextView.setText(quantityMain);
        optionalDetailTextView.setText(object.getDetailsOptional());
        String quantityOptional = object.getQuantityOptional() + " " + object.getPieceOrKgOptional();
        optionalQuantityTextView.setText(quantityOptional);
        bioOrCheapestOrNone.setText(object.getBioOrCheapestOrNone());
        removeButton.setVisibility(View.GONE);

        return convertView;
    }

}
