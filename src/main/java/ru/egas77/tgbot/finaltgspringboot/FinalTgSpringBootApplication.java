package ru.egas77.tgbot.finaltgspringboot;

import org.joda.time.DateTimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Locale;
import java.util.TimeZone;

@SpringBootApplication
@EnableJpaRepositories(value = "ru.egas77.tgbot.finaltgspringboot.repository")
public class FinalTgSpringBootApplication {
    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("RU"));
        TimeZone.setDefault(DateTimeZone.UTC.toTimeZone());
        DateTimeZone.setDefault(DateTimeZone.UTC);
        SpringApplication.run(FinalTgSpringBootApplication.class, args);
    }

}
