package ru.egas77.tgbot.finaltgspringboot.config;

import lombok.Data;
import net.iakovlev.timeshape.TimeZoneEngine;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatDescription;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.egas77.tgbot.finaltgspringboot.bot.Bot;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "bot")
public class BotConfig {
    private String token;
    private String username;
    private String path;


    @Bean
    public Bot bot() throws TelegramApiException {
        Bot bot = new Bot();
        bot.setBotToken(token);
        bot.setBotUserName(username);
        bot.setBotPath(path);

        SetMyCommands setMyCommands = new SetMyCommands();
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "Запустить бота"));
        commands.add(new BotCommand("/stop", "Отмена действия"));
        commands.add(new BotCommand("/help", "Справка"));
        setMyCommands.setCommands(commands);
        bot.execute(setMyCommands);

        return bot;
    }

    @Bean
    public TimeZoneEngine timeZoneEngine() {
        return TimeZoneEngine.initialize();
    }
}
