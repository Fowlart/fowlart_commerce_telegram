package com.fowlart.main;

import com.fowlart.main.state.Buttons;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardHelper {

    public static ReplyKeyboardMarkup buildMainMenu() {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("❗️Товари");
        keyboardRow.add("❗Корзинв");
        keyboardRow.add("❗Борг");

        return ReplyKeyboardMarkup.builder().keyboard(List.of(keyboardRow)).selective(true).resizeKeyboard(true).oneTimeKeyboard(false).build();
    }

    private InlineKeyboardButton buildButton(String text,String callBackText) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callBackText);
        return button;
    }

    public InlineKeyboardMarkup buildReplyCatalogMenuKeyboardMenu() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> line1 = new ArrayList<>();
        List<InlineKeyboardButton> line2 = new ArrayList<>();
        line1.add(buildButton("1", Buttons.CATALOG.name()));
        line1.add(buildButton("2", Buttons.CATALOG.name()));
        line1.add(buildButton("3", Buttons.CATALOG.name()));
        line1.add(buildButton("4", Buttons.CATALOG.name()));
        line1.add(buildButton("5", Buttons.CATALOG.name()));

        line2.add(buildButton("6", Buttons.CATALOG.name()));
        line2.add(buildButton("7", Buttons.CATALOG.name()));
        line2.add(buildButton("8", Buttons.CATALOG.name()));
        line2.add(buildButton("9", Buttons.CATALOG.name()));
        line2.add(buildButton("10", Buttons.CATALOG.name()));

        rowsInline.add(line1);
        rowsInline.add(line2);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup buildReplyMainMenuKeyboardMenu() {
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
