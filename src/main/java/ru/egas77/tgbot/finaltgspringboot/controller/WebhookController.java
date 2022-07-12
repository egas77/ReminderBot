package ru.egas77.tgbot.finaltgspringboot.controller;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.egas77.tgbot.finaltgspringboot.bot.Bot;
import ru.egas77.tgbot.finaltgspringboot.bot.state.StateManager;
import ru.egas77.tgbot.finaltgspringboot.models.User;
import ru.egas77.tgbot.finaltgspringboot.repository.UserRepository;

import java.util.Objects;
import java.util.Optional;


@RestController
@Slf4j
public class WebhookController {
    private final Bot bot;
    private final UserRepository userRepository;
    private final StateManager stateManager;
    private final JavaMailSenderImpl javaMailSender;

    @Autowired
    WebhookController(Bot bot, UserRepository userRepository, StateManager stateManager,
                      JavaMailSenderImpl javaMailSender) {
        this.bot = bot;
        this.userRepository = userRepository;
        this.stateManager = stateManager;
        this.javaMailSender = javaMailSender;
    }

    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    private BotApiMethod<?> updateBotRequest(@RequestBody Update update) {
        org.telegram.telegrambots.meta.api.objects.User user =
                update.getMessage().getFrom();
        long userid = user.getId();

        long chatId = update.getMessage().getChatId();

        if (userRepository.getBytgid(userid) == null) {
            ru.egas77.tgbot.finaltgspringboot.models.User tgUser
                    = new ru.egas77.tgbot.finaltgspringboot.models.User();
            tgUser.setTgid(userid);
            userRepository.save(tgUser);
            userRepository.flush();
            log.info("Create new user" + tgUser);
        } else {
            ru.egas77.tgbot.finaltgspringboot.models.User tgUser = userRepository.getBytgid(userid);
            if (tgUser.isIsblock()) {
                return null;
            }
        }
        if (!stateManager.isCacheUser(chatId)) {
            stateManager.initCacheUser(chatId);
        }
        return bot.onWebhookUpdateReceived(update);
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    private String test() {
        return "Server tg bot is active<br>" +
                new LocalDateTime();
    }

    @RequestMapping(value = "/test-email/{email}", method = RequestMethod.GET)
    private String testEmail(@PathVariable String email) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(Objects.requireNonNull(javaMailSender.getUsername()));
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("Subject");
        simpleMailMessage.setText("Test");
        javaMailSender.send(simpleMailMessage);
        return email;
    }

    @RequestMapping(value = "/check-user/{userId}", method = RequestMethod.GET)
    private String testUser(@PathVariable long userId) {
        Optional<User> userOpt = userRepository.findUserById(userId);
        if (userOpt.isEmpty()) {
            return "User not found";
        }
        User user = userOpt.get();
        DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(user.getTimeZone());
        return user.toString();
    }
}
