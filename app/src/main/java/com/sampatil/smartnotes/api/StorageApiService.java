package com.sampatil.smartnotes.api;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface StorageApiService {
    
    @POST("storage/v1/object/{bucket}/{filename}")
    Call<ResponseBody> uploadFile(
        @Path("bucket") String bucket,
        @Path("filename") String filename,
        @Body RequestBody fileBody
    );
    
    @DELETE("storage/v1/object/{bucket}/{filename}")
    Call<ResponseBody> deleteFile(
        @Path("bucket") String bucket,
        @Path("filename") String filename
    );
}
