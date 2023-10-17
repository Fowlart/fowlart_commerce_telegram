package com.fowlart.main.controllers;

import com.fowlart.main.ScalaHelper;
import com.fowlart.main.state.BotVisitorService;
import com.fowlart.main.state.Catalog;
import com.fowlart.main.state.cosmos.Item;
import com.fowlart.main.state.inmem.SessionCarts;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("pdp")
public class PdpController {

    private final static Logger logger = LoggerFactory.getLogger(PdpController.class);
    private final Catalog catalog;
    private final String inputForImgPath;
    private final String inputForHTMLPath;
    private final BotVisitorService botVisitorService;
    private final ScalaHelper scHelper;

    private final SessionCarts carts;


    public PdpController(@Autowired Catalog catalog, @Autowired BotVisitorService botVisitorService, @Value("${app.bot.items.img.folder}") String inputForImgPath, @Value("${app.bot.html.templates}") String inputForHTMLPath, @Autowired SessionCarts carts) {
        this.botVisitorService = botVisitorService;
        this.catalog = catalog;
        this.inputForImgPath = inputForImgPath;
        this.inputForHTMLPath = inputForHTMLPath;
        this.scHelper = new ScalaHelper();
        this.carts = carts;
    }

    @GetMapping(value = "/bucket-sum", produces = "text/html")
    public ResponseEntity<String> getBucketSum(HttpServletRequest request) {

        // if remove this sleep, API call might return old qty. Interesting!
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String jsessionid = null;
        ResponseEntity<String> response = ResponseEntity.ok("0.0");

        //get JSESSIONID cookie
        if (Objects.nonNull(request.getCookies())) {
            jsessionid = Arrays
                    .stream(request.getCookies())
                    .filter(c -> c.getName().equals("JSESSIONID"))
                    .findFirst()
                    .orElse(new Cookie("JSESSIONID", "NO_JSESSIONID"))
                    .getValue();
        } else {
            jsessionid = "NO_JSESSIONID";
        }

        if (jsessionid.equals("NO_JSESSIONID")) {
            return response;
        } else {
            var cart = carts.getCart(jsessionid);
            var sum = cart.stream()
                    .map(item -> item.qty()*item.price())
                    .reduce(Double::sum)
                    .orElse(0.0);
            return ResponseEntity.ok(sum.toString());
        }
    }

