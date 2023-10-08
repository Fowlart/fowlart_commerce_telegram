package com.fowlart.main;


import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;


@SpringBootApplication
@Configuration
public class BotCommerceApplication {

    private final static Logger logger = LoggerFactory.getLogger(BotCommerceApplication.class);

    public static void main(String[] args) {
        // add this line to your code, it will start app insights
        ApplicationInsights.attach();
        var activeProfile = System.getProperty("spring.profiles.active");
        System.out.println("ACTIVE PROFILE: " + activeProfile);
        SpringApplication.run(BotCommerceApplication.class, args);
    }
}
