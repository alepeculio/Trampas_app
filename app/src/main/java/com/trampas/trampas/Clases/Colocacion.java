package com.trampas.trampas.Clases;

import java.io.Serializable;

public class Colocacion implements Serializable {
    int idColocacion;
    Double lat;
    Double lon;
    float tempMax;
    float tempMin;
    float humMin;
    float humMax;
    float tempProm;
    float humProm;
    String fechaInicio;
    String fechaFin;
    Boolean leishmaniasis;
    int periodo;
    int flevotomo;
    int perros;
    Trampa trampa;
    int usuario;

    public int getId() {
        return idColocacion;
    }

    public void setId(int id) {
        this.idColocacion = id;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public float getTempMax() {
        return tempMax;
    }

    public void setTempMax(float tempMax) {
        this.tempMax = tempMax;
    }

    public float getTempMin() {
        return tempMin;
    }

    public void setTempMin(float tempMin) {
        this.tempMin = tempMin;
    }

    public float getHumMin() {
        return humMin;
    }

    public void setHumMin(float humMin) {
        this.humMin = humMin;
    }

    public float getHumMax() {
        return humMax;
    }

    public void setHumMax(float humMax) {
        this.humMax = humMax;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Trampa getTrampa() {
        return trampa;
    }

    public void setTrampa(Trampa trampa) {
        this.trampa = trampa;
    }

    public int getUsuario() {
        return usuario;
    }

    public void setUsuario(int usuario) {
        this.usuario = usuario;
    }

    public int getIdColocacion() {
        return idColocacion;
    }

    public void setIdColocacion(int idColocacion) {
        this.idColocacion = idColocacion;
    }

    public float getTempProm() {
        return tempProm;
    }

    public void setTempProm(float tempProm) {
        this.tempProm = tempProm;
    }

    public float getHumProm() {
        return humProm;
    }

    public void setHumProm(float humProm) {
        this.humProm = humProm;
    }

    public Boolean getLeishmaniasis() {
        return leishmaniasis;
    }

    public void setLeishmaniasis(Boolean leishmaniasis) {
        this.leishmaniasis = leishmaniasis;
    }

    public int getPeriodo() {
        return periodo;
    }

    public void setPeriodo(int periodo) {
        this.periodo = periodo;
    }

    public int getFlevotomo() {
        return flevotomo;
    }

    public void setFlevotomo(int flevotomo) {
        this.flevotomo = flevotomo;
    }

    public int getPerros() {
        return perros;
    }

    public void setPerros(int perros) {
        this.perros = perros;
    }
}
