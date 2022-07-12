package ru.egas77.tgbot.finaltgspringboot.bot.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.iakovlev.timeshape.TimeZoneEngine;
import org.joda.time.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.egas77.tgbot.finaltgspringboot.anotations.CheckMessage;
import ru.egas77.tgbot.finaltgspringboot.bot.keyboard.Keyboard;
import ru.egas77.tgbot.finaltgspringboot.bot.state.State;
import ru.egas77.tgbot.finaltgspringboot.bot.state.StateManager;
import ru.egas77.tgbot.finaltgspringboot.models.Post;
import ru.egas77.tgbot.finaltgspringboot.models.User;
import ru.egas77.tgbot.finaltgspringboot.repository.PostRepository;
import ru.egas77.tgbot.finaltgspringboot.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@EnableScheduling
@Slf4j
public class MessageHandler extends HandlerBase {
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private final EntityManager entityManager;
    private final StateManager stateManager;

    private final TimeZoneEngine timeZoneEngine;

    private final static String TIME_ZONE_PREFIX = "Etc/GMT";
    private final static HashMap<Integer, String> months =
            new HashMap<>() {{
                put(1, "Янаврь");
                put(2, "Февраль");
                put(3, "Март");
                put(4, "Апрель");
                put(5, "Май");
                put(6, "Июнь");
                put(7, "Июль");
                put(8, "Август");
                put(9, "Сентябрь");
                put(10, "Октябрь");
                put(11, "Ноябрь");
                put(12, "Декабрь");
            }};
    private static final Pattern timeRegexPattern = Pattern.compile("\\d\\d\\.\\d\\d");


