package com.example.timerstudy.view.contracts;

import com.example.timerstudy.model.ShopItem;

import java.util.List;

public interface ShopContract {
    interface View {
        void updateShopItems(List<ShopItem> items);
        void updateCoins(int coins);
        void showPurchaseDialog(ShopItem item);
        void showInsufficientFundsDialog(ShopItem item); // New method
        void showAdForItem(ShopItem item); // Show ad for free items
        void showPurchaseSuccess(String message);
        void showPurchaseError(String message);
        void updateItemPurchased(int itemId);
        void showLoading();
        void hideLoading();
        void updateSelectedBackground(int itemId);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void onItemClicked(ShopItem item);
        void onPurchaseConfirmed(ShopItem item);
        void onAdWatchedForItem(ShopItem item); // Called when user watched ad for item
        int getUserCoins();
        void setUserId(int userId);
        void onBackgroundSelected(ShopItem item);
    }
}
