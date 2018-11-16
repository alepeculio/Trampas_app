package com.trampas.trampas.Adaptadores;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.trampas.trampas.AdministrarUsuarios;
import com.trampas.trampas.BD.BDCliente;
import com.trampas.trampas.BD.BDInterface;
import com.trampas.trampas.BD.Respuesta;
import com.trampas.trampas.Clases.Usuario;
import com.trampas.trampas.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdaptadorListaUsuarios extends RecyclerView.Adapter<AdaptadorListaUsuarios.UsuarioViewHolder> {
    private List<Usuario> usuarios;
    private Context mContext;
    private AdministrarUsuarios administrarUsuarios;

    public AdaptadorListaUsuarios(List<Usuario> usuarios, Context mContext, AdministrarUsuarios administrarUsuarios) {
        this.usuarios = usuarios;
        this.mContext = mContext;
        this.administrarUsuarios = administrarUsuarios;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lista_usuarios, parent, false);
        UsuarioViewHolder usuarioViewHolder = new UsuarioViewHolder(view);
        return usuarioViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        holder.bindUsuario(usuarios.get(position));
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public class UsuarioViewHolder extends RecyclerView.ViewHolder {
        private Context context;

        @BindView(R.id.tvNombre)
        TextView tvNombre;

        @BindView(R.id.tvCorreo)
        TextView tvCorreo;

        @BindView(R.id.tvPrivilegio)
        TextView tvPrivilegio;

        @BindView(R.id.swNivelPrivilegio)
        SwitchCompat swNivelPrivilegio;

        @BindView(R.id.btnEliminar)
        Button btnEliminar;

        @BindView(R.id.progressBarEliminar)
        ProgressBar progressBarEliminar;

        public UsuarioViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext = itemView.getContext();
        }

        @SuppressLint("SetTextI18n")
        public void bindUsuario(final Usuario usuario) {
            tvNombre.setText(usuario.getNombre() + " " + usuario.getApellido());
            tvCorreo.setText(usuario.getCorreo());
            String privilegio;
            if (usuario.getAdmin() == 1) {
                privilegio = "Administrador";
                swNivelPrivilegio.setChecked(true);
            } else {
                privilegio = "Usuario";
                swNivelPrivilegio.setChecked(false);
            }
            tvPrivilegio.setText(privilegio);


            swNivelPrivilegio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actualizarPrivilegios(usuario, swNivelPrivilegio.isChecked());
                }
            });

            btnEliminar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmarEliminarUsuario(usuario, getAdapterPosition());
                }
            });
        }

        private void cambiarPrivilegio(boolean isChecked) {
            if (isChecked)
                tvPrivilegio.setText("Administrador");
            else
                tvPrivilegio.setText("Usuario");
        }

        private void actualizarPrivilegios(final Usuario usuario, final boolean isChecked) {
            final int admin;
            if (isChecked) {
                admin = 1;
            } else {
                admin = 0;
            }
            tvPrivilegio.setText(R.string.cambiando_privilegio);
            BDInterface bd = BDCliente.getClient().create(BDInterface.class);
            Call<Respuesta> call = bd.actualizarPrivilegios(usuario.getId(), admin);
            call.enqueue(new Callback<Respuesta>() {
                @Override
                public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                    if (response.body() != null) {
                        if (response.body().getCodigo().equals("1")) {
                            cambiarPrivilegio(isChecked);
                            Snackbar.make(itemView, response.body().getMensaje(), Snackbar.LENGTH_LONG).show();
                            for (Usuario u : usuarios) {
                                if (u.getId() == usuario.getId()) {
                                    u.setAdmin(admin);
                                }
                            }
                            administrarUsuarios.setUsuarios(usuarios);
                        } else {
                            cambiarPrivilegio(!isChecked);
                            Toast.makeText(mContext, response.body().getMensaje(), Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        cambiarPrivilegio(!isChecked);
                        Toast.makeText(mContext, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Respuesta> call, Throwable t) {
                    cambiarPrivilegio(!isChecked);
                    Toast.makeText(mContext, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
                }
            });
        }


        public void confirmarEliminarUsuario(final Usuario usuario, final int position) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Confirmar eliminación");
            alertDialog.setMessage("¿Está seguro que desea eliminar el usuario?");
            alertDialog.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    eliminarUsuario(usuario, position);
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

        public void eliminarUsuario(final Usuario usuario, final int position) {
            progressBarEliminar.setVisibility(View.VISIBLE);
            btnEliminar.setVisibility(View.GONE);
            BDInterface bd = BDCliente.getClient().create(BDInterface.class);
            Call<Respuesta> call = bd.eliminarUsuario(usuario.getId());
            call.enqueue(new Callback<Respuesta>() {
                @Override
                public void onResponse(Call<Respuesta> call, Response<Respuesta> response) {
                    progressBarEliminar.setVisibility(View.GONE);
                    btnEliminar.setVisibility(View.VISIBLE);
                    if (response.body() != null) {
                        Snackbar.make(itemView, response.body().getMensaje(), Snackbar.LENGTH_LONG).show();
                        if (response.body().getCodigo().equals("1")) {
                            usuarios.remove(usuario);
                            notifyItemRemoved(position);
                            administrarUsuarios.setUsuarios(usuarios);
                        }
                    } else {
                        Toast.makeText(mContext, R.string.error_interno_servidor, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Respuesta> call, Throwable t) {
                    progressBarEliminar.setVisibility(View.GONE);
                    btnEliminar.setVisibility(View.VISIBLE);
                    Toast.makeText(mContext, R.string.error_conexion_servidor, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void actualizarUsuarios(List<Usuario> us) {
        usuarios = new ArrayList<>();
        usuarios.addAll(us);
        notifyDataSetChanged();
    }

    public void agregarUsuario(Usuario usuario, int posicion) {
        usuarios.add(posicion, usuario);
        notifyItemInserted(posicion);
    }
}
