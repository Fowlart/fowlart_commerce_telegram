package com.fowlart.main;

import com.fowlart.main.catalog_fetching.ExcelFetcher;
import com.fowlart.main.state.Buttons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardHelper {

    private final ExcelFetcher excelFetcher;

    public KeyboardHelper(@Autowired ExcelFetcher excelFetcher) {
        this.excelFetcher = excelFetcher;
    }

    public static ReplyKeyboardMarkup buildMainMenu() {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("❗️Товари");
        keyboardRow.add("❗Корзинв");
        keyboardRow.add("❗Борг");

        return ReplyKeyboardMarkup.builder().keyboard(List.of(keyboardRow)).selective(true).resizeKeyboard(true).oneTimeKeyboard(false).build();
    }

    private InlineKeyboardButton buildButton(String text, String callBackText) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callBackText);
        return button;
    }

    public InlineKeyboardMarkup buildReplySubCatalogMenuKeyboardMenu(String topLevelItem) throws IOException {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (String item : excelFetcher.getGoodsFromProductGroup(topLevelItem)) {
            rowInline.add(buildButton(item, item));

            if (rowInline.size() >= 3) {
                rowsInline.add(rowInline);
                rowInline = new ArrayList<>();
            }
        }

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup buildMainMenuReply() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("Каталог", Buttons.CATALOG.name()));
        rowInline.add(buildButton("Корзина", Buttons.BUCKET.name()));
        rowInline.add(buildButton("Борг", Buttons.DEBT.name()));

        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup buildCatalogItemsMenu() {
        List<String> items;
        try {
            items = this.excelFetcher.getProductGroupsFromSheet();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (String item : items) {
            rowInline.add(buildButton(item, item));
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
        rowInline.add(buildButton("Підтвердити", Buttons.SUBMIT.name()));

        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
