package com.trampas.trampas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.Respuesta;
import com.trampas.trampas.BD.RespuestaLogin;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verificarLogin();
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesion();
            }
        });
    }

    private void iniciarSesion() {
        String correo = etCorreo.getText().toString().trim();
        String contrasenia = etContrasenia.getText().toString().trim();

        etCorreoError.setError(null);
        etContraseniaError.setError(null);

        if (correo.equals("")) {
            etCorreoError.setError("Correo incorrecto");
            return;
        } else if (contrasenia.equals("")) {
            etContraseniaError.setError("Contraseña incorrecta");
            return;
        } else {
            btnIniciarSesion.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            verificarUsuario(correo, contrasenia);
        }
    }

    private void verificarUsuario(final String correo, final String contrasenia) {

        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<RespuestaLogin> call = bd.login(correo, contrasenia);
        call.enqueue(new Callback<RespuestaLogin>() {
            @Override
            public void onResponse(Call<RespuestaLogin> call, Response<RespuestaLogin> response) {
                if (response.body() != null) {
                    if (response.body().getCodigo().equals("1") && response.body().getUsuario() != null) {
                        guardarUsuario(response.body().getUsuario());
                    } else {
                        Toast.makeText(Login.this, response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                        btnIniciarSesion.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(Login.this, "Error interno del servidor, Reintente.", Toast.LENGTH_SHORT).show();
                    btnIniciarSesion.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<RespuestaLogin> call, Throwable t) {
                Toast.makeText(Login.this, "Error de conexión con el servidor: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnIniciarSesion.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

            }
        });
    }

    private void guardarUsuario(Usuario usuario) {
        SharedPreferences sp = getSharedPreferences("usuario_guardado", MODE_PRIVATE);
        SharedPreferences.Editor et = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(usuario);
        et.putString("usuario", json);
        et.commit();

        cargarInicio();
    }

    private void verificarLogin() {
        SharedPreferences sp = getSharedPreferences("usuario_guardado", MODE_PRIVATE);
        String jsonUsuario = sp.getString("usuario", null);

        if (jsonUsuario != null)
            cargarInicio();

    }

    private void cargarInicio() {
        Intent i = new Intent(Login.this, MenuPrincipal.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

}
