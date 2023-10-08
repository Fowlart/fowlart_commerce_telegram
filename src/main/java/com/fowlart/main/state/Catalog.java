package com.fowlart.main.state;

import com.fowlart.main.catalog_fetching.ExcelFetcher;
import com.fowlart.main.state.cosmos.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Catalog {

    public Catalog(@Autowired ExcelFetcher excelFetcher) {
        this.itemList = excelFetcher.getCatalogItems();
        this.groupList = excelFetcher.getProductGroupsFromPrice();
    }

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
