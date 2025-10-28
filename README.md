# Smart Pantry Manager

**Mobile App Development 700 (MAD700) - Practical Assessment**  
**Institution:** Richfield Graduate Institute of Technology  
**Language:** Pure Java (JDK 17 / Android SDK 34)  
**Architecture:** Model-View-Adapter (MVA) with SQLite Persistence  

---

## 1. Project Concept & Overview
Smart Pantry Manager is a native Android application engineered to combat domestic food waste. Domestic households generate substantial food waste primarily due to forgotten perishables and lack of spontaneous culinary knowledge. 

Smart Pantry Manager resolves this by allowing users to log the ingredients they genuinely have at home and employing a **Strict-Matching Algorithm**. Unlike generic recipe apps that demand extensive grocery shopping trips, this application *strictly* suggests recipes where **100% of required ingredients are already present in the user's pantry in sufficient quantity**.

---

## 2. Core Business Logic: The Strict-Matching Engine
As defined in **Section 2.3** of the assignment brief, strict matching is the single most critical piece of business logic:
- **Strict Invariant:** If a recipe requires 5 ingredients and the pantry contains 4, that recipe is **strictly excluded** from the "Ready to Cook" suggestions.
- **Quantity & Unit Sufficiency:** An ingredient only counts as satisfied if `pantryQuantity >= recipeRequiredQuantity` considering unit conversions (e.g., grams to kilograms, millilitres to litres, tablespoons to teaspoons).
- **Robust Canonical Normalization:** The engine normalizes trivial string discrepancies (case-insensitivity, whitespace trimming, and English pluralization rules like `tomato` vs `tomatoes`, `potato` vs `potatoes`, `onion` vs `onions`, `egg` vs `eggs`, `clove` vs `cloves`).
- **Bonus Stretch Feature ("Almost There"):** Features a dedicated view listing recipes where exactly **one** ingredient is missing, allowing users to make an informed, minimal grocery choice without compromising the strict matching invariant.

---

## 3. Database Choice & Technical Justification
The application utilizes **SQLite via Android's native `SQLiteOpenHelper` framework** for persistent local storage, satisfying **Section 3.2**:

### Academic & Technical Justification:
1. **Offline-First Resilience:** Pantry inventory management occurs primarily in kitchens and pantries where cellular or Wi-Fi connectivity may be intermittent. An embedded SQLite database guarantees zero-latency access and uninterrupted offline availability.
2. **Zero Overhead & Privacy:** Unlike cloud backends (Firebase, Supabase, PostgreSQL) which require API keys, external network calls, network error fallbacks, and authentication setups, SQLite stores all records in a self-contained `.db` file within the application's protected sandbox (`/data/data/com.richfield.smartpantry/databases/`).
3. **Structured Relational Integrity:** The relational schema cleanly models the 1-to-many relationship between recipes and recipe ingredients via Foreign Key cascades (`recipes._id` -> `recipe_ingredients.recipe_id`).
4. **Full CRUD Compliance:** The app provides complete Create, Read, Update, and Delete operations that persist permanently across application reboots and device restarts.

---

## 4. Application Architecture & Screens
The application features five (5) distinct, highly responsive Activities complying with **Section 3.1**:

| Screen # | Activity Name | Description & Key Functionality |
| :--- | :--- | :--- |
| **Screen 1** | `MainActivity` | Displays current pantry inventory in a `RecyclerView`. Floating action button launches creation. Includes item editing and alert-dialog deletion. |
| **Screen 2** | `AddEditIngredientActivity` | Form for creating or modifying ingredients. Features rigorous input validation (non-empty name, positive quantity, unit selection) and interactive `DatePickerDialog`. |
| **Screen 3** | `SuggestedRecipesActivity` | Runs the `IngredientMatcher` engine across the SQLite database. Displays zero-state feedback when no recipes match, and provides toggleable views for "Strict Matches" vs "Almost There". |
| **Screen 4** | `RecipeDetailActivity` | Detailed recipe view with prep time, step-by-step instructions, and an interactive ingredients checklist comparing required amounts with current pantry stock. Includes an "I Cooked This!" action that decrements used ingredients from the SQLite database. |
| **Screen 5** | `SettingsActivity` | Allows toggling expiry alerts, configuring default measurement preferences, and provides a one-tap SQLite factory reset button to restore default sample pantry items and 20 seeded recipes. |

---

## 5. Setup & Run Instructions

### Prerequisites:
- Android Studio Hedgehog / Iguana / Jellyfish or newer.
- JDK 17 (bundled with Android Studio).
- Android SDK Platform 34 (Android 14) and Android Emulator (API 26+) or physical Android device with USB debugging enabled.

### Step-by-Step Execution:
1. **Open Project:** Launch Android Studio, select **Open**, and browse to this directory (`SmartPantryManager`).
2. **Gradle Sync:** Allow Android Studio to automatically sync and download Gradle dependencies.
3. **Run on Device / Emulator:**
   - Select an Android Virtual Device (AVD) or connected physical device from the top toolbar device selector.
   - Click the green **Run** button (or press `Shift + F10`).
4. **Initial Data Seeding:** On first launch, the `DatabaseHelper.onCreate()` lifecycle method automatically seeds 20 diverse recipes and initial sample pantry items.

---

## 6. Testing the Strict-Matching Rule Live
To verify and demonstrate the strict-matching logic on screen:
1. Open the app; observe the initial pantry items (e.g. Eggs, Milk, Butter, Salt, Tomato, Bread).
2. Tap the **Suggested** tab; notice recipes like *Classic Scrambled Eggs* and *Tomato & Herb Omelette* appear as strictly matched.
3. Tap **Pantry**, tap the delete icon next to **Tomato**, and confirm deletion.
4. Return to **Suggested**; observe *Tomato & Herb Omelette* immediately **disappears** from Strict Matches and moves into the **Almost There** section.
5. Re-add **Tomato** (quantity 2, unit 'unit'); notice *Tomato & Herb Omelette* reappears in the Strict Matches list instantly.

---

## 7. Version Control & Git History
This repository was developed using strict incremental Git version control, documenting every development phase across clean, descriptive commits:
1. `Initial project structure with Gradle 8.2 and Material Design dependencies`
2. `Implement SQLiteOpenHelper and relational database schema`
3. `Seed SQLite database with 20 preloaded recipes and ingredient relations`
4. `Create PantryItem, Recipe, and RecipeIngredient model entities`
5. `Implement IngredientMatcher utility with strict-matching and pluralization normalization`
6. `Build PantryAdapter and RecyclerView layout with CardView presentation`
7. `Implement MainActivity with RecyclerView binding and floating action button`
8. `Create AddEditIngredientActivity with input validation and DatePickerDialog`
9. `Implement SQLite CRUD operations (Insert, Read, Update, Delete) with dialog confirmation`
10. `Develop SuggestedRecipesActivity implementing strict-matching algorithm and zero-match state`
11. `Implement 'Almost There' bonus stretch feature for single-missing ingredient recipes`
12. `Create RecipeDetailActivity with live pantry checklist and inventory deduction button`
13. `Implement SettingsActivity for preference toggles and database reset`
14. `Add BottomNavigationView across main screens and finalize technical documentation`
