package com.sampatil.smartnotes.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.sampatil.smartnotes.R;
import com.sampatil.smartnotes.databinding.ActivityAddNoteBinding;
import com.sampatil.smartnotes.models.Note;
import com.sampatil.smartnotes.utils.LocalDbHelper;
import com.sampatil.smartnotes.utils.PdfUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddNoteActivity extends AppCompatActivity {

    private ActivityAddNoteBinding binding;
    private String selectedColorHex = "#B2FAB4";
    private List<Uri> selectedImages = new ArrayList<>();
    private ExecutorService executorService;
    private Handler mainHandler;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImages.clear();
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            selectedImages.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        selectedImages.add(result.getData().getData());
                    }
                    binding.tvImageCount.setText(selectedImages.size() + " image(s)");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        setupColorPicker();
        setupActions();
    }

    private void setupColorPicker() {
        binding.colorRed.setOnClickListener(v -> updateColor("#FFB3B3", R.color.cardPink));
        binding.colorGreen.setOnClickListener(v -> updateColor("#B2FAB4", R.color.cardGreen));
        binding.colorPurple.setOnClickListener(v -> updateColor("#D5B8FF", R.color.cardPurple));
    }

    private void updateColor(String hex, int colorRes) {
        selectedColorHex = hex;
        binding.noteCard.setCardBackgroundColor(ContextCompat.getColor(this, colorRes));
    }

    private void setupActions() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnAttachImages.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(Intent.createChooser(intent, "Select Images"));
        });
        binding.btnSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = binding.etTitle.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String content = binding.etContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please write a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!selectedImages.isEmpty()) {
            savePdfNote(title, description, content);
        } else if (!content.isEmpty()) {
            saveTextNote(title, description, content);
        } else {
            Toast.makeText(this, "Write some text or attach images", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTextNote(String title, String description, String content) {
        String id = UUID.randomUUID().toString();
        Note note = new Note(id, title, content, selectedColorHex, true);
        note.setDescription(description);
        LocalDbHelper.saveNote(this, note);
        Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void savePdfNote(String title, String description, String content) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        executorService.execute(() -> {
            try {
                String filename = "note_" + UUID.randomUUID().toString() + ".pdf";
                File cachePdf = PdfUtils.createPdfFromImages(this, selectedImages, filename);
                File permanentFile = new File(getFilesDir(), filename);
                copyFile(cachePdf, permanentFile);
                cachePdf.delete();

                mainHandler.post(() -> {
                    String id = UUID.randomUUID().toString();
                    Note note = new Note(id, title, "Image Note", permanentFile.getAbsolutePath());
                    note.setColorHex(selectedColorHex);
                    note.setDescription(description);
                    if (!content.isEmpty()) {
                        note.setTextContent(content);
                    }
                    LocalDbHelper.saveNote(this, note);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(this, "PDF note saved!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void copyFile(File source, File dest) throws Exception {
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(dest);
        byte[] buffer = new byte[4096];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        fis.close();
        fos.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
