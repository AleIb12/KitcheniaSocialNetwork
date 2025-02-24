package com.example.kitchenia.card;

import lombok.Data;

/**
 * La clase Carta representa una carta con varios atributos como nombre, descripción, imagen,
 * estado de me gusta/no me gusta, URL y publicador.
 */
@Data
public class Carta {
    private String nombre;
    private String descripcion;
    private int imagen;
    private boolean meGusta;
    private boolean noMeGusta;
    private String url; // Add this field
    private String publicador;

    /**
     * Constructor para crear una instancia de Carta.
     *
     * @param nombre      El nombre de la carta.
     * @param descripcion La descripción de la carta.
     * @param imagen      El identificador de la imagen de la carta.
     * @param meGusta     Indica si la carta tiene un "me gusta".
     * @param noMeGusta   Indica si la carta tiene un "no me gusta".
     * @param url         La URL asociada a la carta.
     * @param publicador  El publicador de la carta.
     */
    public Carta(String nombre, String descripcion, int imagen, boolean meGusta, boolean noMeGusta, String url, String publicador) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.meGusta = meGusta;
        this.noMeGusta = noMeGusta;
        this.url = url;
        this.publicador = publicador;
    }

    /**
     * Obtiene la URL de la carta.
     *
     * @return La URL de la carta.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Establece la URL de la carta.
     *
     * @param url La nueva URL de la carta.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Obtiene el nombre de la carta.
     *
     * @return El nombre de la carta.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre de la carta.
     *
     * @param nombre El nuevo nombre de la carta.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene la descripción de la carta.
     *
     * @return La descripción de la carta.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripción de la carta.
     *
     * @param descripcion La nueva descripción de la carta.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el identificador de la imagen de la carta.
     *
     * @return El identificador de la imagen de la carta.
     */
    public int getImagen() {
        return imagen;
    }

    /**
     * Establece el identificador de la imagen de la carta.
     *
     * @param imagen El nuevo identificador de la imagen de la carta.
     */
    public void setImagen(int imagen) {
        this.imagen = imagen;
    }

    /**
     * Indica si la carta tiene un "me gusta".
     *
     * @return true si la carta tiene un "me gusta", false en caso contrario.
     */
    public boolean isMeGusta() {
        return meGusta;
    }

    /**
     * Establece el estado de "me gusta" de la carta.
     *
     * @param meGusta El nuevo estado de "me gusta" de la carta.
     */
    public void setMeGusta(boolean meGusta) {
        this.meGusta = meGusta;
    }

    /**
     * Indica si la carta tiene un "no me gusta".
     *
     * @return true si la carta tiene un "no me gusta", false en caso contrario.
     */
    public boolean isNoMeGusta() {
        return noMeGusta;
    }

    /**
     * Establece el estado de "no me gusta" de la carta.
     *
     * @param noMeGusta El nuevo estado de "no me gusta" de la carta.
     */
    public void setNoMeGusta(boolean noMeGusta) {
        this.noMeGusta = noMeGusta;
    }

    /**
     * Obtiene el publicador de la carta.
     *
     * @return El publicador de la carta.
     */
    public String getPublicador() {
        return publicador;
    }

    /**
     * Establece el publicador de la carta.
     *
     * @param publicador El nuevo publicador de la carta.
     */
    public void setPublicador(String publicador) {
        this.publicador = publicador;
    }
}