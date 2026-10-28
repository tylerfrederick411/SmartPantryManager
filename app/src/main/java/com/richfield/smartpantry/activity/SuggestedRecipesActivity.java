package com.richfield.smartpantry.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.richfield.smartpantry.R;
import com.richfield.smartpantry.adapter.RecipeAdapter;
import com.richfield.smartpantry.database.DatabaseHelper;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeMatchResult;
import com.richfield.smartpantry.util.IngredientMatcher;

import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private TextView tvEmptyState, tvSubtitle;
    private Button btnStrictMatches, btnAlmostThere;
    private BottomNavigationView bottomNav;
    private DatabaseHelper dbHelper;

    private List<RecipeMatchResult> strictList = new ArrayList<>();
    private List<RecipeMatchResult> almostList = new ArrayList<>();
    private boolean isShowingStrict = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Suggested Recipes");
        }

        dbHelper = DatabaseHelper.getInstance(this);

        recyclerView = findViewById(R.id.recyclerViewSuggested);
        tvEmptyState = findViewById(R.id.tvEmptySuggested);
        tvSubtitle = findViewById(R.id.tvFilterHeader);
        btnStrictMatches = findViewById(R.id.btnFilterStrict);
        btnAlmostThere = findViewById(R.id.btnFilterAlmost);
        bottomNav = findViewById(R.id.bottomNavigation);

        adapter = new RecipeAdapter(this, new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnStrictMatches.setOnClickListener(v -> {
            isShowingStrict = true;
            renderView();
        });

        btnAlmostThere.setOnClickListener(v -> {
            isShowingStrict = false;
            renderView();
        });

        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_suggested);
        calculateSuggestions();
    }

    private void calculateSuggestions() {
        List<PantryItem> pantry = dbHelper.getAllPantryItems();
        List<Recipe> recipes = dbHelper.getAllRecipesWithIngredients();

        strictList.clear();
        almostList.clear();

        for (Recipe r : recipes) {
            RecipeMatchResult result = IngredientMatcher.evaluateRecipe(r, pantry);
            if (result.isStrictMatch()) {
                strictList.add(result);
            } else if (result.isAlmostThere()) {
                almostList.add(result);
            }
        }

        btnStrictMatches.setText("Ready to Cook (" + strictList.size() + ")");
        btnAlmostThere.setText("Missing 1 Item (" + almostList.size() + ")");

        renderView();
    }

    private void renderView() {
        if (isShowingStrict) {
            btnStrictMatches.setBackgroundResource(R.drawable.btn_tab_active);
            btnStrictMatches.setTextColor(Color.WHITE);
            btnAlmostThere.setBackgroundResource(R.drawable.btn_tab_inactive);
            btnAlmostThere.setTextColor(Color.parseColor("#4A5568"));
            tvSubtitle.setText("Recipes you can make right now with what you have");

            if (strictList.isEmpty()) {
                tvEmptyState.setText("No recipes match your pantry yet.\n\nAdd a few more leftover ingredients or check the 'Missing 1 Item' tab above!");
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.updateList(strictList);
            }
        } else {
            btnStrictMatches.setBackgroundResource(R.drawable.btn_tab_inactive);
            btnStrictMatches.setTextColor(Color.parseColor("#4A5568"));
            btnAlmostThere.setBackgroundResource(R.drawable.btn_tab_active_amber);
            btnAlmostThere.setTextColor(Color.WHITE);
            tvSubtitle.setText("Recipes where you only need 1 more ingredient");

            if (almostList.isEmpty()) {
                tvEmptyState.setText("No almost-matching recipes right now.\nAdd more ingredients to your pantry list!");
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.updateList(almostList);
            }
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("EXTRA_RECIPE_ID", recipe.getId());
        startActivity(intent);
    }

    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_suggested) {
                return true;
            } else if (id == R.id.nav_pantry) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}
