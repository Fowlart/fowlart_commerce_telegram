package com.fowlart.main.state;

import com.fowlart.main.in_mem_catalog.Item;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class BotVisitor implements Serializable {

    private String phoneNumber;
    private boolean isPhoneNumberFillingMode;
    
    private String name;
    
    private boolean isNameEditingMode;

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isPhoneNumberFillingMode() {
        return isPhoneNumberFillingMode;
    }

    public void setPhoneNumberFillingMode(boolean phoneNumberFillingMode) {
        isPhoneNumberFillingMode = phoneNumberFillingMode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNameEditingMode() {
        return isNameEditingMode;
    }

    public void setNameEditingMode(boolean nameEditingMode) {
        isNameEditingMode = nameEditingMode;
    }

    @Override
    public String toString() {
        return "BotVisitor{" +
                "user=" + user +
                ", userId=" + userId +
                '}';
    }
}
