package com.fowlart.main.state.cosmos;
import org.springframework.data.annotation.Id;

import java.util.Set;

public class Order  {

    @Id
    private String orderId;
    private String date;
    private String userId;
    private String userName;
    private String userPhoneNumber;
    private Set<Item> orderBucket;

    public Order() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public Set<Item> getOrderBucket() {
        return orderBucket;
    }

    public void setOrderBucket(Set<Item> orderBucket) {
        this.orderBucket = orderBucket;
    }
}
