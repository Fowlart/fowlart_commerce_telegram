package com.fowlart.main.state;

import com.fowlart.main.state.rocks_db.RocksDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Component
public class BotVisitorService {

    @Autowired
    private RocksDBRepository rocksDBRepository;
    
    public boolean saveBotVisitor(BotVisitor botVisitor) {
       return rocksDBRepository.save(botVisitor.getUserId(),botVisitor);
    }

    public BotVisitor getBotVisitorByUserId(String id) {
        Optional<Object> botVisitor = rocksDBRepository.find(id);
        return (BotVisitor) botVisitor.orElse(null);
    }

    public BotVisitor getOrCreateVisitor(User user) {
        BotVisitor botVisitor;
        Optional<Object> userFromDb = rocksDBRepository.find(String.valueOf(user.getId()));
        if (userFromDb.isPresent()) {
            // get from RocksDb
            botVisitor = (BotVisitor) userFromDb.get();
        } else {
            //write to RocksDb
            botVisitor = new BotVisitor(user, Buttons.MAIN_SCREEN, user.getId());
            rocksDBRepository.save(String.valueOf(user.getId()), botVisitor);
        }

        return botVisitor;
    }

}
