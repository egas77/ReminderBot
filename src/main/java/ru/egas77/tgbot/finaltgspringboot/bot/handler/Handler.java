package ru.egas77.tgbot.finaltgspringboot.bot.handler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.egas77.tgbot.finaltgspringboot.anotations.Button;
import ru.egas77.tgbot.finaltgspringboot.anotations.CheckMessage;
import ru.egas77.tgbot.finaltgspringboot.anotations.Command;
import ru.egas77.tgbot.finaltgspringboot.bot.keyboard.Keyboard;
import ru.egas77.tgbot.finaltgspringboot.bot.state.State;
import ru.egas77.tgbot.finaltgspringboot.bot.state.StateManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j(topic = "Handler")
@Service
public class Handler extends HandlerBase {
    private final CommandHandler commandHandler;
    private final MessageHandler messageHandler;
    private final StateManager stateManager;
    private final Keyboard keyboard;
    private static final Map<String, List<MessageSource>> commandsHandlersMethods = new HashMap<>();
    private static final Map<State, List<MessageSource>> messagesHandlersMethods = new HashMap<>();
    private static final Map<String, List<MessageSource>> buttonsHandlersMethods = new HashMap<>();

    private static final List<Method> commandHandlers = new ArrayList<>();
    private static final List<Method> messageHandlers = new ArrayList<>();

    static {
        for (Method method : MessageHandler.class.getMethods()) {
            if (method.isAnnotationPresent(CheckMessage.class)) {
                messageHandlers.add(method);
            }
        }
        for (Method method : CommandHandler.class.getMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                commandHandlers.add(method);
            }
        }
    }

    static {
        for (Method method : commandHandlers) {
            ru.egas77.tgbot.finaltgspringboot.anotations.Command command =
                    method.getAnnotation(ru.egas77.tgbot.finaltgspringboot.anotations.Command.class);
            if (!commandsHandlersMethods.containsKey(command.command())) {
                commandsHandlersMethods.put(command.command(), new ArrayList<>());
            }
            commandsHandlersMethods.get(command.command()).add(new MessageSource(method, String.class));
        }
    }

    static {
        for (Method method : messageHandlers) {
            CheckMessage checkMessage = method.getAnnotation(CheckMessage.class);
            State curMessageState = checkMessage.state();
            if (checkMessage.button().isBlank()) {
                if (!messagesHandlersMethods.containsKey(curMessageState)) {
                    messagesHandlersMethods.put(curMessageState, new ArrayList<>());
                }
                for (Class<?> clazz: checkMessage.dataType()) {
                    messagesHandlersMethods.get(curMessageState).add(new MessageSource(method, clazz));
                }
            }
        }
    }

    static {
        String buttonId;
        for (Field field : Keyboard.class.getFields()) {
            if (field.isAnnotationPresent(Button.class)) {
                Button button = field.getAnnotation(Button.class);
                if (button.buttonName().isBlank()) {
                    buttonId = field.getName();
                } else {
                    buttonId = button.buttonName();
                }
                for (Method method : messageHandlers) {
                    CheckMessage checkMessage = method.getAnnotation(CheckMessage.class);
                    if (!checkMessage.button().isBlank() & checkMessage.button().equals(buttonId)) {
                        if (!buttonsHandlersMethods.containsKey(buttonId)) {
                            buttonsHandlersMethods.put(buttonId, new ArrayList<>());
                        }
                        for (Class<?> clazz: checkMessage.dataType()) {
                            buttonsHandlersMethods.get(buttonId).add(new MessageSource(method, clazz));
                        }

                    }
                }
            }
        }
    }

    @Autowired
    Handler(CommandHandler commandHandler, MessageHandler messageHandler, StateManager stateManager,
            Keyboard keyboard) {
        this.commandHandler = commandHandler;
        this.messageHandler = messageHandler;
        this.stateManager = stateManager;
        this.keyboard = keyboard;
    }

    public SendMessage getSendMessage(Update update) {
        if (update.getMessage().hasEntities()) {
            log.info(update.getMessage().getEntities().toString());
        } else {
            log.info("Message not exist Entities");
        }
        SendMessage sendMessage;
        if (!update.hasMessage()) {
            return null;
        }
        long chatId = this.getChatId(update);
        if (update.getMessage().isCommand()) {
            sendMessage = commandHandler(update);
        } else {
            sendMessage = messageHandler(update);
        }
        try {
            if (sendMessage != null) {
                if (!sendMessage.getText().isBlank()) {
                    sendMessage.setChatId(chatId);
                    sendMessage.setParseMode(ParseMode.HTML);
                    return sendMessage;
                }
            }
        } catch (NullPointerException e) {
            log.warn("Error in handler");
        }
        log.warn("Empty message text");
        return null;
    }

    private SendMessage commandHandler(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Данная команда сейчасм не доступна");
        long chatId = getChatId(update);
        State curUserState = stateManager.getStateUser(chatId);
        String textMessage = update.getMessage().getText();
        try {
            String[] args = textMessage.split(" ");
            String command = args[0];
            String[] nArgs = Arrays.copyOfRange(args, 1, args.length);

            List<MessageSource> messageSources = commandsHandlersMethods.get(command);
            for (MessageSource messageSource : messageSources) {
                Command annotation = messageSource.getMethod().getAnnotation(Command.class);
                if (annotation.state().equals(State.ALL_STATE) ||
                        annotation.state().equals(curUserState)) {
                    sendMessage = (SendMessage) messageSource.getMethod().invoke(commandHandler, update, nArgs);
                    break;
                }
            }


        } catch (Exception e) {
            sendMessage.setText("Ошибка. Команда не найдена");
            e.printStackTrace();
        }
        return sendMessage;
    }

    protected SendMessage messageHandler(Update update) {
        SendMessage sendMessage = new SendMessage();
        long chatId = getChatId(update);
        State curUserState = stateManager.getStateUser(chatId);
        if (keyboard.isButtonClick(update)) {
            String buttonId = keyboard.getButtonId(update);
            List<MessageSource> messageSources = buttonsHandlersMethods.getOrDefault(buttonId, null);
            if (messageSources == null) {
                sendMessage.setText("Ошибка");
                return sendMessage;
            }
            for (MessageSource messageSource : messageSources) {
                CheckMessage checkMessage = messageSource.getMethod().getAnnotation(CheckMessage.class);
                if (
                        (checkMessage.state().equals(State.ALL_STATE) ||
                                checkMessage.state().equals(curUserState)) &
                                Arrays.asList(checkMessage.dataType()).contains((Class<?>) getTypeMessage(update))
                ) {
                    try {
                        sendMessage = (SendMessage) messageSource.getMethod().invoke(messageHandler, update);
                        break;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }

        } else {
            List<MessageSource> messageSources = messagesHandlersMethods.getOrDefault(curUserState, null);
            if (messageSources == null) {
                sendMessage.setText("Не совсем понял о чем речь)");
            } else {
                for (MessageSource messageSource: messageSources) {
                    CheckMessage checkMessage = messageSource.getMethod().getAnnotation(CheckMessage.class);
                    if (
                            (checkMessage.state().equals(State.ALL_STATE) ||
                                    checkMessage.state().equals(curUserState)) &
                                    Arrays.asList(checkMessage.dataType()).contains((Class<?>) getTypeMessage(update))
                    ) {
                        try {
                            System.out.println(getTypeMessage(update));
                            sendMessage = (SendMessage) messageSource.getMethod().invoke(messageHandler, update);
                            break;
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            sendMessage.setText("Ошибочка вышла");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return sendMessage;
    }
}
