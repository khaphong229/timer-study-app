package com.example.timerstudy.view.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.timerstudy.R;
import com.example.timerstudy.databinding.ActivityMainBinding;

/**
 * MainActivity - NavHost container
 * Đóng vai trò container cho Navigation Component
 * Quản lý Bottom Navigation và Toolbar
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sử dụng ViewBinding thay vì findViewById
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Navigation Component
        setupNavigation();
    }

    /**
     * Cấu hình Navigation Component
     * Kết nối NavController với BottomNavigationView và Toolbar
     */
    private void setupNavigation() {
        // Lấy NavHostFragment từ layout
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Cấu hình các top-level destinations
            // Những màn hình này sẽ không hiển thị back button
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.homeFragment,
                    R.id.profileFragment
            ).build();

            // Kết nối Toolbar với NavController
            NavigationUI.setupWithNavController(
                    binding.toolbar,
                    navController,
                    appBarConfiguration
            );

        // Kết nối NavigationRailView (right-side) với NavController
        // Tự động điều hướng khi click vào các item
        NavigationUI.setupWithNavController(
            binding.navigationRail,
            navController
        );

            // Optional: Listener để xử lý navigation events
            setupNavigationListener();
        }
    }

    /**
     * Setup listener cho navigation events
     * Có thể dùng để tracking, analytics, hoặc custom behaviors
     */
    private void setupNavigationListener() {
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Log hoặc xử lý khi destination thay đổi
            String destinationName = destination.getLabel() != null
                    ? destination.getLabel().toString()
                    : "Unknown";

            // Có thể thêm logic tracking ở đây
            // Analytics.logScreenView(destinationName);
        });
    }

    /**
     * Xử lý back button
     * Ưu tiên để Navigation Component xử lý
     */
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    /**
     * Clean up resources
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
