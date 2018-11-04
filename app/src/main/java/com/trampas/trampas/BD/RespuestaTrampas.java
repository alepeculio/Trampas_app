package com.trampas.trampas.BD;

import com.google.gson.annotations.SerializedName;
import com.trampas.trampas.Clases.Trampa;

import java.util.List;

public class RespuestaTrampas {
    @SerializedName("codigo")
    private String codigo;
    @SerializedName("mensaje")
    private String mensaje;
    @SerializedName("trampas")
    private List<Trampa> trampas;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<Trampa> getTrampas() {
        return trampas;
    }

    public void setTrampas(List<Trampa> trampas) {
        this.trampas = trampas;
    }
}
