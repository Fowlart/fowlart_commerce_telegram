package com.fowlart.main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fowlart.main.dto.BotVisitorDto;
import com.fowlart.main.in_mem_catalog.Item;
import com.fowlart.main.state.BotVisitorService;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/statistic")
public class StatisticController {

    private final BotVisitorService botVisitorService;

    public StatisticController(BotVisitorService botVisitorService) {
        this.botVisitorService = botVisitorService;
    }

    @GetMapping("/all-visitors")
    public String getExampleData() {

        final ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);

        var visitors = botVisitorService.getAllVisitors();
        return visitors.stream().map(botVisitor -> {

            var bucketConverted = botVisitor.getBucket()
                    .stream()
                    .map(Item::name)
                    .collect(Collectors.toSet());


            try {
                return mapper.writeValueAsString(new BotVisitorDto(botVisitor.getName(),
                        bucketConverted,
                        botVisitor.getPhoneNumber(), botVisitor.getUser()
                        .getFirstName(), botVisitor.getUser().getLastName()));

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        }).reduce((s1, s2) -> s1 +"<br/>" +s2 ).orElse("None");
    }
}
