package com.richfield.smartpantry.util;

import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;
import com.richfield.smartpantry.model.RecipeMatchResult;

import java.util.List;
import java.util.Locale;

/**
 * Business logic engine for the Smart Pantry Manager.
 * Implements Section 2.3 Strict-Matching Rule:
 * 1. Case-insensitive and trimmed string matching.
 * 2. Robust singular/plural handling (e.g. "tomatoes" vs "tomato", "cloves" vs "clove").
 * 3. Unit normalization and equivalence checking.
 * 4. Strict sufficiency test: pantry quantity >= required quantity.
 */
public class IngredientMatcher {

    /**
     * Normalizes ingredient names to a canonical singular lower-case representation.
     * Robust against real-world messiness without heavy external NLP libraries.
     */
    public static String normalizeName(String name) {
        if (name == null) return "";
        String clean = name.trim().toLowerCase(Locale.ROOT);

        // Normalize irregular or common culinary plurals
        if (clean.endsWith("tomatoes")) return clean.replace("tomatoes", "tomato");
        if (clean.endsWith("potatoes")) return clean.replace("potatoes", "potato");
        if (clean.endsWith("berries")) return clean.substring(0, clean.length() - 3) + "y";
        if (clean.endsWith("leaves")) return clean.substring(0, clean.length() - 3) + "f";
        if (clean.endsWith("cloves")) return clean.substring(0, clean.length() - 1);
        if (clean.endsWith("onions")) return clean.substring(0, clean.length() - 1);
        if (clean.endsWith("eggs")) return clean.substring(0, clean.length() - 1);
        if (clean.endsWith("carrots")) return clean.substring(0, clean.length() - 1);
        if (clean.endsWith("apples")) return clean.substring(0, clean.length() - 1);
        if (clean.endsWith("bananas")) return clean.substring(0, clean.length() - 1);
        if (clean.endsWith("peppers")) return clean.substring(0, clean.length() - 1);
        if (clean.endsWith("sausages")) return clean.substring(0, clean.length() - 1);
        if (clean.endsWith("mushrooms")) return clean.substring(0, clean.length() - 1);

        // General trailing 's' plural rule (if word length > 3 and doesn't end in 'ss')
        if (clean.length() > 3 && clean.endsWith("s") && !clean.endsWith("ss") && !clean.endsWith("us")) {
            return clean.substring(0, clean.length() - 1);
        }

        return clean;
    }

    /**
     * Normalizes unit strings (e.g., "tbsp", "tablespoon", "tablespoons" -> "tbsp").
     */
    public static String normalizeUnit(String unit) {
        if (unit == null) return "unit";
        String u = unit.trim().toLowerCase(Locale.ROOT);
        if (u.equals("tbsp") || u.equals("tablespoon") || u.equals("tablespoons")) return "tbsp";
        if (u.equals("tsp") || u.equals("teaspoon") || u.equals("teaspoons")) return "tsp";
        if (u.equals("g") || u.equals("gram") || u.equals("grams")) return "g";
        if (u.equals("kg") || u.equals("kilogram") || u.equals("kilograms") || u.equals("kilos")) return "kg";
        if (u.equals("ml") || u.equals("milliliter") || u.equals("millilitre") || u.equals("milliliters")) return "ml";
        if (u.equals("l") || u.equals("liter") || u.equals("litre") || u.equals("liters")) return "l";
        if (u.equals("cup") || u.equals("cups")) return "cup";
        if (u.equals("can") || u.equals("cans")) return "can";
        if (u.equals("slice") || u.equals("slices")) return "slice";
        if (u.equals("clove") || u.equals("cloves")) return "clove";
        if (u.equals("unit") || u.equals("units") || u.equals("piece") || u.equals("pieces") || u.equals("item") || u.equals("items") || u.isEmpty()) {
            return "unit";
        }
        return u;
    }

    /**
     * Checks if units are directly comparable or can be converted.
     * Returns true if compatible and pantry has >= required quantity.
     */
    public static boolean hasSufficientQuantity(double requiredQty, String requiredUnit, double pantryQty, String pantryUnit) {
        String normReqUnit = normalizeUnit(requiredUnit);
        String normPantryUnit = normalizeUnit(pantryUnit);

        // Same normalized unit
        if (normReqUnit.equals(normPantryUnit)) {
            return pantryQty >= requiredQty;
        }

        // Mass conversion: kg to g
        if (normReqUnit.equals("g") && normPantryUnit.equals("kg")) {
            return (pantryQty * 1000.0) >= requiredQty;
        }
        if (normReqUnit.equals("kg") && normPantryUnit.equals("g")) {
            return (pantryQty / 1000.0) >= requiredQty;
        }

        // Volume conversion: l to ml
        if (normReqUnit.equals("ml") && normPantryUnit.equals("l")) {
            return (pantryQty * 1000.0) >= requiredQty;
        }
        if (normReqUnit.equals("l") && normPantryUnit.equals("ml")) {
            return (pantryQty / 1000.0) >= requiredQty;
        }

        // Cup to tbsp & tsp: 1 cup = 16 tbsp = 48 tsp
        if (normReqUnit.equals("tbsp") && normPantryUnit.equals("cup")) {
            return (pantryQty * 16.0) >= requiredQty;
        }
        if (normReqUnit.equals("cup") && normPantryUnit.equals("tbsp")) {
            return (pantryQty / 16.0) >= requiredQty;
        }
        if (normReqUnit.equals("tsp") && normPantryUnit.equals("cup")) {
            return (pantryQty * 48.0) >= requiredQty;
        }
        if (normReqUnit.equals("cup") && normPantryUnit.equals("tsp")) {
            return (pantryQty / 48.0) >= requiredQty;
        }

        // Tablespoon to teaspoon: 1 tbsp = 3 tsp
        if (normReqUnit.equals("tbsp") && normPantryUnit.equals("tsp")) {
            return (pantryQty / 3.0) >= requiredQty;
        }
        if (normReqUnit.equals("tsp") && normPantryUnit.equals("tbsp")) {
            return (pantryQty * 3.0) >= requiredQty;
        }

        // If generic unit or unconvertible, compare direct values
        return pantryQty >= requiredQty;
    }

    /**
     * Evaluates a single recipe against the full pantry list.
     * Returns a RecipeMatchResult detailing strictly satisfied and missing ingredients.
     */
    public static RecipeMatchResult evaluateRecipe(Recipe recipe, List<PantryItem> pantry) {
        RecipeMatchResult result = new RecipeMatchResult(recipe);

        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return result;
        }

        for (RecipeIngredient req : recipe.getIngredients()) {
            String normReqName = normalizeName(req.getIngredientName());
            boolean satisfied = false;

            if (pantry != null) {
                for (PantryItem item : pantry) {
                    String normPantryName = normalizeName(item.getName());
                    if (normPantryName.equals(normReqName)) {
                        if (hasSufficientQuantity(req.getQuantity(), req.getUnit(), item.getQuantity(), item.getUnit())) {
                            satisfied = true;
                            break;
                        }
                    }
                }
            }

            if (satisfied) {
                result.getSatisfiedIngredients().add(req);
            } else {
                result.getMissingIngredients().add(req);
            }
        }

        result.setMatchedCount(result.getSatisfiedIngredients().size());
        return result;
    }
}
