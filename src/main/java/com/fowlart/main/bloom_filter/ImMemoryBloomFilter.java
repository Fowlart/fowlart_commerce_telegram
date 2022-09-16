package com.fowlart.main.bloom_filter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.fowlart.main.hi_level_api_usage.Parquet;
import com.fowlart.main.hi_level_api_usage.ParquetReaderUtils;
import org.apache.parquet.example.data.simple.SimpleGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImMemoryBloomFilter {

    private final static int expectedInsertions = 10000;

    private static final List<BloomFilter<Integer>> inMemoryBloomFilters = new ArrayList<>();

    private static BloomFilter<Integer> createIntegerBloomFilter() {

        return BloomFilter.create(Funnels.integerFunnel(), expectedInsertions);
    }

    public static void main(String[] args) throws IOException {

        Parquet parquet = ParquetReaderUtils
                .getParquetData("/Users/artur/IdeaProjects/bloom_filter" +
                        "/src/main/resources/db_example" +
                        "/part-00000-0ed92653-38ca-42d2-99d1-ad74b73d164d-c000.snappy.parquet");

        int bloomFilterIndex = 0;

        BloomFilter<Integer> skuNumberInMemoryBloomFilter = BloomFilter.create(Funnels.integerFunnel(), expectedInsertions);

        for (SimpleGroup simpleGroup : parquet.getSimpleGroups()) {
            int skuNumber = simpleGroup.getInteger("SKU_NUMBER", 0);
            if (bloomFilterIndex <= expectedInsertions) {
                skuNumberInMemoryBloomFilter.put(skuNumber);
            } else {
                inMemoryBloomFilters.add(skuNumberInMemoryBloomFilter);
                skuNumberInMemoryBloomFilter = createIntegerBloomFilter();
                bloomFilterIndex=0;
            }
            bloomFilterIndex++;
        }

        int skuNumberForSearch = 2290690;

        inMemoryBloomFilters.forEach(integerBloomFilter -> {
            System.out.println(integerBloomFilter.mightContain(skuNumberForSearch));
        });
    }
}
