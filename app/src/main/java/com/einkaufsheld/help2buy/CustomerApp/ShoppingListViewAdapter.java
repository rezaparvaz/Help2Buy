package com.einkaufsheld.help2buy.CustomerApp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.einkaufsheld.help2buy.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.einkaufsheld.help2buy.CustomerApp.ShoppingListActivity.START_EDIT_ITEM_IN_SHOPPING_LIST_ACTIVITY;


public class ShoppingListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<ItemDetails> dataSet;
    private ItemDetails object;
    private Context mContext;



    public interface EventListener {
        void FavouritesItemViewsCheck();
    }

    public ShoppingListViewAdapter(ArrayList<ItemDetails> data, Context context) {
        this.dataSet = data;
        this.mContext = context;


    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cus_shopping_list_item, parent, false);
        return new ShoppingListViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int listPosition) {

        object = dataSet.get(listPosition);

        ((ShoppingListViewHolder) holder).mainDetailTextView.setText(object.getDetailsMain());

        String quantityMain = object.getQuantityMain() + " " + object.getPieceOrKgMain();
        ((ShoppingListViewHolder) holder).mainQuantityTextView.setText(quantityMain);

        ((ShoppingListViewHolder) holder).optionalDetailTextView.setText(object.getDetailsOptional());

        String quantityOptional = object.getQuantityOptional() + " " + object.getPieceOrKgOptional();
        ((ShoppingListViewHolder) holder).optionalQuantityTextView.setText(quantityOptional);


        ((ShoppingListViewHolder) holder).bioOrCheapestOrNone.setText(object.getBioOrCheapestOrNone());


        ((ShoppingListViewHolder) holder).removeButton.setOnClickListener(view -> {
            dataSet.remove(listPosition);
            notifyDataSetChanged();
            ((ShoppingListActivity) mContext).ItemViewsCheck();


        });

        ((ShoppingListViewHolder) holder).view.setOnClickListener(view -> {

            Intent editItemIntent = new Intent(mContext, AddNewOrEditItemActivity.class);
            editItemIntent.putExtra("quantityMainText", object.getQuantityMain());
            editItemIntent.putExtra("spinnerMainText", object.getPieceOrKgMain());
            editItemIntent.putExtra("detailsMainText", object.getDetailsMain());
            editItemIntent.putExtra("quantityOptionalText", object.getQuantityOptional());
            editItemIntent.putExtra("spinnerOptionalText", object.getPieceOrKgOptional());
            editItemIntent.putExtra("detailsOptionalText", object.getDetailsOptional());
            editItemIntent.putExtra("radioButtonText", object.getBioOrCheapestOrNone());
            editItemIntent.putExtra("listPosition", listPosition);
            editItemIntent.putExtra("requestCode", START_EDIT_ITEM_IN_SHOPPING_LIST_ACTIVITY);
            ((Activity) mContext).startActivityForResult(editItemIntent, START_EDIT_ITEM_IN_SHOPPING_LIST_ACTIVITY);


        });


    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }





    public static class ShoppingListViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView mainDetailTextView;
        TextView mainQuantityTextView;
        TextView optionalDetailTextView;
        TextView optionalQuantityTextView;
        TextView bioOrCheapestOrNone;
        ImageView removeButton;

        public ShoppingListViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.mainDetailTextView = itemView.findViewById(R.id.shopping_list_item_main_detail);
            this.mainQuantityTextView = itemView.findViewById(R.id.shopping_list_item_main_quantity);
            this.optionalDetailTextView = itemView.findViewById(R.id.shopping_list_item_optional_detail);
            this.optionalQuantityTextView = itemView.findViewById(R.id.shopping_list_item_optional_quantity);
            this.bioOrCheapestOrNone = itemView.findViewById(R.id.shopping_list_item_bio_or_cheapest_or_none);
            this.removeButton = itemView.findViewById(R.id.shopping_list_item_remove_button);


        }

    }

}
