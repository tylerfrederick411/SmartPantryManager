package com.richfield.smartpantry.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    public boolean isStrictMatch() {
        return totalRequired > 0 && missingIngredients.isEmpty();
    }

    public boolean isAlmostThere() {
        return missingIngredients.size() == 1;
    }
}
