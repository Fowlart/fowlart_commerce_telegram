package com.fowlart.main.state.inmem;

import com.fowlart.main.state.cosmos.Item;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class SessionCarts {

    private final Map<String, List<Item>> carts;

    public SessionCarts() {
        carts = Maps.newHashMap();
    }

    public List<Item> getCart(String userId) {
        var cart =carts.get(userId);
        if (Objects.nonNull(cart)) {
            return cart;
        }
        else {
            return new ArrayList<>();
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
            cart = new ArrayList<>();
            cart.add(item);
        } else {
            cart.add(item);
        }
        carts.put(userId, cart);
    }

}
