package ru.egas77.tgbot.finaltgspringboot.bot.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.egas77.tgbot.finaltgspringboot.anotations.Command;
import ru.egas77.tgbot.finaltgspringboot.bot.keyboard.Keyboard;
import ru.egas77.tgbot.finaltgspringboot.bot.state.State;
import ru.egas77.tgbot.finaltgspringboot.bot.state.StateManager;

@Service
public class CommandHandler extends HandlerBase {
    private final StateManager stateManager;

    static String startMessageText =
            "Добро пожаловать. Я бот - напоминалка. Вы можете создать какое-нибудь напоминание," +
                    "и когда придет время, я вам напишу. Для начала рекомендую выполнить команду /help," +
                    "чтобы узнать больше информации.";

    static String helpMessageText = """
                В этом боте вы можете устанавливать себе различные напоминания для повседневной жизни.
                Для этого нажмите на кнопку "Создать напоминание" на коавиатуре внизу.
                Чтобы все работало верно, нкжно установить правильный часовой пояс в настройках бота.
                Также вы можете установить свою электронную почту, чтобы напоминания приходили и туда.
                Удачного использования, надеюсь я буду вам полезен.
                """;

    @Autowired
    CommandHandler(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Command(command = "/start")
    public SendMessage start(Update update, Object... args) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(startMessageText);
        sendMessage.setReplyMarkup(Keyboard.startKeyboard);
        stateManager.setStateUser(getChatId(update), State.START_STATE);
        stateManager.clearCacheUser(getChatId(update));
        return sendMessage;
    }

    @Command(command = "/help")
    public SendMessage help(Update update, Object... args) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(helpMessageText);
        return sendMessage;
    }

    @Command(command = "/stop")
    public SendMessage stop(Update update, Object... args) {
        stateManager.clearCacheUser(update.getMessage().getChatId());
        stateManager.setStateUser(update.getMessage().getChatId(), State.START_STATE);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Главное меню");
        sendMessage.setReplyMarkup(Keyboard.startKeyboard);
        return sendMessage;
    }

    @Command(command = "/settings")
    public SendMessage settings(Update update, Object... args) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(Keyboard.settingsKeyboard);
        return sendMessage;
    }
}
