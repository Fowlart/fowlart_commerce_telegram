package com.fowlart.main;

import com.fowlart.main.catalog_fetching.ExcelFetcher;
import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.messages.EditQtyForItemMessage;
import com.fowlart.main.messages.ItemAddToBucketMessage;
import com.fowlart.main.messages.RemoveItemFromBucketMessage;
import com.fowlart.main.messages.ResponseWithSendMessage;
import com.fowlart.main.state.BotVisitor;
import com.fowlart.main.state.BotVisitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

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

    private void handleInlineButtonClicks(CallbackQuery callbackQuery) throws TelegramApiException, IOException {
        ScalaHelper scalaHelper = new ScalaHelper();
        Long userId = callbackQuery.getFrom().getId();
        BotVisitor visitor = this.botVisitorService.getBotVisitorByUserId(userId.toString());
        log.info(visitor.toString());
        String callBackButton = callbackQuery.getData();
        String firstName = callbackQuery.getFrom().getFirstName();


        if (callBackButton.startsWith("QTY_")) {
            displayEditItemQtyMenu(scalaHelper, visitor, callBackButton);
            return;
        }

        var answer = switch (callBackButton) {

            case "GOODS_QTY_EDIT" ->
                    SendMessage.builder().allowSendingWithoutReply(true).text("Обирай товар у корзині для редагування кількості:").chatId(userId).replyMarkup(this.keyboardHelper.buildEditQtyItemMenu(visitor.getBucket())).build();

            case "CATALOG" -> {
                execute(getGoodsContainerSendPhotoMsg(userId));
                yield SendMessage.builder().allowSendingWithoutReply(true).text("Обирай з товарних груп:").chatId(userId).replyMarkup(this.keyboardHelper.buildCatalogItemsMenu()).build();
            }

            case "BUCKET" -> {
                visitor = this.botVisitorService.getOrCreateVisitor(visitor.getUser());
                yield scalaHelper.getBucketMessage(visitor, visitor.getUserId(), keyboardHelper);
            }

            case "CONTACTS" ->
                    SendMessage.builder().chatId(userId).text(scalaHelper.getContactsMsg()).replyMarkup(keyboardHelper.buildMainMenuReply()).build();

            case "MAIN_SCREEN" ->
                    SendMessage.builder().chatId(userId).text(scalaHelper.getMainMenuText(firstName)).replyMarkup(keyboardHelper.buildMainMenuReply()).build();

            case "SUBMIT" -> {
                visitor.getBucket().clear();
                this.botVisitorService.saveBotVisitor(visitor);
                yield SendMessage.builder().chatId(userId).text("Замовлення прийнято!").replyMarkup(keyboardHelper.buildMainMenuReply()).build();
            }

            case "DISCARD" -> {
                visitor.getBucket().clear();
                this.botVisitorService.saveBotVisitor(visitor);
                yield SendMessage.builder().chatId(userId).text(scalaHelper.getMainMenuText(firstName)).replyMarkup(keyboardHelper.buildMainMenuReply()).build();
            }

            // clicked some catalog group sub-menu
            default -> {
                var subGroupItems = scalaHelper.getSubMenuText(this.catalog.getItemList(), callBackButton);

                for (String str : subGroupItems) {
                    var lastMessage = subGroupItems[subGroupItems.length - 1];
                    var subCatalogAnswer = SendMessage.builder().allowSendingWithoutReply(true).text(str).chatId(userId).build();
                    if (str.equals(lastMessage)) {
                        subCatalogAnswer.setReplyMarkup(this.keyboardHelper.buildMainMenuReply());
                    }
                    this.sendApiMethod(subCatalogAnswer);
                }
                this.botVisitorService.saveBotVisitor(visitor);
                yield null;
            }
        };

        this.botVisitorService.saveBotVisitor(visitor);

        if (Objects.nonNull(answer)) {
            this.sendApiMethod(answer);
        }
    }

    private void displayEditItemQtyMenu(ScalaHelper scalaHelper,
                                        BotVisitor visitor,
                                        String callBackButton) throws TelegramApiException {
        String itemId;
        if (Objects.nonNull(callBackButton)) {
            itemId = callBackButton.replaceAll("QTY_", "");
        } else {
            itemId = visitor.getItemToEditQty().id();
        }
        final String finalItemId = itemId;
        var item = visitor.getBucket().stream().filter(i -> finalItemId.equals(i.id())).findFirst().get();
        visitor.setItemToEditQty(item);
        var editItemQtyAnswer = SendMessage.builder().allowSendingWithoutReply(true).text(scalaHelper.getEditItemQtyMsg(item)).chatId(visitor.getUserId()).build();
        this.botVisitorService.saveBotVisitor(visitor);
        this.sendApiMethod(editItemQtyAnswer);
    }

    private void handleInputMsgOrCommand(Update update) throws TelegramApiException {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String textFromUser = update.getMessage().getText();
            String userId = update.getMessage().getFrom().getId().toString();
            Long chatId = update.getMessage().getChatId();
            String userFirstName = update.getMessage().getFrom().getFirstName();
            ScalaHelper scalaHelper = new ScalaHelper();
            BotVisitor botVisitor = this.botVisitorService.getBotVisitorByUserId(userId);
            log.info("[chatID:{}, userFirstName:{}] : {}", chatId, userFirstName, textFromUser);

            // main menu by default
            SendMessage sendMessage = SendMessage.builder().chatId(chatId.toString()).text(scalaHelper.getMainMenuText(userFirstName)).replyMarkup(keyboardHelper.buildMainMenuReply()).build();

            if (textFromUser.startsWith(REMOVE_COMMAND)) {
                var removeItemFromBucketMessage = new RemoveItemFromBucketMessage(botVisitor, botVisitorService, keyboardHelper);
                ResponseWithSendMessage response = (ResponseWithSendMessage) BotMessageHandler.handleBotMessage(removeItemFromBucketMessage);
                sendMessage = response.sendMessageResponse();
            }

            if (Objects.nonNull(botVisitor.getItemToEditQty())) {
                if (scalaHelper.isNumeric(textFromUser)) {
                    Integer qty = Integer.parseInt(textFromUser);
                    var editQtyForItemMessage = new EditQtyForItemMessage(qty, botVisitor, botVisitorService, keyboardHelper);
                    ResponseWithSendMessage response = (ResponseWithSendMessage) BotMessageHandler.handleBotMessage(editQtyForItemMessage);
                    sendMessage = response.sendMessageResponse();

                } else {
                    displayEditItemQtyMenu(scalaHelper, botVisitor, null);
                    return;
                }
            }

            if (textFromUser.startsWith(GOOD_ADD_COMMAND)) {
                var itemAddToBucketMessage = new ItemAddToBucketMessage(textFromUser.replaceAll("/", ""), botVisitor, botVisitorService, catalog, sendMessage);
                ResponseWithSendMessage response = (ResponseWithSendMessage) BotMessageHandler.handleBotMessage(itemAddToBucketMessage);
                sendMessage = response.sendMessageResponse();
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
                handleInputMsgOrCommand(update);
            }
        } catch (TelegramApiException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }
}