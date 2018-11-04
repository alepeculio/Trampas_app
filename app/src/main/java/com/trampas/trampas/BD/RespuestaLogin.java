package com.trampas.trampas.BD;

import com.google.gson.annotations.SerializedName;
import com.trampas.trampas.Clases.Usuario;

public class RespuestaLogin {
    @SerializedName("codigo")
    private String codigo;
    @SerializedName("mensaje")
    private String mensaje;
    @SerializedName("usuario")
    private Usuario usuario;

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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
