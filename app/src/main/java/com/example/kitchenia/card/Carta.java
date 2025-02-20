package com.example.kitchenia.card;

import lombok.Data;

@Data
public class Carta {
    private String descripcion;
    private String imagen;
    private int meGusta;
    private boolean noMeGusta;
    private boolean url;
    private String userId;


    public Carta(String descripcion, String imagen, int meGusta, boolean noMeGusta, boolean url, String userId){

        this.descripcion = descripcion;
        this.imagen = imagen;
        this.meGusta = meGusta;
        this.noMeGusta = noMeGusta;
        this.url = url;
        this.userId = userId; // <-- Inicializarlo en el constructor
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setUrl(boolean url) {
        this.url = url;
    }
    public boolean getUrl() {
        return url;
    }
}
