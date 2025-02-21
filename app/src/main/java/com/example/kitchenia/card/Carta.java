package com.example.kitchenia.card;

public class Carta {
    private String username;
    private String descripcion;
    private int imageUrl; // Si es un recurso drawable, usa int. Si es una URL, usa String.
    private boolean liked;
    private boolean shared;
    private String imageLink;

    public Carta(String username, String descripcion, int imageUrl, boolean liked, boolean shared, String imageLink) {
        this.username = username;
        this.descripcion = descripcion;
        this.imageUrl = imageUrl;
        this.liked = liked;
        this.shared = shared;
        this.imageLink = imageLink;
    }

    // Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(int imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }
}
