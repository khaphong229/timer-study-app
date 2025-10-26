package com.example.timerstudy.view.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.timerstudy.R;
import com.google.android.material.navigationrail.NavigationRailView;

public class MainActivity extends AppCompatActivity {
    
    private NavigationRailView navigationRail;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupNavigation();
    }
    
    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        navigationRail = findViewById(R.id.navigation_rail);
        NavigationUI.setupWithNavController(navigationRail, navController);
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
