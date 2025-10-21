package com.example.timerstudy.ui.base;

public interface BaseContract {
    
    interface BaseView {
        void showLoading(boolean isLoading);
        void showError(String message);
        void showSuccess(String message);
    }
    
    interface BasePresenter<V extends BaseView> {
        void attachView(V view);
        void detachView();
    }
}
