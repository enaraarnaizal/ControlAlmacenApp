package com.example.controlalmacen.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlalmacen.R;
import com.example.controlalmacen.model.Producto;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import android.net.Uri;

public class ProductoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        //para la inactiviada del usuario, saber cuando hace clicks
        void onClickAddProducto();
        void onSumar(Producto p);
        void onRestar(Producto p);
    }

    private static final int TYPE_ADD = 0;
    private static final int TYPE_PRODUCTO = 1;
    private static final int TYPE_HEADER = 2;

    private final Listener listener;

    private final List<Producto> topList = new ArrayList<>();
    private final List<Producto> restList = new ArrayList<>();

    public ProductoAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Top + Resto */
    public void setData(List<Producto> top, List<Producto> resto) {
        topList.clear();
        restList.clear();
        if (top != null) topList.addAll(top);
        if (resto != null) restList.addAll(resto);
        notifyDataSetChanged();
    }

    /** Para refrescar solo una card sin reordenar */
    public void notifyProductoChangedById(long productoId) {
        int pos = findPositionById(productoId);
        if (pos != -1) notifyItemChanged(pos);
        else notifyDataSetChanged();
    }

    private int findPositionById(long id) {

        for (int i = 0; i < topList.size(); i++) {
            if (topList.get(i).getId() == id) {
                return 1 + i;
            }
        }

        if (restList.isEmpty()) return -1;

        int headerPos = 1 + topList.size();
        for (int i = 0; i < restList.size(); i++) {
            if (restList.get(i).getId() == id) {
                return headerPos + 1 + i;
            }
        }

        return -1;
    }

    @Override
    public int getItemCount() {
        int count = 1; // add
        count += topList.size();

        if (!restList.isEmpty()) {
            count += 1; // header
            count += restList.size();
        }

        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_ADD;

        int topStart = 1;
        int topEnd = topStart + topList.size() - 1;

        if (position >= topStart && position <= topEnd) return TYPE_PRODUCTO;

        if (!restList.isEmpty()) {
            int headerPos = 1 + topList.size();
            if (position == headerPos) return TYPE_HEADER;
            return TYPE_PRODUCTO;
        }

        return TYPE_PRODUCTO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_ADD) {
            View v = inf.inflate(R.layout.item_add_producto, parent, false);
            return new AddVH(v);
        } else if (viewType == TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = inf.inflate(R.layout.item_producto, parent, false);
            return new ProductoVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof AddVH) {
            ((AddVH) holder).bind(listener);
            return;
        }

        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).bind("Resto de productos");
            return;
        }

        // Producto
        Producto p = getProductoByAdapterPosition(position);
        ((ProductoVH) holder).bind(p, listener);
    }

    private Producto getProductoByAdapterPosition(int position) {
        int topIndex = position - 1;
        if (topIndex >= 0 && topIndex < topList.size()) {
            return topList.get(topIndex);
        }

        int headerPos = 1 + topList.size();
        int restIndex = position - (headerPos + 1);

        if (restIndex >= 0 && restIndex < restList.size()) {
            return restList.get(restIndex);
        }

        throw new IllegalStateException(
                "Posición inválida: position=" + position +
                        ", topSize=" + topList.size() +
                        ", restSize=" + restList.size()
        );
    }

    static class AddVH extends RecyclerView.ViewHolder {
        AddVH(@NonNull View itemView) { super(itemView); }
        void bind(Listener l) { itemView.setOnClickListener(v -> l.onClickAddProducto()); }
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tv;
        HeaderVH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvHeader);
        }
        void bind(String text) { tv.setText(text); }
    }

    static class ProductoVH extends RecyclerView.ViewHolder {

        MaterialCardView cardProducto;

        ImageView imgProducto;
        TextView tvNombre, tvCantidad, tvMinimo;
        Button btnRetirar, btnAnadir;

        ProductoVH(@NonNull View itemView) {
            super(itemView);

            cardProducto = itemView.findViewById(R.id.cardProducto);

            imgProducto = itemView.findViewById(R.id.imgProducto);
            tvNombre = itemView.findViewById(R.id.tvNombreProducto);
            tvCantidad = itemView.findViewById(R.id.tvCantidadProducto);
            tvMinimo = itemView.findViewById(R.id.tvMinimo);
            btnRetirar = itemView.findViewById(R.id.btnRetirar);
            btnAnadir = itemView.findViewById(R.id.btnAnadir);
        }

        void bind(Producto p, Listener l) {
            tvNombre.setText(p.getNombre());
            tvCantidad.setText("Cantidad: " + p.getCantidad());

            boolean enMinimo = p.isHabilitado()
                    && p.getCantidadMin() > 0
                    && p.getCantidad() <= p.getCantidadMin();

            boolean sinStock = p.getCantidad() == 0;

            tvMinimo.setVisibility(enMinimo ? View.VISIBLE : View.GONE);

            if (p.getImg() != null && !p.getImg().isEmpty()) {
                try {
                    imgProducto.setImageURI(Uri.parse(p.getImg()));
                    imgProducto.setScaleType(ImageView.ScaleType.FIT_CENTER);
                } catch (Exception e) {
                    imgProducto.setImageResource(android.R.drawable.ic_menu_gallery);
                    imgProducto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            } else {
                imgProducto.setImageResource(android.R.drawable.ic_menu_gallery);
                imgProducto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }

            if (cardProducto != null) {
                if (sinStock) {
                    cardProducto.setCardBackgroundColor(Color.parseColor("#FEE2E2"));
                    cardProducto.setStrokeColor(Color.parseColor("#EF4444"));
                    cardProducto.setStrokeWidth(4);
                } else {
                    cardProducto.setCardBackgroundColor(Color.parseColor("#FFFFFF"));

                    if (enMinimo) {
                        cardProducto.setStrokeColor(Color.parseColor("#F97316"));
                        cardProducto.setStrokeWidth(4);
                    } else {
                        cardProducto.setStrokeColor(Color.parseColor("#E5E7EB"));
                        cardProducto.setStrokeWidth(2);
                    }
                }
            }

            btnAnadir.setOnClickListener(v -> l.onSumar(p));
            btnRetirar.setOnClickListener(v -> l.onRestar(p));
        }
    }
}