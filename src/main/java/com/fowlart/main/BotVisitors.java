package com.fowlart.main;

import com.google.common.collect.Maps;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Map;

@Component
@Scope("singleton")
public class BotVisitors {

    private Map<Long,User> userMap = Maps.newHashMap();

    public Map<Long, User> getUserMap() {
        return userMap;
    }
}
