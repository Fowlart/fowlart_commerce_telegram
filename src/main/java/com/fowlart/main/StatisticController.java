package com.fowlart.main;

import com.fowlart.main.state.BotVisitor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistic")
public class StatisticController {

    @GetMapping("/{id}")
    public String getExampleData(@PathVariable int id) {
        return "visitor!";
    }
}
