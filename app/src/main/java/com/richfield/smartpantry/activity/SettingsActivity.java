package com.richfield.smartpantry.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.richfield.smartpantry.R;
import com.richfield.smartpantry.database.DatabaseHelper;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchExpiryAlerts, switchShowAlmostThere;
    private Button btnResetDatabase;
    private BottomNavigationView bottomNav;
    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
        }

        dbHelper = DatabaseHelper.getInstance(this);
        prefs = getSharedPreferences("SmartPantryPrefs", MODE_PRIVATE);

        switchExpiryAlerts = findViewById(R.id.switchExpiryAlerts);
        switchShowAlmostThere = findViewById(R.id.switchShowAlmostThere);
        btnResetDatabase = findViewById(R.id.btnResetDatabase);
        bottomNav = findViewById(R.id.bottomNavigation);

        switchExpiryAlerts.setChecked(prefs.getBoolean("alerts_enabled", true));
        switchShowAlmostThere.setChecked(prefs.getBoolean("almost_there_enabled", true));

        switchExpiryAlerts.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean("alerts_enabled", checked).apply();
            Toast.makeText(this, checked ? "Alerts enabled" : "Alerts disabled", Toast.LENGTH_SHORT).show();
        });

        switchShowAlmostThere.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean("almost_there_enabled", checked).apply();
        });

        btnResetDatabase.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset to Default?")
                    .setMessage("This will restore the 20 default recipes and sample ingredients.")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        dbHelper.resetToDefaults();
                        Toast.makeText(this, "Database reset to defaults", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_settings);
    }

    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_settings) {
                return true;
            } else if (id == R.id.nav_pantry) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_suggested) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}
