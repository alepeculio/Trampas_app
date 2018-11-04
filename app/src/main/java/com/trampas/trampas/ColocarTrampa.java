package com.trampas.trampas;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.trampas.trampas.Adaptadores.AdaptadorListaTrampasColocar;
import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.RespuestaTrampas;
import com.trampas.trampas.Clases.Trampa;
import com.trampas.trampas.Clases.Usuario;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ColocarTrampa extends Fragment {
    private MostrarTrampasColocadasInterface mpi;
    private FusedLocationProviderClient mFusedLocationClient;
    public static final int SOLICITUD_OBTENER_POSICION_ACTUAL = 111;
    Usuario usuario;
    List<Trampa> trampas;
    private AdaptadorListaTrampasColocar adaptadorListaTrampasColocar;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private String ultimaBusqueda = null;

    /*@BindView(R.id.progressBar)
    ProgressBar progressBar;*/
    /*@BindView(R.id.progressBarSpinner)
    ProgressBar progressBarSpinner;*/

    @BindView(R.id.listaTrampas)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.noHayTrampas)
    LinearLayout noHayTrampas;

    @BindView(R.id.tvNoHayTrampas)
    TextView tvNoHayTrampas;

    public ColocarTrampa() {
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_colocar_trampa, container, false);
        ButterKnife.bind(this, view);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        setAdaptadorListaTrampas();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                cargarTrampas();
            }
        });
        cargarTrampas();
        return view;
    }

    public void setAdaptadorListaTrampas() {
        if (adaptadorListaTrampasColocar == null) {
            adaptadorListaTrampasColocar = new AdaptadorListaTrampasColocar(new ArrayList<Trampa>(), getActivity());
            adaptadorListaTrampasColocar.setUsuario(usuario);
            mRecyclerView.setAdapter(adaptadorListaTrampasColocar);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setHasFixedSize(true);
            obtenerLocalizacion();
        }
    }

    private void filtrarTrampas() {
        List<Trampa> trampasFinal = new ArrayList<>();
        tvNoHayTrampas.setText("No hay trampas para colocar");

        if (trampas != null) {
            if (ultimaBusqueda != null) {
                for (Trampa t : trampas) {
                    if (t.getNombre().toLowerCase().contains(ultimaBusqueda) || String.valueOf(t.getId()).toLowerCase().contains(ultimaBusqueda)) {
                        trampasFinal.add(t);
                    }
                }

                if (trampasFinal.size() == 0) {
                    if (!ultimaBusqueda.equals("")) {
                        tvNoHayTrampas.setText("No hay resultados para \"" + ultimaBusqueda + "\"");
                    } else {
                        Toast.makeText(getActivity(), String.valueOf(trampas.size()), Toast.LENGTH_SHORT).show();
                        trampasFinal = trampas;
                        tvNoHayTrampas.setText("No hay trampas para colocar");
                    }
                }

            } else {
                trampasFinal = trampas;
                if (trampasFinal.size() == 0)
                    tvNoHayTrampas.setText("No hay trampas para colocar");
            }
        }

        if (trampasFinal.size() == 0) {
            noHayTrampas.setVisibility(View.VISIBLE);
        } else {
            noHayTrampas.setVisibility(View.GONE);
        }
        adaptadorListaTrampasColocar.actualizarTrampas(trampasFinal);
        swipeRefresh.setRefreshing(false);
    }


    private void cargarTrampas() {
        swipeRefresh.setRefreshing(true);
        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<RespuestaTrampas> call = bd.obtenerTrampasNoColocadas();
        call.enqueue(new Callback<RespuestaTrampas>() {
            @Override
            public void onResponse(Call<RespuestaTrampas> call, Response<RespuestaTrampas> response) {
                if (response.body() != null) {
                    if (response.body().getCodigo().equals("1")) {
                        trampas = response.body().getTrampas();
                        filtrarTrampas();
                    } else {
                        trampas = null;
                        filtrarTrampas();
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    trampas = null;
                    filtrarTrampas();
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), "Error interno del servidor, Reintente", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onFailure(Call<RespuestaTrampas> call, Throwable t) {
                trampas = null;
                filtrarTrampas();
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Error de conexión con el servidor: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(getActivity().SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    ultimaBusqueda = newText.toLowerCase().trim();
                    filtrarTrampas();
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    ultimaBusqueda = query.toLowerCase().trim();
                    filtrarTrampas();
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SOLICITUD_OBTENER_POSICION_ACTUAL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    obtenerLocalizacion();
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(getContext(), "Su ubicación es necesaria para colocar la trampa.", Toast.LENGTH_LONG).show();
                        //cargando(false);
                    }
                }
            }
        }
    }

   /* public void cargando(boolean cargando) {
        if (cargando) {
            btnColocar.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            btnColocar.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }*/

    public void obtenerLocalizacion() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, SOLICITUD_OBTENER_POSICION_ACTUAL);
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                       adaptadorListaTrampasColocar.setLat(location.getLatitude());
                       adaptadorListaTrampasColocar.setLon(location.getLongitude());
                    } else {
                        Toast.makeText(getContext(), "No se pudo obtener su ubicación, compruebe la configuración de localización del dispositivo.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

   /* private void colocarTrampa(final Double lat, final Double lon) {
        if (trampaSeleccionada == null) {
            Toast.makeText(getContext(), "Debe seleccionar una trampa", Toast.LENGTH_LONG).show();
            cargando(false);
        } else {
            int idTrampa = Integer.parseInt(trampaSeleccionada);

            BDInterface bd = BDCliente.getClient().create(BDInterface.class);
            Call<Respuesta> call = bd.colocarTrampa(lat, lon, idTrampa, usuario.getId());
            call.enqueue(new Callback<Respuesta>() {
                @Override
                public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                    if (response.body() != null) {
                        if (!response.body().getCodigo().equals("0")) {
                            cargando(false);

                            final String codigo = response.body().getCodigo();

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Trampa colocada");
                            builder.setMessage("¿Quiere ver su ubicación en el mapa?");

                            builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mpi.irAlMapa(codigo);
                                }
                            });

                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    cargarTrampas();
                                }
                            });

                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();

                            /*
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                            }, 1000);*/

    /* } else {
         Toast.makeText(getActivity(), response.body().getMensaje(), Toast.LENGTH_SHORT).show();
         cargando(false);
     }
 } else {
     Toast.makeText(getActivity(), "Error interno del servidor, reintente.", Toast.LENGTH_SHORT).show();
     cargando(false);
 }
}

@Override
public void onFailure(Call<Respuesta> call, Throwable t) {
 Toast.makeText(getActivity(), "Error de conexión con el servidor: " + t.getMessage(), Toast.LENGTH_SHORT).show();
 cargando(false);
}
});
}
}
*/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MostrarTrampasColocadasInterface) {
            mpi = (MostrarTrampasColocadasInterface) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AgregarParadaInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mpi = null;
    }

}