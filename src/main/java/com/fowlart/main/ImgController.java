package com.fowlart.main;

import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.in_mem_catalog.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

@RestController
@RequestMapping("/pdp")
public class ImgController {

    private final Catalog catalog;
    private final String inputForImgPath;


    public ImgController(@Autowired Catalog catalog,
                         @Value("${app.bot.items.img.folder}") String inputForImgPath) {
        this.catalog = catalog;
        this.inputForImgPath = inputForImgPath;
    }

    @GetMapping(value = "/{id}", produces = "text/html")
    public @ResponseBody String getProductInfo(@PathVariable String id, @RequestHeader Map<String, String> headers) throws IOException {
        var item = catalog.getItemList().stream().filter(i -> i.id().equals(id)).findFirst().orElse(null);
        var hostAndPort = headers.get("host");

        if (Objects.isNull(item)) return "No such item";

        return item.buildPage("http://" + hostAndPort + "/pdp/img/");
    }

    @GetMapping(value = "/img/{id}", produces = "image/png")
    public @ResponseBody byte[] getImage(@PathVariable() String id) throws IOException {

        var noImageImg = new File(inputForImgPath + "/no_image_available.png");

        var item = catalog.getItemList().stream().filter(i -> i.id().equals(id)).findFirst().orElse(new Item("mock_id", "no_image_available", 0.0, "mock_group", 0));

        BiPredicate<Path, BasicFileAttributes> biPredicate = (path, x) -> {
            var theFile = path.toFile();
            var mimetype = new MimetypesFileTypeMap().getContentType(theFile);
            var theType = mimetype.split("/")[0];
            var res = path.getFileName().toString().toLowerCase().contains(item.name().toLowerCase()) && theType.equals("image");
            return res;
        };

        var itemImgOp = Files.find(Path.of(inputForImgPath+"/"), 1, biPredicate).findFirst();

        // if no image found, return no_image_available.png
        if (itemImgOp.isEmpty()) {
            return Files.readAllBytes(noImageImg.toPath());
        } else {
            var itemImg = itemImgOp.get().toFile();
            return Files.readAllBytes(itemImg.toPath());
        }
    }
}
