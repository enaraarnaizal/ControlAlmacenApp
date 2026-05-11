package com.example.controlalmacen.view;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.model.Producto;

import java.util.ArrayList;
import java.util.List;

public class InventarioAdapter extends RecyclerView.Adapter<InventarioAdapter.VH> {

    private final List<Producto> productos = new ArrayList<>();

    public InventarioAdapter(List<Producto> listaInicial) {
        if (listaInicial != null) {
            productos.addAll(listaInicial);
        }
    }

    public void updateData(List<Producto> nuevosProductos) {
        productos.clear();
        if (nuevosProductos != null) {
            productos.addAll(nuevosProductos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventario_producto, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Producto producto = productos.get(position);

        holder.tvNombreProducto.setText(producto.getNombre());
        holder.tvCantidadActual.setText(String.valueOf(producto.getCantidad()));

        if (producto.getCantidadMin() > 0) {
            holder.tvCantidadMinima.setText(String.valueOf(producto.getCantidadMin()));
        } else {
            holder.tvCantidadMinima.setText("-");
        }

        if (producto.getImg() != null && !producto.getImg().isEmpty()) {
            try {
                holder.imgProducto.setImageURI(Uri.parse(producto.getImg()));
            } catch (Exception e) {
                holder.imgProducto.setImageResource(R.drawable.ic_box);
            }
        } else {
            holder.imgProducto.setImageResource(R.drawable.ic_box);
        }

        boolean enMinimo = producto.isHabilitado()
                && producto.getCantidadMin() > 0
                && producto.getCantidad() <= producto.getCantidadMin();

        boolean sinStock = producto.getCantidad() == 0; // nuevo

        if (sinStock) {
            // aplicar estilo rojo para sin stock
            holder.rowContainer.setBackgroundColor(Color.parseColor("#FEE2E2"));
            holder.tvCantidadActual.setTextColor(Color.parseColor("#EF4444"));
            aplicarChip(holder.tvEstado, "Sin stock", "#FEE2E2", "#EF4444");
        } else if (!producto.isHabilitado()) {
            holder.rowContainer.setBackgroundColor(Color.parseColor("#F8FAFC"));
            holder.tvCantidadActual.setTextColor(Color.parseColor("#4B5563"));
            aplicarChip(holder.tvEstado, "Deshabilitado", "#E5E7EB", "#4B5563");
        } else if (enMinimo) {
            holder.rowContainer.setBackgroundColor(Color.parseColor("#FFF7ED"));
            holder.tvCantidadActual.setTextColor(Color.parseColor("#EA580C"));
            aplicarChip(holder.tvEstado, "Límite mínimo", "#FFE7D1", "#F97316");
        } else {
            holder.rowContainer.setBackgroundColor(Color.WHITE);
            holder.tvCantidadActual.setTextColor(Color.parseColor("#111827"));
            aplicarChip(holder.tvEstado, "Normal", "#DCFCE7", "#16A34A");
        }
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    private void aplicarChip(TextView textView, String texto, String colorFondo, String colorTexto) {
        textView.setText(texto);
        textView.setTextColor(Color.parseColor(colorTexto));

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(colorFondo));
        drawable.setCornerRadius(999f);

        textView.setBackground(drawable);
    }

    static class VH extends RecyclerView.ViewHolder {

        LinearLayout rowContainer;
        ImageView imgProducto;
        TextView tvNombreProducto;
        TextView tvCantidadActual;
        TextView tvCantidadMinima;
        TextView tvEstado;

        VH(@NonNull View itemView) {
            super(itemView);
            rowContainer = itemView.findViewById(R.id.rowContainerInventario);
            imgProducto = itemView.findViewById(R.id.imgProductoInventario);
            tvNombreProducto = itemView.findViewById(R.id.tvNombreProductoInventario);
            tvCantidadActual = itemView.findViewById(R.id.tvCantidadActualInventario);
            tvCantidadMinima = itemView.findViewById(R.id.tvCantidadMinimaInventario);
            tvEstado = itemView.findViewById(R.id.tvEstadoInventario);
        }
    }
}
