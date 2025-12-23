package com.example.sitepulse.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sitepulse.R;
import com.example.sitepulse.data.local.entity.Invoice;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    private List<Invoice> invoiceList = new ArrayList<>();
    private OnInvoiceClickListener listener;

    public interface OnInvoiceClickListener {
        void onInvoiceClick(Invoice invoice);
    }

    public void setOnInvoiceClickListener(OnInvoiceClickListener listener) {
        this.listener = listener;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoiceList = invoices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        Invoice invoice = invoiceList.get(position);
        holder.bind(invoice);
    }

    @Override
    public int getItemCount() {
        return invoiceList.size();
    }

    class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceNumber, tvClientName, tvDate, tvAmount, tvStatus;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceNumber = itemView.findViewById(R.id.tvInvoiceNumber);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onInvoiceClick(invoiceList.get(position));
                }
            });
        }

        public void bind(Invoice invoice) {
            tvInvoiceNumber.setText("#" + invoice.invoiceNumber);
            tvClientName.setText(invoice.clientName);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            tvDate.setText(sdf.format(new Date(invoice.date)));

            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            tvAmount.setText(format.format(invoice.totalAmount));
            
            tvStatus.setText(invoice.status);
        }
    }
}