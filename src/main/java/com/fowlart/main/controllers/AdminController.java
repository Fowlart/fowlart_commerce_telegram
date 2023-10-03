package com.fowlart.main.controllers;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fowlart.main.Bot;
import com.fowlart.main.KeyboardHelper;
import com.fowlart.main.ScalaHelper;
import com.fowlart.main.az_service_bus.ActivityTracker;
import com.fowlart.main.open_ai.CatalogEnhancer;
import com.fowlart.main.state.BotVisitorService;
import com.fowlart.main.state.Catalog;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public AdminController(BotVisitorService botVisitorService, @Value("${app.bot.admin.secret}") String adminSecretFromEnv, @Value("${app.bot.email.gmail.user}") String gmailAccName, @Value("${app.bot.host.url}") String hostName, @Value("${app.bot.admins}") String botAdminsList, @Autowired KeyboardHelper keyboardHelper, Bot bot, Catalog catalog, CatalogEnhancer catalogEnhancer, @Value("${azure.storage.container.name}") String containerName, @Value("${azure.storage.connection.string}") String connectionString, @Autowired ActivityTracker activityTracker) {
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
