package com.trampas.trampas;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.Respuesta;
import com.trampas.trampas.BD.RespuestaColocaciones;
import com.trampas.trampas.Clases.Colocacion;
import com.trampas.trampas.Clases.Trampa;
import com.trampas.trampas.Clases.Usuario;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
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
    private Usuario usuario;
    private String idColocacionCreada;
    private FusedLocationProviderClient mFusedLocationClient;
    public static final int SOLICITUD_OBTENER_POSICION_ACTUAL = 111;

    @BindView(R.id.llGuardar)
    LinearLayout llGuardar;

    @BindView(R.id.btnGuardar)
    Button btnGuardar;

    @BindView(R.id.tvDesde)
    TextView tvDesde;

    @BindView(R.id.tvHasta)
    TextView tvHasta;

    @BindView(R.id.btnBuscar)
    Button btnBuscar;

    Calendar calendario = Calendar.getInstance();

    public MostrarTrampasColocadas() {
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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

        final DatePickerDialog.OnDateSetListener fechaDesde = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendario.set(Calendar.YEAR, year);
                calendario.set(Calendar.MONTH, monthOfYear);
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "dd/MM/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
                tvDesde.setText(sdf.format(calendario.getTime()));
            }


        };

        tvDesde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog1 = new DatePickerDialog(getActivity(),
                        fechaDesde,
                        calendario.get(Calendar.YEAR),
                        calendario.get(Calendar.MONTH),
                        calendario.get(Calendar.DAY_OF_MONTH));

                dialog1.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            tvDesde.setText("");
                        }
                    }
                });

                dialog1.show();
            }
        });

        final DatePickerDialog.OnDateSetListener fechaHasta = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendario.set(Calendar.YEAR, year);
                calendario.set(Calendar.MONTH, monthOfYear);
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "dd/MM/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
                tvHasta.setText(sdf.format(calendario.getTime()));
            }

        };

        tvHasta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog2 = new DatePickerDialog(getActivity(),
                        fechaHasta,
                        calendario.get(Calendar.YEAR),
                        calendario.get(Calendar.MONTH),
                        calendario.get(Calendar.DAY_OF_MONTH));

                dialog2.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            tvHasta.setText("");
                        }
                    }
                });

                dialog2.show();
            }
        });


        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filtrarColocaciones();
            }
        });

        return v;
    }

    private void filtrarColocaciones() {
        if (colocaciones == null) {
            Toast.makeText(getActivity(), "No hay trampas colocadas", Toast.LENGTH_SHORT).show();
            return;
        }

        String fechaDesde = tvDesde.getText().toString();
        String fechaHasta = tvHasta.getText().toString();
        Date inicio = null;
        Date fin = null;
        List<Colocacion> colocs = new ArrayList<>();

        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        try {
            inicio = sdf.parse(fechaDesde);
            fin = sdf.parse(fechaHasta);
        } catch (ParseException pe) {
            if (inicio == null) {
                Toast.makeText(getActivity(), R.string.seleccionar_fecha_inicio, Toast.LENGTH_SHORT).show();
                return;
            }
            if (fin == null)
                fin = new Date();

        }

        if (inicio.after(fin)) {
            Toast.makeText(getActivity(), "La fecha de comienzo debe ser anterior a la de finalización", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Colocacion c : colocaciones) {
            Date fColocacion = null;
            try {
                fColocacion = sdf.parse(convertFormat(c.getFechaInicio()));
            } catch (ParseException pe) {
                pe.printStackTrace();
            }
            Log.d("fColocacion: ", fColocacion.toString());
            Log.d("Inicio: ", inicio.toString());
            Log.d("Fin: ", fin.toString());

            if ((fColocacion.after(inicio) || fColocacion.equals(inicio)) && (fColocacion.before(fin) || fColocacion.equals(fin))) {
                Log.d("filtro: ", "Entro ");
                colocs.add(c);
            }
        }

        prepararMapa(colocs);

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
                        prepararMapa(null);
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

    private void prepararMapa(List<Colocacion> colocs) {
        if (colocaciones != null) {
            if (marcadores == null)
                marcadores = new ArrayList<>();

            for (Marker m : marcadores)
                m.remove();

            marcadores.clear();

            if (colocs != null) {
                for (Colocacion c : colocs) {
                    Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(c.getLat(), c.getLon())).title(c.getTrampa().getNombre()));
                    if (c.getFechaFin() == null) {
                        if (c.getUsuario() == usuario.getId()) {
                            m.setDraggable(true);
                            m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                            m.setSnippet("Arrastre para editar ubicación");
                        } else {
                            m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            m.setSnippet("Actualmente colocada");
                        }
                    } else {
                        m.setSnippet("Ver gráfica");
                        m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    }
                    m.setTag(c.getId());
                    marcadores.add(m);

                    if (idColocacionCreada != null) {
                        if (idColocacionCreada.equals(String.valueOf(c.getId()))) {
                            marcador = m;
                        }
                    }
                }
            } else {
                for (Colocacion c : colocaciones) {
                    Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(c.getLat(), c.getLon())).title(c.getTrampa().getNombre()));
                    if (c.getFechaFin() == null) {
                        if (c.getUsuario() == usuario.getId()) {
                            m.setDraggable(true);
                            m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                            m.setSnippet("Arrastre para editar ubicación");
                        } else {
                            m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            m.setSnippet("Actualmente colocada");
                        }
                    } else {
                        m.setSnippet("Ver gráfica");
                        m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    }
                    m.setTag(c.getId());
                    marcadores.add(m);

                    if (idColocacionCreada != null) {
                        if (idColocacionCreada.equals(String.valueOf(c.getId()))) {
                            marcador = m;
                        }
                    }
                }
            }
            if (marcador != null) {
                marcador.showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(marcador.getPosition()));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14.0f));
                marcador = null;
            }

            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    llGuardar.setVisibility(View.VISIBLE);

                    if (marcador != null && !(marker.getTitle().equals(marcador.getTitle()))) {
                        mostrarMensaje();

                    }

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    if (marcador == null)
                        marcador = marker;
                }
            });
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    for (Colocacion c : colocaciones)
                        if (c.getId() == (int) marker.getTag() && c.getFechaFin() != null) {
                            Intent intent = new Intent(getActivity(), ColocacionGrafica.class);
                            intent.putExtra("colocacion", c);
                            getActivity().startActivity(intent);
                        }
                }
            });

            btnGuardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (marcador != null) {
                        int id = 0;
                        for (Colocacion c : colocaciones) {
                            if (c.getTrampa().getNombre().equals(marcador.getTitle())) {
                                id = c.getId();
                            }
                        }

                        if (id != 0) {
                            double lat = marcador.getPosition().latitude;
                            double lon = marcador.getPosition().longitude;

                            BDInterface bd = BDCliente.getClient().create(BDInterface.class);
                            Call<Respuesta> call = bd.actualizarUbicacionColocacion(id, lat, lon);
                            call.enqueue(new Callback<Respuesta>() {
                                @Override
                                public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                                    if (response.body() != null) {
                                        if (response.body().getCodigo().equals("1")) {
                                            llGuardar.setVisibility(View.GONE);
                                            marcador = null;
                                        } else {
                                            if (getActivity() != null)
                                                Toast.makeText(getActivity(), response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if (getActivity() != null)
                                            Toast.makeText(getActivity(), "Error interno del servidor, Reintente", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Respuesta> call, Throwable t) {
                                    if (getActivity() != null)
                                        Toast.makeText(getActivity(), "Error de conexión con el servidor: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                        }

                    }

                }
            });
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
                        centrarMapa(0.0, 0.0);
                    } else {
                        centrarMapa(0.0, 0.0);
                        // Toast.makeText(getContext(), "No se pudo obtener su ubicación, compruebe la configuración de localización del dispositivo.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
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

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        //Cargar mapa en segundo plano con asynctask
        new LongOperationMap().execute();
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

    public void mostrarMensaje() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Cambios no guardados");
        builder.setMessage("Si continua perderá los cambios del último marcador");

        builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                marcador = null;
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    //Transformar fechas.
    public String convertFormat(String inputDate) {
        if (inputDate == null)
            return "";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date == null)
            return "";

        SimpleDateFormat convetDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return convetDateFormat.format(date);
    }
}

