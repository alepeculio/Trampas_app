package com.trampas.trampas.Adaptadores;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trampas.trampas.AgregarTrampa;
import com.trampas.trampas.Clases.Colocacion;
import com.trampas.trampas.Clases.Trampa;
import com.trampas.trampas.ColocarTrampa;
import com.trampas.trampas.DatosTrampa;
import com.trampas.trampas.MostrarTrampasColocadasInterface;
import com.trampas.trampas.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdaptadorListaTrampas extends RecyclerView.Adapter<AdaptadorListaTrampas.TrampaViewHolder> {
    private List<Trampa> trampas;
    private Context mContext;

    public AdaptadorListaTrampas(List<Trampa> trampas, Context mContext) {
        this.trampas = trampas;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public TrampaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lista_trampas, parent, false);
        TrampaViewHolder trampaViewHolder = new TrampaViewHolder(view);
        return trampaViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TrampaViewHolder holder, int position) {
        // YoYo.with(Techniques.ZoomIn).duration(500).playOn(holder.cardView);
        holder.bindTrampa(trampas.get(position));
    }

    @Override
    public int getItemCount() {
        return trampas.size();
    }

    public class TrampaViewHolder extends RecyclerView.ViewHolder {
        private MostrarTrampasColocadasInterface mpi;
        private Context mContext;
        @BindView(R.id.item_lista_trampas)
        CardView cardView;
        @BindView(R.id.expandir)
        LinearLayout expandir;
        @BindView(R.id.btnExpandir)
        Button btnExpandir;
        @BindView(R.id.nombreTrampa)
        TextView tvNombre;
        @BindView(R.id.idTrampa)
        TextView tvId;
        @BindView(R.id.colocadaActualmente)
        TextView tvColocadaActualmente;
        @BindView(R.id.fechaInicio)
        TextView tvFechaInicio;
        @BindView(R.id.fechaFin)
        TextView tvFechaFin;
        @BindView(R.id.macTrampa)
        TextView tvMac;
        @BindView(R.id.humProm)
        TextView tvHumProm;
        @BindView(R.id.tempProm)
        TextView tvTempProm;
        @BindView(R.id.llDetalles)
        LinearLayout llDetalles;
        @BindView(R.id.detalles)
        TextView tvDetalles;
        @BindView(R.id.llFechaInicio)
        LinearLayout llFechaInicio;
        @BindView(R.id.llFechaFin)
        LinearLayout llFechaFin;
        @BindView(R.id.llTempProm)
        LinearLayout llTempProm;
        @BindView(R.id.llHumProm)
        LinearLayout llHumProm;
        @BindView(R.id.btnIrAlMapa)
        Button btnIrAlMapa;
        @BindView(R.id.btnDatosTrampa)
        Button btnDatosTrampa;

        public TrampaViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.mContext = itemView.getContext();
            if (mContext instanceof MostrarTrampasColocadasInterface) {
                mpi = (MostrarTrampasColocadasInterface) mContext;
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expandir(btnExpandir);
                }
            });

            btnExpandir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expandir(v);
                }
            });
        }

        public void expandir(View v) {
            if (expandir.getVisibility() == View.GONE && v.getRotation() == 0) {
                ViewPropertyAnimator viewPropertyAnimator = v.animate().rotationBy(90);
                viewPropertyAnimator.setDuration(150);
                viewPropertyAnimator.withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        expandir.setVisibility(View.VISIBLE);
                    }
                });
                viewPropertyAnimator.start();
            } else if (expandir.getVisibility() == View.VISIBLE && v.getRotation() == 90) {
                ViewPropertyAnimator viewPropertyAnimator = v.animate().rotationBy(-90);
                viewPropertyAnimator.setDuration(150);
                viewPropertyAnimator.withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        expandir.setVisibility(View.GONE);
                    }
                });
                viewPropertyAnimator.start();

            }
        }

        public void bindTrampa(final Trampa trampa) {
            tvNombre.setText(trampa.getNombre());
            tvId.setText(String.valueOf(trampa.getId()));
            tvMac.setText(trampa.getMac());

            Colocacion c = trampa.getColocacion();
            if (c != null) {
                llDetalles.setVisibility(View.VISIBLE);
                String fechaFin = c.getFechaFin();

                btnDatosTrampa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, DatosTrampa.class);
                        intent.putExtra("trampa", trampa);
                        mContext.startActivity(intent);
                    }
                });

                if (fechaFin == null) {
                    tvColocadaActualmente.setText("Actualmente colocada");
                    tvColocadaActualmente.setTextColor(Color.GREEN);
                    tvDetalles.setText("Información:");
                    btnIrAlMapa.setVisibility(View.VISIBLE);
                    final String codigo = String.valueOf(c.getId());
                    btnIrAlMapa.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mpi.irAlMapa(codigo);
                        }
                    });

                } else {
                    tvColocadaActualmente.setText("No colocada actualmente");
                    tvColocadaActualmente.setTextColor(Color.RED);
                    tvDetalles.setText("Información ultima vez:");
                    llFechaFin.setVisibility(View.VISIBLE);
                    tvFechaFin.setText(convertFormat(fechaFin));

                    String humProm = c.getHumProm();
                    if (humProm != null) {
                        tvHumProm.setText(humProm + "%");
                        llHumProm.setVisibility(View.VISIBLE);
                    }

                    String tempProm = c.getTempProm();
                    if (tempProm != null) {
                        tvTempProm.setText(tempProm + "°C");
                        llTempProm.setVisibility(View.VISIBLE);
                    }
                }
                llFechaInicio.setVisibility(View.VISIBLE);
                tvFechaInicio.setText(convertFormat(c.getFechaInicio()));
            }

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

    }

    public void actualizarTrampas(List<Trampa> ts) {
        trampas = new ArrayList<>();
        trampas.addAll(ts);
        notifyDataSetChanged();
    }

    public void agregarTrampa(Trampa trampa, int posicion) {
        trampas.add(posicion, trampa);
        notifyItemInserted(posicion);
    }
}
