package com.fowlart.main.state;

import java.io.Serializable;

public class Item implements Serializable {

    private String name;
    private Integer qty;

    public Item(String name, Integer qty) {
        this.name = name;
        this.qty = qty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    @Override
    public String toString() {
        return this.name +" - "+this.qty;
    }
}
