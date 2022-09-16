package com.fowlart.main.state;

import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;

public class BotVisitor implements Serializable {

    private User user;

    private State state;
    
    private long userId;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public BotVisitor(User user, State state) {
        this.user = user;
        this.state = state;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "BotVisitor{" +
                "user_id=" + user.getId() +
                ", state=" + state +
                '}';
    }
}
