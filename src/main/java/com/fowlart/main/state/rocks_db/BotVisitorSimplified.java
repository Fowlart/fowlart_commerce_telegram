package com.fowlart.main.state.rocks_db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fowlart.main.state.cosmos.Item;
import org.hibernate.annotations.ColumnTransformer;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Set;

@Entity(name = "bot_visitor")
public class BotVisitorSimplified {
    private String phoneNumber;
    private String name;

    @Id
    private long userId;

    @Column(columnDefinition = "json")
    @ColumnTransformer(write = "?::json")
    private String userJSON;
    private String bucket;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(Set<Item> bucket) {
        this.bucket = bucket.stream().map(Item::toString).reduce("", (a, b) -> a + b);
    }

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

