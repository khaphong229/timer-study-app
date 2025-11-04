package com.example.timerstudy.model;

public class ShopItem {
    private int id;
    private String name;
    private String description;
    private int price;
    private int imageResourceId;
    private boolean isPurchased;
    private ItemType type;
    private boolean selected;

    public enum ItemType {
        BACKGROUND,
    }

    public ShopItem(int id, String name, String description, int price,
            int imageResourceId, ItemType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResourceId = imageResourceId;
        this.type = type;
        this.isPurchased = false;
        this.selected = false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
    }

    public ItemType getType() {
        return type;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
