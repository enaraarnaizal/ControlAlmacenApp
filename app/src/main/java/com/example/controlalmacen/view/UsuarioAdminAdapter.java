package com.example.controlalmacen.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.model.Usuario;

import java.util.List;

public class UsuarioAdminAdapter extends RecyclerView.Adapter<UsuarioAdminAdapter.ViewHolder> {

    private final List<Usuario> lista;
    private final Listener listener;

    public interface Listener {
        void onEditar(Usuario u);
        void onDeshabilitar(Usuario u);
        void onBorrar(Usuario u);
        void onNuevo();
    }

    public UsuarioAdminAdapter(List<Usuario> lista, Listener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (lista.get(position) == null) ? 0 : 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = (viewType == 0)
                ? R.layout.item_nuevo_perfil
                : R.layout.item_usuario_perfil;

        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        Usuario u = lista.get(position);

        if (u == null) {
            h.itemView.setOnClickListener(v -> listener.onNuevo());
            return;
        }

        h.tvNombre.setText(u.getNombre());

        if (u.isAdministrador()) {
            h.tvRol.setVisibility(View.VISIBLE);
            h.tvRol.setText("Administrador");
            h.badgeAdmin.setVisibility(View.VISIBLE);
        } else {
            h.tvRol.setVisibility(View.GONE);
            h.badgeAdmin.setVisibility(View.GONE);
        }

        h.imgUsuario.setImageResource(R.drawable.ic_user_white);

        if (h.capaDeshabilitado != null) {
            h.capaDeshabilitado.setVisibility(u.isHabilitado() ? View.GONE : View.VISIBLE);
        }

        h.btnEditar.setOnClickListener(v -> listener.onEditar(u));
        h.btnDisable.setOnClickListener(v -> listener.onDeshabilitar(u));
        h.btnDelete.setOnClickListener(v -> listener.onBorrar(u));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre, tvRol;
        ImageView imgUsuario;
        View badgeAdmin;
        ImageButton btnEditar, btnDisable, btnDelete;
        View capaDeshabilitado;

        ViewHolder(View v, int viewType) {
            super(v);

            if (viewType == 1) {
                tvNombre = v.findViewById(R.id.tvNombre);
                tvRol = v.findViewById(R.id.tvRol);
                imgUsuario = v.findViewById(R.id.imgUsuario);
                badgeAdmin = v.findViewById(R.id.badgeAdmin);

                btnEditar = v.findViewById(R.id.btnEditar);
                btnDisable = v.findViewById(R.id.btnDisable);
                btnDelete = v.findViewById(R.id.btnDelete);

                capaDeshabilitado = v.findViewById(R.id.capaDeshabilitado);
            }
        }
    }
}