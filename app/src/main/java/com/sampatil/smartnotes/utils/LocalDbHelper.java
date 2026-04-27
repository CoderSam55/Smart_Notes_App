package com.sampatil.smartnotes.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sampatil.smartnotes.models.Note;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocalDbHelper {
    private static final String PREF_NAME = "SmartNotesDb";
    private static final String KEY_NOTES = "notes_list";

    public static void saveNote(Context context, Note note) {
        List<Note> notes = getAllNotes(context);
        // Add to beginning for descending order
        notes.add(0, note);
        saveList(context, notes);
    }

    public static List<Note> getAllNotes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_NOTES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public static void deleteNote(Context context, String noteId) {
        List<Note> notes = getAllNotes(context);
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getId().equals(noteId)) {
                notes.remove(i);
                break;
            }
        }
        saveList(context, notes);
    }

    public static void updateNote(Context context, Note updatedNote) {
        List<Note> notes = getAllNotes(context);
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getId().equals(updatedNote.getId())) {
                notes.set(i, updatedNote);
                break;
            }
        }
        saveList(context, notes);
    }

    private static void saveList(Context context, List<Note> notes) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(notes);
        editor.putString(KEY_NOTES, json);
        editor.apply();
    }
}
