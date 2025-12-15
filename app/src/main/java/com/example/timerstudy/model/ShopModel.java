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
    private SharedPreferences prefs;
    private static final String PREF_NAME = "shop_prefs";
    private static final String KEY_PURCHASED_ITEMS = "purchased_items";
    private static final String KEY_SELECTED_BG = "selected_background_id";

    public ShopModel() {
        shopItems = new ArrayList<>();
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
                if (name.startsWith("sbg_")) {
                    int resourceId = field.getInt(null);
                    
                    // Xác định giá cho từng background
                    int price = getPriceForBackground(name, id);
                    
                    ShopItem item = new ShopItem(
                            id,
                            formatBackgroundName(name),
                            getBackgroundDescription(price),
                            price,
                            resourceId,
                            ShopItem.ItemType.BACKGROUND
                    );

                    if (name.equals("sbg_default")) {
                        item.setPurchased(true);
                        savePurchasedItem(item.getId());
                    }

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
    
    /**
     * Xác định giá cho background:
     * Tất cả items đều có giá, không có items FREE
     * Nếu user không đủ coins, họ xem ad để kiếm thêm
     */
    private int getPriceForBackground(String name, int id) {
        // Default background miễn phí và đã unlock
        if (name.equals("sbg_default")) {
            return 0; // Đã unlock sẵn
        }
        
        // Phân loại giá dựa theo vị trí
        int position = id % 3;
        
        if (position == 0) {
            // Giá rẻ - 30,000 coins
            return 30000;
        } else if (position == 1) {
            // Giá vừa - 50,000 coins (bằng 1 lần xem ad)
            return 50000;
        } else {
            // Giá cao - 100,000 coins
            return 100000;
        }
    }
    
    private String formatBackgroundName(String name) {
        // Chuyển "sbg_rain_girl_frog" thành "Rain Girl Frog"
        String formatted = name.replace("sbg_", "")
                               .replace("_", " ");
        // Capitalize first letter of each word
        String[] words = formatted.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }
    
    private String getBackgroundDescription(int price) {
        if (price == 0) {
            return "Default unlocked";
        } else if (price <= 30000) {
            return "Basic background";
        } else if (price <= 50000) {
            return "Premium background";
        } else {
            return "Exclusive background";
        }
    }

   
    public boolean purchaseItem(ShopItem item) {
        item.setPurchased(true);
        savePurchasedItem(item.getId());
        return true;
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
