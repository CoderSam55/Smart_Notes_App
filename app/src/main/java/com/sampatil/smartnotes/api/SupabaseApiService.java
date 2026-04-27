package com.sampatil.smartnotes.api;

import com.sampatil.smartnotes.models.Note;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApiService {
    
    @GET("rest/v1/notes?select=*&order=created_at.desc")
    Call<List<Note>> getAllNotes();

    @POST("rest/v1/notes")
    Call<Void> insertNote(
        @Header("Prefer") String prefer, // Pass "return=minimal" or "return=representation"
        @Body Note note
    );

    @DELETE("rest/v1/notes")
    Call<Void> deleteNote(@Query("id") String idFilter);
}
