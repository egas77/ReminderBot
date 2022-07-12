package ru.egas77.tgbot.finaltgspringboot.bot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface HandlerInterface {
    long getChatId(Update update);
}
