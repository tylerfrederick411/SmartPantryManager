package com.richfield.smartpantry.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the strict evaluation of a recipe against the current pantry.
 * Used to classify recipes into "Strict Matches" (0 missing) or "Almost There" (1 missing).
 */
public class RecipeMatchResult implements Serializable {
    private Recipe recipe;
    private int totalRequired;
    private int matchedCount;
    private List<RecipeIngredient> missingIngredients;
    private List<RecipeIngredient> satisfiedIngredients;

    public RecipeMatchResult(Recipe recipe) {
        this.recipe = recipe;
        this.totalRequired = recipe.getIngredients() != null ? recipe.getIngredients().size() : 0;
        this.matchedCount = 0;
        this.missingIngredients = new ArrayList<>();
        this.satisfiedIngredients = new ArrayList<>();
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public int getTotalRequired() {
        return totalRequired;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(int matchedCount) {
        this.matchedCount = matchedCount;
    }

    public List<RecipeIngredient> getMissingIngredients() {
        return missingIngredients;
    }

    public List<RecipeIngredient> getSatisfiedIngredients() {
        return satisfiedIngredients;
    }

    public int getMissingCount() {
        return missingIngredients.size();
    }

    /**
     * The core rule: true if and only if EVERY required ingredient is satisfied.
     */
    public boolean isStrictMatch() {
        return totalRequired > 0 && missingIngredients.isEmpty();
    }

    /**
     * Bonus stretch rule: missing exactly 1 ingredient.
     */
    public boolean isAlmostThere() {
        return missingIngredients.size() == 1;
    }
}
