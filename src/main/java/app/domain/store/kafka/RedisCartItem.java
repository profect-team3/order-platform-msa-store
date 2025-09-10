package app.domain.store.kafka;

import java.util.UUID;

public class RedisCartItem {
    private UUID menuId;
    private UUID storeId;
    private int quantity;

    public RedisCartItem() {}

    public RedisCartItem(UUID menuId, UUID storeId, int quantity) {
        this.menuId = menuId;
        this.storeId = storeId;
        this.quantity = quantity;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public UUID getMenuId() {
        return menuId;
    }

    public int getQuantity(){
        return quantity;
    }
}