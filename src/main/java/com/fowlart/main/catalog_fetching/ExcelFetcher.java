package com.fowlart.main.catalog_fetching;

import com.fowlart.main.state.inmem.Item;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ExcelFetcher {

    public static final String DEFAULT_CATALOG_PATH = "src/main/resources/catalog/catalog.xls";
    public ExcelFetcher(@Value("${app.bot.catalog.path}") String pathToCatalog) {
        if (pathToCatalog.isEmpty()) {
            this.PATH_TO_CATALOG = DEFAULT_CATALOG_PATH;
        }
        else {
            this.PATH_TO_CATALOG = pathToCatalog;
        }
    }

    private static final String START_PARSING_PHRASE = "Продукция";
    private String PATH_TO_CATALOG;

    private Sheet getSheet() throws IOException {
        FileInputStream file = new FileInputStream(PATH_TO_CATALOG);
        Workbook workbook = new HSSFWorkbook(file);
        return workbook.getSheetAt(0);
    }

    public List<Item> getCatalogItems() {
        List<Item> catalogItems = new ArrayList<>();
        int id = 0;

        try {
            List<String> groups = getProductGroupsFromPrice();
            for (String group : groups) {
                List<String> items = getUnparsedGoodsFromProductGroup(group);
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

    private Integer rowNumberOfGroup(String productGroup) throws IOException {
        Sheet sheet = getSheet();
        for (Row row : sheet) {
            for (Cell cell : row) {
                var cellType = cell.getCellType();
                if (cellType.equals(CellType.STRING) && productGroup.trim().equals(cell.getRichStringCellValue().getString().trim())) {
                    return cell.getRow().getRowNum();
                }
            }
        }
        return 0;
    }


    public List<String> getUnparsedGoodsFromProductGroup(String productGroup) throws IOException {

        List<String> productGroupsFromPrice = this.getProductGroupsFromPrice();
        String trimmedPG = productGroup.trim();
        List<String> items = new ArrayList<>();

        if (!productGroupsFromPrice.contains(trimmedPG)) {
            return items;
        }

        Sheet sheet = getSheet();

        int rowNumberFromWhereWeShouldCollectItems = rowNumberOfGroup(trimmedPG);

        int indexOfCurrentGroupItem = productGroupsFromPrice.indexOf(trimmedPG);
        String nextGroup;
        int nextGroupIndex = indexOfCurrentGroupItem + 1;
        if (nextGroupIndex <= productGroupsFromPrice.size() - 1) {
            nextGroup = productGroupsFromPrice.get(nextGroupIndex);
        } else {
            nextGroup = "EOF";
        }

        int rowNumberWhereWeShouldStop = sheet.getLastRowNum()+1;

        if (!"EOF".equals(nextGroup)) {
            rowNumberWhereWeShouldStop = rowNumberOfGroup(nextGroup);
        }

        if (rowNumberWhereWeShouldStop>=rowNumberFromWhereWeShouldCollectItems) {
           items = IntStream.range(rowNumberFromWhereWeShouldCollectItems+1,rowNumberWhereWeShouldStop)
                    .mapToObj(sheet::getRow)
                    .map(row->row.getCell(0)
                            .getRichStringCellValue()
                            .getString().trim()+"|"+row.getCell(1).getNumericCellValue())
                    .collect(Collectors.toList());
        }

        return items;
    }

    public List<String> getProductGroupsFromPrice() {
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
                        if (resetIndex == 3 && Objects.nonNull(mayBeGroup)) {
                            groups.add(mayBeGroup.trim());
                            resetIndex = 0;
                        }
                    }
                    default -> {}
                }
            }
        }
        groups.remove(null);
        groups.remove(START_PARSING_PHRASE);
        return groups;
    }
}

