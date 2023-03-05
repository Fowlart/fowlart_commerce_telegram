package com.fowlart.main.state;

import com.fowlart.main.state.rocks_db.RocksDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BotVisitorService {

    private final RocksDBRepository rocksDBRepository;

    public BotVisitorService(RocksDBRepository rocksDBRepository) {
        this.rocksDBRepository = rocksDBRepository;
    }

    public boolean saveBotVisitor(BotVisitor botVisitor) {
        return rocksDBRepository.save(botVisitor.getUserId(), botVisitor);
    }

    public BotVisitor getBotVisitorByUserId(String id) {
        Optional<Object> botVisitor = rocksDBRepository.find(id);
        return (BotVisitor) botVisitor.orElse(null);
    }

    public List<BotVisitor> getAllVisitors() {
        var res = new ArrayList<BotVisitor>();
        var rocksIterator = rocksDBRepository.getIterator();
        for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
            var key = new String(rocksIterator.key());
            var mbVisitor = rocksDBRepository.find(key);
            if (mbVisitor.isPresent() && (mbVisitor.get() instanceof BotVisitor)) {
                res.add((BotVisitor) mbVisitor.get());
            }
        }
        return res;
    }

    public BotVisitor getOrCreateVisitor(User user) {
        BotVisitor botVisitor;
        Optional<Object> userFromDb = rocksDBRepository.find(String.valueOf(user.getId()));
        if (userFromDb.isPresent()) {
            // get from RocksDb
            botVisitor = (BotVisitor) userFromDb.get();
        } else {
            //write to RocksDb
            botVisitor = new BotVisitor(user, user.getId());
            rocksDBRepository.save(String.valueOf(user.getId()), botVisitor);
        }

        return botVisitor;
    }

}
