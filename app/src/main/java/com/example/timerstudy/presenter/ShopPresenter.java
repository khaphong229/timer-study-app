package com.example.timerstudy.presenter;

import android.content.Context;
import androidx.lifecycle.Observer;

import com.example.timerstudy.R;
import com.example.timerstudy.data.repository.ShopRepository;
import com.example.timerstudy.data.repository.UserRepository;
import com.example.timerstudy.data.local.database.entities.UserEntity;
import com.example.timerstudy.model.ShopItem;
import com.example.timerstudy.utils.UserManager;
import com.example.timerstudy.view.contracts.ShopContract;

import java.util.List;

public class ShopPresenter implements ShopContract.Presenter {

    private ShopContract.View view;
    private ShopRepository repository;
    public static ShopPresenter instance;

    private Context context;
    private UserManager userManager;
    
    private Observer<List<ShopItem>> shopItemsObserver = new Observer<List<ShopItem>>() {
        @Override
        public void onChanged(List<ShopItem> items) {
            if (view != null) {
                view.updateShopItems(items);
                view.hideLoading();
                
                // Update selected background if needed
                int selectedId = repository.getSelectedBackgroundId();
                view.updateSelectedBackground(selectedId);
            }
        }
    };
    
    private Observer<String> errorObserver = new Observer<String>() {
        @Override
        public void onChanged(String error) {
            if (view != null && error != null) {
                view.showPurchaseError(error);
                view.hideLoading();
            }
        }
    };

    private Observer<UserEntity> userObserver = new Observer<UserEntity>() {
        @Override
        public void onChanged(UserEntity userEntity) {
            if (view != null && userEntity != null) {
                view.updateCoins(userEntity.getTotalCoins());
                userManager.loadCurrentUser();
            }
        }
    };

    public static ShopPresenter getInstance() {
        if (instance == null) {
            instance = new ShopPresenter();
        }
        return instance;
    }

    public ShopPresenter() {
        // Repository initialized in initialize()
    }

    public void initialize(Context context) {
        this.context = context;
        this.userManager = UserManager.getInstance(context);
        this.repository = ShopRepository.getInstance(context);

        repository.getShopItems().observeForever(shopItemsObserver);
        repository.getError().observeForever(errorObserver);
        UserRepository.getInstance(context).getCurrentUserLiveData().observeForever(userObserver);
        
        repository.fetchShopItems();
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
            if (userManager.getCurrentUser() == null || !userManager.getCurrentUser().isLoggedIn()) {
                view.showLoginRequired();
                return;
            }

            view.showLoading();
            view.updateCoins(userManager.getTotalCoins());
            
            List<ShopItem> currentItems = repository.getShopItems().getValue();
            if (currentItems != null) {
                view.updateShopItems(currentItems);
                view.hideLoading();
                view.updateSelectedBackground(repository.getSelectedBackgroundId());
            } else {
                repository.fetchShopItems();
            }
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
            repository.selectBackground(item.getId());
            if (view != null) {
                view.updateSelectedBackground(item.getId());
                view.showPurchaseSuccess("Background đã được áp dụng!");
            }
        }
    }

    @Override
    public void onPurchaseConfirmed(ShopItem item) {
        // Check coins locally first
        if (userManager.getTotalCoins() >= item.getPrice()) {
            // Call repository to handle purchase (it will do optimistic update and API call)
            repository.purchaseItem(item.getId());
            
            if (view != null) {
                view.showPurchaseSuccess("Purchased " + item.getName() + " successfully!");
                view.updateItemPurchased(item.getId());
                // Coin update will happen via LiveData observer
                
                if (item.getType() == ShopItem.ItemType.BACKGROUND) {
                    onBackgroundSelected(item);
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
            // Coin update will happen via LiveData observer

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

    public int getBackgroundResourceId(int itemId) {
        return repository.getBackgroundResourceId(itemId);
    }

    public int getBackgroundSelectedResourceId() {
        if (repository == null) {
            return R.drawable.sbg_default;
        }
        int id = repository.getSelectedBackgroundId();
        int resourceId = repository.getBackgroundResourceId(id);
        if (resourceId == 0) { // 0 means not found
            resourceId = R.drawable.sbg_default;
        }
        return resourceId;
    }
}