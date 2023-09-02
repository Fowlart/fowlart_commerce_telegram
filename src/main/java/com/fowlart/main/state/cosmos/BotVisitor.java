package com.fowlart.main.state.cosmos;

import com.fowlart.main.state.cosmos.Item;
import org.springframework.data.annotation.Id;
import org.telegram.telegrambots.meta.api.objects.User;


import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class BotVisitor implements Serializable {

    private String phoneNumber;
    private boolean isPhoneNumberFillingMode;

    private String name;

    private boolean isNameEditingMode;

    private com.fowlart.main.state.cosmos.Item itemToEditQty;

    private String lastVisit;

    private User user;

    @Id
    private long userId;

    private Set<com.fowlart.main.state.cosmos.Item> bucket = new HashSet<>();

    private LinkedList<String> orders;

    public BotVisitor() {}

    public BotVisitor(User user, long userId) {
        this.user = user;
        this.userId = userId;
    }

    public String getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(String lastVisit) {
        this.lastVisit = lastVisit;
    }

    public Set<com.fowlart.main.state.cosmos.Item> getBucket() {
        return bucket;
    }

    public void setBucket(Set<com.fowlart.main.state.cosmos.Item> bucket) {
        this.bucket = bucket;
    }

    public com.fowlart.main.state.cosmos.Item getItemToEditQty() {
        return itemToEditQty;
    }

    public void setItemToEditQty(com.fowlart.main.state.cosmos.Item itemToEditQty) {
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

    public LinkedList<String> getOrders() {
        return orders;
    }

    public void setOrders(LinkedList<String> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "BotVisitor{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", name='" + name + '\'' +
                ", user=" + user +
                ", userId=" + userId +
                ", bucket=" + bucket +
                ", orders=" + orders +
                '}';
    }
}
