package com.fowlart.main.state;

import com.fowlart.main.in_mem_catalog.Item;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class BotVisitor implements Serializable {

    private Item itemToEditQty;

    private User user;

    private long userId;

    private Set<Item> bucket = new HashSet<>();

    public BotVisitor(User user, long userId) {
        this.user = user;
        this.userId = userId;
    }

    public Set<Item> getBucket() {
        return bucket;
    }

    public void setBucket(Set<Item> bucket) {
        this.bucket = bucket;
    }

    public Item getItemToEditQty() {
        return itemToEditQty;
    }

    public void setItemToEditQty(Item itemToEditQty) {
        this.itemToEditQty = itemToEditQty;
    }

    public String getUserId() {
        return String.valueOf(userId);
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "BotVisitor{" +
                "itemToEditQty=" + itemToEditQty +
                ", user=" + user +
                ", userId=" + userId +
                '}';
    }
}
