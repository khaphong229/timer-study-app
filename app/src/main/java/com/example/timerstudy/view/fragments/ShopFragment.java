package com.example.timerstudy.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.timerstudy.R;
import com.example.timerstudy.view.adapters.ShopAdapter;
import com.example.timerstudy.model.ShopItem;
import com.example.timerstudy.presenter.ShopPresenter;
import com.example.timerstudy.view.contracts.ShopContract;
import com.example.timerstudy.utils.AdManager;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment implements ShopContract.View {

    private ShopPresenter presenter;
    private GridView gridViewShop;
    private TextView tvCoins;
    private ProgressBar progressBar;
    private ShopAdapter adapter;
    private AdManager adManager;
    private ShopItem pendingAdItem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAdapter();
        setupPresenter();
    }

    private void initViews(View view) {
        gridViewShop = view.findViewById(R.id.gridViewShop);
        tvCoins = view.findViewById(R.id.tvCoins);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupAdapter() {
        adapter = new ShopAdapter(requireContext(), new ArrayList<>());
        gridViewShop.setAdapter(adapter);
        adapter.setOnItemClickListener(item -> presenter.onItemClicked(item));
    }

    private void setupPresenter() {
        presenter = ShopPresenter.getInstance();
        presenter.initialize(requireContext());
        presenter.attachView(this);

        initializeAdManager();
    }

    private void initializeAdManager() {
        adManager = new AdManager(requireContext());
        adManager.preloadAd();
    }

    @Override
    public void updateShopItems(List<ShopItem> items) {
        adapter.setItems(items);
    }

    @Override
    public void updateCoins(int coins) {
        tvCoins.setText(String.valueOf(coins));

        adapter.setUserCoins(coins);
    }

    @Override
    public void showPurchaseDialog(ShopItem item) {
        // Not used - using showAdForItem instead
    }

    @Override
    public void showInsufficientFundsDialog(ShopItem item) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Not enough coins")
                .setMessage("You don't have enough coins to buy this item. Watch an ad to earn coins?")
                .setPositiveButton("Watch Ad", (dialog, which) -> {
                    showAdForItem(item);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void showAdForItem(ShopItem item){
        pendingAdItem = item;

        if (adManager.isAdReady()) {
            showRewardedAdForItem();
        } else {
            showLoading();
            Toast.makeText(requireContext(), "Loading ad, please wait...", Toast.LENGTH_SHORT).show();

            adManager.loadRewardedAd(new AdManager.RewardListener() {
                @Override
                public void onAdLoaded() {
                    hideLoading();
                    showRewardedAdForItem();
                }

                @Override
                public void onAdFailedToLoad(String error) {
                    hideLoading();
                    Toast.makeText(requireContext(),
                        "Ad not available. Please try again later.",
                        Toast.LENGTH_SHORT).show();
                    pendingAdItem = null;
                }

                @Override
                public void onUserEarnedReward() {
                    // Handled in showRewardedAdForItem
                }

                @Override
                public void onAdDismissed() {
                    // Handled in showRewardedAdForItem
                }
            });
        }
    }

    private void showRewardedAdForItem() {
        if (pendingAdItem == null || !isAdded()) return;

        adManager.showRewardedAd(requireActivity(), new AdManager.RewardListener() {
            @Override
            public void onAdLoaded() {
                // Ad loaded
            }

            @Override
            public void onAdFailedToLoad(String error) {
                Toast.makeText(requireContext(), "Ad failed to load", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUserEarnedReward() {
                android.util.Log.d("ShopFragment", "onUserEarnedReward called");
                if (pendingAdItem != null && presenter != null) {
                    presenter.onAdWatchedForItem(pendingAdItem);
                    pendingAdItem = null;
                } else {
                    android.util.Log.e("ShopFragment", "pendingAdItem or presenter is null");
                }
            }

            @Override
            public void onAdDismissed() {
                // User closed ad without watching completely
                if (pendingAdItem != null) {
                    Toast.makeText(requireContext(),
                        "Watch the full ad to unlock this item",
                        Toast.LENGTH_SHORT).show();
                    pendingAdItem = null;
                }
            }
        });
    }

    @Override
    public void showPurchaseSuccess(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPurchaseError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateItemPurchased(int itemId) {
        adapter.updateItemPurchased(itemId);
    }

    @Override
    public void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (gridViewShop != null) {
            gridViewShop.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (gridViewShop != null) {
            gridViewShop.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateSelectedBackground(int itemId) {
        if (adapter != null) {
            adapter.updateSelectedItem(itemId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }
}