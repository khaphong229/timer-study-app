package com.example.timerstudy.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

public class ViewAnimator {
    
    /**
     * Button click animation - Scale bounce effect
     */
    public static void animateButtonClick(View view) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                    })
                    .start();
            })
            .start();
    }
    
    /**
     * Pulse animation - Liên tục scale lên xuống
     */
    public static void animatePulse(View view) {
        view.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .alpha(0.7f)
            .setDuration(500)
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(500)
                    .withEndAction(() -> animatePulse(view))
                    .start();
            })
            .start();
    }
    
    /**
     * Stop pulse animation
     */
    public static void stopPulse(View view) {
        view.animate().cancel();
        view.setScaleX(1f);
        view.setScaleY(1f);
        view.setAlpha(1f);
    }
    
    /**
     * Scale up animation - Phóng to nhẹ
     */
    public static void animateScaleUp(View view) {
        view.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(150)
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start();
            })
            .start();
    }
    
    /**
     * Fade in animation
     */
    public static void animateFadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setListener(null);
    }
    
    /**
     * Fade out animation
     */
    public static void animateFadeOut(View view, long duration) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
    }
    
    /**
     * Fade transition - Fade out → thay đổi → Fade in
     */
    public static void animateFadeTransition(View view, Runnable onChange, long duration) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction(() -> {
                onChange.run();
                view.animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .start();
            })
            .start();
    }
    
    /**
     * Shake animation - Rung lắc qua lại
     */
    public static void animateShake(View view) {
        view.animate()
            .translationX(-20f)
            .setDuration(100)
            .withEndAction(() -> {
                view.animate()
                    .translationX(20f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        view.animate()
                            .translationX(-10f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                view.animate()
                                    .translationX(0f)
                                    .setDuration(100)
                                    .start();
                            })
                            .start();
                    })
                    .start();
            })
            .start();
    }
    
    /**
     * Rotate animation
     */
    public static void animateRotate(View view, float degrees, long duration) {
        view.animate()
            .rotation(view.getRotation() + degrees)
            .setDuration(duration)
            .start();
    }
    
    /**
     * Scale with fade animation - Phóng to + hiện
     */
    public static void animateScaleFadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setVisibility(View.VISIBLE);
        
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setListener(null);
    }
    
    /**
     * Scale with fade out animation - Thu nhỏ + mờ
     */
    public static void animateScaleFadeOut(View view, long duration) {
        view.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(duration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                }
            });
    }
    
    /**
     * Bounce animation - Nảy lên xuống
     */
    public static void animateBounce(View view) {
        view.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(150)
            .withEndAction(() -> {
                view.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start();
                    })
                    .start();
            })
            .start();
    }
    
    /**
     * Slide in from bottom
     */
    public static void animateSlideInFromBottom(View view, long duration) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);
        view.animate()
            .translationY(0f)
            .setDuration(duration)
            .setListener(null);
    }
    
    /**
     * Slide out to bottom
     */
    public static void animateSlideOutToBottom(View view, long duration) {
        view.animate()
            .translationY(view.getHeight())
            .setDuration(duration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                    view.setTranslationY(0f);
                }
            });
    }
    
    /**
     * Slide in from top
     */
    public static void animateSlideInFromTop(View view, long duration) {
        view.setTranslationY(-view.getHeight());
        view.setVisibility(View.VISIBLE);
        view.animate()
            .translationY(0f)
            .setDuration(duration)
            .setListener(null);
    }
}