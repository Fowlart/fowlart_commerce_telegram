package com.fowlart.main.hi_level_api_usage;

import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.MessageTypeParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ParquetWriteExample {

    public static void main(String[] args) throws IOException {

        List<List<String>> rows = getDataForFile();
        MessageType messageTypeFromFile = getSchemaForParquetFile();
        System.out.println(messageTypeFromFile);
        CustomParquetWriter writer = getParquetWriter(messageTypeFromFile);
        
        rows.forEach(data->{
            try {
                writer.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        writer.close();
    }

    private static CustomParquetWriter getParquetWriter(MessageType schema) throws IOException {
        String outputFilePath = "/Users/artur/IdeaProjects/bloom_filter/src/main/resources/peoples"+ "/" + System.currentTimeMillis() + ".parquet";
        File outputParquetFile = new File(outputFilePath);
        Path path = new Path(outputParquetFile.toURI().toString());
        return new CustomParquetWriter(path, schema, false, CompressionCodecName.UNCOMPRESSED);
    }

    private static List<List<String>> getDataForFile() {
        List<List<String>> data = new ArrayList<>();

        List<String> row1 = new ArrayList<>();
        row1.add("1");
        row1.add("Artur");
        row1.add("3500");

        List<String> row2 = new ArrayList<>();
        row2.add("2");
        row2.add("Olena");
        row2.add("1500");

        data.add(row1);
        data.add(row2);
        return data;
    }

    private static MessageType getSchemaForParquetFile() throws IOException {
        String schemaFilePath = "/Users/artur/IdeaProjects/bloom_filter/src/main/resources/schema/people_schema.txt";
        File resource = new File(schemaFilePath);
        String rawSchema = new String(Files.readAllBytes(resource.toPath()));
        return MessageTypeParser.parseMessageType(rawSchema);
    }
}