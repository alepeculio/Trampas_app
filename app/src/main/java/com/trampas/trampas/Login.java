package com.trampas.trampas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.RespuestaLogin;
import com.trampas.trampas.Clases.Sha1Hash;
import com.trampas.trampas.Clases.Usuario;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    @BindView(R.id.etCorreo)
    EditText etCorreo;
    @BindView(R.id.etContrasenia)
    EditText etContrasenia;
    @BindView(R.id.btnIniciarSesion)
    Button btnIniciarSesion;

    @BindView(R.id.etCorreoError)
    TextInputLayout etCorreoError;
    @BindView(R.id.etContraseniaError)
    TextInputLayout etContraseniaError;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.btnRegistrarse)
    Button btnRegistrarse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesion();
            }
        });
        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registro.class);
                startActivity(intent);
            }
        });

        etCorreo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    etCorreoError.setError("");
            }
        });

        etContrasenia.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    etContraseniaError.setError("");
            }
        });
    }

    private void iniciarSesion() {
        String correo = etCorreo.getText().toString().trim();
        String contrasenia = etContrasenia.getText().toString().trim();

        Boolean error = false;

        if (correo.equals("")) {
            etCorreoError.setError(getString(R.string.campo_requerido));
            error = true;
        } else if (!isEmailValid(correo)) {
            etCorreoError.setError(getString(R.string.correo_invalido));
            error = true;
        }

        if (contrasenia.equals("")) {
            etContraseniaError.setError(getString(R.string.campo_requerido));
            error = true;
        } else {
            try {
                contrasenia = Sha1Hash.SHA1(contrasenia);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "No se pudo encriptar la contraseña, reintente.", Toast.LENGTH_SHORT).show();
                error = true;
            }
        }

        if (error)
            return;

        btnIniciarSesion.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        verificarUsuario(correo, contrasenia);


    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void verificarUsuario(final String correo, final String contrasenia) {

        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<RespuestaLogin> call = bd.login(correo, contrasenia);
        call.enqueue(new Callback<RespuestaLogin>() {
            @Override
            public void onResponse(Call<RespuestaLogin> call, Response<RespuestaLogin> response) {
                btnIniciarSesion.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                if (response.body() != null) {
                    if (response.body().getCodigo().equals("1") && response.body().getUsuario() != null) {
                        guardarUsuario(response.body().getUsuario());
                    } else {
                        etContraseniaError.setError(response.body().getMensaje());
                    }
                } else {
                    Toast.makeText(Login.this, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaLogin> call, Throwable t) {
                btnIniciarSesion.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Login.this, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarUsuario(Usuario usuario) {
        SharedPreferences sp = getSharedPreferences("usuario_guardado", MODE_PRIVATE);
        SharedPreferences.Editor et = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(usuario);
        et.putString("usuario", json);
        et.apply();

        cargarInicio();
    }


    private void cargarInicio() {
        Intent i = new Intent(Login.this, MenuPrincipal.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

}
