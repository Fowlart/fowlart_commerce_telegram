package com.fowlart.main.hi_level_parquet_api_usage;

import java.io.IOException;

public class TryReadBinaryParquet {

    public static void main(String[] args) throws IOException {

        Parquet parquet = ParquetReaderUtils
                .getParquetData("src/main/resources/peoples/1662711081250.parquet");

        parquet.getSchema().forEach(System.out::println);

        String userName1 = parquet.getSimpleGroups().get(0).getString("username",0);
        String userName2 = parquet.getSimpleGroups().get(1).getString("username",0);

        System.out.println(userName1);
        System.out.println(userName2);
    }
}
