package com.trampas.trampas.Clases;

import java.io.Serializable;

public class Colocacion implements Serializable {
    int idColocacion;
    Double lat;
    Double lon;
    String tempMax;
    String tempMin;
    String humMin;
    String humMax;
    String tempProm;
    String humProm;
    String fechaInicio;
    String fechaFin;
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

    public String getTempMax() {
        return tempMax;
    }

    public void setTempMax(String tempMax) {
        this.tempMax = tempMax;
    }

    public String getTempMin() {
        return tempMin;
    }

    public void setTempMin(String tempMin) {
        this.tempMin = tempMin;
    }

    public String getHumMin() {
        return humMin;
    }

    public void setHumMin(String humMin) {
        this.humMin = humMin;
    }

    public String getHumMax() {
        return humMax;
    }

    public void setHumMax(String humMax) {
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

    public String getTempProm() {
        return tempProm;
    }

    public void setTempProm(String tempProm) {
        this.tempProm = tempProm;
    }

    public String getHumProm() {
        return humProm;
    }

    public void setHumProm(String humProm) {
        this.humProm = humProm;
    }
}
