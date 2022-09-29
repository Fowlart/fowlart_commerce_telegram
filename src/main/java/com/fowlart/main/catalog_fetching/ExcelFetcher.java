package com.fowlart.main.catalog_fetching;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ExcelFetcher {

    private static final Logger log = LoggerFactory.getLogger(ExcelFetcher.class);

    private static final String START_PARSING_PHRASE = "Продукция";
    private static final String PATH_TO_CATALOG = "src/main/resources/catalog/catalog.xls";

    public List<String> getGoodsFromProductGroup(String productGroup) throws IOException {

        FileInputStream file = new FileInputStream(PATH_TO_CATALOG);
        Workbook workbook = new HSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        List<String> items = new ArrayList<>();
        List<String> groupItems = this.getProductGroupsFromSheet();
        int indexOfCurrentGroupItem = groupItems.indexOf(productGroup);
        //todo: indexOutOfBound
        String nextGroup = groupItems.get(indexOfCurrentGroupItem + 1);
        boolean shouldCollect = false;
        List<String> itemWithPrice = new ArrayList<>();

        for (Row row : sheet) {
            for (Cell cell : row) {
                // do nothing
                if (cell.getCellType() == CellType.STRING) {

                    String currentCellVal = cell.getRichStringCellValue().toString().trim();

                    if (shouldCollect) {
                        if (itemWithPrice.size() == 1) {
                            items.add(itemWithPrice.stream().reduce((itemName, price) -> itemName + price).get());
                            itemWithPrice = new ArrayList<>();
                        } else {
                            if (!currentCellVal.equals("шт") && !currentCellVal.equals("уп.")) itemWithPrice.add(currentCellVal);
                        }
                    }

                    if (currentCellVal.equals(productGroup)) {
                        shouldCollect = true;
                    }

                    if (currentCellVal.equals(nextGroup)) {
                        System.out.println(Arrays.toString(items.toArray()));
                        return items;
                    }
                }
            }
        }

        items.remove(null);
        items.remove(START_PARSING_PHRASE);
        System.out.println(Arrays.toString(items.toArray()));
        return items;
    }

    public List<String> getProductGroupsFromSheet() throws IOException {

        FileInputStream file = new FileInputStream(PATH_TO_CATALOG);
        Workbook workbook = new HSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        List<String> groups = new ArrayList<>();

        for (Row row : sheet) {
            String mayBeGroup = null;
            int resetIndex = 0;
            for (Cell cell : row) {
                switch (cell.getCellType()) {
                    case STRING -> {
                        mayBeGroup = cell.getRichStringCellValue().toString().trim();
                    }
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
        System.out.println(Arrays.toString(groups.toArray()));
        return groups;
    }
}

