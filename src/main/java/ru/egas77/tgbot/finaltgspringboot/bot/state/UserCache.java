package ru.egas77.tgbot.finaltgspringboot.bot.state;

import java.util.HashMap;

public class UserCache {
    private final HashMap<String, String> cache;

    UserCache() {
        cache = new HashMap<>();
    }

    public void setCacheUser(String key, String value) {
        cache.put(key, value);
    }

    public HashMap<String, String> getCache() {
        return cache;
    }

    public void clear() {
        cache.clear();
    }

    @Override
    public String toString() {
        return cache.toString();
    }
}
