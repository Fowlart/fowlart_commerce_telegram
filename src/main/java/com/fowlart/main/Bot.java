package com.fowlart.main;

import com.fowlart.main.state.BotVisitor;
import com.fowlart.main.state.BotVisitors;
import com.fowlart.main.state.State;
import com.fowlart.main.state.rocks_db.RocksDBRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private RocksDBRepository rocksDBRepository;

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

    private void handleInlineButtonClick(CallbackQuery callbackQuery) {

        Long chatId = callbackQuery.getFrom().getId();
        BotVisitor visitor = this.botVisitors.getUserMap().get(chatId);
        log.info(visitor.toString());

        String callBackButton = callbackQuery.getData();

        SendMessage answer = null;

        if (Objects.equals(callBackButton, State.CATALOG.textCode)) {
            answer = SendMessage.builder().chatId(chatId).text("Тут буде каталог товарів!").replyMarkup(KeyboardHelper.buildReplyMainMenuKeyboardMenu()).build();
            visitor.setState(State.CATALOG);
        }

        if (Objects.equals(callBackButton, State.DELIVERY.textCode)) {
            answer = SendMessage.builder().chatId(chatId).text("Тут буде інфо про доставку!").replyMarkup(KeyboardHelper.buildReplyMainMenuKeyboardMenu()).build();
            visitor.setState(State.DELIVERY);
        }

        if (Objects.equals(callBackButton, State.DEBT.textCode)) {
            answer = SendMessage.builder().chatId(chatId).text("Тут буде інфо про борг!").replyMarkup(KeyboardHelper.buildReplyMainMenuKeyboardMenu()).build();
            visitor.setState(State.DEBT);
        }

        this.botVisitors.getUserMap().put(chatId, visitor);
        this.rocksDBRepository.save(String.valueOf(chatId), visitor);

        try {
            this.sendApiMethod(answer);
        } catch (TelegramApiException e) {
            log.error("Exception when sending message: ", e);
        }
    }

    private void handleInputMsg(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            String textFromUser = update.getMessage().getText();
            Long userId = update.getMessage().getChatId();
            String userFirstName = update.getMessage().getFrom().getFirstName();

            log.info("[{}, {}] : {}", userId, userFirstName, textFromUser);

            ScalaTextHelper scalaTextHelper = new ScalaTextHelper();

            SendMessage sendMessage = SendMessage.builder().chatId(userId.toString()).text(scalaTextHelper.getMainMenuText(userFirstName)).replyMarkup(KeyboardHelper.buildReplyMainMenuKeyboardMenu()).build();
            try {
                this.sendApiMethod(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Exception when sending message: ", e);
            }
        } else {
            log.warn("Unexpected update from user");
        }
    }

    private BotVisitor getVisitorFromDb(User user) {
        BotVisitor botVisitor;
        Optional<Object> userFromDb = rocksDBRepository.find(String.valueOf(user.getId()));
        if (userFromDb.isPresent()) {
            // get from RocksDb
            botVisitor = (BotVisitor) userFromDb.get();
        } else {
            //write to RocksDb
            botVisitor = new BotVisitor(user, State.MAIN_SCREEN);
            rocksDBRepository.save(String.valueOf(user.getId()), botVisitor);
        }

        //save user into session hash
        botVisitors.getUserMap().put(user.getId(), botVisitor);
        return botVisitor;
    }

    /**
     * Main method for handling input messages
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            User user = callbackQuery.getFrom();
            getVisitorFromDb(user);
            handleInlineButtonClick(callbackQuery);
        } else {
            User user = update.getMessage().getFrom();
            getVisitorFromDb(user);
            handleInputMsg(update);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }
}