package com.example.controlalmacen.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.model.Albaran;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AlbaranAdapter extends RecyclerView.Adapter<AlbaranAdapter.VH> {

    public interface OnAlbaranClick {
        void onClick(Albaran albaran);
    }

    public interface OnSelectionChanged {
        void onChanged(Set<Long> idsSeleccionados);
    }

    private List<Albaran> albaranes;
    private final OnAlbaranClick listener;
    private final OnSelectionChanged selectionListener;
    private final SimpleDateFormat dateFormat;
    private final NumberFormat currencyFormat;
    private final Set<Long> idsSeleccionados = new HashSet<>();

    public AlbaranAdapter(List<Albaran> albaranes, OnAlbaranClick listener, OnSelectionChanged selectionListener) {
        this.albaranes = albaranes;
        this.listener = listener;
        this.selectionListener = selectionListener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_albaran, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Albaran albaran = albaranes.get(position);

        holder.tvDescripcion.setText(albaran.getNombre());
        holder.tvTipo.setText("CIF: " + albaran.getCif());
        holder.tvFecha.setText(dateFormat.format(new Date(albaran.getFechaMs())));

        // Mostrar importe
        String importeTexto = currencyFormat.format(albaran.getImporte());
        holder.tvObservaciones.setText(importeTexto);
        holder.tvObservaciones.setVisibility(View.VISIBLE);

        // Cambiar color e icono según estado de pago
        if (albaran.isPagado()) {
            holder.tvTipo.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
            holder.imgTipoAlbaran.setImageResource(android.R.drawable.ic_menu_send);
            holder.imgTipoAlbaran.setColorFilter(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));

            if (albaran.getFechaPagoMs() != null) {
                holder.tvFecha.setText(holder.tvFecha.getText() + " · Pagado: " +
                    dateFormat.format(new Date(albaran.getFechaPagoMs())));
            }
        } else {
            holder.tvTipo.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
            holder.imgTipoAlbaran.setImageResource(android.R.drawable.ic_menu_recent_history);
            holder.imgTipoAlbaran.setColorFilter(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        }

        // Configurar checkbox de selección
        if (selectionListener != null) {
            // Modo selección activo (lista normal de albaranes)
            holder.cbSeleccionarItem.setVisibility(View.VISIBLE);
            holder.cbSeleccionarItem.setOnCheckedChangeListener(null); // Limpiar listener anterior
            holder.cbSeleccionarItem.setChecked(idsSeleccionados.contains(albaran.getId()));

            holder.cbSeleccionarItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    idsSeleccionados.add(albaran.getId());
                } else {
                    idsSeleccionados.remove(albaran.getId());
                }
                selectionListener.onChanged(idsSeleccionados);
            });
        } else {
            // Modo informe (sin selección)
            holder.cbSeleccionarItem.setVisibility(View.GONE);
        }

        // Solo el ícono de opciones abre el diálogo de detalles
        holder.imgOpciones.setOnClickListener(v -> listener.onClick(albaran));
    }

    @Override
    public int getItemCount() {
        return albaranes.size();
    }

    public void updateData(List<Albaran> nuevosAlbaranes) {
        this.albaranes = nuevosAlbaranes;
        idsSeleccionados.clear(); // Limpiar selecciones al actualizar datos
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onChanged(idsSeleccionados);
        }
    }

    public Set<Long> getIdsSeleccionados() {
        return new HashSet<>(idsSeleccionados);
    }

    public void limpiarSeleccion() {
        idsSeleccionados.clear();
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onChanged(idsSeleccionados);
        }
    }

    public void seleccionarTodos() {
        idsSeleccionados.clear();
        for (Albaran albaran : albaranes) {
            idsSeleccionados.add(albaran.getId());
        }
        notifyDataSetChanged();
        if (selectionListener != null) {
            selectionListener.onChanged(idsSeleccionados);
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgTipoAlbaran;
        TextView tvDescripcion;
        TextView tvTipo;
        TextView tvFecha;
        TextView tvObservaciones;
        ImageView imgOpciones;
        CheckBox cbSeleccionarItem;

        VH(@NonNull View itemView) {
            super(itemView);
            imgTipoAlbaran = itemView.findViewById(R.id.imgTipoAlbaran);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvObservaciones = itemView.findViewById(R.id.tvObservaciones);
            imgOpciones = itemView.findViewById(R.id.imgOpciones);
            cbSeleccionarItem = itemView.findViewById(R.id.cbSeleccionarItem);
        }
    }
}