    @GetMapping(produces = "text/html")
    public ResponseEntity<String> getItemList(@RequestParam(required = false) String group, HttpServletRequest request) throws IOException {

        var responseHeaders = new HttpHeaders();

        String jsessionid = null;

        if (Objects.nonNull(request.getCookies())) {
             jsessionid = Arrays
                     .stream(request.getCookies())
                     .filter(c -> c.getName().equals("JSESSIONID"))
                     .findFirst()
                     .orElse(new Cookie("JSESSIONID", UUID.randomUUID().toString())).getValue();

            responseHeaders.add("Set-Cookie", "JSESSIONID=" + jsessionid + "; HttpOnly");
            logger.info("JSESSIONID is " + jsessionid);
        } else {
            jsessionid = UUID.randomUUID().toString();
            responseHeaders.add("Set-Cookie", "JSESSIONID=" + jsessionid + "; HttpOnly");
            logger.info("JSESSIONID is " + jsessionid);
        }

        group = StringUtils.isAllBlank(group) ? catalog.getGroupList().get(0) : group;
        String firstImageId = null;
        String pdpHtml = null;
        String groupLinks = null;
        String itemList = null;
        String productImageUri = null;

        try {

            pdpHtml = Files.readString(Path.of(inputForHTMLPath + "/public_catalog.html"));

            final String finalGroup = group;

            firstImageId = this.catalog.getItemList().stream().filter(i -> i.group().equals(finalGroup)).findFirst().map(Item::id).orElse("ID0");

            itemList = this.catalog.getItemList().stream().filter(i -> i.group().equals(finalGroup)).map(this.scHelper::getButtonHtml).collect(Collectors.joining("\n"));

            groupLinks = this.catalog.getGroupList().stream().map(this.scHelper::getGroupLinkHtml).collect(Collectors.joining("\n"));

            productImageUri = "/pdp/img/" + firstImageId;


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String bucketContent = this
                .scHelper
                .getBucketContent(this.carts.getCart(jsessionid));

        logger.info("bucketContent: " + bucketContent);

        var res = pdpHtml.replace("{{productImageUri}}", productImageUri)
                .replace("{{groupLinks}}", groupLinks)
                .replace("{{bucketContent}}", bucketContent)
                .replace("{{nameAndPrice}}", group)
                .replace("{{itemList}}", itemList);

        return ResponseEntity.ok().headers(responseHeaders).body(res);
    }

    @PostMapping(value = "/remove-item", produces = "text/html", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public @ResponseBody ResponseEntity<String> removeItem(@RequestBody MultiValueMap<String, String> formData, HttpServletRequest request) {

        var itemId = formData.getFirst("itemId");

        var jsessionId = Arrays.stream(request.getCookies()).filter(c -> c.getName().equals("JSESSIONID")).findFirst().orElse(null);

        var html = "";

        var item = catalog.getItemList().stream().filter(i -> i.id().equals(itemId)).findFirst().orElse(null);

        if (Objects.isNull(jsessionId)) {
            logger.info("JSESSIONID is null");
        } else {
            logger.info("JSESSIONID is " + jsessionId.getValue());
            logger.info("remove itemId" + itemId);
            carts.removeItem(item, jsessionId.getValue());

            html = scHelper.getBucketContent(carts.getCart(jsessionId.getValue()));
        }
        return new ResponseEntity<>(html, HttpStatus.OK);
    }

    @PostMapping(value = "/accept-item", produces = "text/html", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public @ResponseBody ResponseEntity<String> acceptItem(@RequestBody MultiValueMap<String, String> formData,
                                                           HttpServletRequest request) {

        var itemId = formData.getFirst("itemId");
        var itemQuantity = formData.getFirst("itemQuantity");
        logger.info("itemId: " + itemId);
        logger.info("itemQuantity: " + itemQuantity);
        itemQuantity = (Objects.isNull(itemQuantity))? "1": itemQuantity;

        var jsessionId = Arrays.stream(request.getCookies()).filter(c -> c.getName().equals("JSESSIONID")).findFirst().orElse(null);

        var html = "";

        if (Objects.isNull(jsessionId)) {
            logger.info("JSESSIONID is null");
        } else {
            logger.info("JSESSIONID is " + jsessionId.getValue());
            logger.info("adding item " + itemId);
            var item = catalog.getItemList().stream().filter(i -> i.id().equals(itemId)).findFirst().orElse(null);

            var modifiedItem = new Item(item.id(), item.name(), item.price(), item.group(), Integer.parseInt(itemQuantity));

            carts.addItem(modifiedItem, jsessionId.getValue());

            html = scHelper.getBucketContent(carts.getCart(jsessionId.getValue()));
        }


        logger.info("html: " + html);

        return new ResponseEntity<>(html, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = "text/html")
    public @ResponseBody String getProductInfo(@PathVariable String id, @RequestParam String userId, HttpServletResponse servletResponse) throws IOException {

        var item = catalog.getItemList().stream().filter(i -> i.id().equals(id)).findFirst().orElse(null);

        if (Objects.isNull(item)) return "No such item";

        var allItemsInGroup = catalog.getItemList().stream().filter(i -> i.group().equals(item.group())).map(i -> "<p><a href='/pdp/" + i.id() + "?userId=" + userId + "'>" + i.name() + "</a></p>").reduce((s1, s2) -> s1 + s2).orElse("<p>!от я ніколи не побачу цей аутпут!</p>");

        // read pdp.html as a string
        var pdpHtml = Files.readString(Path.of(inputForHTMLPath + "/pdp.html"));
        var productId = item.id();
        var productImageUri = "/pdp/img/" + productId;

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

            var res = path.getFileName().toString().toLowerCase().trim().contains(item.name().toLowerCase().trim().replaceAll("/", "_")) && theType.equals("image");

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
