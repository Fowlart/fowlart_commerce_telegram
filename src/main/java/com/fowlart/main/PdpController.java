package com.fowlart.main;

import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.in_mem_catalog.Item;
import com.fowlart.main.state.BotVisitor;
import com.fowlart.main.state.BotVisitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiPredicate;

@RestController
@RequestMapping("/pdp")
public class PdpController {

    private final static Logger logger = LoggerFactory.getLogger(PdpController.class);
    private final Catalog catalog;
    private final String inputForImgPath;
    private final String hostAndPort;
    private final String inputForHTMLPath;
    private final BotVisitorService botVisitorService;


    public PdpController(@Autowired Catalog catalog, @Autowired BotVisitorService botVisitorService, @Value("${app.bot.items.img.folder}") String inputForImgPath, @Value("${app.bot.host.url}") String hostAndPort, @Value("${app.bot.html.templates}") String inputForHTMLPath) {
        this.botVisitorService = botVisitorService;
        this.catalog = catalog;
        this.inputForImgPath = inputForImgPath;
        this.hostAndPort = hostAndPort;
        this.inputForHTMLPath = inputForHTMLPath;
    }

    @PostMapping(value = "/accept-item")
    public @ResponseBody ResponseEntity<String> acceptItem(@RequestParam String userID, @RequestParam String itemID, @RequestParam int qty) {
        // Todo: save visitor data, and item qty
        logger.info("userID " + userID);
        logger.info("itemID " + itemID);
        logger.info("qty " + qty);
        Optional<Item> item = catalog.getItemList().stream().filter(it -> itemID.equals(it.id())).findFirst();
        ResponseEntity<String> response = new ResponseEntity<>("Щось пішло не так!", HttpStatus.BAD_REQUEST);
        var user = botVisitorService.getBotVisitorByUserId(userID);
        if (item.isPresent() && Objects.nonNull(user)) {
            var actualItem = item.get();
            final var newActualItem = new Item(actualItem.id(),actualItem.name(),actualItem.price(),actualItem.group(),qty);
            var allItemsInGroup = catalog.getItemList().stream().filter(i -> i.group().equals(newActualItem.group())).map(i -> "<p><a href='/pdp/" + i.id() + "?userId=" + userID + "'>" + i.name() + "</a></p>").reduce((s1, s2) -> s1 + s2).orElse("<p>!от я ніколи не побачу цей аутпут!</p>");
            Set<Item> newBucket = new HashSet<>(user.getBucket());
            newBucket.add(newActualItem);
            user.setBucket(newBucket);
            botVisitorService.saveBotVisitor(user);
            response = new ResponseEntity<>(allItemsInGroup, HttpStatus.ACCEPTED);
        }
        return response;
    }

    @GetMapping(value = "/{id}", produces = "text/html")
    public @ResponseBody String getProductInfo(@PathVariable String id, @RequestParam String userId, HttpServletResponse servletResponse) throws IOException {

        var item = catalog.getItemList().stream().filter(i -> i.id().equals(id)).findFirst().orElse(null);

        if (Objects.isNull(item)) return "No such item";
        var allItemsInGroup = catalog.getItemList().stream().filter(i -> i.group().equals(item.group())).map(i -> "<p><a href='/pdp/" + i.id() + "?userId=" + userId + "'>" + i.name() + "</a></p>").reduce((s1, s2) -> s1 + s2).orElse("<p>!от я ніколи не побачу цей аутпут!</p>");

        // read pdp.html as a string
        var pdpHtml = Files.readString(Path.of(inputForHTMLPath + "/pdp.html"));
        var productId = item.id();
        var productImageUri = hostAndPort + "/pdp/img/" + productId;

        servletResponse.addCookie(new Cookie("userId", userId));

        return pdpHtml.replace("{{productImageUri}}", productImageUri).replace("{{productPrice}}", item.price().toString()).replace("{{productName}}", item.name()).replace("{{groupName}}", item.group()).replace("{{dialogItems}}", allItemsInGroup);
    }

    @GetMapping(value = "/img/{id}", produces = "image/png")
    public @ResponseBody byte[] getImage(@PathVariable() String id) throws IOException {

        var noImageImg = new File(inputForImgPath + "/no_image_available.png");

        var item = catalog.getItemList().stream().filter(i -> i.id().equals(id)).findFirst().orElse(new Item("mock_id", "no_image_available", 0.0, "mock_group", 0));

        BiPredicate<Path, BasicFileAttributes> biPredicate = (path, x) -> {
            var theFile = path.toFile();
            var mimetype = new MimetypesFileTypeMap().getContentType(theFile);
            var theType = mimetype.split("/")[0];
            var res = path.getFileName().toString().toLowerCase().trim().contains(item.name().toLowerCase().trim()) && theType.equals("image");
            return res;
        };

        var itemImgOp = Files.find(Path.of(inputForImgPath + "/"), 1, biPredicate).findFirst();

        // if no image found, return no_image_available.png
        if (itemImgOp.isEmpty()) {
            return Files.readAllBytes(noImageImg.toPath());
        } else {
            var itemImg = itemImgOp.get().toFile();
            return Files.readAllBytes(itemImg.toPath());
        }
    }
}
