package com.richfield.smartpantry.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.richfield.smartpantry.R;
import com.richfield.smartpantry.adapter.PantryAdapter;
import com.richfield.smartpantry.database.DatabaseHelper;
import com.richfield.smartpantry.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 1: Pantry Management Screen.
 * Displays all current pantry items in a RecyclerView bound to SQLite.
 * Provides Add, Edit, Delete (CRUD) operations and persistent storage.
 */
public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemActionListener {

    private RecyclerView recyclerView;
    private PantryAdapter adapter;
    private List<PantryItem> pantryList;
    private TextView tvEmptyState;
    private FloatingActionButton fabAdd;
    private BottomNavigationView bottomNav;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = DatabaseHelper.getInstance(this);

        recyclerView = findViewById(R.id.recyclerViewPantry);
        tvEmptyState = findViewById(R.id.tvEmptyPantry);
        fabAdd = findViewById(R.id.fabAddIngredient);
        bottomNav = findViewById(R.id.bottomNavigation);

        pantryList = new ArrayList<>();
        adapter = new PantryAdapter(this, pantryList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_pantry);
        loadPantryItems();
    }

    private void loadPantryItems() {
        pantryList = dbHelper.getAllPantryItems();
        adapter.updateList(pantryList);

        if (pantryList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
        intent.putExtra("EXTRA_ITEM_ID", item.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage("Are you sure you want to remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deletePantryItem(item.getId());
                    Toast.makeText(MainActivity.this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
                    loadPantryItems();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_pantry) {
                return true;
            } else if (itemId == R.id.nav_suggested) {
                Intent intent = new Intent(MainActivity.this, SuggestedRecipesActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}
