package com.fowlart.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.LinkedList;
import java.util.List;

@Component
public class SimpleEchoBot extends TelegramLongPollingBot implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(SimpleEchoBot.class);

    private static SimpleEchoBot instance;
    private final LinkedList<String> menuItems = new LinkedList<>();
    @Value("${app.bot.userName}")
    private String userName;
    @Value("${app.bot.userName.token}")
    private String token;

    public static SimpleEchoBot getInstance() {
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

    private String addMenuItem(String item) {
        if (this.menuItems.isEmpty()) {
            menuItems.add("\n" + "1/[" + item + "]");
        } else {
            int lastElementIndex = Integer.parseInt(menuItems.getLast().replaceAll("\n", "").split("/")[0]);
            ++lastElementIndex;
            menuItems.add("\n" + lastElementIndex + "/[" + item + "]");
        }
        return menuItems.stream().reduce("",(s1, s2) -> s1+s2);
    }

    private void handleInlineButtons(Update update) {
        //Todo: add some simple handler
        log.info("callback query: " + update.getCallbackQuery());
        log.info(update.getCallbackQuery().getData());
    }

    private void handleInputMsgOrReplayKeyBoardMsg(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            String textFromUser = update.getMessage().getText();

            Long userId = update.getMessage().getChatId();
            String userFirstName = update.getMessage().getFrom().getFirstName();

            log.info("[{}, {}] : {}", userId, userFirstName, textFromUser);

            addMenuItem("замовлення");
            addMenuItem("доставка");

            SendMessage sendMessage = SendMessage.builder().chatId(userId.toString()).text("Привіт, " + userFirstName + "! "
                            + "Вибери щось з наступних варіантів:" +addMenuItem("борг"))
                    // add replay keyboard
                    .replyMarkup(KeyboardHelper.buildMainMenu())
                    // add inline keyboard
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

        if (update.hasCallbackQuery()) {
            handleInlineButtons(update);
        } else {
            handleInputMsgOrReplayKeyBoardMsg(update);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }
}