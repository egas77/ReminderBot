package ru.egas77.tgbot.finaltgspringboot.bot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.egas77.tgbot.finaltgspringboot.bot.handler.Handler;

@Getter
@Setter
public class Bot extends TelegramWebhookBot {
    private String botUserName;
    private String botToken;
    private String botPath;
    @Autowired
    private Handler handler;

    public Bot() {
        super();
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return handler.getSendMessage(update);
    }

    @Override
    public String getBotPath() {
        return botPath;
    }
}
