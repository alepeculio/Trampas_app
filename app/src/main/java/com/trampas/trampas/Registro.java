package com.trampas.trampas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.trampas.trampas.Clases.Sha1Hash;

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
            etNombreError.setError(getString(R.string.campo_requerido));
            error = true;
        }

        if (apellido.equals("")) {
            etApellidoError.setError(getString(R.string.campo_requerido));
            error = true;
        }

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
            }
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
                btnRegistrarse.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                if (response.body() != null) {
                    if (response.body().getCodigo().equals("1")) {
                        Snackbar.make(getWindow().getDecorView().getRootView(), response.body().getMensaje(), Snackbar.LENGTH_LONG).show();
                        if (admin == 2)
                            mensajeAdmin(login);
                        else
                            startActivity(login);
                    } else {
                        etCorreoError.setError(response.body().getMensaje());
                    }
                } else {
                    Toast.makeText(Registro.this, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Respuesta> call, Throwable t) {
                btnRegistrarse.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Registro.this, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
            }
        });


    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void mensajeAdmin(final Intent login) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.registro_admin_titulo);
        alertDialog.setMessage(R.string.registro_admin_mensaje);
        alertDialog.setPositiveButton(R.string.registro_admin_boton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(login);
            }
        });
        alertDialog.show();
    }
}


