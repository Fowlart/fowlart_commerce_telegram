package com.fowlart.main.state;

import com.google.common.collect.Maps;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("singleton")
public class BotVisitors implements Serializable {

    private Map<Long, BotVisitor> userMap = Maps.newHashMap();

    public Map<Long, BotVisitor> getUserMap() {
        return userMap;
    }
}
