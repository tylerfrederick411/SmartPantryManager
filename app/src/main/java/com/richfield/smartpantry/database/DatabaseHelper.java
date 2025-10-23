package com.richfield.smartpantry.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;
import com.richfield.smartpantry.util.IngredientMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Manager for Smart Pantry Manager.
 * Implements persistent local storage using SQLiteOpenHelper as required by Section 3.2.
 * Handles full CRUD operations on Pantry Items and seeds 20 initial recipes.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_PANTRY = "pantry";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    // Common columns
    public static final String COLUMN_ID = "_id";

    // Pantry columns
    public static final String COLUMN_PANTRY_NAME = "name";
    public static final String COLUMN_PANTRY_QUANTITY = "quantity";
    public static final String COLUMN_PANTRY_UNIT = "unit";
    public static final String COLUMN_PANTRY_EXPIRY = "expiry_date";
    public static final String COLUMN_PANTRY_CATEGORY = "category";

    // Recipes columns
    public static final String COLUMN_RECIPE_NAME = "name";
    public static final String COLUMN_RECIPE_CATEGORY = "category";
    public static final String COLUMN_RECIPE_PREP_TIME = "prep_time_minutes";
    public static final String COLUMN_RECIPE_INSTRUCTIONS = "instructions";

    // Recipe Ingredients columns
    public static final String COLUMN_RI_RECIPE_ID = "recipe_id";
    public static final String COLUMN_RI_INGREDIENT_NAME = "ingredient_name";
    public static final String COLUMN_RI_QUANTITY = "quantity";
    public static final String COLUMN_RI_UNIT = "unit";

    // Singleton instance to prevent multiple database helper leaks
    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Pantry Table
        String createPantryTable = "CREATE TABLE " + TABLE_PANTRY + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PANTRY_NAME + " TEXT NOT NULL, " +
                COLUMN_PANTRY_QUANTITY + " REAL NOT NULL, " +
                COLUMN_PANTRY_UNIT + " TEXT NOT NULL, " +
                COLUMN_PANTRY_EXPIRY + " TEXT, " +
                COLUMN_PANTRY_CATEGORY + " TEXT" +
                ");";
        db.execSQL(createPantryTable);

        // Create Recipes Table
        String createRecipesTable = "CREATE TABLE " + TABLE_RECIPES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RECIPE_NAME + " TEXT NOT NULL, " +
                COLUMN_RECIPE_CATEGORY + " TEXT, " +
                COLUMN_RECIPE_PREP_TIME + " INTEGER, " +
                COLUMN_RECIPE_INSTRUCTIONS + " TEXT NOT NULL" +
                ");";
        db.execSQL(createRecipesTable);

        // Create Recipe Ingredients Table
        String createRecipeIngredientsTable = "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COLUMN_RI_INGREDIENT_NAME + " TEXT NOT NULL, " +
                COLUMN_RI_QUANTITY + " REAL NOT NULL, " +
                COLUMN_RI_UNIT + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
                ");";
        db.execSQL(createRecipeIngredientsTable);

        // Seed 20 recipes with detailed ingredients
        seedInitialRecipes(db);

        // Seed initial sample pantry items for instant testing
        seedSamplePantry(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ==========================================
    // PANTRY CRUD OPERATIONS (Create, Read, Update, Delete)
    // ==========================================

    /**
     * CREATE: Add a new pantry item.
     */
    public long insertPantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PANTRY_NAME, item.getName().trim());
        values.put(COLUMN_PANTRY_QUANTITY, item.getQuantity());
        values.put(COLUMN_PANTRY_UNIT, item.getUnit());
        values.put(COLUMN_PANTRY_EXPIRY, item.getExpiryDate());
        values.put(COLUMN_PANTRY_CATEGORY, item.getCategory());

        long id = db.insert(TABLE_PANTRY, null, values);
        item.setId(id);
        return id;
    }

    /**
     * READ: Get a single pantry item by ID.
     */
    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            PantryItem item = cursorToPantryItem(cursor);
            cursor.close();
            return item;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    /**
     * READ: Retrieve all pantry items ordered alphabetically.
     */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, null, null, null, null, COLUMN_PANTRY_NAME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                items.add(cursorToPantryItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    /**
     * UPDATE: Modify an existing pantry item.
     */
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PANTRY_NAME, item.getName().trim());
        values.put(COLUMN_PANTRY_QUANTITY, item.getQuantity());
        values.put(COLUMN_PANTRY_UNIT, item.getUnit());
        values.put(COLUMN_PANTRY_EXPIRY, item.getExpiryDate());
        values.put(COLUMN_PANTRY_CATEGORY, item.getCategory());

        return db.update(TABLE_PANTRY, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /**
     * DELETE: Remove a pantry item by ID.
     */
    public int deletePantryItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PANTRY, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    private PantryItem cursorToPantryItem(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_NAME));
        double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_QUANTITY));
        String unit = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_UNIT));
        String expiry = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_EXPIRY));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PANTRY_CATEGORY));
        return new PantryItem(id, name, quantity, unit, expiry, category);
    }

    // ==========================================
    // RECIPES QUERIES
    // ==========================================

    /**
     * Retrieve all seeded recipes with their associated required ingredients.
     */
    public List<Recipe> getAllRecipesWithIngredients() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RECIPES, null, null, null, null, null, COLUMN_RECIPE_NAME + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long recipeId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_CATEGORY));
                int prepTime = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_PREP_TIME));
                String instructions = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_INSTRUCTIONS));

                Recipe recipe = new Recipe(recipeId, name, category, prepTime, instructions);
                recipe.setIngredients(getIngredientsForRecipe(db, recipeId));
                recipes.add(recipe);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return recipes;
    }

    public Recipe getRecipeById(long recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, COLUMN_ID + " = ?",
                new String[]{String.valueOf(recipeId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_CATEGORY));
            int prepTime = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_PREP_TIME));
            String instructions = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_INSTRUCTIONS));

            Recipe recipe = new Recipe(recipeId, name, category, prepTime, instructions);
            recipe.setIngredients(getIngredientsForRecipe(db, recipeId));
            cursor.close();
            return recipe;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        Cursor cursor = db.query(TABLE_RECIPE_INGREDIENTS, null, COLUMN_RI_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String ingName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RI_INGREDIENT_NAME));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RI_QUANTITY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RI_UNIT));

                list.add(new RecipeIngredient(id, recipeId, ingName, qty, unit));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    /**
     * Deducts used recipe ingredients from the user's pantry.
     * If an item's quantity reaches zero or below, it is automatically removed.
     */
    public void deductIngredientsForRecipe(Recipe recipe) {
        if (recipe == null || recipe.getIngredients() == null) return;
        List<PantryItem> pantryItems = getAllPantryItems();

        for (RecipeIngredient req : recipe.getIngredients()) {
            String normReqName = IngredientMatcher.normalizeName(req.getIngredientName());
            for (PantryItem pItem : pantryItems) {
                if (IngredientMatcher.normalizeName(pItem.getName()).equals(normReqName)) {
                    double remaining = pItem.getQuantity() - req.getQuantity();
                    if (remaining <= 0.001) {
                        deletePantryItem(pItem.getId());
                    } else {
                        pItem.setQuantity(remaining);
                        updatePantryItem(pItem);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Reset the database to initial clean factory state with sample items and 20 recipes.
     */
    public void resetToDefaults() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECIPE_INGREDIENTS, null, null);
        db.delete(TABLE_RECIPES, null, null);
        db.delete(TABLE_PANTRY, null, null);
        seedInitialRecipes(db);
        seedSamplePantry(db);
    }

    // ==========================================
    // DATABASE SEEDING: 20 PRE-LOADED RECIPES
    // ==========================================
    private void seedInitialRecipes(SQLiteDatabase db) {
        // Recipe 1: Classic Scrambled Eggs
        addRecipe(db, "Classic Scrambled Eggs", "Breakfast", 10,
                "1. Crack eggs into a bowl, add milk and a pinch of salt.\n2. Whisk thoroughly until smooth and airy.\n3. Melt butter in a non-stick skillet over medium-low heat.\n4. Pour in eggs and gently pull curds across the pan with a spatula.\n5. Remove from heat just before fully set and serve immediately.",
                new String[][]{
                        {"Egg", "3", "unit"},
                        {"Butter", "1", "tbsp"},
                        {"Milk", "2", "tbsp"},
                        {"Salt", "1", "tsp"}
                });

        // Recipe 2: Tomato & Herb Omelette
        addRecipe(db, "Tomato & Herb Omelette", "Breakfast", 12,
                "1. Whisk eggs with salt and pepper in a bowl.\n2. Heat olive oil or butter in a skillet.\n3. Sauté diced tomato and chopped onion for 2 minutes.\n4. Pour whisked eggs over the vegetables.\n5. Cook until edges set, fold in half, and serve hot.",
                new String[][]{
                        {"Egg", "2", "unit"},
                        {"Tomato", "1", "unit"},
                        {"Onion", "1", "unit"},
                        {"Cooking Oil", "1", "tbsp"},
                        {"Salt", "1", "tsp"}
                });

        // Recipe 3: Golden French Toast
        addRecipe(db, "Golden French Toast", "Breakfast", 15,
                "1. Whisk together eggs, milk, and sugar in a shallow dish.\n2. Dip bread slices on both sides until soaked but not soggy.\n3. Melt butter in a pan over medium heat.\n4. Fry each bread slice for 3-4 minutes per side until golden brown.\n5. Serve with syrup or fruit.",
                new String[][]{
                        {"Bread", "4", "slice"},
                        {"Egg", "2", "unit"},
                        {"Milk", "0.5", "cup"},
                        {"Butter", "1", "tbsp"},
                        {"Sugar", "1", "tbsp"}
                });

        // Recipe 4: Crispy Grilled Cheese Sandwich
        addRecipe(db, "Crispy Grilled Cheese Sandwich", "Lunch", 10,
                "1. Butter one side of each slice of bread.\n2. Place cheese between the unbuttered sides of bread.\n3. Place sandwich on skillet over medium-low heat.\n4. Toast until golden brown (approx 3-4 mins), flip and toast other side until cheese melts completely.",
                new String[][]{
                        {"Bread", "2", "slice"},
                        {"Cheese", "50", "g"},
                        {"Butter", "1", "tbsp"}
                });

        // Recipe 5: Quick Garlic Butter Rice
        addRecipe(db, "Quick Garlic Butter Rice", "Dinner", 15,
                "1. Heat butter in a pan over medium heat.\n2. Add minced garlic and sauté until fragrant (approx 1 minute).\n3. Add leftover cooked rice and stir thoroughly to coat with garlic butter.\n4. Season with salt and black pepper, cooking for 5 minutes until heated through.",
                new String[][]{
                        {"Cooked Rice", "2", "cup"},
                        {"Butter", "2", "tbsp"},
                        {"Garlic", "3", "clove"},
                        {"Salt", "1", "tsp"}
                });

        // Recipe 6: Classic Egg Fried Rice
        addRecipe(db, "Classic Egg Fried Rice", "Dinner", 15,
                "1. Heat oil in a wok or large pan over high heat.\n2. Sauté chopped onion and garlic until fragrant.\n3. Push vegetables to the side, crack eggs into the pan and scramble quickly.\n4. Add cooked rice and soy sauce, tossing continuously for 3-4 minutes.\n5. Season with salt and serve hot.",
                new String[][]{
                        {"Cooked Rice", "2", "cup"},
                        {"Egg", "2", "unit"},
                        {"Onion", "1", "unit"},
                        {"Soy Sauce", "2", "tbsp"},
                        {"Cooking Oil", "2", "tbsp"}
                });

        // Recipe 7: Simple Tomato & Basil Pasta
        addRecipe(db, "Simple Tomato Pasta", "Dinner", 20,
                "1. Boil pasta in salted water until al dente, drain and reserve 1/4 cup water.\n2. Heat olive oil in a pan, sauté minced garlic and diced tomatoes until softened.\n3. Simmer sauce for 8 minutes until thickened; season with salt.\n4. Toss pasta into the tomato sauce with reserved water and coat evenly.",
                new String[][]{
                        {"Pasta", "200", "g"},
                        {"Tomato", "3", "unit"},
                        {"Garlic", "2", "clove"},
                        {"Olive Oil", "2", "tbsp"},
                        {"Salt", "1", "tsp"}
                });

        // Recipe 8: Rustic Potato & Onion Hash
        addRecipe(db, "Rustic Potato & Onion Hash", "Breakfast", 25,
                "1. Dice potatoes into small cubes.\n2. Heat oil in a skillet, add potatoes and cover for 8 minutes to soften.\n3. Add sliced onions and season with salt and black pepper.\n4. Fry uncovered on medium-high heat, turning occasionally until potatoes are golden and crispy.",
                new String[][]{
                        {"Potato", "3", "unit"},
                        {"Onion", "1", "unit"},
                        {"Cooking Oil", "2", "tbsp"},
                        {"Salt", "1", "tsp"}
                });

        // Recipe 9: Creamy Mashed Potatoes
        addRecipe(db, "Creamy Mashed Potatoes", "Side Dish", 20,
                "1. Peel and cube potatoes; boil in salted water until fork-tender (approx 15 mins).\n2. Drain water completely.\n3. Add warm milk, butter, and salt to the pot.\n4. Mash until smooth, fluffy, and creamy.",
                new String[][]{
                        {"Potato", "4", "unit"},
                        {"Butter", "2", "tbsp"},
                        {"Milk", "0.5", "cup"},
                        {"Salt", "1", "tsp"}
                });

        // Recipe 10: Banana Oat Pancakes
        addRecipe(db, "Banana Oat Pancakes", "Breakfast", 15,
                "1. Mash ripe bananas in a bowl with a fork until smooth.\n2. Whisk in eggs and milk.\n3. Stir in rolled oats or flour until combined into a thick batter.\n4. Cook 1/4 cup scoops on a greased pan over medium heat for 2-3 mins per side.",
                new String[][]{
                        {"Banana", "2", "unit"},
                        {"Egg", "2", "unit"},
                        {"Rolled Oats", "1", "cup"},
                        {"Milk", "0.25", "cup"}
                });

        // Recipe 11: Warm Honey Oatmeal
        addRecipe(db, "Warm Honey Oatmeal", "Breakfast", 10,
                "1. Combine oats and milk in a small saucepan.\n2. Bring to a gentle boil over medium heat, stirring frequently.\n3. Lower heat and simmer for 5 minutes until thickened.\n4. Spoon into a bowl and drizzle generously with honey.",
                new String[][]{
                        {"Rolled Oats", "1", "cup"},
                        {"Milk", "1.5", "cup"},
                        {"Honey", "2", "tbsp"}
                });

        // Recipe 12: Classic Tuna Salad
        addRecipe(db, "Classic Tuna Salad", "Lunch", 10,
                "1. Drain canned tuna and transfer to a mixing bowl.\n2. Add mayonnaise, finely diced onion, and lemon juice.\n3. Season with salt and mix thoroughly with a fork.\n4. Serve on bread or with crackers.",
                new String[][]{
                        {"Canned Tuna", "1", "can"},
                        {"Mayonnaise", "2", "tbsp"},
                        {"Onion", "0.5", "unit"},
                        {"Lemon", "1", "unit"}
                });

        // Recipe 13: Garlic Sautéed Spinach
        addRecipe(db, "Garlic Sautéed Spinach", "Side Dish", 8,
                "1. Heat olive oil in a large skillet over medium-high heat.\n2. Add sliced garlic and cook for 30 seconds until aromatic.\n3. Add fresh spinach in batches, tossing until wilted (approx 2 minutes).\n4. Sprinkle with salt and serve immediately.",
                new String[][]{
                        {"Spinach", "200", "g"},
                        {"Garlic", "2", "clove"},
                        {"Olive Oil", "1", "tbsp"},
                        {"Salt", "0.5", "tsp"}
                });

        // Recipe 14: Refreshing Cucumber & Tomato Salad
        addRecipe(db, "Cucumber & Tomato Salad", "Salad", 10,
                "1. Dice cucumber and tomatoes into bite-sized chunks.\n2. Thinly slice the onion.\n3. Toss vegetables together in a salad bowl.\n4. Drizzle with olive oil, lemon juice, and season with salt.",
                new String[][]{
                        {"Cucumber", "1", "unit"},
                        {"Tomato", "2", "unit"},
                        {"Onion", "0.5", "unit"},
                        {"Olive Oil", "2", "tbsp"},
                        {"Salt", "0.5", "tsp"}
                });

        // Recipe 15: Easy Vegetable Stir Fry
        addRecipe(db, "Easy Vegetable Stir Fry", "Dinner", 15,
                "1. Chop carrot and broccoli florets into bite-sized pieces.\n2. Heat cooking oil in a large skillet over high heat.\n3. Add minced garlic and sliced onion, cooking for 1 minute.\n4. Add broccoli and carrots, stir-frying vigorously for 5 minutes.\n5. Splash with soy sauce and serve hot.",
                new String[][]{
                        {"Carrot", "2", "unit"},
                        {"Broccoli", "1", "unit"},
                        {"Onion", "1", "unit"},
                        {"Soy Sauce", "2", "tbsp"},
                        {"Cooking Oil", "1", "tbsp"}
                });

        // Recipe 16: Crispy Seasoned Potato Wedges
        addRecipe(db, "Crispy Seasoned Potato Wedges", "Snack", 30,
                "1. Cut washed potatoes into wedges.\n2. Toss wedges in olive oil, salt, and minced garlic in a bowl.\n3. Arrange in a single layer on a baking tray or skillet.\n4. Cook at medium-high heat until crisp on the outside and tender inside.",
                new String[][]{
                        {"Potato", "3", "unit"},
                        {"Olive Oil", "2", "tbsp"},
                        {"Garlic", "2", "clove"},
                        {"Salt", "1", "tsp"}
                });

        // Recipe 17: Cheesy Egg Quesadilla
        addRecipe(db, "Cheesy Egg Quesadilla", "Lunch", 12,
                "1. Whisk eggs with salt and scramble lightly in butter, then remove.\n2. Lay tortilla in pan, cover half with shredded cheese and scrambled eggs.\n3. Fold tortilla in half.\n4. Toast both sides in skillet until cheese melts and tortilla is golden brown.",
                new String[][]{
                        {"Tortilla", "2", "unit"},
                        {"Egg", "2", "unit"},
                        {"Cheese", "50", "g"},
                        {"Butter", "1", "tbsp"}
                });

        // Recipe 18: Tomato & Cheese Open Toast
        addRecipe(db, "Tomato & Cheese Open Toast", "Snack", 8,
                "1. Toast bread slices lightly.\n2. Layer thin tomato slices over the bread.\n3. Top with slices or shreds of cheese.\n4. Broil or pan-cover until cheese melts bubble-hot. Season with salt.",
                new String[][]{
                        {"Bread", "2", "slice"},
                        {"Tomato", "1", "unit"},
                        {"Cheese", "40", "g"},
                        {"Salt", "0.5", "tsp"}
                });

        // Recipe 19: Garlic Butter Pasta
        addRecipe(db, "Garlic Butter Pasta", "Dinner", 15,
                "1. Cook pasta in boiling salted water until tender; drain.\n2. In same pot, melt butter over low heat.\n3. Sauté minced garlic for 1 minute until fragrant.\n4. Return pasta to pot, tossing vigorously with the garlic butter and salt.",
                new String[][]{
                        {"Pasta", "200", "g"},
                        {"Butter", "2", "tbsp"},
                        {"Garlic", "3", "clove"},
                        {"Salt", "1", "tsp"}
                });

        // Recipe 20: Hearty Chickpea & Lemon Salad
        addRecipe(db, "Hearty Chickpea Salad", "Salad", 10,
                "1. Rinse and drain canned chickpeas.\n2. In a bowl, combine chickpeas, diced tomato, and chopped onion.\n3. Dress with olive oil, lemon juice, and a pinch of salt.\n4. Toss well and serve chilled or room temperature.",
                new String[][]{
                        {"Canned Chickpeas", "1", "can"},
                        {"Tomato", "1", "unit"},
                        {"Onion", "0.5", "unit"},
                        {"Olive Oil", "1", "tbsp"},
                        {"Lemon", "1", "unit"},
                        {"Salt", "0.5", "tsp"}
                });
    }

    private void addRecipe(SQLiteDatabase db, String name, String category, int prepTime, String instructions, String[][] ingredients) {
        ContentValues rv = new ContentValues();
        rv.put(COLUMN_RECIPE_NAME, name);
        rv.put(COLUMN_RECIPE_CATEGORY, category);
        rv.put(COLUMN_RECIPE_PREP_TIME, prepTime);
        rv.put(COLUMN_RECIPE_INSTRUCTIONS, instructions);
        long recipeId = db.insert(TABLE_RECIPES, null, rv);

        for (String[] ing : ingredients) {
            ContentValues iv = new ContentValues();
            iv.put(COLUMN_RI_RECIPE_ID, recipeId);
            iv.put(COLUMN_RI_INGREDIENT_NAME, ing[0]);
            iv.put(COLUMN_RI_QUANTITY, Double.parseDouble(ing[1]));
            iv.put(COLUMN_RI_UNIT, ing[2]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, iv);
        }
    }

    private void seedSamplePantry(SQLiteDatabase db) {
        // Sample ingredients preloaded so app shows immediate functionality
        String[][] samplePantry = {
                {"Egg", "4", "unit", "2026-11-15", "Dairy & Eggs"},
                {"Milk", "1", "cup", "2026-11-05", "Dairy & Eggs"},
                {"Butter", "3", "tbsp", "2026-12-01", "Dairy & Eggs"},
                {"Salt", "10", "tsp", "2027-01-01", "Pantry"},
                {"Tomato", "2", "unit", "2026-11-02", "Produce"},
                {"Bread", "6", "slice", "2026-11-04", "Bakery"}
        };

        for (String[] item : samplePantry) {
            ContentValues v = new ContentValues();
            v.put(COLUMN_PANTRY_NAME, item[0]);
            v.put(COLUMN_PANTRY_QUANTITY, Double.parseDouble(item[1]));
            v.put(COLUMN_PANTRY_UNIT, item[2]);
            v.put(COLUMN_PANTRY_EXPIRY, item[3]);
            v.put(COLUMN_PANTRY_CATEGORY, item[4]);
            db.insert(TABLE_PANTRY, null, v);
        }
    }
}
