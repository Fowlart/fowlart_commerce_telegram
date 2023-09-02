package com.fowlart.main.state.cosmos;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.fowlart.main.state.cosmos.BotVisitor;
import org.springframework.stereotype.Repository;

@Repository
public interface BotVisitorRepository extends CosmosRepository<com.fowlart.main.state.cosmos.BotVisitor, Long> {}
