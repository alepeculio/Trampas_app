package com.trampas.trampas.Adaptadores;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.Respuesta;
import com.trampas.trampas.Clases.Colocacion;
import com.trampas.trampas.Clases.Trampa;
import com.trampas.trampas.Clases.Usuario;
import com.trampas.trampas.MostrarTrampasColocadasInterface;
import com.trampas.trampas.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdaptadorListaTrampasColocar extends RecyclerView.Adapter<AdaptadorListaTrampasColocar.TrampaViewHolder> {
    private List<Trampa> trampas;
    private Usuario usuario;
    private Context mContext;
    private double lat;
    private double lon;

    public AdaptadorListaTrampasColocar(List<Trampa> trampas, Context mContext) {
        this.trampas = trampas;
        this.mContext = mContext;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @NonNull
    @Override
    public TrampaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lista_trampas_colocar, parent, false);
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
        @BindView(R.id.item_lista_trampas_colocar)
        CardView cardView;
        @BindView(R.id.nombreTrampa)
        TextView tvNombre;
        /*@BindView(R.id.idTrampa)
        TextView tvId;*/

        public TrampaViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.mContext = itemView.getContext();
            if (mContext instanceof MostrarTrampasColocadasInterface) {
                mpi = (MostrarTrampasColocadasInterface) mContext;
            }

        }

        public void bindTrampa(final Trampa trampa) {
            tvNombre.setText(trampa.getNombre());

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    colocar(trampa, getAdapterPosition());
                    return true;
                }
            });
        }

        public void colocar(final Trampa trampa, final int posicion) {
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
