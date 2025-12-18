package com.example.timerstudy.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.timerstudy.R;
import com.example.timerstudy.data.remote.ApiService;
import com.example.timerstudy.data.remote.RetrofitClient;
import com.example.timerstudy.model.ShopItem;
import com.example.timerstudy.utils.UserManager;
import com.example.timerstudy.worker.ShopSyncWorker;
import com.example.timerstudy.data.repository.UserRepository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopRepository {
    private static final String TAG = "ShopRepository";
    private static ShopRepository instance;
    private ApiService apiService;
    private Context context;
    private SharedPreferences prefs;
    private static final String PREF_NAME = "shop_prefs";
    private static final String KEY_PENDING_PURCHASES = "pending_purchases";
    private static final String KEY_PURCHASED_ITEMS = "purchased_items";
    private static final String KEY_SELECTED_BG = "selected_background_id";
    private static final String KEY_CACHED_ITEMS = "cached_shop_items";
    private static final String KEY_LAST_SYNC_TIME = "last_shop_sync_time";
    private static final long CACHE_EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes
    private boolean isFetching = false;
    private static final int MAX_PAGES = 10;

    private MutableLiveData<List<ShopItem>> shopItemsLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private ShopRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ShopRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ShopRepository(context);
        }
        return instance;
    }

    public LiveData<List<ShopItem>> getShopItems() {
        return shopItemsLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void fetchShopItems() {
        if (isFetching) return;

        // Check cache first
        if (loadFromCacheIfValid()) {
            return;
        }

        String token = UserManager.getInstance(context).getCurrentUser().getAccessToken();
        if (token == null) {
            Log.d("ShopRepository", "fetchShopItems: " + "User not logged in");
            errorLiveData.postValue("User not logged in");
            return;
        }

        isFetching = true;
        // Start fetching from page 1
        fetchAllPages(token, 1, new ArrayList<>());
    }

    private void fetchAllPages(String token, int page, List<ApiService.ShopItemResponse> accumulatedItems) {
        if (page > MAX_PAGES) {
            Log.d(TAG, "fetchAllPages: Reached MAX_PAGES (" + MAX_PAGES + "). Stopping.");
            handleFetchSuccess(accumulatedItems);
            isFetching = false;
            return;
        }


        Log.d(TAG, "fetchAllPages: Requesting page " + page + " with page_size=20");

        // Updated API call with individual parameters
        apiService.getAllShopItems("Bearer " + token, "id", "desc", 20, page).enqueue(new Callback<ApiService.ApiResponse<List<ApiService.ShopItemResponse>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<ApiService.ShopItemResponse>>> call, Response<ApiService.ApiResponse<List<ApiService.ShopItemResponse>>> response) {
                Log.d(TAG, "fetchAllPages: Response received for page " + page + ". Code: " + response.code());

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<ApiService.ShopItemResponse> items = response.body().data;
                    if (items != null) {
                        Log.d(TAG, "fetchAllPages: Page " + page + " returned " + items.size() + " items.");
                        for (ApiService.ShopItemResponse item : items) {
                            Log.d(TAG, "   - Item: ID=" + item.shopId + ", Name=" + item.name + ", Price=" + item.price + ", Purchased=" + item.isPurchased);
                        }
                        accumulatedItems.addAll(items);
                    }

                    ApiService.Metadata metadata = response.body().metadata;
                    if (metadata != null) {
                        Log.d(TAG, "fetchAllPages: Metadata - Page: " + metadata.page + ", Size: " + metadata.pageSize + ", Total: " + metadata.total);
                    }

                    // Check if there are more pages
                    // Logic: If current items count (page * pageSize) is less than total, fetch next
                    if (metadata != null && (metadata.page * metadata.pageSize < metadata.total)) {
                        Log.d(TAG, "fetchAllPages: Fetching next page...");
                        fetchAllPages(token, page + 1, accumulatedItems);
                    } else {
                        Log.d(TAG, "fetchAllPages: All pages fetched.");
                        handleFetchSuccess(accumulatedItems);
                        isFetching = false;
                    }
                } else {
                    Log.e(TAG, "fetchAllPages: Request failed. Code: " + response.code());
                    if (response.body() != null) {
                        Log.e(TAG, "fetchAllPages: Message: " + response.body().message);
                    }
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "fetchAllPages: Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "fetchAllPages: Error reading error body", e);
                    }

                    if (!accumulatedItems.isEmpty()) {
                        handleFetchSuccess(accumulatedItems);
                    } else {
                        errorLiveData.postValue("Failed to fetch items: " + (response.body() != null ? response.body().message : response.message()));
                        loadFromCacheForce();
                    }
                    isFetching = false;
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<ApiService.ShopItemResponse>>> call, Throwable t) {
                Log.e(TAG, "fetchAllPages: Network failure on page " + page, t);
                if (!accumulatedItems.isEmpty()) {
                    handleFetchSuccess(accumulatedItems);
                } else {
                    errorLiveData.postValue("Network error: " + t.getMessage());
                    loadFromCacheForce();
                }
                isFetching = false;
            }
        });
    }

    private void handleFetchSuccess(List<ApiService.ShopItemResponse> serverItems) {
        Log.d("ShopRepository", "fetchShopItems: " + serverItems.size() + " items fetched");
        saveToCache(serverItems);
        List<ShopItem> mergedItems = mergeWithLocalResources(serverItems);
        shopItemsLiveData.postValue(mergedItems);
    }

    private boolean loadFromCacheIfValid() {
        long lastSync = prefs.getLong(KEY_LAST_SYNC_TIME, 0);
        if (System.currentTimeMillis() - lastSync < CACHE_EXPIRATION_MS) {
            return loadFromCacheForce();
        }
        return false;
    }

    private boolean loadFromCacheForce() {
        String json = prefs.getString(KEY_CACHED_ITEMS, null);
        if (json != null) {
            try {
                Type type = new TypeToken<List<ApiService.ShopItemResponse>>() {}.getType();
                List<ApiService.ShopItemResponse> serverItems = new Gson().fromJson(json, type);
                if (serverItems != null) {
                    List<ShopItem> mergedItems = mergeWithLocalResources(serverItems);
                    shopItemsLiveData.postValue(mergedItems);
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing cached items", e);
            }
        }
        return false;
    }

    private void saveToCache(List<ApiService.ShopItemResponse> items) {
        try {
            String json = new Gson().toJson(items);
            prefs.edit()
                    .putString(KEY_CACHED_ITEMS, json)
                    .putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving items to cache", e);
        }
    }

    private List<ShopItem> mergeWithLocalResources(List<ApiService.ShopItemResponse> serverItems) {
        List<ShopItem> result = new ArrayList<>();
        Set<Integer> addedIds = new HashSet<>();
        int selectedId = getSelectedBackgroundId(); // Lấy ID đang chọn

        // --- Xử lý Default Item ---
        ShopItem defaultItem = new ShopItem(0, "Default", "Mặc định", 0, R.drawable.sbg_default, ShopItem.ItemType.BACKGROUND);
        defaultItem.setPurchased(true);
        if (defaultItem.getId() == selectedId) {
            defaultItem.setSelected(true); // <--- Đánh dấu nếu đang chọn Default
        }
        result.add(defaultItem);
        addedIds.add(0);
        
        // --- Xử lý Server Items ---
        for (ApiService.ShopItemResponse serverItem : serverItems) {
            if (addedIds.contains(serverItem.shopId)) {
                continue;
            }

            int resourceId = getResourceIdForName(serverItem.name);
            if (resourceId != 0) {
                // FIX: Bỏ qua nếu server trả về item trùng với Default (đã thêm thủ công)
                if (resourceId == R.drawable.sbg_default) {
                    continue;
                }

                ShopItem item = new ShopItem(
                        serverItem.shopId,
                        serverItem.name,
                        serverItem.description,
                        serverItem.price,
                        resourceId,
                        ShopItem.ItemType.BACKGROUND // Assuming all are backgrounds for now
                );

                if (isPurchasePending(serverItem.shopId)) {
                    item.setPurchased(true);
                } else if (serverItem.isPurchased) {
                    item.setPurchased(true);
                }

                if (item.getId() == selectedId) {
                    item.setSelected(true);
                }
                
                result.add(item);
                addedIds.add(serverItem.shopId);
            }
        }
        return result;
    }

    private int getResourceIdForName(String name) {
        if (name == null || name.isEmpty()) return 0;

        String resourceName = name.toLowerCase().trim();

        if (resourceName.contains(".")) {
            resourceName = resourceName.substring(0, resourceName.lastIndexOf('.'));
        }

        try {
            Field field = R.drawable.class.getField(resourceName);
            return field.getInt(null);
        } catch (Exception e) {
            try {
                int resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
                if (resId != 0) return resId;
            } catch (Exception ex) {
                // Ignore
            }
            Log.e(TAG, "Resource not found for name: " + name + " (normalized: " + resourceName + ")");
            return 0;
        }
    }

    public void purchaseItem(int shopId) {
        String token = UserManager.getInstance(context).getCurrentUser().getAccessToken();
        if (token == null) return;

        // Find item to get price and deduct coins locally
        List<ShopItem> currentItems = shopItemsLiveData.getValue();
        ShopItem targetItem = null;
        if (currentItems != null) {
            for (ShopItem item : currentItems) {
                if (item.getId() == shopId) {
                    targetItem = item;
                    break;
                }
            }
        }

        if (targetItem != null) {
            if (targetItem.isPurchased()) {
                return; // Already purchased
            }
            
            // Deduct coins locally (Optimistic update)
            boolean success = UserManager.getInstance(context).subtractCoins(targetItem.getPrice());
            if (!success) {
                errorLiveData.postValue("Not enough coins");
                return;
            }
        }

        addPendingPurchase(shopId);
        
        // Trigger UI update (refresh list)
        if (targetItem != null) {
            targetItem.setPurchased(true);
            shopItemsLiveData.postValue(currentItems);
        }

        apiService.purchaseShopItem("Bearer " + token, shopId, new ApiService.ShopPurchaseRequest()).enqueue(new Callback<ApiService.ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<Void>> call, Response<ApiService.ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    removePendingPurchase(shopId);
                    updateCacheWithPurchase(shopId);
                    
                    // Refresh user profile to update coins
                    UserRepository.getInstance(context).fetchUserCoin(token);
                } else {
                    // Failed, but we have it in pending, so WorkManager will handle it?
                    // Or we should schedule WorkManager here.
                    scheduleSyncWorker();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<Void>> call, Throwable t) {
                // Network fail, schedule WorkManager
                scheduleSyncWorker();
            }
        });
    }

    private void updateCacheWithPurchase(int shopId) {
        String json = prefs.getString(KEY_CACHED_ITEMS, null);
        if (json != null) {
            try {
                Type type = new TypeToken<List<ApiService.ShopItemResponse>>() {}.getType();
                List<ApiService.ShopItemResponse> cachedItems = new Gson().fromJson(json, type);
                
                boolean found = false;
                if (cachedItems != null) {
                    for (ApiService.ShopItemResponse item : cachedItems) {
                        if (item.shopId == shopId) {
                            item.isPurchased = true;
                            found = true;
                            break;
                        }
                    }
                }
                
                if (found) {
                    saveToCache(cachedItems);
                    Log.d(TAG, "Updated local cache for purchased item: " + shopId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating cache for purchase", e);
            }
        }
    }

    private void addPendingPurchase(int shopId) {
        Set<String> pending = prefs.getStringSet(KEY_PENDING_PURCHASES, new HashSet<>());
        Set<String> newPending = new HashSet<>(pending);
        newPending.add(String.valueOf(shopId));
        prefs.edit().putStringSet(KEY_PENDING_PURCHASES, newPending).apply();
    }

    private void removePendingPurchase(int shopId) {
        Set<String> pending = prefs.getStringSet(KEY_PENDING_PURCHASES, new HashSet<>());
        Set<String> newPending = new HashSet<>(pending);
        newPending.remove(String.valueOf(shopId));
        prefs.edit().putStringSet(KEY_PENDING_PURCHASES, newPending).apply();
    }

    private boolean isPurchasePending(int shopId) {
        Set<String> pending = prefs.getStringSet(KEY_PENDING_PURCHASES, new HashSet<>());
        return pending.contains(String.valueOf(shopId));
    }
    
    public Set<String> getPendingPurchases() {
        return prefs.getStringSet(KEY_PENDING_PURCHASES, new HashSet<>());
    }

    public void selectBackground(int id) {
        // 1. Lưu vào SharedPreferences của Shop (như cũ)
        prefs.edit().putInt(KEY_SELECTED_BG, id).apply();
        
        // 2. Đồng bộ sang User Object (MỚI)
        try {
            com.example.timerstudy.model.User user = UserManager.getInstance(context).getCurrentUser();
            if (user != null) {
                user.setSelectedBackgroundId(id);
                UserManager.getInstance(context).saveUser(); // Lưu User xuống local DB/Prefs
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing selected background to User object", e);
        }
    }

    public int getSelectedBackgroundId() {
        // Default to 0 (Default background) if not set
        return prefs.getInt(KEY_SELECTED_BG, 0);
    }

    public int getBackgroundResourceId(int id) {
        if (id == 0) return R.drawable.sbg_default;
        List<ShopItem> items = shopItemsLiveData.getValue();
        if (items != null) {
            for (ShopItem item : items) {
                if (item.getId() == id) {
                    return item.getImageResourceId();
                }
            }
        }
        return 0;
    }

    public boolean syncPendingPurchasesSync() {
        Set<String> pending = getPendingPurchases();
        if (pending.isEmpty()) return true;

        String token = UserManager.getInstance(context).getCurrentUser().getAccessToken();
        if (token == null) return false;

        boolean allSuccess = true;
        for (String shopIdStr : pending) {
            int shopId = Integer.parseInt(shopIdStr);
            try {
                Response<ApiService.ApiResponse<Void>> response = apiService.purchaseShopItem("Bearer " + token, shopId, new ApiService.ShopPurchaseRequest()).execute();
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    removePendingPurchase(shopId);
                } else {
                    allSuccess = false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Sync failed for item " + shopId, e);
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    private void scheduleSyncWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(ShopSyncWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueue(syncRequest);
    }
}
