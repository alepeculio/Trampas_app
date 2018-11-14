package com.trampas.trampas.Adaptadores;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.trampas.trampas.Clases.Usuario;
import com.trampas.trampas.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdaptadorListaUsuarios extends RecyclerView.Adapter<AdaptadorListaUsuarios.UsuarioViewHolder> {
    private List<Usuario> usuarios;
    private Context mContext;

    public AdaptadorListaUsuarios(List<Usuario> usuarios, Context mContext) {
        this.usuarios = usuarios;
        this.mContext = mContext;
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

        public UsuarioViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext = itemView.getContext();

            swNivelPrivilegio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        tvPrivilegio.setText("Administrador");
                    else {
                        tvPrivilegio.setText("Usuario");
                    }
                }
            });
        }

        public void bindUsuario(final Usuario usuario) {
            tvNombre.setText(usuario.getNombre() + " " + usuario.getApellido());
            tvCorreo.setText(usuario.getCorreo());
            String privilegio;
            if (usuario.getAdmin() == 1) {
                privilegio = "Administrador";
                swNivelPrivilegio.setChecked(true);
            } else {
                privilegio = "Usuario";
            }
            tvPrivilegio.setText(privilegio);
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
