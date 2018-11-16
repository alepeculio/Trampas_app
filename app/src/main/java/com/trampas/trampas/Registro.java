package com.trampas.trampas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.Respuesta;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Registro extends AppCompatActivity {
    @BindView(R.id.etNombre)
    EditText etNombre;

    @BindView(R.id.etNombreError)
    TextInputLayout etNombreError;

    @BindView(R.id.etApellido)
    EditText etApellido;

    @BindView(R.id.etApellidoError)
    TextInputLayout etApellidoError;

    @BindView(R.id.etCorreo)
    EditText etCorreo;

    @BindView(R.id.etCorreoError)
    TextInputLayout etCorreoError;

    @BindView(R.id.etContrasenia)
    EditText etContrasenia;

    @BindView(R.id.etContraseniaError)
    TextInputLayout etContraseniaError;

    @BindView(R.id.swNivelPrivilegio)
    Switch swNivelPrivilegio;

    @BindView(R.id.tvNivelPrivilegio)
    TextView tvNivelPrivilegio;

    @BindView(R.id.btnRegistrarse)
    Button btnRegistrarse;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        getSupportActionBar().setTitle("Registro");
        ButterKnife.bind(this);

        swNivelPrivilegio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    tvNivelPrivilegio.setText("Administrador");
                else {
                    tvNivelPrivilegio.setText("Usuario");
                }
            }
        });

        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarse();
            }
        });

        etNombre.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    etNombreError.setError("");
            }
        });

        etApellido.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    etApellidoError.setError("");
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

    void registrarse() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String contrasenia = etContrasenia.getText().toString();
        final int admin = (swNivelPrivilegio.isChecked()) ? 2 : 0;
        Boolean error = false;

        if (nombre.equals("")) {
            etNombreError.setError("Campo requerido");
            error = true;
        }

        if (apellido.equals("")) {
            etApellidoError.setError("Campo requerido");
            error = true;
        }

        if (correo.equals("")) {
            etCorreoError.setError("Campo requerido");
            error = true;
        } else if (!isEmailValid(correo)) {
            etCorreoError.setError("Correo inválido");
            error = true;
        }

        if (contrasenia.equals("")) {
            etContraseniaError.setError("Campo requerido");
            error = true;
        }

        if (error)
            return;

        final Intent login = getParentActivityIntent();
        progressBar.setVisibility(View.VISIBLE);
        btnRegistrarse.setVisibility(View.GONE);
        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<Respuesta> call = bd.agregarUsuario(nombre, apellido, correo, contrasenia, admin);
        call.enqueue(new Callback<Respuesta>() {
            @Override
            public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                if (response.body() != null) {
                    if (response.body().getCodigo().equals("1")) {
                        progressBar.setVisibility(View.GONE);
                        btnRegistrarse.setVisibility(View.VISIBLE);
                        Toast.makeText(Registro.this, response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                        if (admin == 2)
                            mensajeAdmin(login);
                        else
                            startActivity(login);
                    } else {
                        btnRegistrarse.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        etCorreoError.setError(response.body().getMensaje());
                    }
                } else {
                    btnRegistrarse.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Registro.this, "Error interno del servidor, Reintente", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Respuesta> call, Throwable t) {
                btnRegistrarse.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Registro.this, "Error de conexión con el servidor: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void mensajeAdmin(final Intent login) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Privilegio administrador");
        alertDialog.setMessage("Se requiere una validación para esta opción, mientras permanecerá con privilegios de usuario normal.");
        alertDialog.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(login);
            }
        });
        alertDialog.show();
    }
}
