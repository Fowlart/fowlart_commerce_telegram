package com.fowlart.main.in_mem_catalog;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public final class Item implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;
    private final String id;
    private final String name;
    private final Double price;
    private final String group;
    private final Integer qty;

    public Item(String id, String name, Double price, String group, Integer qty) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.group = group;
        this.qty = qty;
    }

    @Override
    public String toString() {
        var str = id + "|" + name + "|" + price + "₴";
        if (Objects.nonNull(qty)) str = str + "|" + qty;
        return str;
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
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.price, that.price) &&
                Objects.equals(this.group, that.group) &&
                Objects.equals(this.qty, that.qty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, group, qty);
    }

}