package com.trampas.trampas;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.Respuesta;
import com.trampas.trampas.BD.RespuestaColocaciones;
import com.trampas.trampas.Clases.Usuario;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExportarDatos extends AppCompatActivity {
    @BindView(R.id.tvDesde)
    TextView tvDesde;

    @BindView(R.id.tvHasta)
    TextView tvHasta;

    @BindView(R.id.btnExportar)
    Button btnExportar;

    @BindView(R.id.progressBarExportar)
    ProgressBar progressBarExportar;

    Calendar calendario = Calendar.getInstance();
    Usuario usuario;

    String desde = null;
    String hasta = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exportar_datos);
        getSupportActionBar().setTitle("Exportar datos");
        ButterKnife.bind(this);

        usuario = (Usuario) getIntent().getSerializableExtra("usuario");
        desde = (String) getIntent().getSerializableExtra("desde");
        hasta = (String) getIntent().getSerializableExtra("hasta");

        if (desde != null)
            tvDesde.setText(desde);

        if (hasta != null)
            tvHasta.setText(hasta);


        final DatePickerDialog.OnDateSetListener fechaDesde = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendario.set(Calendar.YEAR, year);
                calendario.set(Calendar.MONTH, monthOfYear);
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "dd/MM/yyyy";
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
                tvDesde.setText(sdf.format(calendario.getTime()));
            }


        };

        tvDesde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog1 = new DatePickerDialog(ExportarDatos.this,
                        fechaDesde,
                        calendario.get(Calendar.YEAR),
                        calendario.get(Calendar.MONTH),
                        calendario.get(Calendar.DAY_OF_MONTH));

                dialog1.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            tvDesde.setText("");
                        }
                    }
                });

                dialog1.show();
            }
        });

        final DatePickerDialog.OnDateSetListener fechaHasta = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendario.set(Calendar.YEAR, year);
                calendario.set(Calendar.MONTH, monthOfYear);
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "dd/MM/yyyy";
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
                tvHasta.setText(sdf.format(calendario.getTime()));
            }

        };

        tvHasta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog2 = new DatePickerDialog(ExportarDatos.this,
                        fechaHasta,
                        calendario.get(Calendar.YEAR),
                        calendario.get(Calendar.MONTH),
                        calendario.get(Calendar.DAY_OF_MONTH));

                dialog2.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            tvHasta.setText("");
                        }
                    }
                });

                dialog2.show();
            }
        });

        btnExportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportar();
            }
        });
    }

    private void exportar() {
        String desde = tvDesde.getText().toString();
        String hasta = tvHasta.getText().toString();

        if (desde.equals("") && !hasta.equals("")) {
            Toast.makeText(this, R.string.seleccionar_fecha_inicio, Toast.LENGTH_SHORT).show();
            return;
        } else if (!desde.equals("") && hasta.equals("")) {
            Toast.makeText(this, R.string.seleccionar_fecha_fin, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!desde.equals("") && !hasta.equals("")) {
            desde = convertFormat(desde);
            hasta = convertFormat(hasta);
        }

        btnExportar.setVisibility(View.GONE);
        progressBarExportar.setVisibility(View.VISIBLE);

        BDInterface bd = BDCliente.getClient().create(BDInterface.class);
        Call<Respuesta> call = bd.exportarDatos(usuario.getCorreo(), desde, hasta);
        call.enqueue(new Callback<Respuesta>() {
            @Override
            public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                btnExportar.setVisibility(View.VISIBLE);
                progressBarExportar.setVisibility(View.GONE);

                if (response.body() != null) {
                    if (response.body().getCodigo().equals("0")) {
                        Snackbar.make(getWindow().getDecorView().getRootView(), response.body().getMensaje(), Snackbar.LENGTH_LONG).show();
                        tvDesde.setText("");
                        tvHasta.setText("");
                    } else {
                        Toast.makeText(ExportarDatos.this, response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ExportarDatos.this, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Respuesta> call, Throwable t) {
                btnExportar.setVisibility(View.VISIBLE);
                progressBarExportar.setVisibility(View.GONE);
                Toast.makeText(ExportarDatos.this, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
            }

        });
    }

    //Transformar fechas.
    public String convertFormat(String inputDate) {
        if (inputDate == null)
            return "";

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = simpleDateFormat.parse(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date == null)
            return "";

        @SuppressLint("SimpleDateFormat") SimpleDateFormat convetDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return convetDateFormat.format(date);
    }
}
