package com.einkaufsheld.help2buy.CustomerApp.home;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.einkaufsheld.help2buy.CustomerApp.ShoppingListActivity;
import com.einkaufsheld.help2buy.CustomerApp.home.model.OpeningHours;
import com.einkaufsheld.help2buy.CustomerApp.home.model.Result;
import com.einkaufsheld.help2buy.CustomerApp.home.model.Route;
import com.einkaufsheld.help2buy.R;
import com.einkaufsheld.help2buy.ShoppingOrderDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.START_ADD_SUPERMARKET;


public class HomeStoresViewAdapter extends RecyclerView.Adapter<HomeStoresViewAdapter.HomeStoresViewHolder> {
    public static Map<Pair<Location, Result>, Route> ROUTE_MATRIX = new HashMap<>();
    private ArrayList<ResultsWithPhoto> dataSet;
    private HomeStoresFragment mHomeStoresFragment;
    private EventListener mListener;


    public HomeStoresViewAdapter(ArrayList<ResultsWithPhoto> data, HomeStoresFragment homeStoresFragment, EventListener listener) {
        this.dataSet = data;
        this.mHomeStoresFragment = homeStoresFragment;
        this.mListener = listener;
    }

    @NotNull
    @Override
    public HomeStoresViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cus_single_supermarket_layout, parent, false);
        return new HomeStoresViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull HomeStoresViewHolder holder, int position) {

        ResultsWithPhoto item = dataSet.get(position);
        Result result = item.getResult();

        if (result != null) {


            holder.nameText.setText(result.getName());


            StringBuilder builder = new StringBuilder().append(mHomeStoresFragment.getContext().getString(R.string.distance));
            if (mListener.getLocation() != null) {
//                        RequestSingleton.getInstance(mContext).addToRequestQueue(getRouteRequest(currentLocation, item));
                Route route = ROUTE_MATRIX.get(Pair.create(mListener.getLocation(), result));

                if (route != null) {
//                    int transfer = 0;
                    builder.append(": ").append(route.getLegs().get(0).getDistance().getText()).append(";");
//                    for (Step step : route.getLegs().get(0).getSteps()) {
//                        if ("TRANSIT".equals(step.getTravelMode())) {
//                            transfer += 1;
//
//                        }
                }
//                        builder.append(" ").append(transfer).append(" buses;");
            } else {
                builder.append(" ").append("Not Accessible");
            }
//
            holder.distance.setText(builder.deleteCharAt(builder.length() - 1));

            if (item.getBitmap() != null) {
                holder.photo.setImageBitmap(item.getBitmap());
            }

            StringBuilder addressBuilder = new StringBuilder().append(mHomeStoresFragment.getContext().getString(R.string.address));
            addressBuilder.append(": ").append(result.getVicinity());
            holder.addressText.setText(addressBuilder);
            String openNow = "No Info";

            try {
                OpeningHours openingHours =result.getOpeningHours();

                if (openingHours.getOpenNow()) {
                    openNow = "Open";
                    holder.openingHours.setText(R.string.open_now);
                    holder.openingHours.setTextColor(Color.GREEN);
                } else if (!openingHours.getOpenNow()) {
                    openNow = "Closed";
                    holder.openingHours.setText(R.string.closed);
                    holder.openingHours.setTextColor(Color.RED);

                }
            } catch (NullPointerException exception){
                holder.openingHours.setText(R.string.unavailableOpeningHours);
                holder.openingHours.setTextColor(Color.DKGRAY);
            }

            double rate = 0;
            if (result.getRating() != null) {
                rate = result.getRating();
            }
            holder.ratingBar.setRating((float) rate);
            double finalRate = rate;

            String finalOpenNow = openNow;
            holder.itemView.setOnClickListener(v -> {
//            Uri gmmUri = Uri.parse("geo:" + result.getGeometry().getLocation().getLat() + "," + result.getGeometry().getLocation().getLng() +
//                    "?q=" + result.getName() + "," + result.getVicinity());
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent shoppingListIntent = new Intent(mHomeStoresFragment.getContext(), ShoppingListActivity.class);
                ShoppingOrderDetails shoppingOrderDetails = new ShoppingOrderDetails(0.00, 0.00, "0", "0", user.getUid(),
                        0.0, 0.0, result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng(),
                        result.getName(), "", "0", "0", "0", "0", "0.00", "0.00",
                        "0.00", new ArrayList<>(), finalRate, finalOpenNow, builder.toString(), addressBuilder.toString(), result.getReference());

                shoppingListIntent.putExtra("Shopping Details", shoppingOrderDetails);
                shoppingListIntent.putExtra("requestCode", START_ADD_SUPERMARKET);

                mHomeStoresFragment.startActivity(shoppingListIntent);

            });
        }
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public interface EventListener {
        Location getLocation();
    }


    public static class HomeStoresViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView nameText;
        TextView addressText;
        TextView openingHours;
        RatingBar ratingBar;
        TextView distance;
        ImageView photo;

        HomeStoresViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.nameText = itemView.findViewById(R.id.supermarket_name);
            this.addressText = itemView.findViewById(R.id.supermarket_address);
            this.openingHours = itemView.findViewById(R.id.supermarket_opening_hours);
            this.ratingBar = itemView.findViewById(R.id.ratingBar);
            this.distance = itemView.findViewById(R.id.supermarket_distance);
            this.photo = itemView.findViewById(R.id.supermarket_photo);
        }

    }
}
