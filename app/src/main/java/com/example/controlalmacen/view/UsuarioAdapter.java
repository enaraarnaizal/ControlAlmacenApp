package com.example.controlalmacen.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.model.Usuario;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.VH> {

    public interface OnUsuarioClick {
        void onClick(Usuario u);
    }

    private final List<Usuario> usuarios;
    private final OnUsuarioClick listener;

    public UsuarioAdapter(List<Usuario> usuarios, OnUsuarioClick listener) {
        this.usuarios = usuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Usuario u = usuarios.get(position);
        holder.tvNombre.setText(u.getNombre());

            // Sprint 2: si no hay fotoUri, ponemos icono por defecto
        holder.imgFoto.setImageResource(R.drawable.ic_user_white);

        holder.itemView.setOnClickListener(v -> listener.onClick(u));
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgFoto;
        TextView tvNombre;

        VH(@NonNull View itemView) {
            super(itemView);
            imgFoto = itemView.findViewById(R.id.imgFoto);
            tvNombre = itemView.findViewById(R.id.tvNombre);
        }
    }
}