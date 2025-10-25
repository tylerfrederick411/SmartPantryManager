package com.richfield.smartpantry.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantry.R;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.RecipeIngredient;
import com.richfield.smartpantry.util.IngredientMatcher;

import java.util.List;

/**
 * Adapter displaying ingredient requirements for a single recipe,
 * showing whether each ingredient is currently available in the user's pantry.
 */
public class RecipeIngredientAdapter extends RecyclerView.Adapter<RecipeIngredientAdapter.IngredientViewHolder> {

    private Context context;
    private List<RecipeIngredient> ingredients;
    private List<PantryItem> pantryItems;

    public RecipeIngredientAdapter(Context context, List<RecipeIngredient> ingredients, List<PantryItem> pantryItems) {
        this.context = context;
        this.ingredients = ingredients;
        this.pantryItems = pantryItems;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        RecipeIngredient req = ingredients.get(position);
        holder.tvIngredientName.setText(req.getIngredientName());
        holder.tvIngredientAmount.setText(req.getFormattedQuantity() + " " + req.getUnit());

        // Check if user has this ingredient in pantry with sufficient quantity
        boolean satisfied = false;
        String userHasStr = "None";
        String normReq = IngredientMatcher.normalizeName(req.getIngredientName());

        if (pantryItems != null) {
            for (PantryItem p : pantryItems) {
                if (IngredientMatcher.normalizeName(p.getName()).equals(normReq)) {
                    userHasStr = p.getFormattedQuantity() + " " + p.getUnit();
                    if (IngredientMatcher.hasSufficientQuantity(req.getQuantity(), req.getUnit(), p.getQuantity(), p.getUnit())) {
                        satisfied = true;
                    }
                    break;
                }
            }
        }

        if (satisfied) {
            holder.ivStatus.setImageResource(R.drawable.ic_check);
            holder.ivStatus.setColorFilter(Color.parseColor("#2E7D32"));
            holder.tvPantryStatus.setText("In Pantry: " + userHasStr + " (Sufficient)");
            holder.tvPantryStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.ivStatus.setImageResource(R.drawable.ic_warning);
            holder.ivStatus.setColorFilter(Color.parseColor("#C62828"));
            holder.tvPantryStatus.setText("In Pantry: " + userHasStr + " (Missing / Insufficient)");
            holder.tvPantryStatus.setTextColor(Color.parseColor("#C62828"));
        }
    }

    @Override
    public int getItemCount() {
        return ingredients != null ? ingredients.size() : 0;
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        ImageView ivStatus;
        TextView tvIngredientName, tvIngredientAmount, tvPantryStatus;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStatus = itemView.findViewById(R.id.ivIngredientStatus);
            tvIngredientName = itemView.findViewById(R.id.tvIngredientName);
            tvIngredientAmount = itemView.findViewById(R.id.tvIngredientAmount);
            tvPantryStatus = itemView.findViewById(R.id.tvPantryStatus);
        }
    }
}
