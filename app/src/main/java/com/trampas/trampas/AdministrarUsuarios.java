package com.trampas.trampas;


import android.app.SearchManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.trampas.trampas.Adaptadores.AdaptadorListaUsuarios;
import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.RespuestaUsuarios;
import com.trampas.trampas.Clases.Usuario;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AdministrarUsuarios extends Fragment {
    private List<Usuario> usuarios;
    private Usuario usuario;
    private AdaptadorListaUsuarios adaptadorListaUsuarios;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private String ultimaBusqueda = null;

    @BindView(R.id.rvUsuarios)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.noHayUsuarios)
    LinearLayout noHayUsuarios;

    @BindView(R.id.tvNoHayUsuarios)
    TextView tvNoHayUsuarios;

    @BindView(R.id.cbSolicitudAdmin)
    CheckBox cbSolicitudAdmin;

    public AdministrarUsuarios() {
        // Required empty public constructor
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
        filtrarUsuarios();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_administrar_usuarios, container, false);
        ButterKnife.bind(this, v);

        setAdaptadorListaUsuarios();

        swipeRefresh.setRefreshing(true);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                cargarUsuarios();
            }
        });

        cbSolicitudAdmin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filtrarUsuarios();
            }
        });

        cargarUsuarios();
        return v;
    }

    public void setAdaptadorListaUsuarios() {
        if (adaptadorListaUsuarios == null) {
            adaptadorListaUsuarios = new AdaptadorListaUsuarios(new ArrayList<Usuario>(), getActivity(), this);
            mRecyclerView.setAdapter(adaptadorListaUsuarios);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setHasFixedSize(true);
        }
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
                    filtrarUsuarios();
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    ultimaBusqueda = query.toLowerCase().trim();
                    filtrarUsuarios();
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void cargarUsuarios() {
        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<RespuestaUsuarios> call = bd.obtenerUsuarios();
        call.enqueue(new Callback<RespuestaUsuarios>() {
            @Override
            public void onResponse(Call<RespuestaUsuarios> call, Response<RespuestaUsuarios> response) {
                if (response.body() != null) {
                    if (response.body().getCodigo().equals("1")) {
                        usuarios = response.body().getUsuarios();
                        filtrarUsuarios();
                    } else {
                        usuarios = null;
                        filtrarUsuarios();
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    usuarios = null;
                    filtrarUsuarios();
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), "Error interno del servidor, Reintente", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RespuestaUsuarios> call, Throwable t) {
                usuarios = null;
                filtrarUsuarios();
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Error de conexión con el servidor: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filtrarUsuarios() {
        List<Usuario> usuariosFinal = new ArrayList<>();

        if (usuarios != null) {
            //Remover usuario logueado de la lista y agregar los que solicitaron admin si el cb esta chequedo.
            for (int i = 0; i < usuarios.size(); i++) {
                if (usuarios.get(i).getId() == usuario.getId()) {
                    usuarios.remove(i);
                } else if (usuarios.get(i).getAdmin() == 2 && cbSolicitudAdmin.isChecked())
                    usuariosFinal.add(usuarios.get(i));
            }

            if (ultimaBusqueda != null) {
                usuariosFinal.clear();
                for (Usuario u : usuarios) {
                    if (u.getNombre().toLowerCase().contains(ultimaBusqueda) || String.valueOf(u.getId()).toLowerCase().contains(ultimaBusqueda) || u.getCorreo().toLowerCase().contains(ultimaBusqueda)) {
                        if (cbSolicitudAdmin.isChecked()) {
                            if (u.getAdmin() == 2)
                                usuariosFinal.add(u);
                        } else
                            usuariosFinal.add(u);
                    }
                }
                if (usuariosFinal.size() == 0) {
                    if (!ultimaBusqueda.equals("")) {
                        tvNoHayUsuarios.setText("No hay resultados para \"" + ultimaBusqueda + "\"");
                    } else if (cbSolicitudAdmin.isChecked()) {
                        tvNoHayUsuarios.setText("No hay usuarios solicitantes");
                    } else {
                        usuariosFinal = usuarios;
                        tvNoHayUsuarios.setText("No hay usuarios");
                    }
                }

            } else {
                if (usuariosFinal.size() == 0 && !cbSolicitudAdmin.isChecked())
                    usuariosFinal = usuarios;

                if (usuariosFinal.size() == 0) {
                    if (cbSolicitudAdmin.isChecked())
                        tvNoHayUsuarios.setText("No hay usuarios solicitantes");
                    else
                        tvNoHayUsuarios.setText("No hay usuarios");

                }

            }
        }

        if (usuariosFinal.size() == 0) {
            noHayUsuarios.setVisibility(View.VISIBLE);
        } else {
            noHayUsuarios.setVisibility(View.GONE);
        }
        adaptadorListaUsuarios.actualizarUsuarios(usuariosFinal);
        swipeRefresh.setRefreshing(false);
    }
}
