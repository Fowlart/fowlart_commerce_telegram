package com.fowlart.main;

import com.fowlart.main.catalog_fetching.ExcelFetcher;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.io.IOException;

class ExcelFetcherTest {

    public static final String DEFAULT_CATALOG_PATH = "src/main/resources/catalog/catalog.xls";

    @Test
    void testExcelFetcher() throws IOException {
        ExcelFetcher excelFetcher = new ExcelFetcher(DEFAULT_CATALOG_PATH);

        var result_1 = excelFetcher.getUnparsedGoodsFromProductGroup("Шампуні");
        Assert.isTrue(result_1.get(0).equals("Шампунь Teo 350мл|44.0"),"not matched to expected results");
        Assert.isTrue(result_1.get(result_1.size() - 1).equals("Шкарпетки чол. тонкі|13.75"),"not matched to expected results");

        var result_2 = excelFetcher.getUnparsedGoodsFromProductGroup("Анед");
        Assert.isTrue(result_2.get(result_2.size() - 1).equals("Ас відплямлювач для кольору 200г|21.5"),"not matched to expected results");
    }
}
