package com.fowlart.main.in_mem_catalog;

import java.io.Serializable;

public record Item (String id, String name, Double price, String group) implements Serializable {

    @Override
    public String toString() {
        return id+"|"+name+"|"+price;
    }
}
