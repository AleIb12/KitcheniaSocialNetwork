package com.example.kitchenia.card;

import lombok.Data;

@Data
public class Carta {
    private String nombre;
    private String descripcion;
    private int imagen;
    private boolean meGusta;
    private boolean noMeGusta;
    private String url; // Add this field
    private String publicador;
    public Carta(String nombre, String descripcion, int imagen, boolean meGusta, boolean noMeGusta, String url, String publicador) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.meGusta = meGusta;
        this.noMeGusta = noMeGusta;
        this.url = url;
        this.publicador = publicador;
    }


    // Add getter and setter for url
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getImagen() {
        return imagen;
    }

    public void setImagen(int imagen) {
        this.imagen = imagen;
    }

    public boolean isMeGusta() {
        return meGusta;
    }

    public void setMeGusta(boolean meGusta) {
        this.meGusta = meGusta;
    }

    public boolean isNoMeGusta() {
        return noMeGusta;
    }

    public void setNoMeGusta(boolean noMeGusta) {
        this.noMeGusta = noMeGusta;
    }
    public String getPublicador() {
        return publicador;
    }
    public void setPublicador(String publicador) {
        this.publicador = publicador;
    }

}