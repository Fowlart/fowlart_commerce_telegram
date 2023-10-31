package com.fowlart.main;


import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.Properties;


@SpringBootApplication
@Configuration
public class CommerceApplication {

    public static void main(String[] args) {
        // add this line to your code, it will start app insights
        ApplicationInsights.attach();
        var activeProfile = System.getProperty("spring.profiles.active");

        //secret properties
        Properties properties = new Properties();
        String azureStorageConnectionString = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        String emailPassword = System.getenv("EMAIL_PASSWORD");
        String cosmosKey = System.getenv("COSMOS_KEY");
        String serviceBusCS = System.getenv("SERVICE_BUS_CONNECTION_STRING");
        String serviceBusSecret = System.getenv("SERVICE_BUS_SECRET");


        // if all properties are set, then we are in production
        if (Objects.nonNull(azureStorageConnectionString) &&
                Objects.nonNull(emailPassword) &&
                Objects.nonNull(cosmosKey) &&
                Objects.nonNull(serviceBusCS) &&
                Objects.nonNull(serviceBusSecret)) {
            properties.put("azure.storage.connection.string", azureStorageConnectionString);
            properties.put("spring.profiles.active", activeProfile);
            properties.put("app.bot.email.gmail.password", emailPassword);
            properties.put("spring.cloud.azure.cosmos.key", cosmosKey);
            properties.put("servicebus.connection-string", serviceBusCS);
            properties.put("app.bot.admin.secret", System.getenv("SERVICE_BUS_SECRET"));

            System.out.println("ACTIVE PROFILE: " + activeProfile);
            System.out.println("AZURE_STORAGE_CONNECTION_STRING: " + azureStorageConnectionString);
            System.out.println("EMAIL_PASSWORD: " + emailPassword);
            System.out.println("COSMOS_KEY: " + cosmosKey);
            System.out.println("SERVICE_BUS_CONNECTION_STRING: " + serviceBusCS);
            System.out.println("SERVICE_BUS_SECRET: " + serviceBusSecret);
        } else {
           System.out.println("Development mode");
        }
        SpringApplication application = new SpringApplication(CommerceApplication.class);
        application.setDefaultProperties(properties);
        application.run(args);

    }
}
