package com.fowlart.main.controllers;

import com.fowlart.main.Bot;
import com.fowlart.main.KeyboardHelper;
import com.fowlart.main.ScalaHelper;
import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.state.BotVisitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final static Logger logger = LoggerFactory.getLogger(SearchController.class);
    private final Catalog catalog;
    private final String inputForImgPath;
    private final String hostAndPort;
    private final String inputForHTMLPath;
    private final BotVisitorService botVisitorService;
    private final Bot bot;
    private final ScalaHelper scHelper;
    private final KeyboardHelper kbHelper;


    public SearchController(@Autowired Catalog catalog,
                            @Autowired BotVisitorService botVisitorService,
                            @Value("${app.bot.items.img.folder}") String inputForImgPath,
                            @Value("${app.bot.host.url}") String hostAndPort,
                            @Value("${app.bot.html.templates}") String inputForHTMLPath,
                            @Autowired KeyboardHelper keyboardHelper,
                            @Autowired Bot bot) {
        this.botVisitorService = botVisitorService;
        this.catalog = catalog;
        this.inputForImgPath = inputForImgPath;
        this.hostAndPort = hostAndPort;
        this.inputForHTMLPath = inputForHTMLPath;
        this.bot = bot;
        this.scHelper = new ScalaHelper();
        this.kbHelper = keyboardHelper;
    }

    @GetMapping(produces = "text/html")
    public @ResponseBody String getSearchPage(@RequestParam String userId,
                                              HttpServletResponse servletResponse) throws IOException {

        var searchHtml = Files.readString(Path.of(inputForHTMLPath + "/search.html"));

        servletResponse.addCookie(new Cookie("userId", userId));

        return searchHtml;
    }
}
