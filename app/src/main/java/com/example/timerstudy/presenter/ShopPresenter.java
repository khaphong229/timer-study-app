package com.example.timerstudy.presenter;

import android.content.Context;

import com.example.timerstudy.R;
import com.example.timerstudy.data.repository.SessionRepository;
import com.example.timerstudy.model.ShopItem;
import com.example.timerstudy.model.ShopModel;
import com.example.timerstudy.view.contracts.ShopContract;

public class ShopPresenter implements ShopContract.Presenter {

    private ShopContract.View view;
    private ShopModel model;
    public static ShopPresenter instance;

    private Context context;
    private int userId = 1;
    private SessionRepository sessionRepository;
    private int completedSessionsFromDb = 0;

    public static ShopPresenter getInstance() {
        if (instance == null) {
            instance = new ShopPresenter();
        }
        return instance;
    }

    public ShopPresenter() {
        model = new ShopModel();
    }

    public void initialize(Context context) {
        this.context = context;

        model.initialize(context);
        model.loadShopItems();
        this.sessionRepository = SessionRepository.getInstance(context);
        loadCompletedSessionsCount();
    }

    @Override
    public void attachView(ShopContract.View view) {
        this.view = view;
        initializeView();
        observeCompletedSessionsCount();
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    private void initializeView() {
        if (view != null) {
            view.showLoading();
            view.updateShopItems(model.getShopItems());

            model.calculateUserCoins(completedSessionsFromDb);
            view.updateCoins(model.getUserCoins());
            view.hideLoading();
        }
    }

    @Override
    public void onItemClicked(ShopItem item) {
        if (view != null) {
            if (item.isPurchased()) {
                onBackgroundSelected(item);
            } else {

                if (model.getUserCoins() >= item.getPrice()) {
                    onPurchaseConfirmed(item);
                } else {
                    view.showAdForItem(item);
                }
            }
        }
    }

    @Override
    public void onBackgroundSelected(ShopItem item) {
        if (item.isPurchased()) {

            model.selectBackground(item.getId());
            saveSelectedBackground(item.getId());

            if (view != null) {
                view.updateSelectedBackground(item.getId());
                view.showPurchaseSuccess("Background đã được áp dụng!");
            }

        }
    }

    public void saveSelectedBackground(int backgroundId) {
        if (context != null) {
            model.saveSelectedBackground(backgroundId);
        }
    }

    public int getSelectedBackgroundId() {
        if (model != null) {
            return model.getSelectedBackgroundId();
        }
        return -1;
    }

    @Override
    public void onPurchaseConfirmed(ShopItem item) {
        if (model.purchaseItem(item)) {
            if (view != null) {
                view.showPurchaseSuccess("Purchased " + item.getName() + " successfully!");
                view.updateItemPurchased(item.getId());
                view.updateCoins(model.getUserCoins());

                if (item.getType() == ShopItem.ItemType.BACKGROUND) {
                    onBackgroundSelected(item);
                }

                savePurchasedItem(item);
            }
        } else {
            if (view != null) {
                view.showPurchaseError("Not enough coins!");
            }
        }
    }
    
    @Override
    public void onAdWatchedForItem(ShopItem item) {
        // User đã xem quảng cáo đầy đủ - thêm coins thưởng
        int rewardCoins = 50000; // Coins nhận từ xem ad
        model.setUserCoins(model.getUserCoins() + rewardCoins);
        
        if (view != null) {
            view.showPurchaseSuccess("Earned " + rewardCoins + " coins from watching ad!");
            view.updateCoins(model.getUserCoins());
            
            // Sau khi có coins, tự động thử mua item
            if (model.getUserCoins() >= item.getPrice()) {
                onPurchaseConfirmed(item);
            }
        }
    }

    @Override
    public int getUserCoins() {
        return model.getUserCoins();
    }

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
        loadCompletedSessionsCount();
    }

    private void loadCompletedSessionsCount() {
        if (sessionRepository != null) {
            // Load ALL completed sessions (not just today) for total coins calculation
            sessionRepository.loadTotalCompletedSessionsCount(userId);
        }
    }

    private void observeCompletedSessionsCount() {
        if (sessionRepository != null && view != null) {
            sessionRepository.getCompletedSessionsCountLiveData().observe(
                    ((androidx.lifecycle.LifecycleOwner) view),
                    count -> {
                        if (count != null) {
                            completedSessionsFromDb = count;

                            model.calculateUserCoins(count);
                            if (view != null) {
                                view.updateCoins(model.getUserCoins());
                            }
                        }
                    });
        }
    }

    private void savePurchasedItem(ShopItem item) {
        int id = item.getId();
        model.savePurchasedItem(id);
    }

    public int getBackgroundResourceId(int itemId) {
        return model.getBackgroundResourceId(itemId);
    }

    public int getBackgroundSelectedResourceId() {
        if (model == null) {
            return getDefaultBackgroundResourceId();
        }
        int id = getSelectedBackgroundId();
        int resourceId = getBackgroundResourceId(id);
        if (resourceId == -1) {
            resourceId = R.drawable.sbg_default;
            saveSelectedBackground(id);
        }
        return resourceId;
    }

    private int getDefaultBackgroundResourceId() {
        return com.example.timerstudy.R.drawable.sbg_default;
    }
}