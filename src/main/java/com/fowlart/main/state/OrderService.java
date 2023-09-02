package com.fowlart.main.state;

import com.fowlart.main.state.cosmos.Order;
import com.fowlart.main.state.cosmos.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderService {

    @Autowired
    private OrderRepository repository;

    public com.fowlart.main.state.cosmos.Order saveOrder(com.fowlart.main.state.cosmos.Order order) {
        return repository.save(order);
    }

    public com.fowlart.main.state.cosmos.Order getOrderById(Long id) {
        return repository.findById(id).orElse(null);
    }
}
