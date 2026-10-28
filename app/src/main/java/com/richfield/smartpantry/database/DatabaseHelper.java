package com.richfield.smartpantry.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;
import com.richfield.smartpantry.util.IngredientMatcher;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // table names
    public static final String TABLE_PANTRY = "pantry";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    public static final String COLUMN_ID = "_id";

    // pantry columns
    public static final String COLUMN_PANTRY_NAME = "name";
    public static final String COLUMN_PANTRY_QUANTITY = "quantity";
    public static final String COLUMN_PANTRY_UNIT = "unit";
    public static final String COLUMN_PANTRY_EXPIRY = "expiry_date";
    public static final String COLUMN_PANTRY_CATEGORY = "category";

    // recipe columns
    public static final String COLUMN_RECIPE_NAME = "name";
    public static final String COLUMN_RECIPE_CATEGORY = "category";
    public static final String COLUMN_RECIPE_PREP_TIME = "prep_time_minutes";
    public static final String COLUMN_RECIPE_INSTRUCTIONS = "instructions";

    // ingredient columns
    public static final String COLUMN_RI_RECIPE_ID = "recipe_id";
    public static final String COLUMN_RI_INGREDIENT_NAME = "ingredient_name";
    public static final String COLUMN_RI_QUANTITY = "quantity";
    public static final String COLUMN_RI_UNIT = "unit";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPantry = "CREATE TABLE " + TABLE_PANTRY + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PANTRY_NAME + " TEXT NOT NULL, " +
                COLUMN_PANTRY_QUANTITY + " REAL NOT NULL, " +
                COLUMN_PANTRY_UNIT + " TEXT NOT NULL, " +
                COLUMN_PANTRY_EXPIRY + " TEXT, " +
                COLUMN_PANTRY_CATEGORY + " TEXT);";
        db.execSQL(createPantry);

        String createRecipes = "CREATE TABLE " + TABLE_RECIPES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RECIPE_NAME + " TEXT NOT NULL, " +
                COLUMN_RECIPE_CATEGORY + " TEXT, " +
                COLUMN_RECIPE_PREP_TIME + " INTEGER, " +
                COLUMN_RECIPE_INSTRUCTIONS + " TEXT NOT NULL);";
        db.execSQL(createRecipes);

        String createRecipeIngredients = "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COLUMN_RI_INGREDIENT_NAME + " TEXT NOT NULL, " +
                COLUMN_RI_QUANTITY + " REAL NOT NULL, " +
                COLUMN_RI_UNIT + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COLUMN_ID + ") ON DELETE CASCADE);";
        db.execSQL(createRecipeIngredients);

        seedRecipes(db);
        seedSamplePantry(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // Insert new pantry item
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

    // Get item by id
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

    // Get all pantry items
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

    // Update item
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

    // Delete item
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

    // Fetch all recipes with their ingredients
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

    // Deduct ingredients when cooking a recipe
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

    // Reset database back to default state
    public void resetToDefaults() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECIPE_INGREDIENTS, null, null);
        db.delete(TABLE_RECIPES, null, null);
        db.delete(TABLE_PANTRY, null, null);
        seedRecipes(db);
        seedSamplePantry(db);
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Classic Scrambled Eggs", "Breakfast", 10,
                "1. Crack eggs into a bowl, add milk and a pinch of salt.\n2. Whisk thoroughly until smooth.\n3. Melt butter in a non-stick pan over medium heat.\n4. Pour in eggs and gently stir until cooked.",
                new String[][]{
                        {"Egg", "3", "unit"},
                        {"Butter", "1", "tbsp"},
                        {"Milk", "2", "tbsp"},
                        {"Salt", "1", "tsp"}
                });

        addRecipe(db, "Tomato & Herb Omelette", "Breakfast", 12,
                "1. Whisk eggs with salt in a bowl.\n2. Heat butter or oil in a pan.\n3. Sauté diced tomato and onion for 2 minutes.\n4. Pour eggs over vegetables and cook until golden.",
                new String[][]{
                        {"Egg", "2", "unit"},
                        {"Tomato", "1", "unit"},
                        {"Onion", "1", "unit"},
                        {"Cooking Oil", "1", "tbsp"},
                        {"Salt", "1", "tsp"}
                });

        addRecipe(db, "Golden French Toast", "Breakfast", 15,
                "1. Whisk eggs and milk in a bowl.\n2. Dip bread slices on both sides.\n3. Melt butter in a pan.\n4. Fry each slice 3 minutes per side until golden brown.",
                new String[][]{
                        {"Bread", "4", "slice"},
                        {"Egg", "2", "unit"},
                        {"Milk", "0.5", "cup"},
                        {"Butter", "1", "tbsp"}
                });

        addRecipe(db, "Crispy Grilled Cheese Sandwich", "Lunch", 10,
                "1. Butter one side of each slice of bread.\n2. Place cheese between bread slices.\n3. Toast in skillet on medium heat until cheese melts.",
                new String[][]{
                        {"Bread", "2", "slice"},
                        {"Cheese", "50", "g"},
                        {"Butter", "1", "tbsp"}
                });

        addRecipe(db, "Quick Garlic Butter Rice", "Dinner", 15,
                "1. Heat butter in a pan.\n2. Add minced garlic and cook 1 minute.\n3. Add cooked rice and season with salt.\n4. Stir-fry for 4 minutes until warm.",
                new String[][]{
                        {"Cooked Rice", "2", "cup"},
                        {"Butter", "2", "tbsp"},
                        {"Garlic", "3", "clove"},
                        {"Salt", "1", "tsp"}
                });

        addRecipe(db, "Classic Egg Fried Rice", "Dinner", 15,
                "1. Heat oil in a pan.\n2. Sauté chopped onion and garlic.\n3. Scramble eggs in pan, then add rice and soy sauce.\n4. Stir-fry 3 minutes and serve.",
                new String[][]{
                        {"Cooked Rice", "2", "cup"},
                        {"Egg", "2", "unit"},
                        {"Onion", "1", "unit"},
                        {"Soy Sauce", "2", "tbsp"},
                        {"Cooking Oil", "2", "tbsp"}
                });

        addRecipe(db, "Simple Tomato Pasta", "Dinner", 20,
                "1. Boil pasta in salted water and drain.\n2. Heat olive oil, cook minced garlic and chopped tomatoes into a sauce.\n3. Toss pasta in sauce and season with salt.",
                new String[][]{
                        {"Pasta", "200", "g"},
                        {"Tomato", "3", "unit"},
                        {"Garlic", "2", "clove"},
                        {"Olive Oil", "2", "tbsp"},
                        {"Salt", "1", "tsp"}
                });

        addRecipe(db, "Rustic Potato & Onion Hash", "Breakfast", 25,
                "1. Dice potatoes into small cubes.\n2. Heat oil in skillet, fry potatoes with lid on for 8 minutes.\n3. Add sliced onions and salt, cook uncovered until crispy.",
                new String[][]{
                        {"Potato", "3", "unit"},
                        {"Onion", "1", "unit"},
                        {"Cooking Oil", "2", "tbsp"},
                        {"Salt", "1", "tsp"}
                });

        addRecipe(db, "Creamy Mashed Potatoes", "Side Dish", 20,
                "1. Peel and cube potatoes; boil in salted water for 15 minutes.\n2. Drain water, add warm milk, butter, and salt.\n3. Mash until smooth.",
                new String[][]{
                        {"Potato", "4", "unit"},
                        {"Butter", "2", "tbsp"},
                        {"Milk", "0.5", "cup"},
                        {"Salt", "1", "tsp"}
                });

        addRecipe(db, "Banana Oat Pancakes", "Breakfast", 15,
                "1. Mash bananas in a bowl.\n2. Whisk in eggs and milk.\n3. Stir in oats to make batter.\n4. Cook 1/4 cup scoops on pan 2 minutes each side.",
                new String[][]{
                        {"Banana", "2", "unit"},
                        {"Egg", "2", "unit"},
                        {"Rolled Oats", "1", "cup"},
                        {"Milk", "0.25", "cup"}
                });

        addRecipe(db, "Warm Honey Oatmeal", "Breakfast", 10,
                "1. Combine oats and milk in a pot.\n2. Bring to a simmer, stirring frequently for 5 minutes.\n3. Pour into bowl and drizzle honey.",
                new String[][]{
                        {"Rolled Oats", "1", "cup"},
                        {"Milk", "1.5", "cup"},
                        {"Honey", "2", "tbsp"}
                });

        addRecipe(db, "Classic Tuna Salad", "Lunch", 10,
                "1. Drain canned tuna and place in a bowl.\n2. Add mayonnaise, diced onion, and lemon juice.\n3. Season with salt and mix well.",
                new String[][]{
                        {"Canned Tuna", "1", "can"},
                        {"Mayonnaise", "2", "tbsp"},
                        {"Onion", "0.5", "unit"},
                        {"Lemon", "1", "unit"}
                });

        addRecipe(db, "Garlic Sautéed Spinach", "Side Dish", 8,
                "1. Heat olive oil in a pan.\n2. Sauté garlic 30 seconds.\n3. Add fresh spinach and cook 2 minutes until wilted.\n4. Sprinkle salt.",
                new String[][]{
                        {"Spinach", "200", "g"},
                        {"Garlic", "2", "clove"},
                        {"Olive Oil", "1", "tbsp"},
                        {"Salt", "0.5", "tsp"}
                });

        addRecipe(db, "Cucumber & Tomato Salad", "Salad", 10,
                "1. Chop cucumber and tomatoes into bite sizes.\n2. Slice onion.\n3. Toss in bowl with olive oil, lemon juice, and salt.",
                new String[][]{
                        {"Cucumber", "1", "unit"},
                        {"Tomato", "2", "unit"},
                        {"Onion", "0.5", "unit"},
                        {"Olive Oil", "2", "tbsp"},
                        {"Salt", "0.5", "tsp"}
                });

        addRecipe(db, "Easy Vegetable Stir Fry", "Dinner", 15,
                "1. Chop carrot and broccoli into pieces.\n2. Heat oil in skillet, sauté garlic and onion.\n3. Add carrots and broccoli, stir-fry 5 minutes with soy sauce.",
                new String[][]{
                        {"Carrot", "2", "unit"},
                        {"Broccoli", "1", "unit"},
                        {"Onion", "1", "unit"},
                        {"Soy Sauce", "2", "tbsp"},
                        {"Cooking Oil", "1", "tbsp"}
                });

        addRecipe(db, "Crispy Seasoned Potato Wedges", "Snack", 30,
                "1. Cut potatoes into wedges.\n2. Toss in olive oil, salt, and minced garlic.\n3. Fry or bake until crispy outside and tender inside.",
                new String[][]{
                        {"Potato", "3", "unit"},
                        {"Olive Oil", "2", "tbsp"},
                        {"Garlic", "2", "clove"},
                        {"Salt", "1", "tsp"}
                });

        addRecipe(db, "Cheesy Egg Quesadilla", "Lunch", 12,
                "1. Scramble eggs lightly in butter.\n2. Place tortilla in pan, fill with cheese and scrambled eggs.\n3. Fold and toast both sides until cheese melts.",
                new String[][]{
                        {"Tortilla", "2", "unit"},
                        {"Egg", "2", "unit"},
                        {"Cheese", "50", "g"},
                        {"Butter", "1", "tbsp"}
                });

        addRecipe(db, "Tomato & Cheese Open Toast", "Snack", 8,
                "1. Toast bread lightly.\n2. Top with sliced tomato and cheese.\n3. Broil or pan-toast until cheese melts, sprinkle salt.",
                new String[][]{
                        {"Bread", "2", "slice"},
                        {"Tomato", "1", "unit"},
                        {"Cheese", "40", "g"},
                        {"Salt", "0.5", "tsp"}
                });

        addRecipe(db, "Garlic Butter Pasta", "Dinner", 15,
                "1. Boil pasta and drain.\n2. Melt butter in pot and cook minced garlic 1 minute.\n3. Toss pasta in garlic butter and season with salt.",
                new String[][]{
                        {"Pasta", "200", "g"},
                        {"Butter", "2", "tbsp"},
                        {"Garlic", "3", "clove"},
                        {"Salt", "1", "tsp"}
                });

        addRecipe(db, "Hearty Chickpea Salad", "Salad", 10,
                "1. Drain chickpeas into a bowl.\n2. Add diced tomato and chopped onion.\n3. Drizzle with olive oil, lemon juice, and salt.",
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
        String[][] samplePantry = {
                {"Egg", "4", "unit", "2026-11-15", "Dairy & Eggs"},
                {"Milk", "1", "cup", "2026-11-05", "Dairy & Eggs"},
                {"Butter", "3", "tbsp", "2026-12-01", "Dairy & Eggs"},
                {"Salt", "10", "tsp", "2027-01-01", "Pantry"},
                {"Tomato", "2", "unit", "2026-11-02", "Produce"},
                {"Bread", "6", "slice", "2026-11-04", "Bakery"},
                {"Onion", "2", "unit", "2026-11-20", "Produce"},
                {"Cooking Oil", "5", "tbsp", "2027-01-01", "Pantry"},
                {"Cheese", "100", "g", "2026-11-10", "Dairy & Eggs"}
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
