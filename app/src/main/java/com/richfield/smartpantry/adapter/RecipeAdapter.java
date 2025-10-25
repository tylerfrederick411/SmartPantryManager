package com.richfield.smartpantry.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.richfield.smartpantry.R;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeMatchResult;

import java.util.List;

/**
 * Custom RecyclerView Adapter for presenting matched recipes.
 * Visually distinguishes Strict Matches from Almost There recipes.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private Context context;
    private List<RecipeMatchResult> results;
    private OnRecipeClickListener listener;

    public RecipeAdapter(Context context, List<RecipeMatchResult> results, OnRecipeClickListener listener) {
        this.context = context;
        this.results = results;
        this.listener = listener;
    }

    public void updateList(List<RecipeMatchResult> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeMatchResult result = results.get(position);
        Recipe recipe = result.getRecipe();

        holder.tvRecipeTitle.setText(recipe.getName());
        holder.tvCategory.setText(recipe.getCategory());
        holder.tvPrepTime.setText(recipe.getPrepTimeMinutes() + " mins");

        if (result.isStrictMatch()) {
            holder.tvMatchBadge.setText("Ready to Cook (All " + result.getTotalRequired() + " ingredients ready)");
            holder.tvMatchBadge.setBackgroundColor(Color.parseColor("#E8F5E9"));
            holder.tvMatchBadge.setTextColor(Color.parseColor("#2E7D32"));
        } else if (result.isAlmostThere()) {
            String missing = result.getMissingIngredients().get(0).getIngredientName();
            holder.tvMatchBadge.setText("Almost There: Missing only " + missing);
            holder.tvMatchBadge.setBackgroundColor(Color.parseColor("#FFF3E0"));
            holder.tvMatchBadge.setTextColor(Color.parseColor("#E65100"));
        } else {
            holder.tvMatchBadge.setText("Missing " + result.getMissingCount() + " ingredients");
            holder.tvMatchBadge.setBackgroundColor(Color.parseColor("#FFEBEE"));
            holder.tvMatchBadge.setTextColor(Color.parseColor("#C62828"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRecipeClick(recipe);
        });
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecipeTitle, tvCategory, tvPrepTime, tvMatchBadge;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipeTitle = itemView.findViewById(R.id.tvRecipeTitle);
            tvCategory = itemView.findViewById(R.id.tvRecipeCategory);
            tvPrepTime = itemView.findViewById(R.id.tvRecipePrepTime);
            tvMatchBadge = itemView.findViewById(R.id.tvRecipeMatchBadge);
        }
    }
}
