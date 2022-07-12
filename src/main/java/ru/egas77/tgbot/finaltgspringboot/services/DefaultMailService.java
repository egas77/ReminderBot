package ru.egas77.tgbot.finaltgspringboot.services;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.egas77.tgbot.finaltgspringboot.models.Post;
import ru.egas77.tgbot.finaltgspringboot.models.User;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Service
public class DefaultMailService {
    @Value("${mail.username}")
    private String from;

    private final JavaMailSender javaMailSender;

    DefaultMailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public @NotNull MimeMessage getMailForUser(@NotNull User user, @NotNull Post post) throws MessagingException {
        System.out.println(from);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());
        mimeMessageHelper.setFrom(from);
        mimeMessageHelper.setTo(user.getEmail());
        mimeMessageHelper.setSubject("Вы просили напомнить: " + post.getTitle());
        mimeMessageHelper.setText(post.getPostView(user).replace("\n", "<br>"), true);
        return mimeMessage;
    }
}
