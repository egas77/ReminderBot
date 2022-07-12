package ru.egas77.tgbot.finaltgspringboot.anotations;

import ru.egas77.tgbot.finaltgspringboot.bot.state.State;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    String command();
    State state() default State.ALL_STATE;
}
