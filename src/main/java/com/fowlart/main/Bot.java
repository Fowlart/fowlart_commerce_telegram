package com.fowlart.main;

import com.fowlart.main.state.BotVisitors;
import com.fowlart.main.state.ParquetWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.List;

@Component
public class Bot extends TelegramLongPollingBot implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    private static Bot instance;

    @Value("${app.bot.userName}")
    private String userName;

    @Value("${app.bot.userName.token}")
    private String token;

    @Autowired
    private BotVisitors botVisitors;

    @Autowired
    private ParquetWriter parquetWriter;

    public static Bot getInstance() {
        return instance;
    }

    @Override
    public void afterPropertiesSet() {
        instance = this;
    }

    @Override
    public String getBotUsername() {
        return userName;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onRegister() {
    }

    private void handleInlineButtonClick(Update update) {
        //Todo: add some simple handler
        log.info("callback query: " + update.getCallbackQuery());
        log.info(update.getCallbackQuery().getData());
    }

    private void handleInputMsg(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            String textFromUser = update.getMessage().getText();
            Long userId = update.getMessage().getChatId();
            String userFirstName = update.getMessage().getFrom().getFirstName();

            log.info("[{}, {}] : {}", userId, userFirstName, textFromUser);

            ScalaTextHelper scalaTextHelper = new ScalaTextHelper();

            SendMessage sendMessage = SendMessage.builder()
                    .chatId(userId.toString())
                    .text(scalaTextHelper.getMainMenuText(userFirstName))
                    .replyMarkup(KeyboardHelper.buildReplyInlineKeyboardMenu()).build();
            try {
                this.sendApiMethod(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Exception when sending message: ", e);
            }
        } else {
            log.warn("Unexpected update from user");
        }
    }


    @Override
    public void onUpdateReceived(Update update) {

        Long userId = update.getMessage().getChatId();
        String userFirstName = update.getMessage().getFrom().getFirstName();
        User user = update.getMessage().getFrom();

        // save user into session hash
        botVisitors.getUserMap().put(userId,user);

        //write to db
        try {
            parquetWriter.writeUser(userId, userFirstName, "IN_MAIN_SCREEN");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (update.hasCallbackQuery()) {
            handleInlineButtonClick(update);
        } else {
            handleInputMsg(update);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }
}