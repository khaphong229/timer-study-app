package com.example.timerstudy.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.timerstudy.R;
import com.google.android.material.navigationrail.NavigationRailView;
import com.google.android.material.navigation.NavigationBarView;
import com.example.timerstudy.utils.FakeDataSeeder;

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

        navigationRail.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_seed_data) {
                FakeDataSeeder.seed(this);
                android.widget.Toast.makeText(this, "Seeding demo data...", android.widget.Toast.LENGTH_SHORT).show();
                return true;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
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
