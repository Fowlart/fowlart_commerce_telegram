package com.fowlart.FowlartCommerce;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class KeyboardHelper {

    public static ReplyKeyboardMarkup buildMainMenu() {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("❗️Товари");
        keyboardRow.add("❗Доставка");
        keyboardRow.add("❗Борг");

        return ReplyKeyboardMarkup.builder().keyboard(List.of(keyboardRow)).selective(true).resizeKeyboard(true).oneTimeKeyboard(false).build();
    }

    private static InlineKeyboardButton buildButton(String text,String callBackText) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callBackText);
        return button;
    }

    public static InlineKeyboardMarkup buildReplyInlineKeyboardMenu() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            rowInline.add(buildButton(String.valueOf(i), String.valueOf(i)));
        }

        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
