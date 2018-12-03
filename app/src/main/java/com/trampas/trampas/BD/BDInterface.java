package com.trampas.trampas.BD;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface BDInterface {
    @FormUrlEncoded
    @POST("login")
    Call<RespuestaLogin> login(
            @Field("correo") String correo,
            @Field("contrasenia") String contrasenia
    );

    @FormUrlEncoded
    @POST("colocarTrampa")
    Call<Respuesta> colocarTrampa(
            @Field("lat") Double lat,
            @Field("lon") Double lon,
            @Field("id_trampa") int idTrampa,
            @Field("id_usuario") int idUsuario
    );

    @GET("obtenerTrampas")
    Call<RespuestaTrampas> obtenerTrampas();

    @GET("obtenerTrampasLeishmaniasis")
    Call<RespuestaTrampas> obtenerTrampasLeishmaniasis();

    @GET("obtenerTrampasNoColocadas")
    Call<RespuestaTrampas> obtenerTrampasNoColocadas();

    @GET("obtenerTrampasColocadas")
    Call<RespuestaTrampas> obtenerTrampasColocadas();

    @GET("obtenerColocaciones")
    Call<RespuestaColocaciones> obtenerColocaciones();

    @GET("obtenerUsuarios")
    Call<RespuestaUsuarios> obtenerUsuarios();

    @FormUrlEncoded
    @POST("obtenerColocacionesTrampa")
    Call<RespuestaColocaciones> obtenerColocacionesTrampa(
            @Field("id") int id
    );

    @FormUrlEncoded
    @POST("agregarTrampa")
    Call<Respuesta> agregarTrampa(
            @Field("nombre") String nombre,
            @Field("mac") String mac
    );

    @FormUrlEncoded
    @POST("eliminarTrampa")
    Call<Respuesta> eliminarTrampa(
            @Field("id") int id
    );

    @FormUrlEncoded
    @POST("extraerTrampa")
    Call<Respuesta> extraerTrampa(
            @Field("id_trampa") int idTrampa,
            @Field("tmin") float tMin,
            @Field("tmax") float tMax,
            @Field("hmin") float hMin,
            @Field("hmax") float hMax,
            @Field("hprom") float hProm,
            @Field("tprom") float tProm
    );

    @FormUrlEncoded
    @POST("agregarUsuario")
    Call<Respuesta> agregarUsuario(
            @Field("nombre") String nombre,
            @Field("apellido") String apellido,
            @Field("correo") String correo,
            @Field("contrasenia") String contrasenia,
            @Field("admin") int admin
    );

    @FormUrlEncoded
    @POST("actualizarPrivilegios")
    Call<Respuesta> actualizarPrivilegios(
            @Field("id") int id,
            @Field("admin") int admin
    );

    @FormUrlEncoded
    @POST("eliminarUsuario")
    Call<Respuesta> eliminarUsuario(
            @Field("id") int id
    );

    @FormUrlEncoded
    @POST("actualizarUbicacionColocacion")
    Call<Respuesta> actualizarUbicacionColocacion(
            @Field("id") int id,
            @Field("lat") double lat,
            @Field("lon") double lon
    );

    @FormUrlEncoded
    @POST("actualizarColocacion")
    Call<Respuesta> actualizarColocacion(
            @Field("id") int id,
            @Field("lat") double lat,
            @Field("lon") double lon,
            @Field("finicio") String fInicio,
            @Field("ffin") String fFin,
            @Field("tmin") float tMin,
            @Field("tmax") float tMax,
            @Field("tprom") float tProm,
            @Field("hmin") float hMin,
            @Field("hmax") float hMax,
            @Field("hprom") float hProm,
            @Field("leishmaniasis") int leishmaniasis,
            @Field("flevotomo") String flevotomo,
            @Field("perros") String perros
    );

    @FormUrlEncoded
    @POST("cambiarContrasenia")
    Call<Respuesta> cambiarContrasenia(
            @Field("id") int id,
            @Field("contrasenia_actual") String contraseniaActual,
            @Field("contrasenia_nueva") String contraseniaNueva
    );

    @FormUrlEncoded
    @POST("obtenerColocacionesGrafica")
    Call<RespuestaColocaciones> obtenerColocacionesGrafica(
            @Field("id_periodo") int periodo
    );

    @FormUrlEncoded
    @POST("exportarDatos")
    Call<Respuesta> exportarDatos(
            @Field("correo") String correo,
            @Field("desde") String desde,
            @Field("hasta") String hasta
    );
}
