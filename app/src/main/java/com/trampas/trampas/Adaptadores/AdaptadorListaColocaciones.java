package com.trampas.trampas.Adaptadores;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.Respuesta;
import com.trampas.trampas.Clases.Colocacion;
import com.trampas.trampas.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdaptadorListaColocaciones extends RecyclerView.Adapter<AdaptadorListaColocaciones.ColocacionViewHolder> {
    private List<Colocacion> colocaciones;
    private Context mContext;

    public AdaptadorListaColocaciones(List<Colocacion> colocaciones, Context mContext) {
        this.colocaciones = colocaciones;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ColocacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lista_colocaciones, parent, false);
        ColocacionViewHolder colocacionViewHolder = new ColocacionViewHolder(view);
        return colocacionViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ColocacionViewHolder holder, int position) {
        // YoYo.with(Techniques.ZoomIn).duration(500).playOn(holder.cardView);
        holder.bindColocacion(colocaciones.get(position));
    }

    @Override
    public int getItemCount() {
        return colocaciones.size();
    }

    public class ColocacionViewHolder extends RecyclerView.ViewHolder {
        private Context mContext;
        /* @BindView(R.id.item_lista_colocaciones)
         CardView cardView;*/
        @BindView(R.id.detalles)
        TextView tvDetalles;
        @BindView(R.id.fechaInicio)
        EditText etFechaInicio;
        @BindView(R.id.fechaFin)
        EditText etFechaFin;
        @BindView(R.id.lat)
        EditText etLat;
        @BindView(R.id.lon)
        EditText etLon;
        @BindView(R.id.tempMin)
        EditText etTempMin;
        @BindView(R.id.tempMax)
        EditText etTempMax;
        @BindView(R.id.tempProm)
        EditText etTempProm;
        @BindView(R.id.humMin)
        EditText etHumMin;
        @BindView(R.id.humMax)
        EditText etHumMax;
        @BindView(R.id.humProm)
        EditText etHumProm;
        @BindView(R.id.cbLeishmaniasis)
        CheckBox cbLeishmaniasis;
        @BindView(R.id.llEditar)
        LinearLayout llEditar;
        @BindView(R.id.llEditarCampos)
        LinearLayout llEditarCampos;
        @BindView(R.id.llGuardar)
        LinearLayout llGuardar;
        @BindView(R.id.llCancelar)
        LinearLayout llCancelar;
        @BindView(R.id.llProgressBar)
        LinearLayout llProgressBar;
        @BindView(R.id.btnGuardar)
        Button btnGuardar;
        @BindView(R.id.btnCancelar)
        Button btnCancelar;
        @BindView(R.id.llLeishmaniasis)
        LinearLayout llLeishmaniasis;
        @BindView(R.id.perros)
        EditText etPerros;
        @BindView(R.id.cantFlevotomo)
        EditText etCantFlevotomo;


        public ColocacionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.mContext = itemView.getContext();
        }

        public void bindColocacion(final Colocacion colocacion) {
            tvDetalles.setText(mContext.getString(R.string.detalles_de_colocacion) + String.valueOf(colocacion.getId()));
            etFechaInicio.setText(convertFormat(colocacion.getFechaInicio()));
            etFechaFin.setText(convertFormat(colocacion.getFechaFin()));
            etLat.setText(String.valueOf(colocacion.getLat()));
            etLon.setText(String.valueOf(colocacion.getLon()));


            try {
                etTempMin.setText(String.valueOf(colocacion.getTempMin()));
                etTempMax.setText(String.valueOf(colocacion.getTempMax()));
                etTempProm.setText(String.valueOf(colocacion.getTempProm()));
                etHumMin.setText(String.valueOf(colocacion.getHumMin()));
                etHumMax.setText(String.valueOf(colocacion.getHumMax()));
                etHumProm.setText(String.valueOf(colocacion.getHumProm()));
            } catch (NumberFormatException nfe) {
                Log.d("bindColocacion", nfe.getMessage());
            }


            checkLesishmaniasis(colocacion.getLeishmaniasis());
            etCantFlevotomo.setText(String.valueOf(colocacion.getFlevotomo()));
            etPerros.setText(String.valueOf(colocacion.getPerros()));

            cbLeishmaniasis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkLesishmaniasis(isChecked);
                }
            });


            View.OnClickListener editar = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setEditable(true);
                }
            };
            View.OnClickListener cancelar = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setEditable(false);
                }
            };
            View.OnClickListener actualizar = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guardarCambios(colocacion);
                }
            };

            llEditar.setOnClickListener(editar);
            llEditar.findViewById(R.id.btnEditar).setOnClickListener(editar);

            llGuardar.setOnClickListener(actualizar);
            btnGuardar.setOnClickListener(actualizar);

            llCancelar.setOnClickListener(cancelar);
            btnCancelar.setOnClickListener(cancelar);

            setEditable(false);

        }

        private void setEditable(boolean editable) {
            etFechaInicio.setEnabled(editable);
            etFechaFin.setEnabled(editable);
            etLat.setEnabled(editable);
            etLon.setEnabled(editable);
            etTempMin.setEnabled(editable);
            etTempMax.setEnabled(editable);
            etTempProm.setEnabled(editable);
            etHumMin.setEnabled(editable);
            etHumMax.setEnabled(editable);
            etHumProm.setEnabled(editable);
            cbLeishmaniasis.setEnabled(editable);
            etCantFlevotomo.setEnabled(editable);
            etPerros.setEnabled(editable);

            if (editable) {
                llEditarCampos.setVisibility(View.VISIBLE);
                llEditar.setVisibility(View.GONE);
            } else {
                llEditarCampos.setVisibility(View.GONE);
                llEditar.setVisibility(View.VISIBLE);
            }
        }

        private void setCargando(boolean cargando) {
            llGuardar.setEnabled(!cargando);
            btnGuardar.setEnabled(!cargando);
            btnCancelar.setEnabled(!cargando);
            llCancelar.setEnabled(!cargando);

            if (cargando)
                llProgressBar.setVisibility(View.VISIBLE);
            else
                llProgressBar.setVisibility(View.GONE);
        }

        public boolean checkLesishmaniasis(boolean leishmaniasis) {
            cbLeishmaniasis.setChecked(leishmaniasis);
            if (leishmaniasis) {
                cbLeishmaniasis.setTextColor(mContext.getResources().getColor(R.color.colorRojo));
                llLeishmaniasis.setVisibility(View.VISIBLE);
            } else {
                cbLeishmaniasis.setTextColor(mContext.getResources().getColor(R.color.colorGris));
                llLeishmaniasis.setVisibility(View.GONE);
            }

            return leishmaniasis;
        }

        private void guardarCambios(Colocacion colocacion) {
            setCargando(true);
            double lat = 0;
            double lon = 0;
            String fInicio = "";
            String fFin = "";
            float tMin = 0;
            float tMax = 0;
            float tProm = 0;
            float hMin = 0;
            float hMax = 0;
            float hProm = 0;
            int leishmaniasis = (cbLeishmaniasis.isChecked()) ? 1 : 0;

            try {
                lat = Double.valueOf(etLat.getText().toString().trim());
                lon = Double.valueOf(etLon.getText().toString().trim());
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date fI = sdf.parse(etFechaInicio.getText().toString().trim());
                fInicio = simpleDateFormat.format(fI);
                Date fF = sdf.parse(etFechaFin.getText().toString().trim());
                fFin = simpleDateFormat.format(fF);
                tMin = Float.valueOf(etTempMin.getText().toString().trim());
                tMax = Float.valueOf(etTempMax.getText().toString().trim());
                tProm = Float.valueOf(etTempProm.getText().toString().trim());
                hMin = Float.valueOf(etHumMin.getText().toString().trim());
                hMax = Float.valueOf(etHumMax.getText().toString().trim());
                hProm = Float.valueOf(etHumProm.getText().toString().trim());
            } catch (Exception e) {
                Toast.makeText(mContext, R.string.datos_incorrectos, Toast.LENGTH_SHORT).show();
                setCargando(false);
                return;
            }

            String flevotomo = etCantFlevotomo.getText().toString().trim();
            String perros = etPerros.getText().toString().trim();


            BDInterface bd = BDCliente.getClient().create(BDInterface.class);
            Call<Respuesta> call = bd.actualizarColocacion(colocacion.getId(), lat, lon, fInicio, fFin, tMin, tMax, tProm, hMin, hMax, hProm, leishmaniasis, flevotomo, perros);
            call.enqueue(new Callback<Respuesta>() {
                @Override
                public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                    setCargando(false);
                    if (response.body() != null) {
                        if (response.body().getCodigo().equals("1")) {
                            Snackbar.make(itemView, response.body().getMensaje(), Snackbar.LENGTH_LONG).show();
                            setEditable(false);
                        } else {
                            Toast.makeText(mContext, response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mContext, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Respuesta> call, Throwable t) {
                    setCargando(false);
                    Toast.makeText(mContext, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Transformar fechas.
        public String convertFormat(String inputDate) {
            if (inputDate == null)
                return "";

            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = null;
            try {
                date = simpleDateFormat.parse(inputDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (date == null)
                return "";

            @SuppressLint("SimpleDateFormat") SimpleDateFormat convetDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            return convetDateFormat.format(date);
        }

    }

    public void actualizarColocaciones(List<Colocacion> ts) {
        colocaciones = new ArrayList<>();
        colocaciones.addAll(ts);
        notifyDataSetChanged();
    }

    public void agregarColocacion(Colocacion colocacion, int posicion) {
        colocaciones.add(posicion, colocacion);
        notifyItemInserted(posicion);
    }
}
