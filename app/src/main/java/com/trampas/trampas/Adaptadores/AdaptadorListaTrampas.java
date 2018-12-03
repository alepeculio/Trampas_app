package com.trampas.trampas.Adaptadores;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.Respuesta;
import com.trampas.trampas.Clases.Colocacion;
import com.trampas.trampas.Clases.Trampa;
import com.trampas.trampas.Clases.Usuario;
import com.trampas.trampas.DatosTrampa;
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

public class AdaptadorListaTrampas extends RecyclerView.Adapter<AdaptadorListaTrampas.TrampaViewHolder> {
    private List<Trampa> trampas;
    private Context mContext;
    private Usuario usuario;
    private boolean leishmaniasis;

    public AdaptadorListaTrampas(List<Trampa> trampas, Context mContext) {
        this.trampas = trampas;
        this.mContext = mContext;
        this.leishmaniasis = false;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setLeishmaniasis(boolean leishmaniasis) {
        this.leishmaniasis = leishmaniasis;
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
        @BindView(R.id.llBtnIrAlMapa)
        LinearLayout llBtnIrAlMapa;
        @BindView(R.id.btnIrAlMapa)
        Button btnIrAlMapa;
        @BindView(R.id.btnDatosTrampa)
        Button btnDatosTrampa;
        @BindView(R.id.btnEliminarTrampa)
        Button btnEliminar;
        @BindView(R.id.progressBarEliminar)
        ProgressBar progressBarEliminar;

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

            if (usuario.getAdmin() != 1) {
                btnEliminar.setVisibility(View.GONE);
                btnDatosTrampa.setVisibility(View.GONE);
            }

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
            btnEliminar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmarEliminarTrampa(trampa, getAdapterPosition());
                }
            });

            Colocacion c = trampa.getColocacion();
            if (c != null) {
                llDetalles.setVisibility(View.VISIBLE);
                String fechaFin = c.getFechaFin();

                btnDatosTrampa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, DatosTrampa.class);
                        intent.putExtra("trampa", trampa);
                        intent.putExtra("leishmaniasis", leishmaniasis);
                        mContext.startActivity(intent);
                    }
                });

                if (fechaFin == null) {
                    tvColocadaActualmente.setText("Actualmente colocada");
                    tvColocadaActualmente.setTextColor(Color.GREEN);
                    tvDetalles.setText("Información:");
                    llBtnIrAlMapa.setVisibility(View.VISIBLE);
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

                    try {
                        tvHumProm.setText(String.valueOf(c.getHumProm()) + "%");
                        llHumProm.setVisibility(View.VISIBLE);
                        tvTempProm.setText(String.valueOf(c.getTempProm()) + "°C");
                        llTempProm.setVisibility(View.VISIBLE);
                    } catch (NumberFormatException nfe) {
                        Log.d("bindTrampa", nfe.getMessage());
                    }


                }
                llFechaInicio.setVisibility(View.VISIBLE);
                tvFechaInicio.setText(convertFormat(c.getFechaInicio()));
            }

        }

        public void confirmarEliminarTrampa(final Trampa trampa, final int position) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Confirmar eliminación");
            alertDialog.setMessage("¿Está seguro que desea eliminar la trampa?");
            alertDialog.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    eliminarTrampa(trampa, position);
                }
            });
            alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog.show();
        }

        public void eliminarTrampa(final Trampa trampa, final int position) {
            progressBarEliminar.setVisibility(View.VISIBLE);
            btnEliminar.setVisibility(View.GONE);
            BDInterface bd = BDCliente.getClient().create(BDInterface.class);
            Call<Respuesta> call = bd.eliminarTrampa(trampa.getId());
            call.enqueue(new Callback<Respuesta>() {
                @Override
                public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                    progressBarEliminar.setVisibility(View.GONE);
                    btnEliminar.setVisibility(View.VISIBLE);
                    if (response.body() != null) {
                        if (response.body().getCodigo().equals("1")) {
                            trampas.remove(trampa);
                            notifyItemRemoved(position);
                            Snackbar.make(itemView, response.body().getMensaje(), Snackbar.LENGTH_LONG).show();
                        } else {
                            if (mContext != null) {
                                Toast.makeText(mContext, response.body().getMensaje(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        if (mContext != null) {
                            Toast.makeText(mContext, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Respuesta> call, Throwable t) {
                    progressBarEliminar.setVisibility(View.GONE);
                    btnEliminar.setVisibility(View.VISIBLE);
                    if (mContext != null) {
                        Toast.makeText(mContext, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
                    }
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
