package com.example.kitchenia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kitchenia.card.Carta;

import java.util.List;

public class CartaAdapter extends RecyclerView.Adapter<CartaAdapter.CartaViewHolder> {

    private List<Carta> cartaList;

    public CartaAdapter(List<Carta> cartaList) {
        this.cartaList = cartaList;
    }

    public void updateData(List<Carta> newList) {
        this.cartaList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new CartaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartaViewHolder holder, int position) {
        Carta carta = cartaList.get(position);
        holder.bind(carta);
    }

    @Override
    public int getItemCount() {
        return cartaList.size();
    }

    public static class CartaViewHolder extends RecyclerView.ViewHolder {
        private TextView textUsername;
        private TextView textDescripcion;
        private ImageView imageCarta;

        public CartaViewHolder(@NonNull View itemView) {
            super(itemView);
            textUsername = itemView.findViewById(R.id.textUsername);
            textDescripcion = itemView.findViewById(R.id.textDescripcion);
            imageCarta = itemView.findViewById(R.id.imageCarta);
        }

        public void bind(Carta carta) {
            textUsername.setText(carta.getUsername());
            textDescripcion.setText(carta.getDescripcion());
            Glide.with(itemView.getContext()).load(carta.getImageUrl()).into(imageCarta);
        }
    }
}
