package com.fowlart.main.state;

import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.util.ArrayList;

public class BotVisitor implements Serializable {

    private User user;

    private Buttons buttons;
    
    private long userId;

    private ArrayList<Item> bucket = new ArrayList<>();

    public ArrayList<Item> getBucket() {
        return bucket;
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
