package com.example.controlalmacen.view;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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
import com.google.android.material.button.MaterialButton;

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

    public AlbaranAdapter(List<Albaran> albaranes,
                          OnAlbaranClick listener,
                          OnSelectionChanged selectionListener) {
        this.albaranes = albaranes;
        this.listener = listener;
        this.selectionListener = selectionListener;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
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
        holder.tvCif.setText("CIF: " + albaran.getCif());
        holder.tvImporte.setText(currencyFormat.format(albaran.getImporte()));

        String fechaTexto = dateFormat.format(new Date(albaran.getFechaMs()));
        holder.tvFecha.setText(fechaTexto);

        if (albaran.isPagado()) {
            aplicarChip(holder.tvEstadoPago, "Pagado", "#DCFCE7", "#15803D");
        } else {
            aplicarChip(holder.tvEstadoPago, "Sin pagar", "#FDE7D8", "#EA580C");
        }

        if (albaran.getImagenUri() != null && !albaran.getImagenUri().isEmpty()) {
            try {
                if (albaran.getImagenUri().startsWith("android.resource://")) {
                    holder.imgAlbaranPreview.setImageResource(R.drawable.ic_albaran_default);
                } else {
                    holder.imgAlbaranPreview.setImageURI(Uri.parse(albaran.getImagenUri()));
                }
            } catch (Exception e) {
                holder.imgAlbaranPreview.setImageResource(R.drawable.ic_albaran_default);
            }
        } else {
            holder.imgAlbaranPreview.setImageResource(R.drawable.ic_albaran_default);
        }

        if (selectionListener != null) {
            holder.cbSeleccionarItem.setVisibility(View.VISIBLE);
            holder.cbSeleccionarItem.setOnCheckedChangeListener(null);
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
            holder.cbSeleccionarItem.setVisibility(View.GONE);
        }

        holder.btnVerDetalles.setOnClickListener(v -> listener.onClick(albaran));
    }

    @Override
    public int getItemCount() {
        return albaranes.size();
    }

    public void updateData(List<Albaran> nuevosAlbaranes) {
        this.albaranes = nuevosAlbaranes;
        idsSeleccionados.clear();
        notifyDataSetChanged();

        if (selectionListener != null) {
            selectionListener.onChanged(idsSeleccionados);
        }
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

    private void aplicarChip(TextView textView, String texto, String colorFondo, String colorTexto) {
        textView.setText(texto);
        textView.setTextColor(Color.parseColor(colorTexto));

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(colorFondo));
        drawable.setCornerRadius(999f);

        textView.setBackground(drawable);
    }

    static class VH extends RecyclerView.ViewHolder {

        CheckBox cbSeleccionarItem;
        ImageView imgAlbaranPreview;
        TextView tvDescripcion;
        TextView tvCif;
        TextView tvImporte;
        TextView tvFecha;
        TextView tvEstadoPago;
        MaterialButton btnVerDetalles;

        VH(@NonNull View itemView) {
            super(itemView);
            cbSeleccionarItem = itemView.findViewById(R.id.cbSeleccionarItem);
            imgAlbaranPreview = itemView.findViewById(R.id.imgAlbaranPreview);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCif = itemView.findViewById(R.id.tvCif);
            tvImporte = itemView.findViewById(R.id.tvImporte);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvEstadoPago = itemView.findViewById(R.id.tvEstadoPago);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
        }
    }
}