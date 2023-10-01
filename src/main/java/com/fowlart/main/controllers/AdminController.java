package com.fowlart.main.controllers;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fowlart.main.Bot;
import com.fowlart.main.KeyboardHelper;
import com.fowlart.main.ScalaHelper;
import com.fowlart.main.az_service_bus.ActivityTracker;
import com.fowlart.main.dto.BotVisitorDto;
import com.fowlart.main.state.Catalog;
import com.fowlart.main.open_ai.CatalogEnhancer;
import com.fowlart.main.state.BotVisitorService;
import com.fowlart.main.state.cosmos.Item;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
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

    private final String containerName;

    private final String connectionString;

    private final BlobContainerClient containerClient;

    private final String adminSecretFromEnv;

    private final ActivityTracker activityTracker;

    public AdminController(BotVisitorService botVisitorService,
                           @Value("${app.bot.admin.secret}") String adminSecretFromEnv,
                           @Value("${app.bot.email.gmail.user}") String gmailAccName,
                           @Value("${app.bot.host.url}") String hostName, @Value("${app.bot.admins}") String botAdminsList,
                           @Autowired KeyboardHelper keyboardHelper,
                           Bot bot,
                           Catalog catalog,
                           CatalogEnhancer catalogEnhancer,
                           @Value("${azure.storage.container.name}") String containerName,
                           @Value("${azure.storage.connection.string}") String connectionString,
                           @Autowired ActivityTracker activityTracker) {
        this.activityTracker = activityTracker;
        this.adminSecretFromEnv = adminSecretFromEnv;
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
        this.containerName = containerName;
        this.connectionString = connectionString;
        this.containerClient = getBlobContainerClient();
    }

    @GetMapping("/send-message")
    public String sendMessage(@RequestHeader Map<String, String> headers, @RequestParam("userId") String userId, @RequestParam("text") String text) {

        if (notAdmin(headers)) return pleaseLogin;

        var botVisitor = botVisitorService.getBotVisitorByUserId(Long.parseLong(userId));

        if (Objects.nonNull(botVisitor)) {
            try {
                var msg = this.scHelper.buildSimpleReplyMessage(Long.parseLong(userId), "\uD83D\uDCE9 Повідомлення від адміністратора:\n\n" + text, null);
                bot.execute(msg);

                final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
                var output = mapper.writeValueAsString(new BotVisitorDto(botVisitor.getUserId(), botVisitor.getName(), botVisitor.getBucket().stream().map(Item::name).collect(Collectors.toSet()), botVisitor.getPhoneNumber(), botVisitor.getUser().getFirstName(), botVisitor.getUser().getLastName()));

                return "<h1>Повідомлення:<h3>msg</h3> <h1>надіслано користувачу:</h1><pre id=\"json\">user</pre>".replaceAll("msg", text).replaceAll("user", output);

            } catch (TelegramApiException | JsonProcessingException e) {
                logger.error("Failed to send message to user: " + botVisitor.getUserId());
                return "<h1>Помилка. Повідомлення не надіслано.</h1>";
            }
        }

        return "<h1>Користувача не знайдено</h1>";
    }

    @PostMapping("/send-admin-report")
    public ResponseEntity<String> sendAdminReport(@RequestHeader Map<String, String> headers, @RequestBody String text) {

        if (notAdminApiCall(headers)) return new ResponseEntity<>("You are not admin!", HttpStatus.UNAUTHORIZED);

        Arrays.stream(this.botAdminsList.split(",")).forEach(adminId -> {
            String textToBot = "\uD83D\uDCE1 Звіт активності юзерів:\n" + text + "\n";
            var resp = scHelper.buildSimpleReplyMessage(Long.parseLong(adminId), textToBot, null);
            bot.sendAnswer(resp);
        });

        return new ResponseEntity<>("Report sent to admins", HttpStatus.OK);
    }

    @GetMapping("catalog/restore")
    public String catalogRestore(@RequestHeader Map<String, String> headers) {
        if (notAdmin(headers)) return pleaseLogin;
        catalogEnhancer.catalogRestore();
        return "<p>Completed process of catalog restoring.</p>";
    }

    @GetMapping("catalog/enhance")
    public String startCatalogEnhancing(@RequestHeader Map<String, String> headers) {
        if (notAdmin(headers)) return pleaseLogin;
        catalogEnhancer.enhanceCatalog();
        return "<p>Completed process of catalog enhancing.</p>";
    }

    @GetMapping("catalog/enhance-status")
    public String getCatalogEnhancingStatus(@RequestHeader Map<String, String> headers) {
        if (notAdmin(headers)) return pleaseLogin;
        return catalogEnhancer.getInternalLogger().stream().map(str -> "<p>" + str + "</p>").collect(Collectors.joining());
    }

    private BlobContainerClient getBlobContainerClient() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(Objects.requireNonNull(connectionString)).buildClient();
        return blobServiceClient.createBlobContainerIfNotExists(containerName);
    }

    @GetMapping("statistic/all-items")
    public String getItemList(@RequestHeader Map<String) {
        var response = new ArrayList<String>();
        var groupItemsMap = this.catalog.getItemList().stream().collect(Collectors.groupingBy(Item::group));
        var containerItemsList = this.containerClient.listBlobs().stream().map(BlobItem::getName).toList();
        groupItemsMap.forEach((key, value) -> {
            response.add("<h4>" + key + "</h4>");
            value.forEach(item -> {
                // check if image exists using Azure blob api
                var imageExist = containerItemsList.stream().anyMatch(blobItemName -> blobItemName.toLowerCase().contains(item.name().toLowerCase().trim().replaceAll("/", "_")));
                response.add("<p style='font-size: 10px; margin: 1px'>" + item.name() + (imageExist ? "✅" : "❌" + "</p>"));
            });
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
            String event = "Подія у сховищі. Тип: " + eventGridEvent.getEventType() + "\n" + "Уточнені данні: " + eventGridEvent.getData().toString();
            activityTracker.sendMessage(event);
        });

        return "accepted web-hook POST";
    }

    @PutMapping("web-hooks/accept")
    public String acceptEventWebHookPuts(@RequestBody String request) {
        logger.info("Accepted Web-Hook PUT: {}", request);
        return "accepted web-hook PUT";
    }

    private boolean notAdmin(Map<String, String> headers) {
       // var googleAccessToken = headers.get("x-ms-token-google-access-token");
       // logger.info("googleAccessToken: " + googleAccessToken);
       // return !StringUtils.hasText(googleAccessToken) || !gmailAccName.equals(getEmailByToken(googleAccessToken));
        return false;
    }

    private boolean notAdminApiCall(Map<String, String> headers) {
        var adminSecret = headers.get("admin-secret");
        logger.info("adminSecret: " + adminSecret);
        return !adminSecretFromEnv.equals(adminSecret);
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
