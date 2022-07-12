package ru.egas77.tgbot.finaltgspringboot.anotations;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.egas77.tgbot.finaltgspringboot.bot.state.State;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public @interface CheckMessage {
    State state() default State.ALL_STATE;
    String text() default "";
    String button() default "";
    Class<?>[] dataType() default {String.class};
}
