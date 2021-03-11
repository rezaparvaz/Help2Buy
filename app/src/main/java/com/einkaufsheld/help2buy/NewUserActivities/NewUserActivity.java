package com.einkaufsheld.help2buy.NewUserActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.einkaufsheld.help2buy.PhoneNumberVerificationActivity;
import com.einkaufsheld.help2buy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        Button enterAsCustomer = findViewById(R.id.enter_as_customer_button);
        Button enterAsSupplier = findViewById(R.id.enter_as_supplier_button);
        Button enterAsSeller = findViewById(R.id.enter_as_seller_button);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        assert user != null;
        enterAsCustomer.setOnClickListener(view -> {
            db.collection("users").document(user.getUid()).update("UserIsCustomer", "1");

            Intent intent = new Intent(getApplicationContext(), PhoneNumberVerificationActivity.class);
            startActivity(intent);
        });

        enterAsSupplier.setOnClickListener(view -> {
            db.collection("users").document(user.getUid()).update("UserIsSupplier", "1");

            Intent intent = new Intent(getApplicationContext(), PhoneNumberVerificationActivity.class);
            startActivity(intent);
        });

        enterAsSeller.setOnClickListener(view -> {
            db.collection("users").document(user.getUid()).update("UserIsStoreOwner", "1");
            Intent intent = new Intent(getApplicationContext(), PhoneNumberVerificationActivity.class);
            startActivity(intent);
        });

    }
}