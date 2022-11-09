package com.fowlart.main.state;

import com.fowlart.main.in_mem_catalog.Item;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BotVisitor implements Serializable {

    private Item itemToEditQty;

    private User user;

    private Buttons buttons;
    
    private long userId;

    private Set<Item> bucket = new HashSet<>();

    public void setBucket(Set<Item> bucket) {
        this.bucket = bucket;
    }

    public Set<Item> getBucket() {
        return bucket;
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

    public BotVisitor(User user, Buttons buttons, long userId) {
        this.user = user;
        this.buttons = buttons;
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Buttons getState() {
        return buttons;
    }

    public void setState(Buttons buttons) {
        this.buttons = buttons;
    }

    @Override
    public String toString() {
        return "BotVisitor{" +
                "user_id=" + user.getId() +
                ", state=" + buttons +
                '}';
    }
}
