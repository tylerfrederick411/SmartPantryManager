package com.richfield.smartpantry.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.richfield.smartpantry.R;
import com.richfield.smartpantry.database.DatabaseHelper;
import com.richfield.smartpantry.model.PantryItem;

import java.util.Calendar;
import java.util.Locale;

public class AddEditIngredientActivity extends AppCompatActivity {

    private EditText etName, etQuantity, etExpiryDate;
    private Spinner spinnerUnit, spinnerCategory;
    private Button btnSave, btnPickDate;
    private TextView tvScreenTitle;

    private DatabaseHelper dbHelper;
    private long editingItemId = -1;
    private PantryItem existingItem;

    private final String[] UNITS = {"unit", "g", "kg", "ml", "l", "cup", "tbsp", "tsp", "can", "slice", "clove"};
    private final String[] CATEGORIES = {"Produce", "Dairy & Eggs", "Meat & Seafood", "Pantry & Grains", "Bakery", "Condiments", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ingredient Details");
        }

        dbHelper = DatabaseHelper.getInstance(this);

        etName = findViewById(R.id.etIngredientName);
        etQuantity = findViewById(R.id.etIngredientQuantity);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSaveIngredient);
        btnPickDate = findViewById(R.id.btnPickDate);
        tvScreenTitle = findViewById(R.id.tvFormTitle);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, UNITS);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        btnPickDate.setOnClickListener(v -> showDatePicker());

        if (getIntent().hasExtra("EXTRA_ITEM_ID")) {
            editingItemId = getIntent().getLongExtra("EXTRA_ITEM_ID", -1);
            if (editingItemId != -1) {
                existingItem = dbHelper.getPantryItem(editingItemId);
                if (existingItem != null) {
                    populateFields(existingItem);
                }
            }
        }

        btnSave.setOnClickListener(v -> saveItem());
    }

    private void populateFields(PantryItem item) {
        tvScreenTitle.setText("Edit Ingredient");
        etName.setText(item.getName());
        etQuantity.setText(item.getFormattedQuantity());
        etExpiryDate.setText(item.getExpiryDate() != null ? item.getExpiryDate() : "");

        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equalsIgnoreCase(item.getUnit())) {
                spinnerUnit.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equalsIgnoreCase(item.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog picker = new DatePickerDialog(this, (view, y, m, d) -> {
            String formatted = String.format(Locale.ROOT, "%04d-%02d-%02d", y, m + 1, d);
            etExpiryDate.setText(formatted);
        }, year, month, day);
        picker.show();
    }

    private void saveItem() {
        String name = etName.getText().toString().trim();
        String qtyStr = etQuantity.getText().toString().trim();
        String expiry = etExpiryDate.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Ingredient name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(qtyStr)) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(qtyStr);
            if (quantity <= 0) {
                etQuantity.setError("Quantity must be greater than zero");
                etQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError("Please enter a valid number");
            etQuantity.requestFocus();
            return;
        }

        if (existingItem != null) {
            existingItem.setName(name);
            existingItem.setQuantity(quantity);
            existingItem.setUnit(unit);
            existingItem.setExpiryDate(expiry);
            existingItem.setCategory(category);
            dbHelper.updatePantryItem(existingItem);
            Toast.makeText(this, "Updated " + name, Toast.LENGTH_SHORT).show();
        } else {
            PantryItem newItem = new PantryItem(name, quantity, unit, expiry, category);
            dbHelper.insertPantryItem(newItem);
            Toast.makeText(this, "Added " + name + " to pantry", Toast.LENGTH_SHORT).show();
        }

        finish();
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
