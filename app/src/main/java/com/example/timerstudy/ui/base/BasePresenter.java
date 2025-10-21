package com.example.timerstudy.ui.base;

import java.lang.ref.WeakReference;

public abstract class BasePresenter<V extends BaseContract.BaseView> implements BaseContract.BasePresenter<V> {
    
    private WeakReference<V> viewRef;
    
    @Override
    public void attachView(V view) {
        viewRef = new WeakReference<>(view);
    }
    
    @Override
    public void detachView() {
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
    }
    
    protected V getView() {
        return viewRef != null ? viewRef.get() : null;
    }
    
    protected boolean isViewAttached() {
        return getView() != null;
    }
    
    protected void checkViewAttached() {
        if (!isViewAttached()) {
            throw new MvpViewNotAttachedException();
        }
    }
    
    public static class MvpViewNotAttachedException extends RuntimeException {
        public MvpViewNotAttachedException() {
            super("Please call attachView() before requesting data to the presenter");
        }
    }
}
