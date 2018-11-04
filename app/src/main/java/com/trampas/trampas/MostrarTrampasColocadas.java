package com.trampas.trampas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.RespuestaColocaciones;
import com.trampas.trampas.Clases.Colocacion;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MostrarTrampasColocadas extends Fragment {
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private Marker marcador;
    private List<Marker> marcadores;
    private List<Colocacion> colocaciones;
    private String idColocacionCreada;

    private FusedLocationProviderClient mFusedLocationClient;
    public static final int SOLICITUD_OBTENER_POSICION_ACTUAL = 111;

    public MostrarTrampasColocadas() {
    }

    public void setIdColocacionCreada(String idColocacionCreada) {
        this.idColocacionCreada = idColocacionCreada;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mostrar_trampas_colocadas, container, false);
        ButterKnife.bind(this, v);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        //Cargar mapa en segundo plano con asynctask
        new LongOperationMap().execute();

        return v;
    }

    private void cargarColocaciones() {
        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<RespuestaColocaciones> call = bd.obtenerColocacionesActivas();
        call.enqueue(new Callback<RespuestaColocaciones>() {
            @Override
            public void onResponse(Call<RespuestaColocaciones> call, Response<RespuestaColocaciones> response) {
                if (response.body() != null) {
                    if (response.body().getCodigo().equals("1")) {
                        colocaciones = response.body().getColocaciones();
                        prepararMapa();
                    } else {
                        colocaciones = null;
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    colocaciones = null;
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), "Error interno del servidor, Reintente", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaColocaciones> call, Throwable t) {
                colocaciones = null;
                if (getActivity() != null)
                    Toast.makeText(getActivity(), "Error de conexión con el servidor: " + t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void prepararMapa() {
        if (colocaciones != null) {
            if (marcadores == null)
                marcadores = new ArrayList<>();

            for (Marker m : marcadores)
                m.remove();

            marcadores.clear();

            for (Colocacion c : colocaciones) {
                Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(c.getLat(), c.getLon())).title(c.getTrampa().getNombre()));
                m.setSnippet("Colocada: " + c.getFechaInicio());
                marcadores.add(m);

                if (idColocacionCreada != null) {
                    if (idColocacionCreada.equals(String.valueOf(c.getId()))) {
                        marcador = m;
                    }
                }
            }

            if (marcador != null) {
                marcador.showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(marcador.getPosition()));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14.0f));
                marcador = null;
            }
        }
    }

    private void centrarMapa(Double lat, Double lon) {
        LatLng centrar;
        if (lat != 0 && lon != 0) {
            centrar = new LatLng(lat, lon);
        } else {
            centrar = new LatLng(-32.315827, -58.065113);
        }

        //mMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centrar, 12.0f));
    }


    public void obtenerLocalizacion() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, SOLICITUD_OBTENER_POSICION_ACTUAL);
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        try {
                            mMap.setMyLocationEnabled(true);
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        } catch (SecurityException se) {
                            se.printStackTrace();
                        }
                        centrarMapa(location.getLatitude(), location.getLongitude());
                    } else {
                        centrarMapa(0.0, 0.0);
                        Toast.makeText(getContext(), "No se pudo obtener su ubicación, compruebe la configuración de localización del dispositivo.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SOLICITUD_OBTENER_POSICION_ACTUAL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    obtenerLocalizacion();
                } else {
                    centrarMapa(0.0, 0.0);
                }
            }
        }
    }

    private class LongOperationMap extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (mapFragment == null) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                mapFragment = SupportMapFragment.newInstance();
                ft.replace(R.id.map, mapFragment).commit();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    cargarColocaciones();
                    obtenerLocalizacion();
                }
            });
        }
    }
}

