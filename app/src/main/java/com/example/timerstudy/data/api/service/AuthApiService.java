package com.example.timerstudy.data.api.service;

import com.example.timerstudy.data.api.request.LoginFirebaseRequest;
import com.example.timerstudy.data.api.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    
    /**
     * Login with Firebase ID Token
     * @param request LoginFirebaseRequest containing firebase_id_token
     * @return LoginResponse with access_token and refresh_token
     */
    @POST("api/auth/user-entity/login-firebase")
    Call<LoginResponse> loginWithFirebase(@Body LoginFirebaseRequest request);
}
