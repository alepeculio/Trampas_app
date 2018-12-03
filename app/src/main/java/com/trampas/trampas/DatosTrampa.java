package com.trampas.trampas;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trampas.trampas.Adaptadores.AdaptadorListaColocaciones;
import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.RespuestaColocaciones;
import com.trampas.trampas.Clases.Colocacion;
import com.trampas.trampas.Clases.Trampa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatosTrampa extends AppCompatActivity {
    List<Colocacion> colocaciones;
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

    @BindView(R.id.cbLeishmaniasis)
    CheckBox cbLeishmaniasis;

    @BindView(R.id.cvCheckBox)
    CardView cvCheckBox;

    @BindView(R.id.tvColocaciones)
    TextView tvColocaciones;

    @BindView(R.id.noHayColocaciones)
    LinearLayout noHayColocaciones;

    int periodo;

    private boolean leishmaniasis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_trampa);
        ButterKnife.bind(this);

        final Trampa trampa = (Trampa) getIntent().getSerializableExtra("trampa");
        Serializable l = getIntent().getSerializableExtra("leishmaniasis");
        leishmaniasis = (boolean) ((l != null) ? l : false);
        final Colocacion colocacion = (Colocacion) getIntent().getSerializableExtra("colocacion");
        getSupportActionBar().setTitle("Información detallada");
        Serializable l2 = getIntent().getSerializableExtra("periodo");
        periodo = (int) ((l2 != null) ? l2 : 0);
        tvNombre.setText(trampa.getNombre());
        tvIdTrampa.setText(String.valueOf(trampa.getId()));
        tvMacTrampa.setText(String.valueOf(trampa.getMac()));

        setAdaptadorListaColocaciones();
        if (colocacion != null) {
            List<Colocacion> cs = new ArrayList<>();
            cs.add(colocacion);
            adaptadorListaColocaciones.actualizarColocaciones(cs);
            cvCheckBox.setVisibility(View.GONE);
            tvColocaciones.setText("Colocación");
            swipeRefresh.setEnabled(false);
        } else {
            cargarColocaciones(trampa.getId());

            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    cargarColocaciones(trampa.getId());
                }
            });

            cbLeishmaniasis.setChecked(leishmaniasis);
            cbLeishmaniasis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    leishmaniasis = isChecked;
                    filtrarColocaciones();
                }
            });
        }

        if (periodo != 0) {
            cvCheckBox.setVisibility(View.GONE);
            tvColocaciones.setText("Colocaciones del periodo " + periodo);
        }


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
            for (Colocacion c : colocaciones) {
                if (leishmaniasis) {
                    if (c.getLeishmaniasis())
                        colocacionesFinal.add(c);
                } else if (periodo != 0) {
                    if (c.getPeriodo() == periodo) {
                        colocacionesFinal.add(c);
                    }
                } else {
                    colocacionesFinal.add(c);
                }
            }

            if (colocacionesFinal.size() == 0 && leishmaniasis)
                noHayColocaciones.setVisibility(View.VISIBLE);
            else
                noHayColocaciones.setVisibility(View.GONE);
        }

        adaptadorListaColocaciones.actualizarColocaciones(colocacionesFinal);
        swipeRefresh.setRefreshing(false);

        if (periodo != 0)
            swipeRefresh.setEnabled(false);

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
