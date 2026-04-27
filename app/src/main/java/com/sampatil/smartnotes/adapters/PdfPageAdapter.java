package com.sampatil.smartnotes.adapters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sampatil.smartnotes.databinding.ItemPdfPageBinding;
import java.util.HashMap;
import java.util.Map;

public class PdfPageAdapter extends RecyclerView.Adapter<PdfPageAdapter.PdfPageViewHolder> {

    private final PdfRenderer pdfRenderer;
    private Map<Integer, Bitmap> pageDrawings = new HashMap<>();

    public PdfPageAdapter(PdfRenderer pdfRenderer) {
        this.pdfRenderer = pdfRenderer;
    }

    public void setPageDrawings(Map<Integer, Bitmap> drawings) {
        this.pageDrawings = drawings;
    }

    @NonNull
    @Override
    public PdfPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPdfPageBinding binding = ItemPdfPageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PdfPageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfPageViewHolder holder, int position) {
        PdfRenderer.Page page = pdfRenderer.openPage(position);

        int width = holder.itemView.getResources().getDisplayMetrics().widthPixels;
        int height = holder.itemView.getResources().getDisplayMetrics().heightPixels;

        float ratio = (float) page.getWidth() / page.getHeight();
        if (width / ratio > height) {
            width = (int) (height * ratio);
        } else {
            height = (int) (width / ratio);
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();

        // Composite saved drawing onto the page
        Bitmap drawing = pageDrawings.get(position);
        if (drawing != null) {
            Canvas canvas = new Canvas(bitmap);
            // Scale drawing to match page bitmap size
            Matrix matrix = new Matrix();
            matrix.setScale(
                (float) bitmap.getWidth() / drawing.getWidth(),
                (float) bitmap.getHeight() / drawing.getHeight()
            );
            canvas.drawBitmap(drawing, matrix, null);
        }

        holder.binding.photoView.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return pdfRenderer != null ? pdfRenderer.getPageCount() : 0;
    }

    static class PdfPageViewHolder extends RecyclerView.ViewHolder {
        final ItemPdfPageBinding binding;

        PdfPageViewHolder(ItemPdfPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