    @Autowired
    MessageHandler(UserRepository userRepository, PostRepository postRepository, StateManager stateManager,
                   EntityManager entityManager, TimeZoneEngine timeZoneEngine) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.stateManager = stateManager;
        this.entityManager = entityManager;
        this.timeZoneEngine = timeZoneEngine;


    }

    @CheckMessage(state = State.START_STATE, button = "createRecord")
    public SendMessage createRecord(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Введите заголовок");
        sendMessage.setReplyMarkup(Keyboard.removeKeyboard);
        stateManager.setStateUser(getChatId(update), State.SET_TITLE);
        return sendMessage;
    }

    @CheckMessage(state = State.SET_TITLE)
    public SendMessage setTitle(Update update) {
        String title = update.getMessage().getText();
        stateManager.setCacheUser(getChatId(update), "title", title);
        stateManager.setStateUser(getChatId(update), State.SET_DESCRIPTION);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Заголовок <b>" + title + "</b> установлен! \nВведите содержание");
        return sendMessage;
    }

    @CheckMessage(state = State.SET_DESCRIPTION)
    public SendMessage setDescription(Update update) {
        SendMessage sendMessage = new SendMessage();
        String description = update.getMessage().getText();
        stateManager.setStateUser(getChatId(update), State.SET_MONTH);
        stateManager.setCacheUser(getChatId(update), "description", description);
        StringBuilder answerText = new StringBuilder();
        answerText.append("Содержание добавлено. Выберете месяц (введите его номер), когда нужно напомнить\n");
        for (int monthNum : months.keySet()) {
            answerText.append(monthNum).append(" - ").append(months.get(monthNum)).append("\n");
        }
        answerText.append("\n0 - Текущий месяц");
        sendMessage.setText(answerText.toString());
        return sendMessage;
    }

    @CheckMessage(state = State.SET_MONTH)
    public SendMessage setMonth(Update update) {
        SendMessage sendMessage = new SendMessage();
        String messageText = update.getMessage().getText().trim();
        try {
            int mouthNumber = Integer.parseInt(messageText);
            if (months.containsKey(mouthNumber) || mouthNumber == 0) {
                DateTime dateTimeNow = DateTime.now();
                DateTime dateTimeNowUser = dateTimeNow.withZone(
                        DateTimeZone.forTimeZone(userRepository.getBytgid(getChatId(update)).getTimeZone()));
                if (mouthNumber == 0) {
                    mouthNumber = dateTimeNowUser.getMonthOfYear();
                }
                int maxDay = YearMonth.of(dateTimeNowUser.getYear(), mouthNumber).lengthOfMonth();
                stateManager.setCacheUser(getChatId(update), "month", Integer.toString(mouthNumber));
                stateManager.setCacheUser(getChatId(update), "maxDay", Integer.toString(maxDay));
                stateManager.setStateUser(getChatId(update), State.SET_DAY);

                String text = "Введите день месяца. Дней в месяце \"" +
                        months.get(mouthNumber) + "\" - " + maxDay + "\n";
                if (mouthNumber == dateTimeNowUser.getMonthOfYear()) {
                    text += "Чтобы поставить напоминание на сегодня, введите 0";
                    stateManager.setCacheUser(getChatId(update), "isNowMonth", "1");
                }
                sendMessage.setText(text.trim());
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sendMessage.setText("Неверный месяц");
        }
        return sendMessage;
    }

    @CheckMessage(state = State.SET_DAY)
    public SendMessage setDay(Update update) {
        SendMessage sendMessage = new SendMessage();
        String textMessage = update.getMessage().getText();
        int maxDay = Integer.parseInt(stateManager.getCacheUser(getChatId(update)).get("maxDay"));
        try {
            int day = Integer.parseInt(textMessage);
            User user = userRepository.getBytgid(getChatId(update));
            HashMap<String, String> cacheUser = stateManager.getCacheUser(getChatId(update));
            if (day == 0 && cacheUser.containsKey("isNowMonth")) {
                DateTime dateTimeNow = DateTime.now();
                DateTime dateTimeNowUser = dateTimeNow.withZone(
                        DateTimeZone.forTimeZone(user.getTimeZone()));
                System.out.println(dateTimeNowUser.getDayOfMonth());
                day = dateTimeNowUser.getDayOfMonth();
            }
            if (day >= 1 && day <= maxDay) {
                stateManager.setCacheUser(getChatId(update), "day", Integer.toString(day));
                stateManager.setStateUser(getChatId(update), State.SET_TIME);
                sendMessage.setText("Число установлено. Введите время напоминания в формате \"hh.mm\"");
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sendMessage.setText("Неверная дата");
        }
        return sendMessage;
    }

    @CheckMessage(state = State.SET_TIME)
    public SendMessage setTime(Update update) {
        SendMessage sendMessage = new SendMessage();
        String textMessage = update.getMessage().getText().trim();
        Matcher matcherTime = timeRegexPattern.matcher(textMessage);
        try {
            if (!matcherTime.matches()) {
                sendMessage.setText("Неверный формат времени");
                throw new IllegalFieldValueException("Error", "Error");
            } else {
                String time = matcherTime.group(0);
                String[] timeSplit = time.split("\\.");
                int hour = Integer.parseInt(Arrays.stream(timeSplit).toList().get(0));
                int minute = Integer.parseInt(Arrays.stream(timeSplit).toList().get(1));
                HashMap<String, String> userCache = stateManager.getCacheUser(getChatId(update));
                int mouth = Integer.parseInt(userCache.get("month"));
                int day = Integer.parseInt(userCache.get("day"));
                int year = DateTime.now().getYear();
                User user = userRepository.getBytgid(getChatId(update));
                String title = userCache.get("title");
                String description = userCache.get("description");
                DateTime dateTime = new DateTime(year, mouth, day, hour, minute,
                        DateTimeZone.forTimeZone(user.getTimeZone()));

                Post post = new Post();
                post.setTitle(title);
                post.setData(description);
                post.setUser(userRepository.getBytgid(getChatId(update)));
                post.setTimeForUser(user, dateTime);

                entityManager.persist(post);

                sendMessage.setText("Напоминание устанолено на " +
                        dateTime.toString(post.getStringTimeForUser(user)) + " местного времени");
                sendMessage.setReplyMarkup(Keyboard.startKeyboard);
                stateManager.setStateUser(getChatId(update), State.START_STATE);
                stateManager.clearCacheUser(getChatId(update));
            }
        } catch (IllegalFieldValueException e) {
            sendMessage.setText("Неверный формат времени");
        }
        return sendMessage;
    }

    @CheckMessage(state = State.START_STATE, button = "listPosts")
    public SendMessage getAllPosts(Update update) {
        SendMessage sendMessage = new SendMessage();

        User user = userRepository.getBytgid(getChatId(update));
        List<Post> posts = postRepository.getPostsByUserOrderByDatecreated(user);
        if (!posts.isEmpty()) {
            StringBuilder postsBuilder = new StringBuilder();
            HashMap<Long, Long> postsMapCountToId = new HashMap<>();
            for (int i = 0; i < posts.size(); i++) {
                Post post = posts.get(i);
                postsBuilder.append(post.getPostView(i + 1, user)).append("\n");
                postsMapCountToId.put((long) i + 1, post.getId());
            }
            try {
                stateManager.setCacheUser(user.getTgid(), "posts",
                        new ObjectMapper().writeValueAsString(postsMapCountToId));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
            sendMessage.setText(postsBuilder.toString().trim());
            sendMessage.setReplyMarkup(Keyboard.listKeyboardEnableRemoveKeyboard);
            stateManager.setStateUser(user.getTgid(), State.LIST_POSTS_STATE);
        } else {
            sendMessage.setText("Напоминалки не найдены");
        }
        return sendMessage;
    }

    @CheckMessage(state = State.LIST_POSTS_STATE, button = "enableRemoveNof")
    public SendMessage enableRemoveNof(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Тут вы можете указывать номера напоминалок, которые хотите удалить. " +
                "Можете указывать в одном сообщении несколько номеров, разделив их пробелом");
        stateManager.setStateUser(getChatId(update), State.REMOVE_POSTS_STATE);
        sendMessage.setReplyMarkup(Keyboard.listKeyboardDisableRemoveKeyboard);
        return sendMessage;
    }

    @CheckMessage(state = State.REMOVE_POSTS_STATE, button = "disableRemoveNof")
    public SendMessage disableRemoveNof(Update update) {
        stateManager.setStateUser(getChatId(update), State.START_STATE);
        User user = userRepository.getBytgid(getChatId(update));
        List<Post> posts = user.getPosts();
        if (posts.isEmpty()) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText("Напоминалок больше нет. Завидую, хоть я и робот");
            sendMessage.setReplyMarkup(Keyboard.startKeyboard);
            stateManager.setStateUser(getChatId(update), State.START_STATE);
            stateManager.clearCacheUser(getChatId(update));
            return sendMessage;
        } else {
            return getAllPosts(update);
        }
    }

    @CheckMessage(state = State.REMOVE_POSTS_STATE)
    public SendMessage removeNof(Update update) {
        HashMap<String, String> cacheUser = stateManager.getCacheUser(getChatId(update));
        TypeReference<HashMap<Long, Long>> typeReference = new TypeReference<>() {};
        HashMap<Long, Long> posts;
        try {
            posts = new ObjectMapper().readValue(cacheUser.get("posts"),  typeReference);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        SendMessage sendMessage = new SendMessage();
        String text = update.getMessage().getText();
        List<Integer> postsIndex = new ArrayList<>();
        Pattern pattern = Pattern.compile("[1-9]\\d\\d\\d|[1-9]\\d\\d|[1-9]\\d|[1-9]");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            postsIndex.add(Integer.valueOf(text.substring(start, end)));
        }
        List<String> postsTitles = new ArrayList<>();
        for (long postIndex: postsIndex) {
            if (posts.containsKey(postIndex)) {
                Optional<Post> postOptional = postRepository.findById(posts.get(postIndex));
                if (postOptional.isPresent()) {
                    Post post = postOptional.get();
                    postsTitles.add(post.getTitle());
                    postRepository.delete(post);
                }
            }
        }
        StringBuilder textAnswer = new StringBuilder();
        if (!postsTitles.isEmpty()) {
            postRepository.flush();
            textAnswer.append("Удалены следующие напоминалки:\n");
            for (int i = 0; i < postsTitles.size(); i++) {
                textAnswer.append(i + 1).append(". <b>").append(postsTitles.get(i)).append("</b>\n");
            }
        } else {
            textAnswer.append("Не нашлось напоминалок, оторые вы хотите удалить");
        }
        sendMessage.setText(textAnswer.toString());
        return sendMessage;
    }

    @CheckMessage(state = State.START_STATE, button = "settings")
    public SendMessage settings(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Настройки");
        sendMessage.setReplyMarkup(Keyboard.settingsKeyboard);
        stateManager.setStateUser(getChatId(update), State.SETTINGS_STATE);
        return sendMessage;
    }

    @CheckMessage(state = State.SETTINGS_STATE, button = "timezoneSetting")
    public SendMessage setTimezone(Update update) {
        SendMessage sendMessage = new SendMessage();
        User user = userRepository.getBytgid(getChatId(update));
        sendMessage.setText("""
                Настройка часового пояса
                Можете ввести смещение вашего часового пояса (к примеру -3 - Москва), либо воспользоваться автоматической настройкой, нажав на кнопку ниже.
                Сейчас у вас установлен следующий часовой пояс: - <b>""" + user.getTimeZone().getDisplayName() + "</b>");
        sendMessage.setReplyMarkup(Keyboard.timezoneKeyboard);
        stateManager.setStateUser(getChatId(update), State.SETTING_TIMEZONE);
        return sendMessage;
    }

    @CheckMessage(state = State.SETTING_TIMEZONE, dataType = {Location.class, String.class})
    public SendMessage setTimezoneAuto(Update update) {
        DateTimeZone timeZone = null;
        SendMessage sendMessage = new SendMessage();
        if (getTypeMessage(update).equals(Location.class)) {
            Location location = update.getMessage().getLocation();
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            Optional<ZoneId> timeZoneOpt = timeZoneEngine.query(latitude, longitude);
            if (timeZoneOpt.isPresent()) {
                ZoneId zoneId = timeZoneOpt.get();
                timeZone = DateTimeZone.forID(String.valueOf(zoneId));
            }

        } else if (getTypeMessage(update).equals(String.class)) {
            timeZone = DateTimeZone.forID(TIME_ZONE_PREFIX + update.getMessage().getText().trim());
        }
        if (timeZone != null) {
            User user = userRepository.getBytgid(getChatId(update));
            DateTimeZone dateTimeZoneOld = DateTimeZone.forTimeZone(user.getTimeZone());
            stateManager.setCacheUser(getChatId(update), "oldTimeZone", dateTimeZoneOld.getID());
            List<Post> posts = postRepository.getPostsByUserOrderByDatecreated(user);
            if (!posts.isEmpty()) {
                log.info("Found posts for user " + user.getId());
                sendMessage.setText("Скорректировать время постов " +
                        "(время публикации останется такуим же, но для нового часового пояса)?");
                sendMessage.setReplyMarkup(Keyboard.dialogKeyboard);
                stateManager.setStateUser(getChatId(update), State.CORRECT_TIMEZONE_POST);
            } else {
                sendMessage.setReplyMarkup(Keyboard.startKeyboard);
                stateManager.clearCacheUser(getChatId(update));
                stateManager.setStateUser(getChatId(update), State.START_STATE);
                DateTime userDateTime = DateTime.now().withZone(timeZone);
                sendMessage.setText("Зона " + timeZone.toTimeZone().getDisplayName() + " установлена.\n" +
                        "Сейчас у вас " + userDateTime.toString(Post.dateTimeFormat));
            }
            user.setTimeZone(timeZone.toTimeZone());
            userRepository.flush();
        } else {
            sendMessage.setReplyMarkup(Keyboard.startKeyboard);
            stateManager.clearCacheUser(getChatId(update));
            stateManager.setStateUser(getChatId(update), State.START_STATE);
            sendMessage.setText("Не удалось определить зону");
        }
        return sendMessage;
    }

    @CheckMessage(state = State.CORRECT_TIMEZONE_POST, button = "dialogOk")
    public SendMessage correctTimezonePostOk(Update update) {
        long chatId = getChatId(update);
        User user = userRepository.getBytgid(chatId);
        DateTimeZone dateTimeZoneOld = DateTimeZone.forID(stateManager.getCacheUser(chatId).get("oldTimeZone"));
        System.out.println(dateTimeZoneOld);
        DateTimeZone timeZone = DateTimeZone.forTimeZone(user.getTimeZone());
        List<Post> posts = user.getPosts();
        for (Post post : posts) {
            post.convertToNewTimeZone(dateTimeZoneOld, timeZone);
        }
        postRepository.flush();
        SendMessage sendMessage = correctTimezonePostNo(update);
        String text = sendMessage.getText();
        text += "\nВремя постов скооректировано под новый часовой пояс";
        sendMessage.setText(text);
        return sendMessage;
    }

    @CheckMessage(state = State.CORRECT_TIMEZONE_POST, button = "dialogNo")
    public SendMessage correctTimezonePostNo(Update update) {
        SendMessage sendMessage = new SendMessage();
        DateTimeZone timeZone = DateTimeZone.forTimeZone(userRepository.getBytgid(getChatId(update)).getTimeZone());
        DateTime userDateTime = DateTime.now().withZone(timeZone);
        sendMessage.setText("Зона " + timeZone.getID() + " установлена.\n" +
                "Сейчас у вас " + userDateTime.toString(Post.dateTimeFormat));
        sendMessage.setReplyMarkup(Keyboard.startKeyboard);
        stateManager.clearCacheUser(getChatId(update));
        stateManager.setStateUser(getChatId(update), State.START_STATE);
        return sendMessage;
    }


    @CheckMessage(state = State.SETTINGS_STATE, button = "emailSetting")
    public SendMessage settingEmail(Update update) {
        SendMessage sendMessage = new SendMessage();
        stateManager.setStateUser(getChatId(update), State.SETTING_EMAIL);
        User user = userRepository.getBytgid(getChatId(update));
        String email = user.getEmail();
        String text = "Тут вы можете установить вашу электронную почту. ";
        text += "Также тут вы можете включить или выключить уедомления на вашу почту, если она установлен\n";
        if (email == null) {
            text += "Сейчас у вас не установлена почта. Вы мотете просто отправить мне ее сейчас, и я ее запишу\n";
        } else {
            text += "Ваша почта: <code>" + email + "</code>.\n" +
                    "Если хотите сменить, то просто отправьте мне новый адрес\n";
        }
        if (user.isNofemail()) {
            text += "Сейчас у вас <b>включены</b> уведомления на почту";
        } else {
            text += "Сейчас у вас <b>выключены</b> уведомления на почту";
        }
        sendMessage.setText(text.strip());
        sendMessage.setReplyMarkup(Keyboard.settingsEmailKeyboard);
        return sendMessage;
    }

    @CheckMessage(state = State.SETTING_EMAIL)
    public SendMessage setEmail(Update update) {
        SendMessage sendMessage = new SendMessage();
        String email = update.getMessage().getText().trim();
        User user = userRepository.getBytgid(getChatId(update));
        user.setEmail(email);
        try {
            entityManager.flush();
            sendMessage.setText("Почта <b>" + email + "</b> установлена");
            sendMessage.setReplyMarkup(Keyboard.startKeyboard);
            stateManager.setStateUser(getChatId(update), State.START_STATE);
            stateManager.clearCacheUser(getChatId(update));
        } catch (ConstraintViolationException e) {
            log.warn("No valid email format");
            sendMessage.setText("Неверный формат электронной почты");
        }
        return sendMessage;
    }

    @CheckMessage(state = State.SETTING_EMAIL, button = "enableEmailNof")
    public SendMessage enableEmailNof(Update update) {
        SendMessage sendMessage = new SendMessage();
        User user = userRepository.getBytgid(getChatId(update));
        user.setNofemail(true);
        sendMessage.setText("Уведомления включены");
        stateManager.clearCacheUser(getChatId(update));
        stateManager.setStateUser(getChatId(update), State.START_STATE);
        sendMessage.setReplyMarkup(Keyboard.startKeyboard);
        return sendMessage;
    }

    @CheckMessage(state = State.SETTING_EMAIL, button = "disableEmailNof")
    public SendMessage disableEmailNof(Update update) {
        SendMessage sendMessage = new SendMessage();
        User user = userRepository.getBytgid(getChatId(update));
        user.setNofemail(false);
        sendMessage.setText("Уведомления отключены");
        stateManager.clearCacheUser(getChatId(update));
        stateManager.setStateUser(getChatId(update), State.START_STATE);
        sendMessage.setReplyMarkup(Keyboard.startKeyboard);
        return sendMessage;
    }
}
