package com.trampas.trampas;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trampas.trampas.Clases.Usuario;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Perfil extends Fragment {
    Usuario usuario;

    @BindView(R.id.tvNombre)
    TextView tvNombre;

    @BindView(R.id.tvCorreo)
    TextView tvCorreo;

    @BindView(R.id.tvNivelUsuario)
    TextView tvNivelUsuairo;

    @BindView(R.id.tvTrampasColocadas)
    TextView tvTrampasColocadas;

    public Perfil() {
        // Required empty public constructor
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_perfil, container, false);
        ButterKnife.bind(this, v);

        tvNombre.setText(usuario.getNombre() + " " + usuario.getApellido());
        tvCorreo.setText(usuario.getCorreo());
        tvNivelUsuairo.setText((usuario.getAdmin() == 1) ? "Administrador" : "Normal");
        return v;
    }

}
