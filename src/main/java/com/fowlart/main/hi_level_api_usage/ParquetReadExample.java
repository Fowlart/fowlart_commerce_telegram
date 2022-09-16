package com.fowlart.main.hi_level_api_usage;

import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.Type;

import java.io.IOException;
import java.util.List;


public class ParquetReadExample {

    public static void main(String[] args) throws IOException {

        Parquet parquet = ParquetReaderUtils
                .getParquetData("src/main/resources/db_example" +
                        "/part-00000-0ed92653-38ca-42d2-99d1-ad74b73d164d-c000.snappy.parquet");

        // 1 - will return basically a container of values(rows).
        // Values should be retrieved by name,index position and type.
        parquet.getSimpleGroups().get(0);
        String DT = parquet.getSimpleGroups().get(0).getString("TRANSACTION_TIME", 0);
        System.out.println("DT column in the very first row: " + DT);

        // 2 - 'Group type' is basically the schema of types for row.
        // It is strange that the schema is added individually to each row.
        GroupType groupType = parquet.getSimpleGroups().get(0).getType();
        System.out.println("Spark schema for the very first row:");
        System.out.println(groupType);

        List<Type> typesForAllParquet = parquet.getSchema();
        System.out.println("Spark schema for the document:");
        typesForAllParquet.forEach(type -> {
            System.out.println("type.getName :" + type.getName());
            System.out.println("type.asPrimitiveType: "+type.asPrimitiveType());
            System.out.println("type.getOriginalType: "+type.getOriginalType());
            System.out.println("type.getRepetition: "+type.getRepetition());
            System.out.println("+++++");
        });

    }
}
