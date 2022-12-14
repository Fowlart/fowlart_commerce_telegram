package com.fowlart.main;

import com.fowlart.main.catalog_fetching.ExcelFetcher;
import com.fowlart.main.email.GmailSender;
import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.in_mem_catalog.Item;
import com.fowlart.main.logging.LoggerHelper;
import com.fowlart.main.messages.ResponseWithPhotoMessageAndScalaBotVisitor;
import com.fowlart.main.messages.ResponseWithSendMessageAndScalaBotVisitor;
import com.fowlart.main.state.BotVisitor;
import com.fowlart.main.state.BotVisitorService;
import com.fowlart.main.state.OrderService;
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

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class Bot extends TelegramLongPollingBot implements InitializingBean {

    public static final String EDIT_PHONE = "EDIT_PHONE";
    public static final String EDIT_PHONE_EXIT = "EDIT_PHONE_EXIT";
    public static final String EDIT_NAME = "EDIT_NAME";
    public static final String DISCARD_ITEM = "DISCARD_ITEM";
    public static final String ITEM_NOT_FOUND_IMG_FILE_NAME = "/no_image_available.png";
    private static final String CATALOG = "CATALOG";
    private static final String MY_DATA = "MYDATA";
    private static final String GOODS_QTY_EDIT = "GOODS_QTY_EDIT";
    private static final String BUCKET = "BUCKET";
    private static final String CONTACTS = "CONTACTS";
    private static final String MAIN_SCREEN = "MAIN_SCREEN";
    private static final String SUBMIT = "SUBMIT";
    private static final String DISCARD = "DISCARD";
    private static Bot instance;
    private final BotVisitorService botVisitorService;
    private final ExcelFetcher excelFetcher;
    private final KeyboardHelper keyboardHelper;
    private final Catalog catalog;
    private final String userName;
    private final String token;
    private final ScalaHelper scalaHelper;
    private final OrderService orderService;
    private final String outputForOrderPath;
    private final GmailSender gmailSender;
    private final String inputForImgPath;
    public Bot(@Autowired GmailSender gmailSender,
               @Autowired BotVisitorService botVisitorService,
               @Autowired ExcelFetcher excelFetcher,
               @Autowired KeyboardHelper keyboardHelper,
               @Autowired Catalog catalog,
               @Autowired OrderService orderService,
               @Value("${app.bot.userName}") String userName,
               @Value("${app.bot.userName.token}") String token,
               @Value("${app.bot.order.output.folder}") String outputForOrderPath,
               @Value("${app.bot.items.img.folder}") String inputForImgPath) {
        this.inputForImgPath = inputForImgPath;
        this.gmailSender = gmailSender;
        this.outputForOrderPath = outputForOrderPath;
        this.orderService = orderService;
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
        this.catalog.setGroupList(this.excelFetcher.getProductGroupsFromPrice());
        this.catalog.setItemList(this.excelFetcher.getCatalogItems());
    }

    private void handleInlineButtonClicks(CallbackQuery callbackQuery) throws TelegramApiException {
        Long userId = callbackQuery.getFrom().getId();
        BotVisitor visitor = this.botVisitorService.getBotVisitorByUserId(userId.toString());
        var scalaBotVisitor = BotVisitorToScalaBotVisitorConverter.convertBotVisitorToScalaBotVisitor(visitor);
        LoggerHelper.logInfoInFile("handleInlineButtonClicks method, visitor: "+scalaBotVisitor);
        var callBackButtonArr = callbackQuery.getData().split("__");
        String callBackButton = callBackButtonArr[0];
        String mbItemId = null;
        if (callBackButtonArr.length > 1) mbItemId = callBackButtonArr[1];

        // todo: add logging HERE
        var answer = switch (callBackButton) {
            case DISCARD_ITEM -> handleItemRemove(visitor, mbItemId);
            case GOODS_QTY_EDIT -> handleGoodsQtyEdit(visitor, mbItemId);
            // personal data editing BEGIN
            case MY_DATA -> handleMyDataEditing(visitor);
            case EDIT_PHONE -> handleEditPhone(visitor);
            case EDIT_PHONE_EXIT -> handleEditPhoneExitPushed(visitor);
            case EDIT_NAME -> handleEditName(visitor);
            // personal data editing END
            case CATALOG -> handleCatalog(visitor);
            case CONTACTS -> handleContacts(visitor);
            case MAIN_SCREEN -> handleMainScreen(visitor);
            case SUBMIT -> handleOrderSubmit(visitor);
            case DISCARD -> handleDiscard(visitor);
            case BUCKET -> handleBucket(visitor);

            default -> {
                var subGroupItems = scalaHelper.getSubMenuText(this.catalog.getItemList(), callBackButton);
                for (String str : subGroupItems) {
                    var lastMessage = subGroupItems[subGroupItems.length - 1];
                    var subCatalogAnswer = SendMessage
                            .builder()
                            .allowSendingWithoutReply(true)
                            .parseMode("html")
                            .disableWebPagePreview(false)
                            .text(str)
                            .chatId(userId)
                            .build();
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

    private SendMessage handleBucket(BotVisitor visitor) throws TelegramApiException {
        LoggerHelper.logInfoInFile("handleBucket method, visitorId "+visitor.getUserId());
        if (visitor.getBucket().isEmpty())
            return scalaHelper.getEmptyBucketMessage(keyboardHelper, visitor.getUser().getId());
        visitor.setNameEditingMode(false);
        this.sendApiMethod(scalaHelper.getItemBucketIntroMessage(visitor.getUserId(), keyboardHelper));
        for (Item item : visitor.getBucket()) {

            var itemMessage = scalaHelper.getItemMessageWithPhotoInBucket(
                    visitor.getUser().getId(),
                    item, inputForImgPath+"/"+ITEM_NOT_FOUND_IMG_FILE_NAME,
                    inputForImgPath,
                    keyboardHelper);

            execute(itemMessage);
        }
        return null;
    }

    private SendMessage handleMyDataEditing(BotVisitor visitor) {
        LoggerHelper.logInfoInFile("handleMyDataEditing method, visitorId "+visitor.getUserId());
        visitor.setNameEditingMode(false);
        return scalaHelper.buildSimpleReplyMessage(
                visitor.getUser().getId(),
                scalaHelper.getPersonalDataEditingSectionText(visitor),
                this.keyboardHelper.buildPersonalDataEditingMenu());
    }

    private SendMessage handleEditPhone(BotVisitor visitor) {
        LoggerHelper.logInfoInFile("handleEditPhone method, visitorId "+visitor.getUserId());
        visitor.setPhoneNumberFillingMode(true);
        visitor.setNameEditingMode(false);
        return scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(),
                scalaHelper.getPhoneEditingText(visitor.getUser().getId()),
                this.keyboardHelper.buildInPhoneEditingModeMenu());
    }

    private SendMessage handleEditPhoneExitPushed(BotVisitor visitor) {
        LoggerHelper.logInfoInFile("handleEditPhoneExitPushed method, visitorId "+visitor.getUserId());
        visitor.setPhoneNumberFillingMode(false);
        return scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(),
                scalaHelper.getPhoneEditingText(visitor.getUser().getId()),
                this.keyboardHelper.buildPersonalDataEditingMenu());
    }

    private SendMessage handleEditName(BotVisitor visitor) {
        LoggerHelper.logInfoInFile("handleEditName method, visitorId "+visitor.getUserId());
        visitor.setNameEditingMode(true);
        return scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(),
                scalaHelper.getNameEditingText(visitor.getUser().getId()),
                this.keyboardHelper.buildPersonalDataEditingMenu());
    }

    private SendMessage handleGoodsQtyEdit(BotVisitor visitor, String itemId) throws TelegramApiException {
        LoggerHelper.logInfoInFile("handleGoodsQtyEdit method, visitorId "+visitor.getUserId());
        visitor.setNameEditingMode(false);
        var itemToEditQty = visitor.getBucket().stream().filter(it -> itemId.equals(it.id())).findFirst();
        visitor.setItemToEditQty(itemToEditQty.orElse(null));
        displayEditItemQtyResponse(scalaHelper, visitor);
        return null;
    }

    private SendMessage handleItemRemove(BotVisitor visitor, String itemId) {
        LoggerHelper.logInfoInFile("handleItemRemove method, visitorId "+visitor.getUserId());
        visitor.setNameEditingMode(false);
        var newBucket = visitor.getBucket().stream().filter(it -> !itemId.equals(it.id())).collect(Collectors.toSet());
        visitor.setItemToEditQty(null);
        visitor.setBucket(newBucket);
        return SendMessage.builder().chatId(visitor.getUserId()).text(" ?????????? ????????????????. ?????????????? ??????????????????. ???? ???????????????? ???????????????????? ????????????????????.").replyMarkup(keyboardHelper.buildMainMenuReply()).build();
    }

    private SendMessage handleCatalog(BotVisitor visitor) {
        LoggerHelper.logInfoInFile("handleCatalog method, visitorId "+visitor.getUserId());
        visitor.setNameEditingMode(false);
        return scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(), "\uD83D\uDD0E ???????????? ?? ???????????????? ????????:",
                keyboardHelper.buildCatalogItemsMenu());
    }

    private SendMessage handleContacts(BotVisitor visitor) {
        LoggerHelper.logInfoInFile("handleContacts method, visitorId "+visitor.getUserId());
        visitor.setNameEditingMode(false);
        return scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(), scalaHelper.getContactsMsg(), keyboardHelper.buildMainMenuReply());
    }

    private SendMessage handleMainScreen(BotVisitor visitor) {
        LoggerHelper.logInfoInFile("handleMainScreen method, visitorId "+visitor.getUserId());
        var msg = scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(), scalaHelper.getMainMenuText(visitor), keyboardHelper.buildMainMenuReply());
        visitor.setNameEditingMode(false);
        return msg;
    }

    private SendMessage handleDiscard(BotVisitor visitor) {
        LoggerHelper.logInfoInFile("handleDiscard method, visitorId "+visitor.getUserId());
        visitor.setBucket(new HashSet<>());
        visitor.setNameEditingMode(false);

        return scalaHelper
                .buildSimpleReplyMessage(visitor.getUser().getId(), "?????????????? ??????????????!",
                        keyboardHelper.buildMainMenuReply());
    }

    private SendMessage handleOrderSubmit(BotVisitor visitor) {

        LoggerHelper.logInfoInFile("handleOrderSubmit method, visitorId "+visitor.getUserId());
        var order = OrderHandler.handleOrder(BotVisitorToScalaBotVisitorConverter.convertBotVisitorToScalaBotVisitor(visitor));

        var orderSubmitReply = scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(), "???????????????????? ????????????????!", keyboardHelper.buildMainMenuReply());

        if (order.orderBucket().isEmpty()) {
            orderSubmitReply.setText("???? ?????????????????????? ???????????????????? ?? ?????????????? ?????????????? ??????????????! ???????? ??????????, ?????????????? ?????????? ????????!");
            return orderSubmitReply;
        }
        // order handling:
        orderService.saveOrder(order);
        var orderList = visitor.getOrders();
        if (Objects.nonNull(orderList)) {
            orderList.addLast(order.orderId());
        } else {
            visitor.setOrders(new LinkedList<>());
        }
        var orderFileName = ("/" + order.userName() + "_" + order.orderId() + "_" + order.date() + ".csv")
                .replaceAll(" ", "_")
                .replaceAll("-", "_");
        // send email, save order on the disk
        try {
            var savedCsv = OrderHandler.saveOrderAsCsv(order, outputForOrderPath+ "/" + orderFileName);
            gmailSender.sendOrderMessage(scalaHelper.getEmailOrderText(order), savedCsv);
        } catch (MessagingException | IOException e) {
            LoggerHelper.logErrorInFile(e.getMessage(),e);
            orderSubmitReply.setText("?????????? ???????????? ?? ?????????????????????????? ??????????! ????????????????????, ???????? ??????????, ????????????!");
            return orderSubmitReply;
        }
        visitor.setBucket(new HashSet<>());
        visitor.setNameEditingMode(false);
        return orderSubmitReply;
    }

    private void displayEditItemQtyResponse(ScalaHelper scalaHelper,
                                            BotVisitor visitor) throws TelegramApiException {

        if (Objects.isNull(visitor.getItemToEditQty())) {
            var noSuchItemInBasket = scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(), "???????????? ???????????? ?????? ?????????? ?? ??????????????.", keyboardHelper.buildMainMenuReply());
            this.sendApiMethod(noSuchItemInBasket);
        }

        String itemId = visitor.getItemToEditQty().id();
        final String finalItemId = itemId;
        var item = visitor.getBucket().stream().filter(i -> finalItemId.equals(i.id())).findFirst().get();
        visitor.setItemToEditQty(item);

        var editItemQtyAnswer = SendMessage
                .builder()
                .allowSendingWithoutReply(true)
                .parseMode("html")
                .text(scalaHelper.getEditItemQtyMsg(item))
                .chatId(visitor.getUserId()).build();

        this.sendApiMethod(editItemQtyAnswer);
    }

    private void handleInputMsgOrCommand(Update update) throws TelegramApiException {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String textFromUser = update.getMessage().getText();
            String userId = update.getMessage().getFrom().getId().toString();
            Long chatId = update.getMessage().getChatId();
            BotVisitor botVisitor = this.botVisitorService.getBotVisitorByUserId(userId);
            var scalaBotVisitor = BotVisitorToScalaBotVisitorConverter.convertBotVisitorToScalaBotVisitor(botVisitor);
            LoggerHelper.logInfoInFile("handleInputMsgOrCommand begin, visitor: "+scalaBotVisitor);

            var response = BotMessageHandler.handleMessageOrCommand(
                    scalaBotVisitor,
                    textFromUser,
                    keyboardHelper,
                    chatId,
                    catalog,
                    inputForImgPath+ITEM_NOT_FOUND_IMG_FILE_NAME,
                    inputForImgPath);

            if (response instanceof ResponseWithSendMessageAndScalaBotVisitor castedResponse) {
                var sendMessage = castedResponse.sendMessageResponse();
                sendApiMethod(sendMessage);
                var updatedBotVisitor = BotVisitorToScalaBotVisitorConverter.convertToJavaBotVisitor(castedResponse.scalaBotVisitor());
                botVisitorService.saveBotVisitor(updatedBotVisitor);
            }

            if (response instanceof ResponseWithPhotoMessageAndScalaBotVisitor castedResponse) {
                var photoMessage = castedResponse.photoMessage();
                execute(photoMessage);
                var updatedBotVisitor = BotVisitorToScalaBotVisitorConverter.convertToJavaBotVisitor(castedResponse.scalaBotVisitor());
                botVisitorService.saveBotVisitor(updatedBotVisitor);
            }

            LoggerHelper.logInfoInFile("handleInputMsgOrCommand end, visitor to be saved: "+scalaBotVisitor);
            var updatedBotVisitor = BotVisitorToScalaBotVisitorConverter.convertToJavaBotVisitor(response.scalaBotVisitor());
            botVisitorService.saveBotVisitor(updatedBotVisitor);

        } else {
            LoggerHelper.logErrorInFile("Unexpected update from user");
        }
    }

    /**
     * <h3 style='color:red'>Main method for handling input messages</h3>
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
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }
}