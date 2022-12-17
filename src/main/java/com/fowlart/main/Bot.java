package com.fowlart.main;

import com.fowlart.main.catalog_fetching.ExcelFetcher;
import com.fowlart.main.email.GmailSender;
import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.in_mem_catalog.Item;
import com.fowlart.main.messages.ResponseWithSendMessageAndScalaBotVisitor;
import com.fowlart.main.state.BotVisitor;
import com.fowlart.main.state.BotVisitorService;
import com.fowlart.main.state.OrderService;
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
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.mail.MessagingException;
import java.io.File;
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
    private static final Logger log = LoggerFactory.getLogger(Bot.class);
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

    public Bot(@Autowired GmailSender gmailSender,
               @Autowired BotVisitorService botVisitorService,
               @Autowired ExcelFetcher excelFetcher,
               @Autowired KeyboardHelper keyboardHelper,
               @Autowired Catalog catalog,
               @Autowired OrderService orderService,
               @Value("${app.bot.userName}") String userName,
               @Value("${app.bot.userName.token}") String token,
               @Value("${app.bot.order.output.folder}") String outputForOrderPath) {
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
        this.catalog.setGroupList(this.excelFetcher.getProductGroupsFromSheet());
        this.catalog.setItemList(this.excelFetcher.getCatalogItems());
    }

    private void handleInlineButtonClicks(CallbackQuery callbackQuery) throws TelegramApiException {
        Long userId = callbackQuery.getFrom().getId();
        BotVisitor visitor = this.botVisitorService.getBotVisitorByUserId(userId.toString());
        log.info(visitor.toString());
        var callBackButtonArr =  callbackQuery.getData().split("__");
        String callBackButton = callBackButtonArr[0];
        String mbItemId = null;
        if (callBackButtonArr.length>1) {
            mbItemId = callBackButtonArr[1];
        }

        var answer = switch (callBackButton) {
            case "DISCARD_ITEM" -> handleItemRemove(visitor,mbItemId);
            case GOODS_QTY_EDIT -> handleGoodsQtyEdit(visitor,mbItemId);
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

    private SendPhoto getMessageWithPhoto(long chatId, String text,String filePath) {
        File goodsContainerImg = new File(filePath);
        return SendPhoto.builder().caption(text).chatId(chatId).photo(new InputFile(goodsContainerImg)).build();
    }

    private SendMessage handleBucket(BotVisitor visitor) throws TelegramApiException {
        if (visitor.getBucket().isEmpty()) return scalaHelper.getBucketMessage(visitor,visitor.getUserId(),keyboardHelper);
        visitor.setNameEditingMode(false);
        this.sendApiMethod(scalaHelper.getItemBucketIntroMessage(visitor.getUserId(),keyboardHelper));
        for (Item item: visitor.getBucket()) {
            var itemMessage = scalaHelper.getItemBucketMessage(item,keyboardHelper, visitor.getUserId());
            this.sendApiMethod(itemMessage);
        }
        return null;
    }

    private SendMessage handleMyDataEditing(BotVisitor visitor) {
        visitor.setNameEditingMode(false);
        return scalaHelper.buildSimpleReplyMessage(
                visitor.getUser().getId(),
                scalaHelper.getPersonalDataEditingSectionText(visitor),
                this.keyboardHelper.buildPersonalDataEditingMenu());
    }

    private SendMessage handleEditPhone(BotVisitor visitor) {
        visitor.setPhoneNumberFillingMode(true);
        visitor.setNameEditingMode(false);
        return scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(),
                scalaHelper.getPhoneEditingText(visitor.getUser().getId()),
                this.keyboardHelper.buildInPhoneEditingModeMenu());
    }

    private SendMessage handleEditPhoneExitPushed(BotVisitor visitor) {
        visitor.setPhoneNumberFillingMode(false);
        return scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(),
                scalaHelper.getPhoneEditingText(visitor.getUser().getId()),
                this.keyboardHelper.buildPersonalDataEditingMenu());
    }

    private SendMessage handleEditName(BotVisitor visitor) {
        visitor.setNameEditingMode(true);
        return scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(),
                scalaHelper.getNameEditingText(visitor.getUser().getId()),
                this.keyboardHelper.buildPersonalDataEditingMenu());
    }

    private SendMessage handleGoodsQtyEdit(BotVisitor visitor, String itemId) throws TelegramApiException {
        visitor.setNameEditingMode(false);
        var itemToEditQty  = visitor.getBucket().stream().filter(it->itemId.equals(it.id())).findFirst();
        visitor.setItemToEditQty(itemToEditQty.orElse(null));
        displayEditItemQtyMenu(scalaHelper, visitor);
        return null;
    }

    private SendMessage handleItemRemove(BotVisitor visitor, String itemId) {
        visitor.setNameEditingMode(false);
        var newBucket = visitor.getBucket().stream().filter(it->!itemId.equals(it.id())).collect(Collectors.toSet());
        visitor.setItemToEditQty(null);
        visitor.setBucket(newBucket);
        return SendMessage.builder().chatId(visitor.getUserId()).text("Товар видалено. Корзину збережено. Не забудьте відправити замовлення.").replyMarkup(keyboardHelper.buildMainMenuReply()).build();
    }

    private SendMessage handleCatalog(BotVisitor visitor) {
        // todo: what we do with the photos?
        //execute(getGoodsContainerPhotoMsg(userId));
        visitor.setNameEditingMode(false);
        return scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(), "Обирай з товарних груп:",
                keyboardHelper.buildCatalogItemsMenu());
    }

    private SendMessage handleContacts(BotVisitor visitor) {
        visitor.setNameEditingMode(false);
        return scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(), scalaHelper.getContactsMsg(), keyboardHelper.buildMainMenuReply());
    }

    private SendMessage handleMainScreen(BotVisitor visitor) {
        var msg = scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(), scalaHelper.getMainMenuText(visitor.getName()), keyboardHelper.buildMainMenuReply());
        visitor.setNameEditingMode(false);
        return msg;
    }

    private SendMessage handleDiscard(BotVisitor visitor) {
        visitor.setBucket(new HashSet<>());
        visitor.setNameEditingMode(false);

        return scalaHelper
                .buildSimpleReplyMessage(visitor.getUser().getId(), scalaHelper.getMainMenuText(visitor.getName()), keyboardHelper.buildMainMenuReply());
    }

    private SendMessage handleOrderSubmit(BotVisitor visitor) {

        var order = OrderHandler.handleOrder(BotVisitorToScalaBotVisitorConverter.convertBotVisitorToScalaBotVisitor(visitor));

        var orderSubmitReply = scalaHelper.buildSimpleReplyMessage(visitor.getUser().getId(), "Замовлення прийнято!", keyboardHelper.buildMainMenuReply());

        if (order.orderBucket().isEmpty()) {
            orderSubmitReply.setText("Ви намагаєтеся відправити в обробку порожню корзину! Будь ласка, замовте бодай щось!");
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
        log.info("saving order: " + order.orderId());
        var orderFileName = ("/" + order.userName() + "_" + order.orderId() + "_" + order.date() + ".csv")
                .replaceAll(" ", "_")
                .replaceAll("-", "_");
        // send email:
        try {
            var savedCsv = OrderHandler.saveOrderAsCsv(order, outputForOrderPath + orderFileName);
            gmailSender.sendOrderMessage(scalaHelper.getEmailOrderText(order), savedCsv);
        } catch (MessagingException | IOException e) {
            log.error(e.getMessage());
            orderSubmitReply.setText("Якась бачіна з відправленням листа! Повторість, будь ласка, спробу!");
            return orderSubmitReply;
        }
        visitor.setBucket(new HashSet<>());
        visitor.setNameEditingMode(false);
        return orderSubmitReply;
    }

    private void displayEditItemQtyMenu(ScalaHelper scalaHelper,
                                        BotVisitor visitor) throws TelegramApiException {
        String itemId = visitor.getItemToEditQty().id();
        final String finalItemId = itemId;
        var item = visitor.getBucket().stream().filter(i -> finalItemId.equals(i.id())).findFirst().get();
        visitor.setItemToEditQty(item);
        var editItemQtyAnswer = SendMessage.builder().allowSendingWithoutReply(true).text(scalaHelper.getEditItemQtyMsg(item)).chatId(visitor.getUserId()).build();
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
        } catch (TelegramApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }
}