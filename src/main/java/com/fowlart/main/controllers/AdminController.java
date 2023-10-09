package com.fowlart.main.controllers;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fowlart.main.az_service_bus.ActivityTracker;
import com.fowlart.main.state.BotVisitorService;
import com.fowlart.main.state.Catalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
public class AdminController {

    private final static Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final String containerName;

    private final String connectionString;

    private final String adminSecretFromEnv;

    private final ActivityTracker activityTracker;

    public AdminController(
            BotVisitorService botVisitorService,
            @Value("${app.bot.admin.secret}") String adminSecretFromEnv,
            @Value("${app.bot.email.gmail.user}") String gmailAccName,
            @Value("${azure.storage.container.name}") String containerName,
            @Value("${azure.storage.connection.string}") String connectionString,
            @Autowired ActivityTracker activityTracker) {
        this.activityTracker = activityTracker;
        this.adminSecretFromEnv = adminSecretFromEnv;
        this.containerName = containerName;
        this.connectionString = connectionString;
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
}
