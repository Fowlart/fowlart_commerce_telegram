package com.fowlart.main;

import com.fowlart.main.state.BotVisitor;
import com.fowlart.main.state.BotVisitors;
import com.fowlart.main.state.Item;
import com.fowlart.main.state.Buttons;
import com.fowlart.main.state.rocks_db.RocksDBRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Autowired
    private KeyboardHelper keyboardHelper;

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

    private List<SendPhoto> getGoodsFromContentFolderAndFormMessage(long chatId) {
        File contentFolder = new File("src/main/resources/goods/");

        return Arrays.stream(contentFolder.listFiles()).map(file -> SendPhoto.builder().caption(file.getName().split("\\.")[0]).chatId(chatId).photo(new InputFile(file)).build()).collect(Collectors.toList());
    }

    private void handleInlineButtonClick(CallbackQuery callbackQuery) throws TelegramApiException {
        Long chatId = callbackQuery.getFrom().getId();
        BotVisitor visitor = this.botVisitors.getUserMap().get(chatId);
        log.info(visitor.toString());
        String callBackButton = callbackQuery.getData();
        Buttons receivedButtons = Buttons.valueOf(callBackButton);
        ScalaTextHelper scalaTextHelper = new ScalaTextHelper();
        String name = callbackQuery.getFrom().getFirstName();

        SendMessage answer = null;

        answer = switch (receivedButtons) {
            case CATALOG -> {
                visitor.setState(Buttons.CATALOG);

                getGoodsFromContentFolderAndFormMessage(chatId).forEach(msg -> {
                    try {
                        execute(msg);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                });

                yield SendMessage.builder().chatId(chatId).text("Дивись вище список доступних товарів! \nВідповідай на повідомлення з товаром вказуючи кількість.").build();
            }
            case BUCKET -> {
                visitor = this.getVisitorFromDb(visitor.getUser());
                visitor.setState(Buttons.BUCKET);
                List<String> itemList = visitor.getBucket().stream().map(Item::toString).toList();
                yield SendMessage.builder().chatId(chatId).text("ЗАМОВЛЕНІ ТОВАРИ: \n" + String.join("\n", itemList)).replyMarkup(keyboardHelper.buildBucketKeyboardMenu()).build();
            }
            case DEBT -> {
                visitor.setState(Buttons.DEBT);
                yield SendMessage.builder().chatId(chatId).text("Тут буде інфо про борг!").replyMarkup(keyboardHelper.buildReplyMainMenuKeyboardMenu()).build();
            }
            // Todo
            case MAIN_SCREEN -> {
                visitor.setState(Buttons.MAIN_SCREEN);
                yield SendMessage.builder().chatId(chatId).text(scalaTextHelper.getMainMenuText(name)).replyMarkup(keyboardHelper.buildReplyMainMenuKeyboardMenu()).build();
            }

            case SUBMIT -> {
                visitor.setState(Buttons.MAIN_SCREEN);
                visitor.getBucket().clear();
                this.rocksDBRepository.save(visitor.getUserId(),visitor);
                yield SendMessage.builder().chatId(chatId).text(scalaTextHelper.getMainMenuText(name)).replyMarkup(keyboardHelper.buildReplyMainMenuKeyboardMenu()).build();
            }

            case DISCARD -> {
                visitor.setState(Buttons.MAIN_SCREEN);
                visitor.getBucket().clear();
                this.rocksDBRepository.save(visitor.getUserId(),visitor);
                yield SendMessage.builder().chatId(chatId).text(scalaTextHelper.getMainMenuText(name)).replyMarkup(keyboardHelper.buildReplyMainMenuKeyboardMenu()).build();
            }
        };

        this.botVisitors.getUserMap().put(chatId, visitor);
        this.rocksDBRepository.save(String.valueOf(chatId), visitor);
        this.sendApiMethod(answer);
    }

    private int findFirstInteger(String stringToSearch) {
        Pattern integerPattern = Pattern.compile("-?\\d+");
        Matcher matcher = integerPattern.matcher(stringToSearch);

        List<String> integerList = new ArrayList<>();
        while (matcher.find()) {
            integerList.add(matcher.group());
        }

        if (integerList.isEmpty()) {
            return 0;
        }

        return Integer.parseInt(integerList.get(0));
    }

    private void handleInputMsg(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            String textFromUser = update.getMessage().getText();
            Message replyMessage = update.getMessage().getReplyToMessage();
            Integer qty = findFirstInteger(textFromUser);

            if (Objects.nonNull(replyMessage)) {
                // add item to the bucket
                BotVisitor botVisitor = (BotVisitor) this.rocksDBRepository.find(update.getMessage().getFrom().getId().toString()).get();
                botVisitor.getBucket().add(new Item(replyMessage.getCaption(), qty));
                this.rocksDBRepository.save(botVisitor.getUserId(), botVisitor);
                //todo: return into same place in catalog
            }

            Long userId = update.getMessage().getChatId();
            String userFirstName = update.getMessage().getFrom().getFirstName();

            log.info("[{}, {}] : {}", userId, userFirstName, textFromUser);

            ScalaTextHelper scalaTextHelper = new ScalaTextHelper();

            SendMessage sendMessage = SendMessage.builder().chatId(userId.toString()).text(scalaTextHelper.getMainMenuText(userFirstName)).replyMarkup(keyboardHelper.buildReplyMainMenuKeyboardMenu()).build();

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
            botVisitor = new BotVisitor(user, Buttons.MAIN_SCREEN, user.getId());
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
            try {
                handleInlineButtonClick(callbackQuery);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
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