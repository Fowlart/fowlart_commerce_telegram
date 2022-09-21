package com.fowlart.main.catalog_fetching;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
                        if (resetIndex==3) {
                            groups.add(mayBeGroup);
                            resetIndex=0;
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

