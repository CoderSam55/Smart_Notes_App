package com.sampatil.smartnotes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sampatil.smartnotes.databinding.ItemNoteBinding;
import com.sampatil.smartnotes.models.Note;
import java.util.ArrayList;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes = new ArrayList<>();
    private final OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
    }

    public NotesAdapter(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNoteBinding binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NoteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.binding.tvSubject.setText(note.getSubject());
        
        // Show description on card (not the full text content)
        if (note.getDescription() != null && !note.getDescription().isEmpty()) {
            holder.binding.tvDescription.setText(note.getDescription());
            holder.binding.tvDescription.setVisibility(View.VISIBLE);
        } else if (note.getLocalPdfPath() != null) {
            holder.binding.tvDescription.setText("📎 Image Note");
            holder.binding.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.binding.tvDescription.setVisibility(View.GONE);
        }
        
        // Set card color
        try {
            int color = android.graphics.Color.parseColor(note.getColorHex());
            holder.binding.cardContainer.setCardBackgroundColor(color);
        } catch (Exception e) {
            holder.binding.cardContainer.setCardBackgroundColor(
                holder.itemView.getContext().getResources().getColor(com.sampatil.smartnotes.R.color.cardGreen)
            );
        }
        
        holder.itemView.setOnClickListener(v -> listener.onNoteClick(note));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onNoteLongClick(note);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        final ItemNoteBinding binding;

        NoteViewHolder(ItemNoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
