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

public class CartaAdapter extends RecyclerView.Adapter<CartaAdapter.CartaViewHolder> {
    private List<Carta> cartas;

    public CartaAdapter(List<Carta> cartas) {
        this.cartas = cartas;
    }

    @NonNull
    @Override
    public CartaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new CartaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartaViewHolder holder, int position) {
        Carta carta = cartas.get(position);
        holder.textoDescripcion.setText(carta.getDescripcion());

        // Load image using Glide
        if (carta.getUrl() != null && !carta.getUrl().isEmpty()) {
            Glide.with(holder.imagen.getContext())
                .load(carta.getUrl())
                .centerCrop()
                .into(holder.imagen);
        }
    }

    @Override
    public int getItemCount() {
        return cartas.size();
    }

    public static class CartaViewHolder extends RecyclerView.ViewHolder {
        TextView textoDescripcion;
        ImageView imagen;

        public CartaViewHolder(View itemView) {
            super(itemView);
            textoDescripcion = itemView.findViewById(R.id.textViewDescription);
            imagen = itemView.findViewById(R.id.imageView);
        }
    }
}