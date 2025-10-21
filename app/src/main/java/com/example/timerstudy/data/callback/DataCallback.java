package com.example.timerstudy.data.callback;

public interface DataCallback<T> {
    void onSuccess(T data);
    void onError(String error);
}
