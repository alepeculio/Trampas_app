package com.trampas.trampas.Clases;

import java.io.Serializable;

public class Trampa implements Serializable {
    int id;
    String nombre;
    String mac;
    Colocacion colocacion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Colocacion getColocacion() {
        return colocacion;
    }

    public void setColocacion(Colocacion colocacion) {
        this.colocacion = colocacion;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
