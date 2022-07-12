package ru.egas77.tgbot.finaltgspringboot.bot.handler;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

@Data
@RequiredArgsConstructor
public class MessageSource {
    @NotNull
    private Method method;
    @NotNull
    private Class<?> type;
}
