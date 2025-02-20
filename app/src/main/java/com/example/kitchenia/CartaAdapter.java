package com.example.kitchenia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kitchenia.R;
import com.example.kitchenia.card.Carta;

import java.util.List;

/**
 * Adapter class for displaying a list of Carta objects in a RecyclerView.
 */
public class CartaAdapter extends RecyclerView.Adapter<CartaAdapter.CartaViewHolder> {

    private List<Carta> cartas;

    /**
     * Constructor for CartaAdapter.
     *
     * @param cartas List of Carta objects to be displayed.
     */
    public CartaAdapter(List<Carta> cartas) {
        this.cartas = cartas;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param viewType The view type of the new View.
     * @return A new CartaViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public CartaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new CartaViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CartaViewHolder holder, int position) {
        Carta carta = cartas.get(position);
        // Load image using Glide
        if (carta.getUrl() != null && !carta.getUrl().isEmpty()) {
            Glide.with(holder.imagen.getContext())
                .load(carta.getUrl())
                .centerCrop()
                .into(holder.imagen);
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return cartas.size();
    }

    /**
     * ViewHolder class for CartaAdapter.
     * Holds the views for each item in the RecyclerView.
     */
    public static class CartaViewHolder extends RecyclerView.ViewHolder {
        TextView textoDescripcion;
        ImageView imagen;

        /**
         * Constructor for CartaViewHolder.
         *
         * @param itemView The view of the item.
         */
        public CartaViewHolder(View itemView) {
            super(itemView);
            textoDescripcion = itemView.findViewById(R.id.textViewDescription);
            imagen = itemView.findViewById(R.id.imageView);
        }
    }
}