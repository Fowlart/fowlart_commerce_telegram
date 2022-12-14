package com.fowlart.main;

import com.fowlart.main.catalog_fetching.ExcelFetcher;
import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.messages.*;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Component
public class Bot extends TelegramLongPollingBot implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(Bot.class);
    private static final String CATALOG = "CATALOG";
    private static final String GOOD_ADD_COMMAND = "/ID";
    private static final String REMOVE_COMMAND = "/remove";
    private static final String MY_DATA = "MYDATA";
    private static final String GOODS_QTY_EDIT = "GOODS_QTY_EDIT";
    private static final String BUCKET = "BUCKET";
    private static final String CONTACTS = "CONTACTS";
    private static final String MAIN_SCREEN = "MAIN_SCREEN";
    private static final String SUBMIT = "SUBMIT";
    private static final String DISCARD = "DISCARD";
    public static final String EDIT_PHONE = "EDIT_PHONE";
    public static final String EDIT_PHONE_EXIT = "EDIT_PHONE_EXIT";
    private static Bot instance;
    private final BotVisitorService botVisitorService;
    private final ExcelFetcher excelFetcher;
    private final KeyboardHelper keyboardHelper;
    private final Catalog catalog;
    private final String userName;
    private final String token;
    private ScalaHelper scalaHelper;

    public Bot(@Autowired BotVisitorService botVisitorService,
               @Autowired ExcelFetcher excelFetcher,
               @Autowired KeyboardHelper keyboardHelper,
               @Autowired Catalog catalog,
               @Value("${app.bot.userName}") String userName,
               @Value("${app.bot.userName.token}") String token) {
        this.botVisitorService = botVisitorService;
        this.excelFetcher = excelFetcher;
        this.keyboardHelper = keyboardHelper;
        this.userName = userName;
        this.token = token;
        this.catalog = catalog;
        this.scalaHelper = new ScalaHelper();
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

    private SendPhoto getGoodsContainerPhotoMsg(long chatId) {
        File goodsContainerImg = new File("src/main/resources/goods/goods_box.webp");
        return SendPhoto.builder().caption("⚡️" + "Каталог ").chatId(chatId).photo(new InputFile(goodsContainerImg)).build();
    }

    private void handleInlineButtonClicks(CallbackQuery callbackQuery) throws TelegramApiException, IOException {

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
            // personal data editing BEGIN
            case MY_DATA ->
                    scalaHelper.buildReplyMessage(userId,
                            scalaHelper.getPersonalDataEditingSectionText(visitor),
                            this.keyboardHelper.buildPersonalDataEditingMenu());

            case EDIT_PHONE -> {

                visitor.setPhoneNumberFillingMode(true);
                this.botVisitorService.saveBotVisitor(visitor);
                yield scalaHelper.buildReplyMessage(userId,
                        scalaHelper.getPhoneEditingText(userId),
                        this.keyboardHelper.buildInPhoneEditingModeMenu());
            }

            case EDIT_PHONE_EXIT -> {
                visitor.setPhoneNumberFillingMode(false);
                this.botVisitorService.saveBotVisitor(visitor);
                yield scalaHelper.buildReplyMessage(userId,
                        scalaHelper.getPhoneEditingText(userId),
                        this.keyboardHelper.buildPersonalDataEditingMenu());
            }

            case GOODS_QTY_EDIT ->
                    scalaHelper.buildReplyMessage(userId, "Обирай товар у корзині для редагування кількості:", this.keyboardHelper.buildEditQtyItemMenu(visitor.getBucket()));
            // personal data editing END

            case CATALOG -> {
                // todo: what we do with the photos?
                //execute(getGoodsContainerPhotoMsg(userId));
                yield scalaHelper.buildReplyMessage(userId, "Обирай з товарних груп:",
                        keyboardHelper.buildCatalogItemsMenu());
            }

            case BUCKET -> {
                visitor = this.botVisitorService.getOrCreateVisitor(visitor.getUser());
                yield scalaHelper
                        .getBucketMessage(visitor, visitor.getUserId(), keyboardHelper);
            }

            case CONTACTS ->
                    scalaHelper
                            .buildReplyMessage(userId, scalaHelper.getContactsMsg(), keyboardHelper.buildMainMenuReply());

            case MAIN_SCREEN ->
                    scalaHelper
                            .buildReplyMessage(userId, scalaHelper.getMainMenuText(firstName), keyboardHelper.buildMainMenuReply());

            case SUBMIT -> {
                visitor.getBucket().clear();
                this.botVisitorService.saveBotVisitor(visitor);
                yield scalaHelper
                        .buildReplyMessage(userId, "Замовлення прийнято!", keyboardHelper.buildMainMenuReply());
            }

            case DISCARD -> {
                visitor.setBucket(new HashSet<>());
                this.botVisitorService.saveBotVisitor(visitor);
                yield scalaHelper
                        .buildReplyMessage(userId,scalaHelper.getMainMenuText(firstName),keyboardHelper.buildMainMenuReply());
            }

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
            BotVisitor botVisitor = this.botVisitorService.getBotVisitorByUserId(userId);
            log.info("[chatID:{}, userFirstName:{}] : {}", chatId, userFirstName, textFromUser);

            var scalaBotVisitor = BotVisitorToScalaBotVisitorConverter.convertBotVisitorToScalaBotVisitor(botVisitor);

            var response = (ResponseWithSendMessageAndScalaBotVisitor) BotMessageHandler.handleMessageOrCommand(scalaBotVisitor, textFromUser, keyboardHelper, chatId, catalog);
            var sendMessage = response.sendMessageResponse();
            var updatedBotVisitor = BotVisitorToScalaBotVisitorConverter.convertToJavaBotVisitor(response.scalaBotVisitor());
            botVisitorService.saveBotVisitor(updatedBotVisitor);

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
     * <h2 style='color:red'>Main method for handling input messages</h2>
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