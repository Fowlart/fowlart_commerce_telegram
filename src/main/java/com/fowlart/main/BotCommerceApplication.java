package com.fowlart.main;


import com.fowlart.main.logging.LoggerBuilder;
import com.fowlart.main.logging.LoggerHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

    @SpringBootApplication
@Configuration
public class BotCommerceApplication {

    public static void main(String[] args) {
        // todo: refactor to switch loggers according to the chosen profile
        var activeProfile = System.getProperty("spring.profiles.active");
        System.out.println("ACTIVE PROFILE: "+activeProfile);
        // this line somehow switch-on all logs
        LoggerBuilder.getLogger();
        System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
        SpringApplication.run(BotCommerceApplication.class, args);
        TelegramBotsApi telegramBotsApi;
        LoggerHelper.logInfoInFile("Registering bot...");
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(Bot.getInstance());
        } catch (TelegramApiException e) {
            LoggerHelper.logErrorInFile("Failed to register bot(check internet connection / bot token or make sure only one instance of bot is running).");
        }
        LoggerHelper.logInfoInFile("Telegram bot is ready to accept updates from user!");
    }
}
