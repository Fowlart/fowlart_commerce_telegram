package com.fowlart.main.state;

import com.fowlart.main.state.rocks_db.RocksDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderService {

    @Autowired
    private RocksDBRepository rocksDBRepository;

    public boolean saveOrder(Order order) {
        return rocksDBRepository.save(order.orderId(), order);
    }

    public Order getOrderById(String id) {
        Optional<Object> order = rocksDBRepository.find(id);
        return (Order) order.orElse(null);
    }
}
