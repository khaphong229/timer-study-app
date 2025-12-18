package com.example.timerstudy.view.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.timerstudy.R;
import com.google.android.material.navigationrail.NavigationRailView;
import com.google.android.material.navigation.NavigationBarView;
import com.example.timerstudy.utils.FakeDataSeeder;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {

    private NavigationRailView navigationRail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Generate Facebook key hash for debugging
        generateKeyHash();

        // Hide system UI for full screen
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_main);

        setupNavigation();
        try {
            android.content.pm.PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.timerstudy", // Đã điền sẵn package của bạn
                    android.content.pm.PackageManager.GET_SIGNATURES);

            for (android.content.pm.Signature signature : info.signatures) {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                android.util.Log.d("KeyHash",
                        android.util.Base64.encodeToString(md.digest(), android.util.Base64.DEFAULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("Facebook KeyHash:", keyHash);
                Log.d("Facebook KeyHash:", "Add this key hash to Facebook app settings: " + keyHash.trim());
            }
        } catch (Exception e) {
            Log.e("Facebook KeyHash", "Error generating key hash", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Get the current fragment from NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            androidx.fragment.app.Fragment currentFragment = navHostFragment.getChildFragmentManager()
                    .getPrimaryNavigationFragment();
            if (currentFragment != null) {
                currentFragment.onActivityResult(requestCode, resultCode, data);
            }
        }

        // Also forward to all fragments as fallback
        for (androidx.fragment.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        navigationRail = findViewById(R.id.navigation_rail);
        NavigationUI.setupWithNavController(navigationRail, navController);

        navigationRail.setOnItemSelectedListener(item -> {
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        com.example.timerstudy.utils.UserManager.getInstance(this)
                .checkAndRefreshToken(new com.example.timerstudy.data.repository.UserRepository.SyncCallback() {
                    @Override
                    public void onSuccess(com.example.timerstudy.model.User user) {
                        Log.d("MainActivity", "Token check/refresh successful");
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("MainActivity", "Token check/refresh failed: " + message);
                    }
                });
    }

    /**
     * Toggle visibility của Navigation Rail
     */
    public void toggleNavigationRail() {
        if (navigationRail != null) {
            if (navigationRail.getVisibility() == View.VISIBLE) {
                navigationRail.setVisibility(View.GONE);
            } else {
                navigationRail.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Ẩn Navigation Rail
     */
    public void hideNavigationRail() {
        if (navigationRail != null) {
            navigationRail.setVisibility(View.GONE);
        }
    }

    /**
     * Hiện Navigation Rail
     */
    public void showNavigationRail() {
        if (navigationRail != null) {
            navigationRail.setVisibility(View.VISIBLE);
        }
    }
}
