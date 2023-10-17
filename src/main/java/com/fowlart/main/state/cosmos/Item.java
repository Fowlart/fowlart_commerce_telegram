package com.fowlart.main.state.cosmos;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Objects;

public final class Item implements Serializable {

    @Id
    private  String id;
    private  String name;
    private Double price;
    private String group;
    private  Integer qty;

    public Item(){}
    public Item(String id, String name, Double price, String group, Integer qty) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.group = group;
        this.qty = qty;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }
    public Double price() {
        return price;
    }

    public String group() {
        return group;
    }

    public Integer qty() {
        return qty;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Item) obj;
        return  Objects.equals(this.id, that.id) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.price, that.price) &&
                Objects.equals(this.group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, group);
    }
}
