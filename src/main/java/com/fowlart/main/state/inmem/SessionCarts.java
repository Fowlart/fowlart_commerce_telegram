package com.fowlart.main.state.inmem;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SessionCarts {

    private final Map<String, Set<Item>> carts;

    public SessionCarts() {
        carts = new HashMap<>();
    }

    public Set<Item> getCart(String userId) {
        var cart =carts.get(userId);
        if (Objects.nonNull(cart)) {
            return cart;
        }
        else {
            return new HashSet<>();
        }
    }

    public void removeItem(Item item, String userId) {
        var cart = carts.get(userId);
        if (cart != null) {
            cart.remove(item);
        }
        carts.put(userId, cart);
    }

    public void addItem(Item item, String userId) {
        var cart = carts.get(userId);
        if (cart == null) {
            cart = new HashSet<>();
            cart.add(item);
        } else {
            cart.remove(item);
            cart.add(item);
        }
        carts.put(userId, cart);
    }
}