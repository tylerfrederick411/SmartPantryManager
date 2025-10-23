package com.richfield.smartpantry.model;

import java.io.Serializable;

/**
 * Model representing an ingredient stored in the user's pantry.
 * Implements Serializable to facilitate passing data via Intents.
 */
public class PantryItem implements Serializable {
    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiryDate; // ISO-8601 string: YYYY-MM-DD
    private String category;

    public PantryItem() {
    }

    public PantryItem(long id, String name, double quantity, String unit, String expiryDate, String category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.category = category;
    }

    public PantryItem(String name, double quantity, String unit, String expiryDate, String category) {
        this(-1, name, quantity, unit, expiryDate, category);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Formats the quantity nicely (e.g. "2" instead of "2.0", but "1.5" stays "1.5").
     */
    public String getFormattedQuantity() {
        if (quantity == (long) quantity) {
            return String.format("%d", (long) quantity);
        } else {
            return String.format("%.2f", quantity);
        }
    }
}
