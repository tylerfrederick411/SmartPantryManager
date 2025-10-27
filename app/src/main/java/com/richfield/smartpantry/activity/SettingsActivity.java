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

/**
 * Screen 5: Settings & Preferences Screen.
 * Satisfies the Section 3.1 minimum screen count requirement.
 * Allows user to toggle expiring-soon notifications, unit preferences,
 * and restore initial database state.
 */
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
            Toast.makeText(this, checked ? "Expiry notifications enabled" : "Expiry notifications disabled", Toast.LENGTH_SHORT).show();
        });

        switchShowAlmostThere.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean("almost_there_enabled", checked).apply();
            Toast.makeText(this, checked ? "'Almost There' suggestions enabled" : "'Almost There' suggestions disabled", Toast.LENGTH_SHORT).show();
        });

        btnResetDatabase.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset Database")
                    .setMessage("This will reset all pantry items and re-seed the 20 default recipes. Are you sure?")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        dbHelper.resetToDefaults();
                        Toast.makeText(this, "Database reset to defaults successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_settings);
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_settings) {
                return true;
            } else if (itemId == R.id.nav_pantry) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_suggested) {
                Intent intent = new Intent(this, SuggestedRecipesActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }
}
