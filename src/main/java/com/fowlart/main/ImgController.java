package com.fowlart.main;

import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.state.BotVisitorService;
import org.apache.poi.util.IOUtils;
import org.glassfish.grizzly.http.server.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/pdp")
public class ImgController {

    private final BotVisitorService botVisitorService;
    private final String inputForImgPath;

    private final Catalog catalog;


    public ImgController(BotVisitorService botVisitorService,
                         @Value("${app.bot.items.img.folder}") String inputForImgPath, @Autowired Catalog catalog) {
        this.botVisitorService = botVisitorService;
        this.inputForImgPath = inputForImgPath;
        this.catalog = catalog;
    }

    @GetMapping(value = "/{id}", produces = "text/html")
    public @ResponseBody String getProductInfo(@PathVariable String id, @RequestHeader Map<String, String> headers) throws IOException {
       var item = catalog.getItemList().stream().filter(i -> i.id().equals(id)).findFirst().orElse(null);
       var hostAndPort = headers.get("host");
       return item.buildPage(hostAndPort);
    }

    // controller for getting image by id
    @GetMapping(value = "/img/{fileName}", produces = "image/png")
    public @ResponseBody byte[] getImage(@PathVariable() String fileName) throws IOException {
        InputStream in = getClass()
                .getResourceAsStream("/imgsForTGbot/"+fileName+".png");
        return IOUtils.toByteArray(in);
    }

}
