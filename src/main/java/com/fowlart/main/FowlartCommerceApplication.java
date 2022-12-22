package com.fowlart.main;


import com.fowlart.main.logging.LoggerBuilder;
import org.apache.logging.log4j.core.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@Configuration
public class FowlartCommerceApplication {

    private static Logger log;

    public static void main(String[] args) {
        System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
        log = LoggerBuilder.getFileLogger();
        SpringApplication.run(FowlartCommerceApplication.class, args);
        TelegramBotsApi telegramBotsApi;
        log.info("Registering bot...");
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(Bot.getInstance());
        } catch (TelegramApiException e) {
            log.error("Failed to register bot(check internet connection / bot token or make sure only one instance of bot is running).", e);
        }
        log.info("Telegram bot is ready to accept updates from user!");
    }

}
