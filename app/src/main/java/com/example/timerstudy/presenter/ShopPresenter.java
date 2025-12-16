package com.example.timerstudy.presenter;

import android.content.Context;

import com.example.timerstudy.R;
import com.example.timerstudy.data.repository.SessionRepository;
import com.example.timerstudy.model.ShopItem;
import com.example.timerstudy.model.ShopModel;
import com.example.timerstudy.utils.UserManager;
import com.example.timerstudy.view.contracts.ShopContract;

public class ShopPresenter implements ShopContract.Presenter {

    private ShopContract.View view;
    private ShopModel model;
    public static ShopPresenter instance;

    private Context context;
    private UserManager userManager;

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
        this.userManager = UserManager.getInstance(context);

        model.initialize(context);
        model.loadShopItems();
    }

    @Override
    public void attachView(ShopContract.View view) {
        this.view = view;
        initializeView();
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    private void initializeView() {
        if (view != null) {
            view.showLoading();
            view.updateShopItems(model.getShopItems());
            view.updateCoins(userManager.getTotalCoins());
            view.hideLoading();
        }
    }

    @Override
    public void onItemClicked(ShopItem item) {
        if (view != null) {
            if (item.isPurchased()) {
                onBackgroundSelected(item);
            } else {
                if (userManager.getTotalCoins() >= item.getPrice()) {
                    onPurchaseConfirmed(item);
                } else {
                    view.showInsufficientFundsDialog(item);
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
        if (userManager.subtractCoins(item.getPrice())) {
            if (model.purchaseItem(item)) {
                if (view != null) {
                    view.showPurchaseSuccess("Purchased " + item.getName() + " successfully!");
                    view.updateItemPurchased(item.getId());
                    view.updateCoins(userManager.getTotalCoins());

                    if (item.getType() == ShopItem.ItemType.BACKGROUND) {
                        onBackgroundSelected(item);
                    }

                    savePurchasedItem(item);
                }
            }
        } else {
            if (view != null) {
                view.showPurchaseError("Not enough coins!");
            }
        }
    }
    
    @Override
    public void onAdWatchedForItem(ShopItem item) {

        int rewardCoins = item.getPrice();
        userManager.addCoins(rewardCoins);
        
        if (view != null) {
            view.showPurchaseSuccess("Earned " + rewardCoins + " coins from watching ad!");
            view.updateCoins(userManager.getTotalCoins());

            if (userManager.getTotalCoins() >= item.getPrice()) {
                onPurchaseConfirmed(item);
            }
        }
    }

    @Override
    public int getUserCoins() {
        return userManager.getTotalCoins();
    }

    @Override
    public void setUserId(long userId) {
        // No longer needed
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