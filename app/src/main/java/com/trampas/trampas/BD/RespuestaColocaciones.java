package com.trampas.trampas.BD;

import com.google.gson.annotations.SerializedName;
import com.trampas.trampas.Clases.Colocacion;

import java.util.List;

public class RespuestaColocaciones {
    @SerializedName("codigo")
    private String codigo;
    @SerializedName("mensaje")
    private String mensaje;
    @SerializedName("colocaciones")
    private List<Colocacion> colocaciones;

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

    public List<Colocacion> getColocaciones() {
        return colocaciones;
    }

    public void setColocaciones(List<Colocacion> colocaciones) {
        this.colocaciones = colocaciones;
    }
}
