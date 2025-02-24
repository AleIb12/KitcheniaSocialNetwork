package com.example.kitchenia;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
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

        // Set publisher name
        holder.textViewUsername.setText(carta.getPublicador());

        // Set description
        holder.textViewDescription.setText(carta.getDescripcion());

        // Load image using Glide
        if (carta.getUrl() != null && !carta.getUrl().isEmpty()) {
            Glide.with(holder.imageView.getContext())
                    .load(carta.getUrl())
                    .centerCrop()
                    .into(holder.imageView);
        }

        // Handle like button click
        holder.buttonLike.setOnClickListener(v -> {
            carta.setMeGusta(!carta.isMeGusta());
            holder.buttonLike.setSelected(carta.isMeGusta());
        });
    }

    @Override
    public int getItemCount() {
        return cartas.size();
    }

    static class CartaViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUsername;
        TextView textViewDescription;
        ImageView imageView;
        ImageButton buttonLike;

        CartaViewHolder(View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            imageView = itemView.findViewById(R.id.imageView);
            buttonLike = itemView.findViewById(R.id.buttonLike);
        }
    }
}