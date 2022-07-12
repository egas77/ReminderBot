package ru.egas77.tgbot.finaltgspringboot.bot.handler;

import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class HandlerBase implements HandlerInterface {
    public long getChatId(Update update) {
        return update.getMessage().getChatId();
    }

    protected Object getTypeMessage(Update update) {
        Message message = update.getMessage();
        if (message.hasText()) {
            return String.class;
        } else if (message.hasLocation()) {
            return Location.class;
        } else if (message.hasDocument()) {
            return Document.class;
        }
        return message.getText().getClass();
    }
}
