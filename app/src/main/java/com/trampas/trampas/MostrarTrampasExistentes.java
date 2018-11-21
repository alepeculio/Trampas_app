package com.trampas.trampas;


import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trampas.trampas.Adaptadores.AdaptadorListaTrampas;
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


public class MostrarTrampasExistentes extends Fragment {
    private List<Trampa> trampas;
    private AdaptadorListaTrampas adaptadorListaTrampas;
    private SearchView searchView = null;
    private String ultimaBusqueda = null;
    private boolean leishmaniasis = false;

    @BindView(R.id.listaTrampas)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.noHayTrampas)
    LinearLayout noHayTrampas;

    @BindView(R.id.tvNoHayTrampas)
    TextView tvNoHayTrampas;

    @BindView(R.id.btnMostrarAgregarTrampa)
    FloatingActionButton btnMostrarAgregarTrampa;

    @BindView(R.id.cbLeishmaniasis)
    CheckBox cbLeishmaniasis;

    Usuario usuario;

    public MostrarTrampasExistentes() {
        // Required empty public constructor
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mostrar_trampas_existentes, container, false);
        ButterKnife.bind(this, view);

        if (usuario.getAdmin() != 1)
            btnMostrarAgregarTrampa.setVisibility(View.GONE);


        setAdaptadorListaTrampas();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                cargarTrampas();
            }
        });


        btnMostrarAgregarTrampa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AgregarTrampa.class);
                startActivity(intent);
            }
        });

        cbLeishmaniasis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                leishmaniasis = isChecked;
                adaptadorListaTrampas.setLeishmaniasis(leishmaniasis);
                cargarTrampas();
            }
        });

        cargarTrampas();
        return view;
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

            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
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

    public void setAdaptadorListaTrampas() {
        if (adaptadorListaTrampas == null) {
            adaptadorListaTrampas = new AdaptadorListaTrampas(new ArrayList<Trampa>(), getActivity());
            adaptadorListaTrampas.setUsuario(usuario);
            mRecyclerView.setAdapter(adaptadorListaTrampas);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setHasFixedSize(true);
        }
    }

    @SuppressLint("SetTextI18n")
    private void filtrarTrampas() {
        List<Trampa> trampasFinal = new ArrayList<>();

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
                    } else if (leishmaniasis) {
                        tvNoHayTrampas.setText("No hay trampas con Leishmaniasis");
                    } else {
                        trampasFinal = trampas;
                        tvNoHayTrampas.setText(R.string.no_hay_trampas);
                    }
                }

            } else {
                trampasFinal = trampas;
                if (trampasFinal.size() == 0) {
                    if (leishmaniasis)
                        tvNoHayTrampas.setText("No hay trampas con Leishmaniasis");
                    else
                        tvNoHayTrampas.setText(R.string.no_hay_trampas);
                }
            }
        }

        if(leishmaniasis)
            tvNoHayTrampas.setText("No hay trampas con Leishmaniasis");

        if (trampasFinal.size() == 0) {
            noHayTrampas.setVisibility(View.VISIBLE);
        } else {
            noHayTrampas.setVisibility(View.GONE);
        }
        adaptadorListaTrampas.actualizarTrampas(trampasFinal);
        swipeRefresh.setRefreshing(false);
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                // Not implemented here
                return false;
            default:
                break;
        }
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onOptionsItemSelected(item);
    }*/



    /*private void hideKeyboard() {
        InputMethodManager mImMan = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        mImMan.hideSoftInputFromWindow(etNombre.getWindowToken(), 0);
    }*/


    private void cargarTrampas() {
        swipeRefresh.setRefreshing(true);
        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<RespuestaTrampas> call;
        if (leishmaniasis)
            call = bd.obtenerTrampasLeishmaniasis();
        else
            call = bd.obtenerTrampas();
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
                        Toast.makeText(getActivity(), R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RespuestaTrampas> call, Throwable t) {
                trampas = null;
                filtrarTrampas();
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
