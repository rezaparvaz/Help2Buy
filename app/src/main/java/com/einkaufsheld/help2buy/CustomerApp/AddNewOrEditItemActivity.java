package com.einkaufsheld.help2buy.CustomerApp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.einkaufsheld.help2buy.R;

import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.RETURN_ADDED_FAVOURITE_ITEM;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.RETURN_EDITED_FAVOURITE_ITEM;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.START_ADD_FAVOURITE_ITEM;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.START_EDIT_FAVOURITE_ITEM;
import static com.einkaufsheld.help2buy.CustomerApp.ShoppingListActivity.RETURN_FROM_ADD_OR_EDIT_ITEM_IN_SHOPPING_LIST_ACTIVITY;
import static com.einkaufsheld.help2buy.CustomerApp.ShoppingListActivity.START_ADD_ITEM_IN_SHOPPING_LIST_ACTIVITY;
import static com.einkaufsheld.help2buy.CustomerApp.ShoppingListActivity.START_EDIT_ITEM_IN_SHOPPING_LIST_ACTIVITY;

public class AddNewOrEditItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cus_activity_add_new_or_edit_item);

        EditText quantityMain = findViewById(R.id.add_new_item_quantity_main);
        Spinner spinnerMain = findViewById(R.id.add_new_item_spinner_main);
        EditText detailsMain = findViewById(R.id.add_new_item_details_main);
        EditText quantityOptional = findViewById(R.id.add_new_item_quantity_optional);
        Spinner spinnerOptional = findViewById(R.id.add_new_item_spinner_optional);
        EditText detailsOptional = findViewById(R.id.add_new_item_details_optional);
        RadioGroup radioGroup = findViewById(R.id.add_new_item_radio_group);
        Button cancelButton = findViewById(R.id.cancel_add_item_button);
        Button addButton = findViewById(R.id.add_new_item_button);

        Intent getIntent = getIntent();
        int requestCode = getIntent.getIntExtra("requestCode",0);
        TextView title = findViewById(R.id.add_new_item_title);

        if (requestCode == START_EDIT_ITEM_IN_SHOPPING_LIST_ACTIVITY || requestCode == START_EDIT_FAVOURITE_ITEM) {
            Log.d("TAG", "22222222222222222222222: ");

            title.setText(R.string.edit_item);
            addButton.setText(R.string.confirm);
            quantityMain.setText(getIntent.getStringExtra("quantityMainText"));
            if (getIntent.getStringExtra("spinnerMainText").equals(getString(R.string.piece))){
                spinnerMain.setSelection(1);
            } else if (getIntent.getStringExtra("spinnerMainText").equals(getString(R.string.kilogram))) {
                spinnerMain.setSelection(2);
            }
            detailsMain.setText(getIntent.getStringExtra("detailsMainText"));
            quantityOptional.setText(getIntent.getStringExtra("quantityOptionalText"));
            if (getIntent.getStringExtra("spinnerOptionalText").equals(getString(R.string.piece))){
                spinnerOptional.setSelection(1);
            } else if (getIntent.getStringExtra("spinnerOptionalText").equals(getString(R.string.kilogram))) {
                spinnerOptional.setSelection(2);
            }
            detailsOptional.setText(getIntent.getStringExtra("detailsOptionalText"));
            if (getIntent.getStringExtra("radioButtonText").equals(getString(R.string.bio))){
                RadioButton radioButton = findViewById(R.id.add_new_item_radio_button_bio);
                radioButton.setChecked(true);
            } else if (getIntent.getStringExtra("radioButtonText").equals(getString(R.string.cheapest))) {
                RadioButton radioButton = findViewById(R.id.add_new_item_radio_button_cheapest);
                radioButton.setChecked(true);
            } else if (getIntent.getStringExtra("radioButtonText").equals(getString(R.string.none))) {
                RadioButton radioButton = findViewById(R.id.add_new_item_radio_button_none);
                radioButton.setChecked(true);
            }


        } else if(requestCode == START_ADD_ITEM_IN_SHOPPING_LIST_ACTIVITY || requestCode == START_ADD_FAVOURITE_ITEM){
            title.setText(R.string.new_item);
        }



        addButton.setOnClickListener(view -> {

            int selectedId = radioGroup.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);

            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            ItemDetails newItemDetails = new ItemDetails(quantityMain.getText().toString(), spinnerMain.getSelectedItem().toString(), detailsMain.getText().toString(),
                    quantityOptional.getText().toString(), spinnerOptional.getSelectedItem().toString(), detailsOptional.getText().toString(), radioButton.getText().toString());
            bundle.putParcelable("Item Details", newItemDetails);
            int listPosition = getIntent.getIntExtra("listPosition",0);
            if (requestCode == START_EDIT_ITEM_IN_SHOPPING_LIST_ACTIVITY || requestCode == START_EDIT_FAVOURITE_ITEM) {
                Log.d("TAG", "333333333333333333333333333: ");
                bundle.putInt("listPosition", listPosition);
            }

            intent.putExtras(bundle);
            if (requestCode == START_ADD_ITEM_IN_SHOPPING_LIST_ACTIVITY || requestCode == START_EDIT_ITEM_IN_SHOPPING_LIST_ACTIVITY) {

                setResult(RETURN_FROM_ADD_OR_EDIT_ITEM_IN_SHOPPING_LIST_ACTIVITY, intent);

            } else if (requestCode == START_ADD_FAVOURITE_ITEM) {

                setResult(RETURN_ADDED_FAVOURITE_ITEM, intent);


            } else if (requestCode == START_EDIT_FAVOURITE_ITEM) {
                setResult(RETURN_EDITED_FAVOURITE_ITEM, intent);

            }

            finish();

        });

        cancelButton.setOnClickListener(view -> {

            finish();
        });





    }
}