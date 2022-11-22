package com.fowlart.main;

import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.in_mem_catalog.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class KeyboardHelper {

    private final Catalog catalog;

    public KeyboardHelper(@Autowired Catalog catalog) {
        this.catalog = catalog;
    }

    private InlineKeyboardButton buildButton(String text, String callBackText) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callBackText);
        return button;
    }

    public InlineKeyboardMarkup buildInPhoneEditingModeMenu() {
        var markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        var rowInline = new ArrayList<InlineKeyboardButton>();
        rowInline.add(buildButton("Вийти", "EDIT_PHONE_EXIT"));
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
    public InlineKeyboardMarkup buildPersonalDataEditingMenu() {
        var markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        var rowInline1 = new ArrayList<InlineKeyboardButton>();
        var rowInline2 = new ArrayList<InlineKeyboardButton>();
        var rowInline3 = new ArrayList<InlineKeyboardButton>();

        rowInline1.add(buildButton("☎️ Змінити номер телефону", "EDIT_PHONE"));
        rowInline2.add(buildButton("\uD83D\uDCCB Змінити ФІО", "EDIT_NAME"));
        rowInline3.add(buildButton("\uD83C\uDF10 Змінити Email", "EDIT_EMAIL"));
        rowsInline.addAll(List.of(rowInline1,rowInline2,rowInline3));
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
    public InlineKeyboardMarkup buildEditQtyItemMenu(Set<Item> items) {
        var markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        items.forEach(item -> {
            var rowInline = new ArrayList<InlineKeyboardButton>();
            rowInline.add(buildButton(item.id(), "QTY_" + item.id()));
            rowsInline.add(rowInline);
        });
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
    public InlineKeyboardMarkup buildMainMenuReply() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(buildButton("\uD83D\uDCD7 Каталог", "CATALOG"));
        rowInline1.add(buildButton("\uD83D\uDCDD Корзина", "BUCKET"));
        rowInline1.add(buildButton("☎️ Контакти", "CONTACTS"));

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(buildButton("\uD83D\uDC64 Мої данні", "MYDATA"));

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(List.of(rowInline1, rowInline2));
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
    public InlineKeyboardMarkup buildCatalogItemsMenu() {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        for (String groupItem : catalog.getGroupList()) {
            rowInline.add(buildButton(groupItem, groupItem));
            rowsInline.add(rowInline);
            rowInline = new ArrayList<>();
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
    public InlineKeyboardMarkup buildBucketKeyboardMenu() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("Очистити", "DISCARD"));
        rowInline.add(buildButton("OK \uD83C\uDF6B", "SUBMIT"));
        rowInline.add(buildButton("Кількість", "GOODS_QTY_EDIT"));

        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
