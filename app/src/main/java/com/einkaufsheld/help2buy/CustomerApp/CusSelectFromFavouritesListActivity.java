package com.einkaufsheld.help2buy.CustomerApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.einkaufsheld.help2buy.R;

import java.util.ArrayList;

import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.RETURN_SELECT_FROM_FAVOURITES;
import static com.einkaufsheld.help2buy.CustomerApp.CustomerMainActivity.favouriteList;

public class CusSelectFromFavouritesListActivity extends AppCompatActivity {

    private LinearLayout selectFromFavouritesEmptyView;
    private ListView selectFromFavouritesListView;
    private CusSelectFromFavouritesViewAdapter favouritesViewAdapter;
    private ArrayList<Integer> selectedListFromFavouritesPositions = new ArrayList<>();
    private ArrayList<ItemDetails> selectedListFromFavourites = new ArrayList<>();

    int checkedCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cus_activity_select_from_favourites_list);

        selectFromFavouritesEmptyView = findViewById(R.id.cus_select_from_favourites_empty_list);
        selectFromFavouritesListView = findViewById(R.id.cus_select_from_favourites_list_view);

        favouritesViewAdapter = new CusSelectFromFavouritesViewAdapter(favouriteList, this);
        selectFromFavouritesListView.setAdapter(favouritesViewAdapter);
        selectFromFavouritesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL); // Important
        selectFromFavouritesItemViewsCheck();

        selectFromFavouritesListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long id, boolean checked) {
                checkedCount = selectFromFavouritesListView.getCheckedItemCount();
                //setting CAB title
                actionMode.setTitle(checkedCount + " Selected");

                //list_item.add(id);
                if(checked){
                    selectedListFromFavouritesPositions.add(i);     // Add to list when checked ==  true
                }else {
                    selectedListFromFavouritesPositions.remove(i);
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.confirm_menu_black, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_confirm_black:
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        for (int position = 0; position < selectedListFromFavouritesPositions.size(); position++) {
                            int selectedItemPosition = selectedListFromFavouritesPositions.get(position);
                            bundle.putParcelable(String.valueOf(position), favouriteList.get(selectedItemPosition));
                        }
                        bundle.putInt("Number of Items",selectedListFromFavouritesPositions.size());
                        intent.putExtras(bundle);
                        setResult(RETURN_SELECT_FROM_FAVOURITES, intent);

                        // Close CAB
                        actionMode.finish();
                        finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
            }
        });
    }

    private void selectFromFavouritesItemViewsCheck() {
        if (favouriteList.isEmpty()) {
            selectFromFavouritesListView.setVisibility(View.GONE);
            selectFromFavouritesEmptyView.setVisibility(View.VISIBLE);
        } else {
            selectFromFavouritesListView.setVisibility(View.VISIBLE);
            selectFromFavouritesEmptyView.setVisibility(View.GONE);
        }
    }
}