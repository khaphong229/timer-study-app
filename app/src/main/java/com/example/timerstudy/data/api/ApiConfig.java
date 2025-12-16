package com.example.timerstudy.data.api;

public class ApiConfig {
    // Base URL cho backend API
    public static final String BASE_URL = "http://10.0.2.2:8669/"; // Dùng cho Android Emulator
    // public static final String BASE_URL = "http://localhost:8669/"; // Dùng cho device thật
    
    // API Endpoints
    public static final String LOGIN_FIREBASE = "api/auth/user-entity/login-firebase";
    public static final String TODOLIST = "api/todolist";
    
    // Request timeout (seconds)
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;
}
