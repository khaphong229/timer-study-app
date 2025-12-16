package com.example.timerstudy.data.api.request;

import com.google.gson.annotations.SerializedName;

public class LoginFirebaseRequest {
    @SerializedName("firebase_id_token")
    private String firebaseIdToken;

    public LoginFirebaseRequest(String firebaseIdToken) {
        this.firebaseIdToken = firebaseIdToken;
    }

    public String getFirebaseIdToken() {
        return firebaseIdToken;
    }

    public void setFirebaseIdToken(String firebaseIdToken) {
        this.firebaseIdToken = firebaseIdToken;
    }
}
