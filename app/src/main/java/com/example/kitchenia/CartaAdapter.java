package com.example.kitchenia;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.kitchenia.card.Carta;
import java.util.List;

public class CartaAdapter extends RecyclerView.Adapter<CartaAdapter.CartaViewHolder> {

    private List<Carta> cartas;
    private OnItemDeleteListener deleteListener;

    public CartaAdapter(List<Carta> cartas) {
        this.cartas = cartas;
    }

    public interface OnItemDeleteListener {
        void onDelete(Carta carta, int position);
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
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
        holder.textViewUsername.setText(carta.getPublicador());
        holder.textViewDescription.setText(carta.getDescripcion());

        if (carta.getUrl() != null && !carta.getUrl().isEmpty()) {
            Glide.with(holder.imageView.getContext())
                    .load(carta.getUrl())
                    .centerCrop()
                    .into(holder.imageView);
        }

        holder.buttonOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.item_options_menu);
            popup.setOnMenuItemClickListener((MenuItem item) -> {
                if (item.getItemId() == R.id.action_delete && deleteListener != null) {
                    deleteListener.onDelete(carta, position);
                    return true;
                }
                return false;
            });
            popup.show();
        });

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
        ImageButton buttonOptions;

        CartaViewHolder(View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            imageView = itemView.findViewById(R.id.imageView);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            buttonOptions = itemView.findViewById(R.id.buttonOptions);
        }
    }
}