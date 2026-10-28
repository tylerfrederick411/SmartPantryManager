package com.richfield.smartpantry.model;

import java.io.Serializable;

public class RecipeIngredient implements Serializable {
    private long id;
    private long recipeId;
    private String ingredientName;
    private double quantity;
    private String unit;

    public RecipeIngredient() {
    }

    public RecipeIngredient(long id, long recipeId, String ingredientName, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
    }

    public RecipeIngredient(String ingredientName, double quantity, String unit) {
        this(-1, -1, ingredientName, quantity, unit);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getFormattedQuantity() {
        if (quantity == (long) quantity) {
            return String.valueOf((long) quantity);
        } else {
            return String.format("%.1f", quantity);
        }
    }
}
