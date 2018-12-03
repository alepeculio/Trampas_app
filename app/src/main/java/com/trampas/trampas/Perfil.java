package com.trampas.trampas;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.Respuesta;
import com.trampas.trampas.Clases.Usuario;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Perfil extends Fragment {
    Usuario usuario;

    @BindView(R.id.tvNombre)
    TextView tvNombre;

    @BindView(R.id.tvCorreo)
    TextView tvCorreo;

    @BindView(R.id.tvNivelUsuario)
    TextView tvNivelUsuairo;

    @BindView(R.id.etContrasenia)
    EditText etContrasenia;

    @BindView(R.id.etContraseniaError)
    TextInputLayout etContraseniaError;

    @BindView(R.id.etContraseniaNueva)
    EditText etContraseniaNueva;

    @BindView(R.id.etContraseniaNuevaError)
    TextInputLayout etContraseniaNuevaError;

    @BindView(R.id.btnCambiar)
    Button btnCambiar;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private Context mContext;

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

        if (usuario == null && mContext != null)
            usuario = ((MenuPrincipal) mContext).getUsuario();

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

        tvNombre.setFocusable(true);

        btnCambiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarContrasenia();
            }
        });

        etContrasenia.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    etContraseniaError.setError("");
            }
        });

        etContraseniaNueva.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    etContraseniaNuevaError.setError("");
            }
        });

        tvNivelUsuairo.setText(priviegio);
        return v;
    }

    public void cambiarContrasenia() {
        String actual = etContrasenia.getText().toString().trim();
        String nueva = etContraseniaNueva.getText().toString().trim();
        boolean error = false;

        if (actual.equals("")) {
            etContraseniaError.setError(getString(R.string.campo_requerido));
            error = true;
        }

        if (nueva.equals("")) {
            etContraseniaNuevaError.setError(getString(R.string.campo_requerido));
            error = true;
        }

        if (error)
            return;

        progressBar.setVisibility(View.VISIBLE);
        btnCambiar.setVisibility(View.GONE);
        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<Respuesta> call = bd.cambiarContrasenia(usuario.getId(), actual, nueva);
        call.enqueue(new Callback<Respuesta>() {
            @Override
            public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                progressBar.setVisibility(View.GONE);
                btnCambiar.setVisibility(View.VISIBLE);
                if (response.body() != null) {
                    if (response.body().getCodigo().equals("1")) {
                        Snackbar.make(((MenuPrincipal) mContext).getWindow().getDecorView().getRootView(), response.body().getMensaje(), Snackbar.LENGTH_LONG).show();
                        etContrasenia.setText("");
                        etContraseniaNueva.setText("");
                    } else if (response.body().getCodigo().equals("2")) {
                        etContraseniaError.setError(response.body().getMensaje());
                    } else {
                        Toast.makeText(mContext, response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Respuesta> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCambiar.setVisibility(View.VISIBLE);
                Toast.makeText(mContext, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}
