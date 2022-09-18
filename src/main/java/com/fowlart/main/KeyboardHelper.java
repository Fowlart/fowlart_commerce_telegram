package com.fowlart.main;

import com.fowlart.main.state.State;
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

    public static InlineKeyboardMarkup buildReplyMainMenuKeyboardMenu() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("1", State.CATALOG.name()));
        rowInline.add(buildButton("2", State.DELIVERY.name()));
        rowInline.add(buildButton("3", State.DEBT.name()));

        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
