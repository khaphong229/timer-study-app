package com.example.timerstudy.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.timerstudy.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShopModel {
    private List<ShopItem> shopItems;
    private int userCoins;
    private SharedPreferences prefs;
    private static final String PREF_NAME = "shop_prefs";
    private static final String KEY_PURCHASED_ITEMS = "purchased_items";
    private static final String KEY_SELECTED_BG = "selected_background_id";

    public ShopModel() {
        shopItems = new ArrayList<>();
        userCoins = 0;
    }

    public void initialize(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public List<ShopItem> loadShopItems() {
        shopItems.clear();
        Set<String> purchasedIds = getPurchasedItemIds();
        int selectedBgId = getSelectedBackgroundId();
        
        int id = 0;
        Field[] fields = R.drawable.class.getFields();
        for (Field field : fields) {
            try {
                String name = field.getName();
                if (name.startsWith("bg_")) {
                    int resourceId = field.getInt(null);
                    ShopItem item = new ShopItem(
                            id,
                            name,
                            "",
                            0,
                            resourceId,
                            ShopItem.ItemType.BACKGROUND
                    );

                    if (purchasedIds.contains(String.valueOf(id))) {
                        item.setPurchased(true);
                    }

                    if (id == selectedBgId) {
                        item.setSelected(true);
                    }
                    
                    shopItems.add(item);
                    id++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return shopItems;
    }

    public void calculateUserCoins(int completedSessions) {
        this.userCoins = completedSessions * 10;
    }

    public int getUserCoins() {
        return userCoins;
    }

    public void setUserCoins(int coins) {
        this.userCoins = coins;
    }

    public boolean purchaseItem(ShopItem item) {
        if (userCoins >= item.getPrice()) {
            userCoins -= item.getPrice();
            item.setPurchased(true);

            savePurchasedItem(item.getId());
            return true;
        }
        return false;
    }

    public void selectBackground(int itemId) {
        for (ShopItem item : shopItems) {
            item.setSelected(false);
        }

        ShopItem selectedItem = getItemById(itemId);
        if (selectedItem != null && selectedItem.isPurchased()) {
            selectedItem.setSelected(true);
            saveSelectedBackground(itemId);
        }
    }

    public void savePurchasedItem(int itemId) {
        if (prefs != null) {
            Set<String> purchased = getPurchasedItemIds();
            purchased.add(String.valueOf(itemId));
            prefs.edit().putStringSet(KEY_PURCHASED_ITEMS, purchased).apply();
        }
    }

    public Set<String> getPurchasedItemIds() {
        if (prefs != null) {
            return new HashSet<>(prefs.getStringSet(KEY_PURCHASED_ITEMS, new HashSet<>()));
        }
        return new HashSet<>();
    }

    public void saveSelectedBackground(int backgroundId) {
        if (prefs != null) {
            prefs.edit().putInt(KEY_SELECTED_BG, backgroundId).apply();
        }
    }

    public int getSelectedBackgroundId() {
        if (prefs != null) {
            return prefs.getInt(KEY_SELECTED_BG, -1);
        }
        return -1;
    }

    public ShopItem getItemById(int itemId) {
        for (ShopItem item : shopItems) {
            if (item.getId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public List<ShopItem> getShopItems() {
        return shopItems;
    }

    public int getBackgroundResourceId(int itemId) {
        for (ShopItem item : getShopItems()) {
            if (item.getId() == itemId && item.getType() == ShopItem.ItemType.BACKGROUND) {
                return item.getImageResourceId();
            }
        }
        return -1;
    }
}
