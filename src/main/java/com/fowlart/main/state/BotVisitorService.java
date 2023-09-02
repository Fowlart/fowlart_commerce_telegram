package com.fowlart.main.state;

import com.fowlart.main.state.cosmos.BotVisitorRepository;
import org.javatuples.Pair;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BotVisitorService {

    private final BotVisitorRepository botVisitorCosmosRepository;

    public BotVisitorService(BotVisitorRepository botVisitorCosmosRepository) {
        this.botVisitorCosmosRepository = botVisitorCosmosRepository;
    }

    public com.fowlart.main.state.cosmos.BotVisitor saveBotVisitor(com.fowlart.main.state.cosmos.BotVisitor botVisitor) {

        return botVisitorCosmosRepository.save(botVisitor);
    }

    public com.fowlart.main.state.cosmos.BotVisitor getBotVisitorByUserId(Long id) {
        return botVisitorCosmosRepository.findById(id).orElse(null);
    }

    public List<com.fowlart.main.state.cosmos.BotVisitor> getAllVisitors() {
        var list = new ArrayList<com.fowlart.main.state.cosmos.BotVisitor>();
        botVisitorCosmosRepository.findAll().forEach(list::add);
        return list;
    }

    public Pair<com.fowlart.main.state.cosmos.BotVisitor,Boolean> getOrCreateVisitor(User user) {
        com.fowlart.main.state.cosmos.BotVisitor botVisitor;
        Optional<com.fowlart.main.state.cosmos.BotVisitor> userFromDb = botVisitorCosmosRepository.findById(user.getId());
        var isNew = Boolean.FALSE;
        if (userFromDb.isPresent()) {
            // get from RocksDb
            botVisitor = userFromDb.get();
        } else {
            //write to RocksDb
            botVisitor = new com.fowlart.main.state.cosmos.BotVisitor(user, user.getId());

            botVisitorCosmosRepository.save(botVisitor);

            isNew = Boolean.TRUE;
        }

        botVisitor.setLastVisit(new java.util.Date().toString());
        return Pair.with(botVisitor,isNew);
    }

}
