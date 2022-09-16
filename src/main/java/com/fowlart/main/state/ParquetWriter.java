package com.fowlart.main.state;

import com.fowlart.main.hi_level_parquet_api_usage.CustomParquetWriter;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.MessageTypeParser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("singleton")
public class ParquetWriter {

    public void writeUser(long id, String userName, String userStatus) throws IOException {
        List<String> userRow = userToRow(id, userName, userStatus);
        MessageType messageTypeFromFile = getSchemaForUser();
        CustomParquetWriter writer = getParquetWriter(messageTypeFromFile);
        writer.write(userRow);
        writer.close();
    }

    private CustomParquetWriter getParquetWriter(MessageType schema) throws IOException {
        String outputFilePath = "src/main/resources/users/userDB_"+System.currentTimeMillis()+".parquet";
        File outputParquetFile = new File(outputFilePath);
        Path path = new Path(outputParquetFile.toURI().toString());
        return new CustomParquetWriter(path, schema, false, CompressionCodecName.UNCOMPRESSED);
    }

    private List<String> userToRow(long id, String userName, String userStatus) {
        List<String> rowData = new ArrayList<>();
        rowData.add(String.valueOf(id));
        rowData.add(userName);
        rowData.add(userStatus);
        return rowData;
    }

    private MessageType getSchemaForUser() throws IOException {
        String schemaFilePath = "src/main/resources/schemas/user.txt";
        File resource = new File(schemaFilePath);
        String rawSchema = new String(Files.readAllBytes(resource.toPath()));
        return MessageTypeParser.parseMessageType(rawSchema);
    }
}