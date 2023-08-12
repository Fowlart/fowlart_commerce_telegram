package com.fowlart.main;


import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

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
        TelegramBotsApi telegramBotsApi;
        logger.info("Registering bot...");
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(Bot.getInstance());
        } catch (TelegramApiException e) {
            logger.info("Failed to register bot(check internet connection / bot token or make sure only one instance of bot is running).");
        }
        logger.info("Telegram bot is ready to accept updates from user!");
    }
}
