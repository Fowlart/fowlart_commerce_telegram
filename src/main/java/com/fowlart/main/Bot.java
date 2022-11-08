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
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class Bot extends TelegramLongPollingBot implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(Bot.class);
    private static final String GOOD_ADD_COMMAND = "/ID";
    private static final String REMOVE_COMMAND = "/remove";
    private static Bot instance;
    private final BotVisitorService botVisitorService;
    private final ExcelFetcher excelFetcher;
    private final KeyboardHelper keyboardHelper;
    private final Catalog catalog;
    private final String userName;
    private final String token;

    public Bot(@Autowired BotVisitorService botVisitorService, @Autowired ExcelFetcher excelFetcher, @Autowired KeyboardHelper keyboardHelper, @Value("${app.bot.userName}") String userName, @Value("${app.bot.userName.token}") String token, @Autowired Catalog catalog) {
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
        this.catalog.setGroupList(this.excelFetcher.getProductGroupsFromSheet());
        this.catalog.setItemList(this.excelFetcher.getCatalogItems());
    }
    private SendPhoto getGoodsContainerSendPhotoMsg(long chatId) {
        File goodsContainerImg = new File("src/main/resources/goods/goods_box.webp");
        return SendPhoto.builder().caption("⚡️" + "Каталог" + "⚡️").chatId(chatId).photo(new InputFile(goodsContainerImg)).build();
    }
    private SendMessage getBucketMessage(BotVisitor visitor, String userId){
        var itemList = visitor.getBucket().stream().filter(Objects::nonNull).map(item->" ⏺ "+item).toList();
        var textInBucket =  String.join("\n\n", itemList);
        if (itemList.isEmpty()) {
            textInBucket = "[Корзина порожня]";
        }
       return SendMessage
               .builder()
               .chatId(userId)
               .text("=КОРЗИНА=" + "\n\n" +textInBucket)
               .replyMarkup(keyboardHelper.buildBucketKeyboardMenu())
               .build();
    }
    private void handleInlineButtonClicks(CallbackQuery callbackQuery) throws TelegramApiException, IOException {
        ScalaTextHelper scalaTextHelper = new ScalaTextHelper();
        Long userId = callbackQuery.getFrom().getId();
        BotVisitor visitor = this.botVisitorService.getBotVisitorByUserId(userId.toString());
        log.info(visitor.toString());
        String callBackButton = callbackQuery.getData();
        Buttons receivedButton;

        // todo: refactor control flow
        try {
            receivedButton = Buttons.valueOf(callBackButton);
        } catch (java.lang.IllegalArgumentException exception) {
            // todo: refactor control flow
            if (callBackButton.startsWith("QTY_")){
                getEditItemQtyMenu(scalaTextHelper, visitor, callBackButton);
                return;
            }

            var subCatalogAnswer = SendMessage
                    .builder()
                    .allowSendingWithoutReply(true)
                    .text(scalaTextHelper.getSubMenuText(this.catalog.getItemList(),callBackButton))
                    .chatId(userId)
                    .replyMarkup(this.keyboardHelper.buildMainMenuReply())
                    .build();

            this.botVisitorService.saveBotVisitor(visitor);

            this.sendApiMethod(subCatalogAnswer);
            return;
        }

        String name = callbackQuery.getFrom().getFirstName();

        var answer = switch (receivedButton) {
            case GOODS_QTY_EDIT ->
                 SendMessage
                         .builder()
                         .allowSendingWithoutReply(true)
                         .text("Обирай товар у корзині для редагування кількості:")
                         .chatId(userId)
                         .replyMarkup(this.keyboardHelper.buildEditQtyItemMenu(visitor.getBucket()))
                         .build();


            case CATALOG -> {
                visitor.setState(Buttons.CATALOG);
                execute(getGoodsContainerSendPhotoMsg(userId));
                yield SendMessage.builder().allowSendingWithoutReply(true).text("Обирай з товарних груп:").chatId(userId).replyMarkup(this.keyboardHelper.buildCatalogItemsMenu()).build();
            }

            case BUCKET -> {
                visitor = this.botVisitorService.getOrCreateVisitor(visitor.getUser());
                visitor.setState(Buttons.BUCKET);
                yield getBucketMessage(visitor,visitor.getUserId());
            }

            case CONTACTS -> {
                visitor.setState(Buttons.CONTACTS);
                yield SendMessage.builder().chatId(userId).text(scalaTextHelper.getContactsMsg()).replyMarkup(keyboardHelper.buildMainMenuReply()).build();
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

    private void getEditItemQtyMenu(ScalaTextHelper scalaTextHelper, BotVisitor visitor, String callBackButton) throws TelegramApiException {

        String itemId;

        if (Objects.nonNull(callBackButton)) {
            itemId  =  callBackButton.replaceAll("QTY_", "");
        }
        else {
            itemId = visitor.getItemToEditQty().id();
        }

        final String finalItemId = itemId;

        var item = visitor
                .getBucket()
                .stream()
                .filter(i -> finalItemId.equals(i.id()))
                .findFirst()
                .get();

        visitor.setItemToEditQty(item);

        var editItemQtyAnswer = SendMessage
                .builder()
                .allowSendingWithoutReply(true)
                .text(scalaTextHelper.getEditItemQtyMsg(item))
                .chatId(visitor.getUserId()).build();

        this.botVisitorService.saveBotVisitor(visitor);
        this.sendApiMethod(editItemQtyAnswer);
    }

    private void handleInputMsg(Update update) throws TelegramApiException {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String textFromUser = update.getMessage().getText();
            String userId = update.getMessage().getFrom().getId().toString();
            Long chatId = update.getMessage().getChatId();
            String userFirstName = update.getMessage().getFrom().getFirstName();
            ScalaTextHelper scalaTextHelper = new ScalaTextHelper();
            BotVisitor botVisitor = this.botVisitorService.getBotVisitorByUserId(userId);

            log.info("[chatID:{}, userFirstName:{}] : {}", chatId, userFirstName, textFromUser);

            // main menu by default
            SendMessage sendMessage = SendMessage
                    .builder()
                    .chatId(chatId.toString())
                    .text(scalaTextHelper.getMainMenuText(userFirstName))
                    .replyMarkup(keyboardHelper.buildMainMenuReply()).build();

            if (textFromUser.startsWith(REMOVE_COMMAND)) {
                Item toRemove = botVisitor.getItemToEditQty();
                botVisitor.getBucket().remove(toRemove);
                botVisitor.setItemToEditQty(null);
                this.botVisitorService.saveBotVisitor(botVisitor);
                sendMessage = getBucketMessage(botVisitor,botVisitor.getUserId());
            }

            if(Objects.nonNull(botVisitor.getItemToEditQty())) {
                if (isNumeric(textFromUser)){
                    Item toRemove = botVisitor.getItemToEditQty();
                    Integer qty = Integer.parseInt(textFromUser);
                    Item toAdd =new Item(toRemove.id(),toRemove.name(),toRemove.price(),toRemove.group(),qty);
                    botVisitor.setItemToEditQty(null);
                    botVisitor.getBucket().remove(toRemove);
                    botVisitor.getBucket().add(toAdd);
                    botVisitor.setState(Buttons.BUCKET);
                    this.botVisitorService.saveBotVisitor(botVisitor);
                    sendMessage = getBucketMessage(botVisitor,botVisitor.getUserId());
                }else {
                    getEditItemQtyMenu(scalaTextHelper,botVisitor,null);
                    return;
                }
            }

            //add item to the bucket handler
            if (textFromUser.startsWith(GOOD_ADD_COMMAND)) {
                var textFromUserCleaned = textFromUser.replaceAll("/", "");
                Optional<Item> maybeItem = this.catalog.getItemList().stream().filter(item -> item.id().equalsIgnoreCase(textFromUserCleaned)).findFirst();
                if (maybeItem.isPresent()) {
                    Item item = maybeItem.get();
                    botVisitor.getBucket().add(item);
                    this.botVisitorService.saveBotVisitor(botVisitor);
                    sendMessage.setText(scalaTextHelper.getItemAcceptedText(item));
                } else {
                    sendMessage.setText(scalaTextHelper.getItemNotAcceptedText());
                    this.botVisitorService.saveBotVisitor(botVisitor);
                }
            }

            try {
                this.sendApiMethod(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Exception when sending message: ", e);
            }
        } else {
            log.warn("Unexpected update from user");
        }
    }

    // todo: extract to helper class
    private boolean isNumeric(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }


    /**
     * Main method for handling input messages
     */
    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasCallbackQuery()) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                User user = callbackQuery.getFrom();
                this.botVisitorService.getOrCreateVisitor(user);
                handleInlineButtonClicks(callbackQuery);
            } else {
                User user = update.getMessage().getFrom();
                this.botVisitorService.getOrCreateVisitor(user);
                handleInputMsg(update);
            }
        }
        catch (TelegramApiException|IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }
}