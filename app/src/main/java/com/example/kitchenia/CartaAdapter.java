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
public class CartaAdapter extends RecyclerView.Adapter {
    private TextView tvUsername;
    private List<Carta> cartas;

    /**
     * Constructor for CartaAdapter.
     *
     * @param cartas List of Carta objects to be displayed.
     */
    public CartaAdapter(List<Carta> cartas) {
        this.cartas = cartas;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}