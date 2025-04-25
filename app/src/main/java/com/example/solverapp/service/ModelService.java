package com.example.solverapp.service;

import com.example.solverapp.dto.request.ModelRequest;
import com.example.solverapp.dto.response.ModelResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface  ModelService {
    @POST("v1/chat/completions")
    @Headers("Authorization: Bearer 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")
    Call<ModelResponse> send(@Body ModelRequest body);
}
