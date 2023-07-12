package com.fowlart.main.controllers;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fowlart.main.Bot;
import com.fowlart.main.KeyboardHelper;
import com.fowlart.main.ScalaHelper;
import com.fowlart.main.dto.BotVisitorDto;
import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.in_mem_catalog.Item;
import com.fowlart.main.open_ai.CatalogEnhancer;
import com.fowlart.main.state.BotVisitorService;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AdminController {

    private final static Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final BotVisitorService botVisitorService;
    private final String gmailAccName;
    private final String hostName;
    private final String pleaseLogin;
    private final Bot bot;
    private final Catalog catalog;
    private final ScalaHelper scHelper;
    private final KeyboardHelper kbHelper;
    private final String botAdminsList;
    private final CatalogEnhancer catalogEnhancer;

    public AdminController(BotVisitorService botVisitorService,
                           @Value("${app.bot.email.gmail.user}") String gmailAccName,
                           @Value("${app.bot.host.url}") String hostName,
                           @Value("${app.bot.admins}") String botAdminsList,
                           @Autowired KeyboardHelper keyboardHelper,
                           Bot bot, Catalog catalog, CatalogEnhancer catalogEnhancer) {

        this.botVisitorService = botVisitorService;
        this.gmailAccName = gmailAccName;
        this.hostName = hostName;
        this.pleaseLogin = "You are not admin! Please login with Google account!" + "  <button onClick=\"javascript:window.location.href='hostname/.auth/login/google/callback'\">Login</button>".replaceAll("hostname", hostName);
        this.bot = bot;
        this.catalog = catalog;
        this.catalogEnhancer = catalogEnhancer;
        this.scHelper = new ScalaHelper();
        this.kbHelper = keyboardHelper;
        this.botAdminsList = botAdminsList;
    }

    // root mapping
    @GetMapping("/")
    public String getRoot(@RequestHeader Map<String, String> headers) throws JsonProcessingException {
        return getAllVisitorList(headers);
    }

    @GetMapping("statistic/all-visitors")
    public String getAllVisitorList(@RequestHeader Map<String, String> headers) throws JsonProcessingException {
        // check if the request is from the admin
        if (notAdmin(headers)) return pleaseLogin;
        final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        var visitors = botVisitorService.getAllVisitors();
        var visitorsDTO = visitors.stream().map(botVisitor -> {
            var bucketConverted = botVisitor.getBucket().stream().map(Item::name).collect(Collectors.toSet());
            return new BotVisitorDto(botVisitor.getUserId(), botVisitor.getName(), bucketConverted, botVisitor.getPhoneNumber(), botVisitor.getUser().getFirstName(), botVisitor.getUser().getLastName());
        }).collect(Collectors.toSet());
        return mapper.writeValueAsString(visitorsDTO);
    }

    @GetMapping("catalog/enhance")
    public String startCatalogEnhancing(@RequestHeader Map<String, String> headers){
        if (notAdmin(headers)) return pleaseLogin;
        catalogEnhancer.enhanceCatalog();
        return "<p>Completed process of catalog enhancing. </p>";
    }

    @GetMapping("catalog/enhance-status")
    public String getCatalogEnhancingStatus(@RequestHeader Map<String, String> headers){
        if (notAdmin(headers)) return pleaseLogin;
        return catalogEnhancer.getInternalLogger().stream().map(str->"<p>"+str+"</p>").collect(Collectors.joining());
    }

    @GetMapping("statistic/all-items")
    public String getItemList(@RequestHeader Map<String, String> headers) {
        if (notAdmin(headers)) return pleaseLogin;

        var response = new ArrayList<String>();

        var groupItemsMap = this.catalog.getItemList().stream().collect(Collectors.groupingBy(Item::group));

        groupItemsMap.forEach((key, value) -> {
            response.add("<h4>"+key+"</h4>");
            value.forEach(item -> response.add("<p>" + item.name() + "</p>"));
        });

        return String.join("", response);
    }

    @PostMapping("web-hooks/accept")
    public String acceptEventWebHookPosts(@RequestBody String request) {
        logger.info("Accepted Web-Hook POST:");
        var events = EventGridEvent.fromString(request);
        events.forEach(eventGridEvent -> {
            logger.info("event type: {}", eventGridEvent.getEventType());
            logger.info("event data(string): {}", eventGridEvent.getData().toString());
            Arrays.stream(botAdminsList.split(",")).forEach(adminId -> {
                String textToBot = "\uD83D\uDCE1 Відбулася подія в сховищі картинок.\n" +
                        "тип: " +
                        eventGridEvent.getEventType() +
                        "\n" +
                        "уточнені данні: " +
                        eventGridEvent.getData().toString();
                var resp = scHelper.buildSimpleReplyMessage(Long.parseLong(adminId), textToBot, null);
                bot.sendAnswer(resp);
            });
        });

        return "accepted web-hook POST";
    }

    @PutMapping("web-hooks/accept")
    public String acceptEventWebHookPuts(@RequestBody String request) {
        logger.info("Accepted Web-Hook PUT: {}", request);
        return "accepted web-hook PUT";
    }

    private boolean notAdmin(Map<String, String> headers) {
        var googleAccessToken = headers.get("x-ms-token-google-access-token");
        logger.info("googleAccessToken: " + googleAccessToken);
        return !StringUtils.hasText(googleAccessToken) || !gmailAccName.equals(getEmailByToken(googleAccessToken));
    }

    private String getEmailByToken(String token) {
        Unirest.setTimeouts(0, 0);
        String email;
        try {
            var response = Unirest.get("https://oauth2.googleapis.com/tokeninfo?access_token=" + token).asString();
            logger.info(response.getBody());
            // convert body to JSON
            var json = new ObjectMapper().readTree(response.getBody());
            email = json.get("email").asText();

        } catch (UnirestException | JsonProcessingException | RuntimeException e) {
            logger.warn(e.getMessage());
            email = null;
        }
        return email;
    }
}
