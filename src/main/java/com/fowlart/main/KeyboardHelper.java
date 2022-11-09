package com.fowlart.main;

import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.in_mem_catalog.Item;
import com.fowlart.main.state.Buttons;
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
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("\uD83D\uDCD7 Каталог", Buttons.CATALOG.name()));
        rowInline.add(buildButton("\uD83D\uDCDD Корзина", Buttons.BUCKET.name()));
        rowInline.add(buildButton("☎️ Контакти", Buttons.CONTACTS.name()));

        rowsInline.add(rowInline);
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
        rowInline.add(buildButton("Очистити", Buttons.DISCARD.name()));
        rowInline.add(buildButton("OK", Buttons.SUBMIT.name()));
        rowInline.add(buildButton("Кількість", Buttons.GOODS_QTY_EDIT.name()));

        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
