package ru.egas77.tgbot.finaltgspringboot.bot.state;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j(topic = "State Manager")
public class StateManager {
    private final ConcurrentHashMap<Long, State> states;

    private final ConcurrentHashMap<Long, HashMap<String, String>> userCache;

    StateManager() {
        states = new ConcurrentHashMap<>();
        userCache = new ConcurrentHashMap<>();
    }

    public State getStateUser(long chatId) {
        if (!states.containsKey(chatId)) {
            states.put(chatId, State.START_STATE);
            return State.START_STATE;
        }
        return states.get(chatId);
    }

    public void setStateUser(Long chatId, State state) {
        log.info("New state for user:" + chatId + ", " + state);
        states.put(chatId, state);
    }

    public HashMap<String, String> getCacheUser(long chatId) {
        if (!userCache.containsKey(chatId)) {
            this.initCacheUser(chatId);
        }
        HashMap<String, String> cache = userCache.get(chatId);
        log.info("Cache for user " + chatId + ": " + cache);
        return cache;
    }

    public void setCacheUser(long chatId, String key, String value) {
        log.info("Set cache for " + chatId + ": " + key + "=" + value);
        HashMap<String, String> cache = userCache.get(chatId);
        System.out.println(cache.toString());
        cache.put(key, value);
        userCache.put(chatId, cache);
        System.out.println(cache);
    }

    public void setCacheUser(long chatId, HashMap<String, String> newCache) {
        HashMap<String, String> cache = userCache.get(chatId);
        for (String key : newCache.keySet()) {
            cache.put(key, newCache.get(key));
        }
        userCache.put(chatId, cache);

    }

    public void clearCacheUser(long chatId) {
        log.info("Clear cache for user " + chatId);
        HashMap<String, String> cache = userCache.get(chatId);
        cache.clear();
        userCache.put(chatId, cache);
    }

    public void initCacheUser(long chatId) {
        log.info("Init cache for " + chatId);
        userCache.put(chatId, new HashMap<>());
    }

    public boolean isCacheUser(long chatId) {
        return userCache.containsKey(chatId);
    }
}
