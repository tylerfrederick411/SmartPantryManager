package com.richfield.smartpantry.util;

import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;
import com.richfield.smartpantry.model.RecipeMatchResult;

import java.util.List;
import java.util.Locale;

public class IngredientMatcher {

    // Helper to clean up plurals and lowercase names for comparing
    public static String normalizeName(String name) {
        if (name == null) return "";
        String clean = name.trim().toLowerCase(Locale.ROOT);

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
        if (clean.endsWith("mushrooms")) return clean.substring(0, clean.length() - 1);

        if (clean.length() > 3 && clean.endsWith("s") && !clean.endsWith("ss")) {
            return clean.substring(0, clean.length() - 1);
        }

        return clean;
    }

    // Helper to standardize common unit words
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
        if (u.equals("unit") || u.equals("units") || u.equals("piece") || u.equals("pieces") || u.equals("item") || u.isEmpty()) {
            return "unit";
        }
        return u;
    }

    // Checks if the pantry has enough quantity and converts units if needed
    public static boolean hasSufficientQuantity(double requiredQty, String requiredUnit, double pantryQty, String pantryUnit) {
        String reqUnit = normalizeUnit(requiredUnit);
        String pUnit = normalizeUnit(pantryUnit);

        if (reqUnit.equals(pUnit)) {
            return pantryQty >= requiredQty;
        }

        // mass conversions
        if (reqUnit.equals("g") && pUnit.equals("kg")) {
            return (pantryQty * 1000.0) >= requiredQty;
        }
        if (reqUnit.equals("kg") && pUnit.equals("g")) {
            return (pantryQty / 1000.0) >= requiredQty;
        }

        // volume conversions
        if (reqUnit.equals("ml") && pUnit.equals("l")) {
            return (pantryQty * 1000.0) >= requiredQty;
        }
        if (reqUnit.equals("l") && pUnit.equals("ml")) {
            return (pantryQty / 1000.0) >= requiredQty;
        }

        // cup and spoon conversions
        if (reqUnit.equals("tbsp") && pUnit.equals("cup")) {
            return (pantryQty * 16.0) >= requiredQty;
        }
        if (reqUnit.equals("cup") && pUnit.equals("tbsp")) {
            return (pantryQty / 16.0) >= requiredQty;
        }
        if (reqUnit.equals("tsp") && pUnit.equals("cup")) {
            return (pantryQty * 48.0) >= requiredQty;
        }
        if (reqUnit.equals("cup") && pUnit.equals("tsp")) {
            return (pantryQty / 48.0) >= requiredQty;
        }
        if (reqUnit.equals("tbsp") && pUnit.equals("tsp")) {
            return (pantryQty / 3.0) >= requiredQty;
        }
        if (reqUnit.equals("tsp") && pUnit.equals("tbsp")) {
            return (pantryQty * 3.0) >= requiredQty;
        }

        return pantryQty >= requiredQty;
    }

    // Compares a recipe against what is in the pantry
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
