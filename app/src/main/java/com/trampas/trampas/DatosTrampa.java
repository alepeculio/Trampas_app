package com.trampas.trampas;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.trampas.trampas.Adaptadores.AdaptadorListaColocaciones;
import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.RespuestaColocaciones;
import com.trampas.trampas.Clases.Colocacion;
import com.trampas.trampas.Clases.Trampa;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatosTrampa extends AppCompatActivity {
    List<Colocacion> colocaciones;
    private String ultimaBusqueda = null;
    AdaptadorListaColocaciones adaptadorListaColocaciones;
    @BindView(R.id.listaTrampas)
    RecyclerView mRecyclerView;

    @BindView(R.id.tvNombre)
    TextView tvNombre;

    @BindView(R.id.idTrampa)
    TextView tvIdTrampa;

    @BindView(R.id.macTrampa)
    TextView tvMacTrampa;

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_trampa);
        ButterKnife.bind(this);

        final Trampa trampa = (Trampa) getIntent().getSerializableExtra("trampa");
        getSupportActionBar().setTitle("Información detallada");
        tvNombre.setText(trampa.getNombre());
        tvIdTrampa.setText(String.valueOf(trampa.getId()));
        tvMacTrampa.setText(String.valueOf(trampa.getMac()));

        setAdaptadorListaColocaciones();
        cargarColocaciones(trampa.getId());

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                cargarColocaciones(trampa.getId());
            }
        });
    }

    public void setAdaptadorListaColocaciones() {
        if (adaptadorListaColocaciones == null) {
            adaptadorListaColocaciones = new AdaptadorListaColocaciones(new ArrayList<Colocacion>(), this);
            mRecyclerView.setAdapter(adaptadorListaColocaciones);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setHasFixedSize(true);
        }
    }

    private void filtrarColocaciones() {
        List<Colocacion> colocacionesFinal = new ArrayList<>();

        if (colocaciones != null) {
            if (ultimaBusqueda != null) {
                for (Colocacion t : colocaciones) {
                    if (String.valueOf(t.getId()).toLowerCase().contains(ultimaBusqueda)) {
                        colocacionesFinal.add(t);
                    }
                }

                if (colocacionesFinal.size() == 0) {
                    if (!ultimaBusqueda.equals("")) {
                        // tvNoHayTrampas.setText("No hay resultados para \"" + ultimaBusqueda + "\"");
                    } else {
                        Toast.makeText(this, String.valueOf(colocaciones.size()), Toast.LENGTH_SHORT).show();
                        colocacionesFinal = colocaciones;
                    }
                }

            } else {
                colocacionesFinal = colocaciones;
            }
        }

        adaptadorListaColocaciones.actualizarColocaciones(colocacionesFinal);
        swipeRefresh.setRefreshing(false);
    }

    private void cargarColocaciones(int id) {
        swipeRefresh.setRefreshing(true);
        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<RespuestaColocaciones> call = bd.obtenerColocacionesTrampa(id);
        call.enqueue(new Callback<RespuestaColocaciones>() {
            @Override
            public void onResponse(Call<RespuestaColocaciones> call, Response<RespuestaColocaciones> response) {
                if (response.body() != null) {
                    if (response.body().getCodigo().equals("1")) {
                        colocaciones = response.body().getColocaciones();
                        filtrarColocaciones();
                    } else {
                        colocaciones = null;
                        filtrarColocaciones();
                        Toast.makeText(DatosTrampa.this, response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    colocaciones = null;
                    filtrarColocaciones();
                    Toast.makeText(DatosTrampa.this, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaColocaciones> call, Throwable t) {
                colocaciones = null;
                filtrarColocaciones();
                Toast.makeText(DatosTrampa.this, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();

            }
        });
    }
}
