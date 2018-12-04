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
    int flebotomos;
    int habitantes;
    String observaciones;
    int perrosExistentes;
    int perrosMuestreados;
    int perrosPositivos;
    String perrosProcedencia;
    int perrosEutanasiados;
    String otrasAcciones;
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

    public int getFlebotomos() {
        return flebotomos;
    }

    public void setFlebotomos(int flebotomos) {
        this.flebotomos = flebotomos;
    }

    public int getPerrosExistentes() {
        return perrosExistentes;
    }

    public void setPerrosExistentes(int perrosExistentes) {
        this.perrosExistentes = perrosExistentes;
    }

    public int getHabitantes() {
        return habitantes;
    }

    public void setHabitantes(int habitantes) {
        this.habitantes = habitantes;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public int getPerrosMuestreados() {
        return perrosMuestreados;
    }

    public void setPerrosMuestreados(int perrosMuestreados) {
        this.perrosMuestreados = perrosMuestreados;
    }

    public int getPerrosPositivos() {
        return perrosPositivos;
    }

    public void setPerrosPositivos(int perrosPositivos) {
        this.perrosPositivos = perrosPositivos;
    }

    public String getPerrosProcedencia() {
        return perrosProcedencia;
    }

    public void setPerrosProcedencia(String perrosProcedencia) {
        this.perrosProcedencia = perrosProcedencia;
    }

    public int getPerrosEutanasiados() {
        return perrosEutanasiados;
    }

    public void setPerrosEutanasiados(int perrosEutanasiados) {
        this.perrosEutanasiados = perrosEutanasiados;
    }

    public String getOtrasAcciones() {
        return otrasAcciones;
    }

    public void setOtrasAcciones(String otrasAcciones) {
        this.otrasAcciones = otrasAcciones;
    }
}
