package dev.paramountdev.zlomCore_PDev.orders;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderManager {

    private final List<Order> orders = new ArrayList<>();

    public void addOrder(Order order) {
        orders.add(order);
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void removeCompletedOrders() {
        orders.removeIf(Order::isCompleted);
    }

    public boolean canCreateMoreOrders(UUID player) {
        int max = ZlomCore_PDev.getInstance().getConfig().getInt("max-orders-per-player", 5);
        return getOrders().stream().filter(o -> o.getCreator().equals(player)).count() < max;
    }

}
