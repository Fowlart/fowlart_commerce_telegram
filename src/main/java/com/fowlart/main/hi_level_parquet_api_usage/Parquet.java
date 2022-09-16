package com.fowlart.main.hi_level_parquet_api_usage;

import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.schema.Type;

import java.util.List;

public class Parquet {
    private List<SimpleGroup> simpleGroups;
    private List<Type> schema;

    public Parquet(List<SimpleGroup> data, List<Type> schema) {
        this.simpleGroups = data;
        this.schema = schema;
    }

    public List<SimpleGroup> getSimpleGroups() {
        return simpleGroups;
    }

    public List<Type> getSchema() {
        return schema;
    }
}