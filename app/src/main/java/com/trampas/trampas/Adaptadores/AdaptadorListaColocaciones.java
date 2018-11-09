package com.trampas.trampas.Adaptadores;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.trampas.trampas.Clases.Colocacion;
import com.trampas.trampas.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        TextView tvFechaInicio;
        @BindView(R.id.fechaFin)
        TextView tvFechaFin;
        @BindView(R.id.lat)
        TextView tvLat;
        @BindView(R.id.lon)
        TextView tvLon;
        @BindView(R.id.tempMin)
        TextView tvTempMin;
        @BindView(R.id.tempMax)
        TextView tvTempMax;
        @BindView(R.id.tempProm)
        TextView tvTempProm;
        @BindView(R.id.humMin)
        TextView tvHumMin;
        @BindView(R.id.humMax)
        TextView tvHumMax;
        @BindView(R.id.humProm)
        TextView tvHumProm;
        @BindView(R.id.cbLeishmaniasis)
        CheckBox cbLeishmaniasis;


        public ColocacionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.mContext = itemView.getContext();
        }

        public void bindColocacion(Colocacion colocacion) {
            tvDetalles.setText("Detalles de colocación: " + String.valueOf(colocacion.getId()));
            tvFechaInicio.setText(convertFormat(colocacion.getFechaInicio()));
            tvFechaFin.setText(convertFormat(colocacion.getFechaFin()));
            tvLat.setText(String.valueOf(colocacion.getLat()));
            tvLon.setText(String.valueOf(colocacion.getLon()));

            String tempMin = colocacion.getTempMin();
            if (tempMin != null)
                tvTempMin.setText(tempMin + "°C");

            String tempMax = colocacion.getTempMax();
            if (tempMax != null)
                tvTempMax.setText(tempMax + "°C");

            String tempProm = colocacion.getTempProm();
            if (tempProm != null)
                tvTempProm.setText(tempProm + "°C");

            String humMin = colocacion.getHumMin();
            if (humMin != null)
                tvHumMin.setText(humMin + "%");

            String humMax = colocacion.getHumMax();
            if (humMax != null)
                tvHumMax.setText(humMax + "%");

            String humProm = colocacion.getHumProm();
            if (humProm != null)
                tvHumProm.setText(humProm + "%");

            checkLesishmaniasis(colocacion.getLeishmaniasis());
            cbLeishmaniasis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkLesishmaniasis(isChecked);
                    Toast.makeText(mContext, "Actualizar datos", Toast.LENGTH_SHORT).show();
                }
            });

        }

        public void checkLesishmaniasis(boolean leishmaniasis) {
            cbLeishmaniasis.setChecked(leishmaniasis);
            if (leishmaniasis)
                cbLeishmaniasis.setTextColor(mContext.getResources().getColor(R.color.colorRojo));
            else
                cbLeishmaniasis.setTextColor(mContext.getResources().getColor(R.color.colorGris));

        }

        //Transformar fechas.
        public String convertFormat(String inputDate) {
            if (inputDate == null)
                return "";

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = null;
            try {
                date = simpleDateFormat.parse(inputDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (date == null)
                return "";

            SimpleDateFormat convetDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            return convetDateFormat.format(date);
        }

       /* public void colocar(final Trampa trampa, final int posicion) {
            if (lat != 0 && lon != 0) {
                BDInterface bd = BDCliente.getClient().create(BDInterface.class);
                Call<Respuesta> call = bd.colocarTrampa(lat, lon, trampa.getId(), usuario.getId());
                call.enqueue(new Callback<Respuesta>() {
                    @Override
                    public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                        if (response.body() != null) {
                            if (!response.body().getCodigo().equals("0")) {
                                //cargando(false);

                                trampas.remove(trampa);
                                notifyItemRemoved(posicion);

                                final String codigo = response.body().getCodigo();
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle("Trampa colocada");
                                builder.setMessage("¿Quiere ver su ubicación en el mapa?");

                                builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mpi.irAlMapa(codigo);
                                    }
                                });

                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });

                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            } else {
                                Toast.makeText(mContext, response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                                //cargando(false);
                            }
                        } else {
                            Toast.makeText(mContext, "Error interno del servidor, reintente.", Toast.LENGTH_SHORT).show();
                            //cargando(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<Respuesta> call, Throwable t) {
                        Toast.makeText(mContext, "Error de conexión con el servidor: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        // cargando(false);
                    }
                });
            }
        }*/

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
