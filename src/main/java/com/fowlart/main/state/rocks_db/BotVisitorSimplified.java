package com.fowlart.main.state.rocks_db;

import com.azure.json.implementation.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Type;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.IOException;

@Entity(name = "bot_visitor")
public class BotVisitorSimplified {
    private String phoneNumber;
    private String name;
    @Id
    private long userId;

    @Column(columnDefinition = "json")
    @ColumnTransformer(write = "?::json")
    private String userJSON;

    // Getters and setters

    public BotVisitorSimplified() {}

    public String getUserJSON() {
        return userJSON;
    }

    public void setUserJSON(User user) {
        try {
            userJSON = new ObjectMapper().writeValueAsString(user);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Error serializing User to JSON: " + e.getMessage(), e);
        }
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}

