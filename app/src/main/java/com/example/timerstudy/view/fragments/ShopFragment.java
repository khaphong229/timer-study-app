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

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment implements ShopContract.View {

    private ShopPresenter presenter;
    private GridView gridViewShop;
    private TextView tvCoins;
    private ProgressBar progressBar;
    private ShopAdapter adapter;

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
    }

    @Override
    public void updateShopItems(List<ShopItem> items) {
        adapter.setItems(items);
    }

    @Override
    public void updateCoins(int coins) {
        tvCoins.setText(String.valueOf(coins));
    }

    @Override
    public void showPurchaseDialog(ShopItem item) {
        
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