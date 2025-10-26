package com.richfield.smartpantry.activity;

import android.content.Intent;
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

/**
 * Screen 3: Suggested Recipes Screen.
 * Executes Section 2.3 Strict-Matching Rule against user's current pantry.
 * Only recipes where 100% of required ingredients are present in sufficient quantity appear.
 * Includes a separate bonus filter for 'Almost There' recipes missing exactly 1 ingredient.
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private TextView tvEmptyState, tvFilterHeader;
    private Button btnStrictMatches, btnAlmostThere;
    private BottomNavigationView bottomNav;
    private DatabaseHelper dbHelper;

    private List<RecipeMatchResult> allStrictMatches = new ArrayList<>();
    private List<RecipeMatchResult> allAlmostThereMatches = new ArrayList<>();
    private boolean showingStrict = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        dbHelper = DatabaseHelper.getInstance(this);

        recyclerView = findViewById(R.id.recyclerViewSuggested);
        tvEmptyState = findViewById(R.id.tvEmptySuggested);
        tvFilterHeader = findViewById(R.id.tvFilterHeader);
        btnStrictMatches = findViewById(R.id.btnFilterStrict);
        btnAlmostThere = findViewById(R.id.btnFilterAlmost);
        bottomNav = findViewById(R.id.bottomNavigation);

        adapter = new RecipeAdapter(this, new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnStrictMatches.setOnClickListener(v -> {
            showingStrict = true;
            updateFilterView();
        });

        btnAlmostThere.setOnClickListener(v -> {
            showingStrict = false;
            updateFilterView();
        });

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_suggested);
        runMatchingEngine();
    }

    /**
     * Executes the strict matching algorithm across all recipes in SQLite.
     */
    private void runMatchingEngine() {
        List<PantryItem> pantry = dbHelper.getAllPantryItems();
        List<Recipe> allRecipes = dbHelper.getAllRecipesWithIngredients();

        allStrictMatches.clear();
        allAlmostThereMatches.clear();

        for (Recipe r : allRecipes) {
            RecipeMatchResult result = IngredientMatcher.evaluateRecipe(r, pantry);
            if (result.isStrictMatch()) {
                allStrictMatches.add(result);
            } else if (result.isAlmostThere()) {
                allAlmostThereMatches.add(result);
            }
        }

        btnStrictMatches.setText("Strict Matches (" + allStrictMatches.size() + ")");
        btnAlmostThere.setText("Almost There (" + allAlmostThereMatches.size() + ")");

        updateFilterView();
    }

    private void updateFilterView() {
        if (showingStrict) {
            btnStrictMatches.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            btnAlmostThere.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            tvFilterHeader.setText("Strictly Matched Recipes (Zero Missing Ingredients)");

            if (allStrictMatches.isEmpty()) {
                tvEmptyState.setText("No recipes match your pantry yet.\nAdd more leftover ingredients to see recipes you can make right now!");
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.updateList(allStrictMatches);
            }
        } else {
            btnStrictMatches.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            btnAlmostThere.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
            tvFilterHeader.setText("Almost There (Missing Exactly 1 Ingredient)");

            if (allAlmostThereMatches.isEmpty()) {
                tvEmptyState.setText("No 'Almost There' recipes found.\nAdd a few more ingredients to get close to recipes!");
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.updateList(allAlmostThereMatches);
            }
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("EXTRA_RECIPE_ID", recipe.getId());
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_suggested) {
                return true;
            } else if (itemId == R.id.nav_pantry) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }
}
