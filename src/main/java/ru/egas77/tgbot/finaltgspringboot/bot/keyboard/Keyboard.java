package ru.egas77.tgbot.finaltgspringboot.bot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.egas77.tgbot.finaltgspringboot.anotations.Button;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ru.egas77.tgbot.finaltgspringboot.anotations.Keyboard
public class Keyboard {
    public final static ReplyKeyboardMarkup startKeyboard = new ReplyKeyboardMarkup();
    public final static ReplyKeyboardMarkup listKeyboardEnableRemoveKeyboard = new ReplyKeyboardMarkup();
    public final static ReplyKeyboardMarkup listKeyboardDisableRemoveKeyboard = new ReplyKeyboardMarkup();
    public final static ReplyKeyboardMarkup settingsKeyboard = new ReplyKeyboardMarkup();
    public final static ReplyKeyboardMarkup settingsEmailKeyboard = new ReplyKeyboardMarkup();
    public final static ReplyKeyboardMarkup timezoneKeyboard = new ReplyKeyboardMarkup();
    public final static ReplyKeyboardMarkup dialogKeyboard = new ReplyKeyboardMarkup();
    public final static ReplyKeyboardRemove removeKeyboard = new ReplyKeyboardRemove(true);

    @Button(buttonName = "createRecord")
    public static final KeyboardButton createRecordBtn = new KeyboardButton("Создать запись");
    @Button(buttonName = "listPosts")
    public static final KeyboardButton listRecordsBtn = new KeyboardButton("Мои напоминалки");
    @Button(buttonName = "enableRemoveNof")
    public static final KeyboardButton enableRemoveNofBtn = new KeyboardButton("Удалить напоминалки");
    @Button(buttonName = "disableRemoveNof")
    public static final KeyboardButton disableRemoveNofBtn = new KeyboardButton("Обновить напоминалки");
    @Button(buttonName = "settings")
    public static final KeyboardButton settingsBtn = new KeyboardButton("Настройки");

    @Button(buttonName = "timezoneSetting")
    public static final KeyboardButton timezoneSettingBtn = new KeyboardButton("Часовой пояс");
    @Button(buttonName = "emailSetting")
    public static final KeyboardButton emailSettingBtn = new KeyboardButton("Электронная почта");

    @Button(buttonName = "requestLocation")
    public static final KeyboardButton requestLocationBtn = new KeyboardButton(
            "Автоматическое местоположение",
            null, true, null, null);

    @Button(buttonName = "enableEmailNof")
    public static final KeyboardButton enableEmailNofBtn = new KeyboardButton("Включить уведомления на почту");

    @Button(buttonName = "disableEmailNof")
    public static final KeyboardButton disableEmailNofBtn = new KeyboardButton("Отключить уведомления на почту");

    @Button(buttonName = "dialogOk")
    public static final KeyboardButton dialogOkBtn = new KeyboardButton("Да");

    @Button(buttonName = "dialogNo")
    public static final KeyboardButton dialogNoBtn = new KeyboardButton("Нет");

    private static final Map<String, String> keyboardButtonsNames = new HashMap<>();


    static {
        for (Field field : Keyboard.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(Button.class)) {
                field.setAccessible(true);
                try {
                    KeyboardButton fieldValue = (KeyboardButton) field.get(new KeyboardButton());
                    Button button = field.getAnnotation(Button.class);
                    if (button.buttonName().isEmpty()) {
                        keyboardButtonsNames.put(fieldValue.getText(), field.getName());
                    } else {
                        keyboardButtonsNames.put(fieldValue.getText(),
                                field.getAnnotation(Button.class).buttonName());
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    Keyboard() {
        initStartKeyboard();
        initSettingsKeyboard();
        initTimezoneKeyboard();
        initDialogKeyboard();
        initSettingsEmailKeyboard();
        initListKeyboardEnableRemoveKeyboard();
        initListKeyboardDisableRemoveKeyboard();
    }

    private void initListKeyboardEnableRemoveKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(enableRemoveNofBtn);
        keyboardRows.add(keyboardRow);
        listKeyboardEnableRemoveKeyboard.setKeyboard(keyboardRows);
        listKeyboardEnableRemoveKeyboard.setResizeKeyboard(true);
    }

    private void initListKeyboardDisableRemoveKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(disableRemoveNofBtn);
        keyboardRows.add(keyboardRow);
        listKeyboardDisableRemoveKeyboard.setKeyboard(keyboardRows);
        listKeyboardDisableRemoveKeyboard.setResizeKeyboard(true);
    }

    private void initStartKeyboard() {
        startKeyboard.setResizeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();

        keyboardRow.add(createRecordBtn);
        keyboardRows.add((KeyboardRow) keyboardRow.clone());
        keyboardRow.clear();

        keyboardRow.add(listRecordsBtn);
        keyboardRows.add((KeyboardRow) keyboardRow.clone());
        keyboardRow.clear();

        keyboardRow.add(settingsBtn);
        keyboardRows.add((KeyboardRow) keyboardRow.clone());
        keyboardRow.clear();

        startKeyboard.setKeyboard(keyboardRows);
    }

    private void initSettingsKeyboard() {
        settingsKeyboard.setResizeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();

        keyboardRow.add(timezoneSettingBtn);
        keyboardRow.add(emailSettingBtn);
        keyboardRows.add((KeyboardRow) keyboardRow.clone());
        keyboardRow.clear();

        settingsKeyboard.setKeyboard(keyboardRows);
    }

    private void initSettingsEmailKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();

        keyboardRow.add(enableEmailNofBtn);
        keyboardRows.add((KeyboardRow) keyboardRow.clone());
        keyboardRow.clear();

        keyboardRow.add(disableEmailNofBtn);
        keyboardRows.add((KeyboardRow) keyboardRow.clone());
        keyboardRow.clear();

        settingsEmailKeyboard.setKeyboard(keyboardRows);
        settingsEmailKeyboard.setResizeKeyboard(true);

    }

    private void initTimezoneKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(requestLocationBtn);
        keyboardRows.add(keyboardRow);
        timezoneKeyboard.setKeyboard(keyboardRows);
        timezoneKeyboard.setResizeKeyboard(true);
    }

    private void initDialogKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(dialogNoBtn);
        keyboardRow.add(dialogOkBtn);
        keyboardRows.add(keyboardRow);
        dialogKeyboard.setKeyboard(keyboardRows);
        dialogKeyboard.setResizeKeyboard(true);
    }

    public boolean isButtonClick(Update update) {
        String messageText = update.getMessage().getText();
        return keyboardButtonsNames.containsKey(messageText);
    }

    public String getButtonId(Update update) {
        String messageText = update.getMessage().getText();
        return keyboardButtonsNames.get(messageText);
    }
}
