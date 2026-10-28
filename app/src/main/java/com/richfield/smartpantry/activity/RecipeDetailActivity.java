package com.richfield.smartpantry.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantry.R;
import com.richfield.smartpantry.adapter.RecipeIngredientAdapter;
import com.richfield.smartpantry.database.DatabaseHelper;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;

import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvCategory, tvPrepTime, tvInstructions;
    private RecyclerView rvIngredients;
    private Button btnCooked;
    private DatabaseHelper dbHelper;
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recipe Details");
        }

        dbHelper = DatabaseHelper.getInstance(this);

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvPrepTime = findViewById(R.id.tvDetailPrepTime);
        tvInstructions = findViewById(R.id.tvDetailInstructions);
        rvIngredients = findViewById(R.id.rvRecipeIngredients);
        btnCooked = findViewById(R.id.btnCookedThis);

        long recipeId = getIntent().getLongExtra("EXTRA_RECIPE_ID", -1);
        if (recipeId != -1) {
            recipe = dbHelper.getRecipeById(recipeId);
            if (recipe != null) {
                displayRecipe();
            }
        }

        btnCooked.setOnClickListener(v -> confirmCook());
    }

    private void displayRecipe() {
        tvTitle.setText(recipe.getName());
        tvCategory.setText(recipe.getCategory());
        tvPrepTime.setText("Prep Time: " + recipe.getPrepTimeMinutes() + " mins");
        tvInstructions.setText(recipe.getInstructions().replace("\\n", "\n"));

        List<PantryItem> pantry = dbHelper.getAllPantryItems();
        RecipeIngredientAdapter adapter = new RecipeIngredientAdapter(this, recipe.getIngredients(), pantry);
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setAdapter(adapter);
    }

    private void confirmCook() {
        new AlertDialog.Builder(this)
                .setTitle("Made This Recipe?")
                .setMessage("This will deduct the used ingredients from your pantry list.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    dbHelper.deductIngredientsForRecipe(recipe);
                    Toast.makeText(this, "Pantry updated! Ingredients deducted.", Toast.LENGTH_SHORT).show();
                    displayRecipe();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
