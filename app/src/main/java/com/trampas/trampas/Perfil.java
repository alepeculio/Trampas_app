package com.trampas.trampas;


import android.annotation.SuppressLint;
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

    /*@BindView(R.id.tvTrampasColocadas)
    TextView tvTrampasColocadas;*/

    public Perfil() {
        // Required empty public constructor
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_perfil, container, false);
        ButterKnife.bind(this, v);

        tvNombre.setText(usuario.getNombre() + " " + usuario.getApellido());
        tvCorreo.setText(usuario.getCorreo());
        String priviegio;

        if (usuario.getAdmin() == 1) {
            priviegio = "Administrador";
        } else if (usuario.getAdmin() == 3) {
            priviegio = "Visitante";
        } else {
            priviegio = "Normal";
        }

        tvNivelUsuairo.setText(priviegio);
        return v;
    }

}
