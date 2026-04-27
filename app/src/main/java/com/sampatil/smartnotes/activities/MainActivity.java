package com.sampatil.smartnotes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.sampatil.smartnotes.adapters.NotesAdapter;
import com.sampatil.smartnotes.databinding.ActivityMainBinding;
import com.sampatil.smartnotes.models.Note;
import com.sampatil.smartnotes.utils.LocalDbHelper;
import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NotesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        
        binding.btnNavAdd.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddNoteActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchNotes();
    }

    private void setupRecyclerView() {
        adapter = new NotesAdapter(new NotesAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(Note note) {
                Intent intent = new Intent(MainActivity.this, ViewNoteActivity.class);
                intent.putExtra("note_id", note.getId());
                startActivity(intent);
            }

            @Override
            public void onNoteLongClick(Note note) {
                showDeleteDialog(note);
            }
        });
        
        androidx.recyclerview.widget.StaggeredGridLayoutManager layoutManager = 
            new androidx.recyclerview.widget.StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
    }

    private void fetchNotes() {
        binding.progressBar.setVisibility(View.GONE);
        List<Note> notes = LocalDbHelper.getAllNotes(this);
        
        if (notes.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            adapter.setNotes(notes);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            adapter.setNotes(notes);
        }
    }

    private void showDeleteDialog(Note note) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Yes", (dialog, which) -> deleteNote(note))
            .setNegativeButton("No", null)
            .show();
    }

    private void deleteNote(Note note) {
        // Delete local file
        if (note.getLocalPdfPath() != null) {
            File file = new File(note.getLocalPdfPath());
            if (file.exists()) {
                file.delete();
            }
        }
        
        // Delete from DB
        LocalDbHelper.deleteNote(this, note.getId());
        Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
        fetchNotes();
    }
}
