package com.fowlart.main.state.postgresql;

import com.fowlart.main.state.rocks_db.BotVisitorSimplified;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotVisitorRepo extends JpaRepository<BotVisitorSimplified, Long> { }