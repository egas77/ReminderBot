package ru.egas77.tgbot.finaltgspringboot.services;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.egas77.tgbot.finaltgspringboot.bot.Bot;
import ru.egas77.tgbot.finaltgspringboot.models.Post;
import ru.egas77.tgbot.finaltgspringboot.models.User;
import ru.egas77.tgbot.finaltgspringboot.repository.PostRepository;
import ru.egas77.tgbot.finaltgspringboot.repository.UserRepository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import java.util.List;


@EnableScheduling
@Service
@Transactional
public class SchedulingService {
    private final Bot bot;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final EntityManager entityManager;
    private final DefaultMailService mailService;
    private final JavaMailSender javaMailSender;

    @Autowired
    SchedulingService(Bot bot, UserRepository userRepository,
                      PostRepository postRepository, EntityManager entityManager,
                      JavaMailSender javaMailSender, DefaultMailService mailService) {
        this.bot = bot;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.entityManager = entityManager;
        this.javaMailSender = javaMailSender;
        this.mailService = mailService;
    }

    private void sendMail(User user, Post post) throws TelegramApiException {
        try {
            MimeMessage simpleMailMessage = mailService.getMailForUser(user, post);
            javaMailSender.send(simpleMailMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
            user.setNofemail(false);

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(user.getTgid());
            sendMessage.setText("Я не смог отправить вам письмо с напоминанием на электронную почту. " +
                    "Проверьте настройки. Уведомления на почту я пока отключил, чтобы не нагружать свой " +
                    "любимый сервер.");
            bot.execute(sendMessage);
        }

    }

    private void sendTg(User user, Post post) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode(ParseMode.HTML);
        sendMessage.setChatId(user.getTgid());
        sendMessage.setText(post.getPostView(user));
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 1000 * 15)
    public void nof() {
        boolean isRunning = true;
        if (!isRunning) return;
        List<User> users = userRepository.findAll();
        boolean isFlush = false;
        if (!users.isEmpty()) {
            for (User user : users) {
                boolean isNof = false;
                List<Post> posts = user.getPosts();
                if (!posts.isEmpty()) {
                    for (Post post : posts) {
                        DateTime dateTimePost = new DateTime(post.getDatecreated());
                        if (dateTimePost.isBeforeNow()) {
                            try {
                                SendMessage sendMessage = new SendMessage();
                                sendMessage.setParseMode(ParseMode.HTML);
                                sendMessage.setChatId(user.getTgid());
                                if (!isNof) {
                                    sendMessage.setText("Вы просили напомнить");
                                    bot.execute(sendMessage);
                                    isNof = true;
                                }
                                sendTg(user, post);
                                if (user.isNofemail() && user.getEmail() != null) {
                                    sendMail(user, post);
                                }
                                postRepository.delete(post);
                                isFlush = true;
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        if (isFlush) {
            entityManager.flush();
        }
    }
}
