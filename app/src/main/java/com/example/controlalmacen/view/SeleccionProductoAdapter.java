package com.example.controlalmacen.view;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.model.Producto;

import java.util.List;

public class SeleccionProductoAdapter extends RecyclerView.Adapter<SeleccionProductoAdapter.ProductoViewHolder> {

    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
    }

    private final List<Producto> listaProductos;
    private final OnProductoClickListener listener;

    public SeleccionProductoAdapter(List<Producto> listaProductos, OnProductoClickListener listener) {
        this.listaProductos = listaProductos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto_seleccion, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);

        holder.tvNombre.setText(producto.getNombre());

        if (producto.getImg() != null && !producto.getImg().isEmpty()) {
            try {
                holder.imgProducto.setImageURI(Uri.parse(producto.getImg()));
            } catch (Exception e) {
                holder.imgProducto.setImageResource(R.drawable.ic_box);
            }
        } else {
            holder.imgProducto.setImageResource(R.drawable.ic_box);
        }

        holder.itemView.setOnClickListener(v -> listener.onProductoClick(producto));
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProducto;
        TextView tvNombre;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProducto = itemView.findViewById(R.id.imgProductoSeleccion);
            tvNombre = itemView.findViewById(R.id.tvNombreProductoSeleccion);
        }
    }
}