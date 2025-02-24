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
import com.example.kitchenia.R;

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

            // Cargar imagen: si `imageLink` no está vacío, usa Glide; si no, usa el recurso local.
            if (carta.getImageLink() != null && !carta.getImageLink().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(carta.getImageLink())  // Carga desde URL Firebase Storage
                        .placeholder(R.drawable.placeholder_image) // Imagen mientras carga
                        .error(R.drawable.error_image) // Imagen en caso de error
                        .into(imageCarta);
            } else {
                imageCarta.setImageResource(carta.getImageUrl()); // Carga desde drawable
            }
        }
    }
}
