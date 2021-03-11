package com.einkaufsheld.help2buy.CustomerApp.cart;

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

import com.einkaufsheld.help2buy.R;
import com.einkaufsheld.help2buy.ShoppingOrderDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CartFragment extends Fragment {
    RecyclerView mRecyclerView;
    CartViewAdapter cartAdapter;
    LinearLayout cartEmptyView;
    ArrayList<ShoppingOrderDetails> customerOrderDetailsArrayList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.cus_fragment_cart, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        cartEmptyView = root.findViewById(R.id.cus_cart_fragment_empty_bag);
        cartAdapter = new CartViewAdapter(customerOrderDetailsArrayList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        mRecyclerView = root.findViewById(R.id.cart_recyclerview);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(cartAdapter);
        CartItemViewsCheck();


        return root;


    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        customerOrderDetailsArrayList.clear();
        cartAdapter.notifyDataSetChanged();
        CartItemViewsCheck();
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ShoppingOrderDetails newOrder = snapshot.getValue(ShoppingOrderDetails.class);
                customerOrderDetailsArrayList.add(newOrder);
                cartAdapter.notifyDataSetChanged();
                CartItemViewsCheck();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int index = 10000;
                ShoppingOrderDetails removedOrder = snapshot.getValue(ShoppingOrderDetails.class);
                for (ShoppingOrderDetails individualOrder: customerOrderDetailsArrayList){
                    if(individualOrder.getOrderID().equals(removedOrder.getOrderID())){
                        index = customerOrderDetailsArrayList.indexOf(individualOrder);

                    }
                }
                if (index != 10000){
                    customerOrderDetailsArrayList.remove(index);
                    cartAdapter.notifyItemRemoved(index);
                    CartItemViewsCheck();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.undone_orders)).orderByChild("customerUid").equalTo(user.getUid()).addChildEventListener(childEventListener);
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.pending_orders)).orderByChild("customerUid").equalTo(user.getUid()).addChildEventListener(childEventListener);
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.ongoing_orders)).orderByChild("customerUid").equalTo(user.getUid()).addChildEventListener(childEventListener);
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.finished_orders)).child(getString(R.string.pending_payments)).orderByChild("customerUid").equalTo(user.getUid()).addChildEventListener(childEventListener);

    }

    public void CartItemViewsCheck() {
        if (customerOrderDetailsArrayList.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            cartEmptyView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            cartEmptyView.setVisibility(View.GONE);
        }
    }


}
