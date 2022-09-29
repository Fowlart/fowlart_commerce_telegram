package com.fowlart.main.in_mem_catalog;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Catalog {

    private List<Item> itemList;

    private List<String> groupList;

    public List<String> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<String> groupList) {
        this.groupList = groupList;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }
}
