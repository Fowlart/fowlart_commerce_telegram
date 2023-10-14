package com.fowlart.main.state.inmem;

import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class SessionCarts {

    private final Map<String, List<String>> carts;

    public SessionCarts() {
        carts = Maps.newHashMap();
    }

    public List<String> getCart(String userId) {
        var cart =carts.get(userId);
        if (Objects.nonNull(cart)) {
            return cart;
        }
        else {
            return new ArrayList<>();
        }
    }

    public void removeItem(String item, String userId) {
        var cart = carts.get(userId);
        if (cart != null) {
            cart.remove(item);
        }
        carts.put(userId, cart);
    }

    public void addItem(String item, String userId) {
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
