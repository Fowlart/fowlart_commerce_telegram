package com.fowlart.main;

import com.fowlart.main.catalog_fetching.ExcelFetcher;
import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.in_mem_catalog.Item;
import com.fowlart.main.state.BotVisitor;
import com.fowlart.main.state.BotVisitorService;
import com.fowlart.main.state.Buttons;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Bot extends TelegramLongPollingBot implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(Bot.class);
    private static Bot instance;
    private final BotVisitorService botVisitorService;
    private final ExcelFetcher excelFetcher;
    private final KeyboardHelper keyboardHelper;
    private String userName;
    private String token;

    private final Catalog catalog;

    public Bot(@Autowired BotVisitorService botVisitorService,
               @Autowired ExcelFetcher excelFetcher,
               @Autowired KeyboardHelper keyboardHelper,
               @Value("${app.bot.userName}")String userName,
               @Value("${app.bot.userName.token}")String token,
               @Autowired Catalog catalog) {
        this.botVisitorService = botVisitorService;
        this.excelFetcher = excelFetcher;
        this.keyboardHelper = keyboardHelper;
        this.userName = userName;
        this.token = token;
        this.catalog = catalog;
    }

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

        List<Item> catalogItems = new ArrayList<>();
        int id = 0;

        try {
            List<String> groups = excelFetcher.getProductGroupsFromSheet();
            this.catalog.setGroupList(groups);
            for (String group : groups) {
                List<String> items = excelFetcher.getGoodsFromProductGroup(group);
                for (String item : items) {
                    id++;
                    var itemName = item.split("\\|")[0];
                    var itemPrice = Double.parseDouble(item.split("\\|")[1]);
                    var itemToAdd = new Item(id,itemName,itemPrice,group);
                    catalogItems.add(itemToAdd);
                }
            }
            this.catalog.setItemList(catalogItems);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SendPhoto getGoodsContainerSendPhotoMsg(long chatId) {

        File goodsContainerImg = new File("src/main/resources/goods/goods_box.webp");

        return SendPhoto.builder().caption("⚡️" + "Каталог" + "⚡️").chatId(chatId).photo(new InputFile(goodsContainerImg)).build();
    }

    private void handleInlineButtonClicks(CallbackQuery callbackQuery) throws TelegramApiException, IOException {
        Long userId = callbackQuery.getFrom().getId();
        BotVisitor visitor = this.botVisitorService.getBotVisitorByUserId(userId.toString());
        log.info(visitor.toString());
        String callBackButton = callbackQuery.getData();
        Buttons receivedButton;

        // todo: refactor control flow
        try {
            receivedButton = Buttons.valueOf(callBackButton);
        } catch (java.lang.IllegalArgumentException exception) {
            // case of selecting catalog menu

            var subCatalogAnswer = SendMessage
                    .builder()
                    .allowSendingWithoutReply(true)

                    .text("Обирай товар! Введи ID товару для замовлення! " + "\n" + catalog.
                            getItemList()
                            .stream()
                            .filter(item -> item.group().equals(callBackButton))
                            .map(item -> item.id() +"\uD83D\uDCCC "+item.name()+" \uD83D\uDCB0"+item.price())
                            .reduce((a, b) -> a+"\n"+b)
                            .orElse("немає товару у группі"))

                    .chatId(userId)
                    .replyMarkup(this.keyboardHelper.buildMainMenuReply())
                    .build();

            this.botVisitorService.saveBotVisitor(visitor);
            this.sendApiMethod(subCatalogAnswer);
            return;
        }

        ScalaTextHelper scalaTextHelper = new ScalaTextHelper();
        String name = callbackQuery.getFrom().getFirstName();

        var answer = switch (receivedButton) {
            case CATALOG -> {
                visitor.setState(Buttons.CATALOG);
                try {
                    execute(getGoodsContainerSendPhotoMsg(userId));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                yield SendMessage.builder().allowSendingWithoutReply(true).text("Обирай з груп:").chatId(userId).replyMarkup(this.keyboardHelper.buildCatalogItemsMenu()).build();
            }
            case BUCKET -> {
                visitor = this.botVisitorService.getOrCreateVisitor(visitor.getUser());
                visitor.setState(Buttons.BUCKET);
                List<String> itemList = visitor.getBucket().stream().map(Item::toString).toList();
                yield SendMessage.builder().chatId(userId).text("ЗАМОВЛЕНІ ТОВАРИ: \n" + String.join("\n", itemList)).replyMarkup(keyboardHelper.buildBucketKeyboardMenu()).build();
            }
            case DEBT -> {
                visitor.setState(Buttons.DEBT);
                yield SendMessage.builder().chatId(userId).text("Тут буде інфо про борг!").replyMarkup(keyboardHelper.buildMainMenuReply()).build();
            }
            case MAIN_SCREEN -> {
                visitor.setState(Buttons.MAIN_SCREEN);
                yield SendMessage.builder().chatId(userId).text(scalaTextHelper.getMainMenuText(name)).replyMarkup(keyboardHelper.buildMainMenuReply()).build();
            }
            case SUBMIT -> {
                visitor.setState(Buttons.MAIN_SCREEN);
                visitor.getBucket().clear();
                this.botVisitorService.saveBotVisitor(visitor);
                yield SendMessage.builder().chatId(userId).text("Замовлення прийнято!").replyMarkup(keyboardHelper.buildMainMenuReply()).build();
            }
            case DISCARD -> {
                visitor.setState(Buttons.MAIN_SCREEN);
                visitor.getBucket().clear();
                this.botVisitorService.saveBotVisitor(visitor);
                yield SendMessage.builder().chatId(userId).text(scalaTextHelper.getMainMenuText(name)).replyMarkup(keyboardHelper.buildMainMenuReply()).build();
            }
        };

        this.botVisitorService.saveBotVisitor(visitor);
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
            String userId = update.getMessage().getFrom().getId().toString();

            // todo: case replying to the message
            if (Objects.nonNull(replyMessage)) {
                // add item to the bucket
                BotVisitor botVisitor = this.botVisitorService.getBotVisitorByUserId(userId);
                this.botVisitorService.saveBotVisitor(botVisitor);
            }

            Long chatId = update.getMessage().getChatId();
            String userFirstName = update.getMessage().getFrom().getFirstName();

            log.info("[chatID:{}, userFirstName:{}] : {}", chatId, userFirstName, textFromUser);

            ScalaTextHelper scalaTextHelper = new ScalaTextHelper();

            SendMessage sendMessage = SendMessage.builder().chatId(chatId.toString()).text(scalaTextHelper.getMainMenuText(userFirstName)).replyMarkup(keyboardHelper.buildMainMenuReply()).build();

            try {
                this.sendApiMethod(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Exception when sending message: ", e);
            }
        } else {
            log.warn("Unexpected update from user");
        }
    }


    /**
     * Main method for handling input messages
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            User user = callbackQuery.getFrom();
            this.botVisitorService.getOrCreateVisitor(user);
            try {
                handleInlineButtonClicks(callbackQuery);
            } catch (TelegramApiException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            User user = update.getMessage().getFrom();
            this.botVisitorService.getOrCreateVisitor(user);
            handleInputMsg(update);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }
}