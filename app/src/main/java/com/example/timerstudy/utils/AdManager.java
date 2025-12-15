package com.example.timerstudy.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class AdManager {
    private static final String TAG = "AdManager";

    private static final String REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"; // Test ID
    
    private Context context;
    private RewardedAd rewardedAd;
    private boolean isLoadingAd = false;
    private RewardListener listener;
    
    public interface RewardListener {
        void onAdLoaded();
        void onAdFailedToLoad(String error);
        void onUserEarnedReward(int coins);
        void onAdDismissed();
    }
    
    public AdManager(Context context) {
        this.context = context;
        
        // Initialize Mobile Ads SDK
        MobileAds.initialize(context, initializationStatus -> {
            Log.d(TAG, "AdMob initialized");
        });
    }
    
    /**
     * Load rewarded ad
     */
    public void loadRewardedAd(RewardListener listener) {
        this.listener = listener;
        
        if (isLoadingAd) {
            Log.w(TAG, "Ad is already loading");
            return;
        }
        
        if (rewardedAd != null) {
            Log.d(TAG, "Ad already loaded");
            listener.onAdLoaded();
            return;
        }
        
        isLoadingAd = true;
        
        AdRequest adRequest = new AdRequest.Builder().build();
        
        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        isLoadingAd = false;
                        Log.d(TAG, "Rewarded ad loaded");
                        
                        if (listener != null) {
                            listener.onAdLoaded();
                        }
                        
                        setupAdCallbacks();
                    }
                    
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        rewardedAd = null;
                        isLoadingAd = false;
                        String error = "Failed to load ad: " + loadAdError.getMessage();
                        Log.e(TAG, error);
                        
                        if (listener != null) {
                            listener.onAdFailedToLoad(error);
                        }
                    }
                });
    }
    
    private void setupAdCallbacks() {
        if (rewardedAd == null) return;
        
        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed");
                rewardedAd = null;
                
                if (listener != null) {
                    listener.onAdDismissed();
                }
                
                // Preload next ad
                loadRewardedAd(listener);
            }
            
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                Log.e(TAG, "Ad failed to show: " + adError.getMessage());
                rewardedAd = null;
                
                if (listener != null) {
                    listener.onAdFailedToLoad(adError.getMessage());
                }
            }
            
            @Override
            public void onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed full screen content");
            }
        });
    }
    
    /**
     * Show rewarded ad
     */
    public void showRewardedAd(Activity activity, int rewardCoins) {
        if (rewardedAd == null) {
            Log.w(TAG, "Rewarded ad is not ready");
            if (listener != null) {
                listener.onAdFailedToLoad("Ad not ready. Please try again.");
            }
            return;
        }
        
        rewardedAd.show(activity, new OnUserEarnedRewardListener() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                // User watched the ad completely
                int amount = rewardCoins; // Use custom reward amount
                Log.d(TAG, "User earned reward: " + amount + " coins");
                
                if (listener != null) {
                    listener.onUserEarnedReward(amount);
                }
            }
        });
    }
    
    /**
     * Check if ad is ready to show
     */
    public boolean isAdReady() {
        return rewardedAd != null;
    }
    
    /**
     * Preload ad for better UX
     */
    public void preloadAd() {
        loadRewardedAd(new RewardListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "Ad preloaded successfully");
            }
            
            @Override
            public void onAdFailedToLoad(String error) {
                Log.e(TAG, "Failed to preload ad: " + error);
            }
            
            @Override
            public void onUserEarnedReward(int coins) {
                // Not used in preload
            }
            
            @Override
            public void onAdDismissed() {
                // Not used in preload
            }
        });
    }
}
