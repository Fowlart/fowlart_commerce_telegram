package com.fowlart.main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fowlart.main.dto.BotVisitorDto;
import com.fowlart.main.in_mem_catalog.Item;
import com.fowlart.main.state.BotVisitorService;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/statistic")
public class StatisticController {

    private final BotVisitorService botVisitorService;

    public StatisticController(BotVisitorService botVisitorService) {
        this.botVisitorService = botVisitorService;
    }

    @GetMapping("/all-visitors")
    public String getAllVisitorList(@RequestHeader Map<String, String> headers) {

        // get headers from request
         headers.forEach((key, value) -> {
             System.out.println(key + " " + value);
         });


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
