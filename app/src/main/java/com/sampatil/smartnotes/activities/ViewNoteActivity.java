package com.sampatil.smartnotes.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.sampatil.smartnotes.adapters.PdfPageAdapter;
import com.sampatil.smartnotes.databinding.ActivityViewNoteBinding;
import com.sampatil.smartnotes.models.Note;
import com.sampatil.smartnotes.utils.LocalDbHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewNoteActivity extends AppCompatActivity {

    private ActivityViewNoteBinding binding;
    private Note currentNote;
    private File localPdfFile;
    private PdfRenderer pdfRenderer;
    private PdfPageAdapter pdfAdapter;
    private boolean isEditMode = false;
    private int currentPage = 0;
    private int totalPages = 0;
    private Map<Integer, Bitmap> pageDrawings = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String noteId = getIntent().getStringExtra("note_id");
        if (noteId == null) {
            Toast.makeText(this, "Invalid Note", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<Note> notes = LocalDbHelper.getAllNotes(this);
        for (Note n : notes) {
            if (noteId.equals(n.getId())) {
                currentNote = n;
                break;
            }
        }

        if (currentNote == null) {
            Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.tvSubject.setText(currentNote.getSubject());
        binding.drawingView.setVisibility(View.GONE);

        setupActions();
        setupEditToolbar();

        boolean hasText = currentNote.getTextContent() != null && !currentNote.getTextContent().isEmpty();
        boolean hasPdf = currentNote.getLocalPdfPath() != null && new File(currentNote.getLocalPdfPath()).exists();

        if (hasText) displayTextNote();
        if (hasPdf) displayPdfNote();
    }

    private void displayTextNote() {
        binding.textNoteContainer.setVisibility(View.VISIBLE);
        binding.tvContent.setText(currentNote.getTextContent());
        try {
            int color = android.graphics.Color.parseColor(currentNote.getColorHex());
            binding.textNoteContainer.setCardBackgroundColor(color);
        } catch (Exception e) { /* fallback */ }
    }

    private void displayPdfNote() {
        localPdfFile = new File(currentNote.getLocalPdfPath());
        binding.pdfFrame.setVisibility(View.VISIBLE);

        try {
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(localPdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(fd);
            totalPages = pdfRenderer.getPageCount();

            // Load saved drawings
            loadAllPageDrawings();

            pdfAdapter = new PdfPageAdapter(pdfRenderer);
            pdfAdapter.setPageDrawings(pageDrawings);
            binding.viewPager.setAdapter(pdfAdapter);

            if (totalPages > 1) {
                binding.pageIndicator.setVisibility(View.VISIBLE);
                binding.pageIndicator.setText("1 / " + totalPages);
            }

            binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    if (isEditMode) {
                        // Save current page drawing before switching
                        Bitmap current = binding.drawingView.captureDrawing();
                        if (current != null) {
                            pageDrawings.put(currentPage, current);
                        }
                        // Restore new page drawing
                        binding.drawingView.restoreDrawing(pageDrawings.get(position));
                    }
                    currentPage = position;
                    if (totalPages > 1) {
                        binding.pageIndicator.setText((position + 1) + " / " + totalPages);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAllPageDrawings() {
        for (int i = 0; i < totalPages; i++) {
            String path = getDrawingPathForPage(i);
            File f = new File(path);
            if (f.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(path);
                if (bmp != null) {
                    pageDrawings.put(i, bmp);
                }
            }
        }
    }

    private String getDrawingPathForPage(int page) {
        return new File(getFilesDir(), "drawing_" + currentNote.getId() + "_p" + page + ".png").getAbsolutePath();
    }

    private void setupActions() {
        binding.btnBack.setOnClickListener(v -> handleBack());
        binding.btnEdit.setOnClickListener(v -> toggleEditMode());

        binding.btnShare.setOnClickListener(v -> {
            if (localPdfFile != null && localPdfFile.exists()) {
                try {
                    Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", localPdfFile);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Smart Note: " + currentNote.getSubject());
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "Share Note"));
                } catch (Exception e) {
                    Toast.makeText(this, "Error sharing", Toast.LENGTH_SHORT).show();
                }
            } else if (currentNote.getTextContent() != null) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Smart Note: " + currentNote.getSubject());
                intent.putExtra(Intent.EXTRA_TEXT, currentNote.getSubject() + "\n\n" + currentNote.getTextContent());
                startActivity(Intent.createChooser(intent, "Share Note"));
            }
        });
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        if (isEditMode) {
            enterEditMode();
        } else {
            exitEditMode();
        }
    }

    private void enterEditMode() {
        isEditMode = true;
        binding.editToolbar.setVisibility(View.VISIBLE);
        binding.drawingView.setVisibility(View.VISIBLE);
        binding.drawingView.setDrawingEnabled(true);
        binding.drawingView.setPenMode();
        binding.viewPager.setUserInputEnabled(false);

        // Load existing drawing for current page into the DrawingView
        Bitmap existing = pageDrawings.get(currentPage);
        if (existing != null) {
            binding.drawingView.post(() -> binding.drawingView.restoreDrawing(existing));
        } else {
            binding.drawingView.post(() -> binding.drawingView.clearAll());
        }

        Toast.makeText(this, "Edit mode ON — draw to highlight", Toast.LENGTH_SHORT).show();
    }

    private void exitEditMode() {
        isEditMode = false;

        // Capture current drawing
        Bitmap captured = binding.drawingView.captureDrawing();
        if (captured != null) {
            pageDrawings.put(currentPage, captured);
        }

        binding.editToolbar.setVisibility(View.GONE);
        binding.drawingView.setVisibility(View.GONE);
        binding.drawingView.setDrawingEnabled(false);
        binding.viewPager.setUserInputEnabled(true);

        // Refresh the page so the drawing is composited onto the image
        if (pdfAdapter != null) {
            pdfAdapter.setPageDrawings(pageDrawings);
            pdfAdapter.notifyItemChanged(currentPage);
        }

        Toast.makeText(this, "Edit mode OFF", Toast.LENGTH_SHORT).show();
    }

    private void handleBack() {
        if (isEditMode) {
            exitEditMode();
        } else {
            finish();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if (isEditMode) {
            exitEditMode();
        } else {
            super.onBackPressed();
        }
    }

    private void setupEditToolbar() {
        // Faint highlighter colors (alpha ~33%)
        binding.hlYellow.setOnClickListener(v -> selectColor("#55FADB5F"));
        binding.hlGreen.setOnClickListener(v -> selectColor("#5566FF66"));
        binding.hlPink.setOnClickListener(v -> selectColor("#55FF69B4"));
        binding.hlBlue.setOnClickListener(v -> selectColor("#5569BFFF"));
        binding.hlOrange.setOnClickListener(v -> selectColor("#55FFA500"));

        binding.btnPen.setOnClickListener(v -> {
            binding.drawingView.setPenMode();
            Toast.makeText(this, "Pen mode", Toast.LENGTH_SHORT).show();
        });

        binding.btnEraser.setOnClickListener(v -> {
            binding.drawingView.setEraserMode();
            Toast.makeText(this, "Eraser mode", Toast.LENGTH_SHORT).show();
        });

        binding.btnClear.setOnClickListener(v -> {
            binding.drawingView.clearAll();
            pageDrawings.remove(currentPage);
            Toast.makeText(this, "Page cleared", Toast.LENGTH_SHORT).show();
        });

        binding.btnSaveDrawing.setOnClickListener(v -> saveAllDrawings());
    }

    private void saveAllDrawings() {
        // Capture current page
        Bitmap current = binding.drawingView.captureDrawing();
        if (current != null) {
            pageDrawings.put(currentPage, current);
        }

        // Save each page drawing to file
        for (Map.Entry<Integer, Bitmap> entry : pageDrawings.entrySet()) {
            String path = getDrawingPathForPage(entry.getKey());
            try {
                FileOutputStream fos = new FileOutputStream(path);
                entry.getValue().compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        currentNote.setDrawingPath(getDrawingPathForPage(0));
        LocalDbHelper.updateNote(this, currentNote);
        Toast.makeText(this, "All drawings saved!", Toast.LENGTH_SHORT).show();
    }

    private void selectColor(String colorHex) {
        binding.drawingView.setPenMode();
        binding.drawingView.setPenColor(colorHex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
        for (Bitmap bmp : pageDrawings.values()) {
            if (bmp != null && !bmp.isRecycled()) bmp.recycle();
        }
        pageDrawings.clear();
    }
}
