package com.fowlart.main;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class FowlartCommerceApplication {

    private static final Logger log = LoggerFactory.getLogger(FowlartCommerceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FowlartCommerceApplication.class, args);
        TelegramBotsApi telegramBotsApi;
        log.info("Registering bot...");
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(SimpleEchoBot.getInstance());
        } catch (TelegramApiException e) {
            log.error("Failed to register bot(check internet connection / bot token or make sure only one instance of bot is running).", e);
        }
        log.info("Telegram bot is ready to accept updates from user!");
    }
}
