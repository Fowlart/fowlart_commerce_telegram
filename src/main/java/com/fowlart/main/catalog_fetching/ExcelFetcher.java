package com.fowlart.main.catalog_fetching;

import com.fowlart.main.in_mem_catalog.Item;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelFetcher {

    private static final Logger log = LoggerFactory.getLogger(ExcelFetcher.class);

    private static final String START_PARSING_PHRASE = "Продукция";
    private static final String PATH_TO_CATALOG = "src/main/resources/catalog/catalog.xls";

    private Sheet getSheet() throws IOException {
        FileInputStream file = new FileInputStream(PATH_TO_CATALOG);
        Workbook workbook = new HSSFWorkbook(file);
        return workbook.getSheetAt(0);
    }

    public List<Item> getCatalogItems() {
        List<Item> catalogItems = new ArrayList<>();
        int id = 0;

        try {
            List<String> groups = getProductGroupsFromSheet();
            for (String group : groups) {
                List<String> items = getGoodsFromProductGroup(group);
                for (String item : items) {
                    id++;
                    var itemName = item.split("\\|")[0];
                    var itemPrice = Double.parseDouble(item.split("\\|")[1]);
                    var itemToAdd = new Item("ID" + id, itemName, itemPrice, group, null);
                    catalogItems.add(itemToAdd);
                }
            }

            return catalogItems;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getGoodsFromProductGroup(String productGroup) throws IOException {

        Sheet sheet = getSheet();
        List<String> items = new ArrayList<>();
        List<String> groupItems = this.getProductGroupsFromSheet();
        int indexOfCurrentGroupItem = groupItems.indexOf(productGroup);

        String nextGroup;
        int nextElementIndex = indexOfCurrentGroupItem + 1;
        if (nextElementIndex <= groupItems.size() - 1) {
            nextGroup = groupItems.get(nextElementIndex);
        } else {
            nextGroup = "EOF";
        }
        boolean shouldCollect = false;
        List<String> itemWithPrice = new ArrayList<>();
        int rowCount = 0;

        for (Row row : sheet) {
            for (Cell cell : row) {
                rowCount++;
                // do nothing
                if (cell.getCellType() == CellType.STRING || cell.getCellType() == CellType.NUMERIC) {

                    String currentCellVal = "";
                    double price = 0d;

                    if (cell.getCellType() == CellType.STRING) {
                        currentCellVal = cell.getRichStringCellValue().toString().trim();
                    } else {
                        price = cell.getNumericCellValue();
                    }

                    if (shouldCollect) {
                        if (itemWithPrice.size() == 2) {
                            items.add(itemWithPrice.stream().reduce((itemName, p) -> itemName + "|" + p).orElse(""));
                            itemWithPrice = new ArrayList<>();
                        } else {
                            if (!currentCellVal.equals("шт") && !currentCellVal.equals("уп.") && !currentCellVal.equals(""))
                                itemWithPrice.add(currentCellVal);
                            if (price != 0d) itemWithPrice.add(Double.toString(price));
                        }
                    }

                    if (currentCellVal.equals(productGroup)) {
                        shouldCollect = true;
                    }

                    if (currentCellVal.equals(nextGroup) || rowCount == sheet.getLastRowNum()) {
                        return items;
                    }
                }
            }
        }

        items.remove(null);
        items.remove(START_PARSING_PHRASE);
        return items;
    }

    public List<String> getProductGroupsFromSheet() {
        Sheet sheet;
        try {
            sheet = getSheet();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> groups = new ArrayList<>();

        for (Row row : sheet) {
            String mayBeGroup = null;
            int resetIndex = 0;
            for (Cell cell : row) {
                switch (cell.getCellType()) {

                    case STRING -> mayBeGroup = cell.getRichStringCellValue().toString().trim();

                    case BLANK -> {
                        resetIndex++;
                        if (resetIndex == 3) {
                            groups.add(mayBeGroup);
                            resetIndex = 0;
                        }
                    }
                    default -> {
                    }
                }
            }
        }
        groups.remove(null);
        groups.remove(START_PARSING_PHRASE);
        return groups;
    }
}

