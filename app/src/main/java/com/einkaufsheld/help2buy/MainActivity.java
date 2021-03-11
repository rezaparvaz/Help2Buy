package com.einkaufsheld.help2buy;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity;
import com.einkaufsheld.help2buy.NewUserActivities.CreateAccountOrLogInOptionsActivity;
import com.einkaufsheld.help2buy.NewUserActivities.NewUserActivity;
import com.einkaufsheld.help2buy.StoreOwnerApp.OwnerMainActivity;
import com.einkaufsheld.help2buy.SupplierApp.SupplierMainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;

public class MainActivity extends AppCompatActivity {

    private int OPEN_LOG_IN_OR_SIGN_UP_PAGE = 0;
    public static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static boolean locationPermissionGranted = false;
    public static Location lastKnownLocation;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                DocumentReference docRef = db.collection("users").document(user.getUid());
                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.get("PhoneNumber") != null && !document.get("PhoneNumber").equals("")) {
                            if (document.get("UserIsCustomer").toString().equals("1")) {
                                Intent intent = new Intent(getApplicationContext(), CustomerMainActivity.class);
                                startActivity(intent);
                            } else if (document.get("UserIsSupplier").toString().equals("1")) {
                                Intent intent = new Intent(getApplicationContext(), SupplierMainActivity.class);
                                startActivity(intent);

                            } else if (document.get("UserIsStoreOwner").toString().equals("1")) {
                                Intent intent = new Intent(getApplicationContext(), OwnerMainActivity.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(getApplicationContext(), NewUserActivity.class);
                                startActivity(intent);
                            }
                        } else {
                            addUserInfoToDatabase(user);
                            Intent intent = new Intent(getApplicationContext(), NewUserActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                });
            } else {
                Intent intent = new Intent(getApplicationContext(), CreateAccountOrLogInOptionsActivity.class);
                startActivityForResult(intent, OPEN_LOG_IN_OR_SIGN_UP_PAGE);
            }
        };
    }




    private void addUserInfoToDatabase(FirebaseUser user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("Name", user.getDisplayName());
        userInfo.put("Email", user.getEmail());
        userInfo.put("PhoneNumber", user.getPhoneNumber());
        userInfo.put("UserIsCustomer", "0");
        userInfo.put("UserIsSupplier", "0");
        userInfo.put("UserIsStoreOwner", "0");

        db.collection("users").document(user.getUid()).set(userInfo);


    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_LOG_IN_OR_SIGN_UP_PAGE) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

}