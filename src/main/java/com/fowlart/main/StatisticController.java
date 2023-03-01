package com.fowlart.main;

import com.fowlart.main.state.BotVisitor;
import com.fowlart.main.state.BotVisitorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistic")
public class StatisticController {

    private final BotVisitorService botVisitorService;

    public StatisticController(BotVisitorService botVisitorService) {
        this.botVisitorService = botVisitorService;
    }

    @GetMapping("/all-visitors")
    public String getExampleData() {
        var visitors = botVisitorService.getAllVisitors();
        return visitors.stream().map(BotVisitor::toString).reduce((s1,s2)->s1+"\n"+s2).orElse("None");
    }
}
