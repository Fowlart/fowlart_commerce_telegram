package com.fowlart.main.open_ai;

import com.fowlart.main.catalog_fetching.ExcelFetcher;
import com.fowlart.main.in_mem_catalog.Catalog;
import com.fowlart.main.in_mem_catalog.Item;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CatalogEnhancer implements InitializingBean {
    private final static List<String> internalLogger = new ArrayList<>();
    private final static Logger logger = LoggerFactory.getLogger(CatalogEnhancer.class);
    private final String openAiAuthToken;
    private final Catalog catalog;
    private final ExcelFetcher excelFetcher;

    public CatalogEnhancer(
            @Value("${openai.token}") String openAiAuthToken,
            @Autowired Catalog catalog, ExcelFetcher excelFetcher) {

        this.openAiAuthToken = openAiAuthToken;
        this.catalog = catalog;
        this.excelFetcher = excelFetcher;
    }

    public List<String> getInternalLogger() {
        return internalLogger;
    }

    public void enhanceCatalog() {
        String token = openAiAuthToken;
        internalLogger.add(new Date() + ": розпочато процес покращення каталогу з допомогою моделі 'gpt-3.5-turbo'.");

        this.catalog
                .getItemList()
                .forEach(item -> {
                    OpenAiService service = new OpenAiService(token);
                    final List<ChatMessage> messages = new ArrayList<>();
                    final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(),
                            "Користувач вводить назву товарної позиції. " +
                                    "Наша ціль - повернути максимально узагальнену назву товарної группи для цієї позиції.\n" +
                                    "Уникай деталізації, наприклад батарейка CAMELION LR14 - це просто 'батарейки', без вказання бренду. " +
                                    "Засіб для прибирання пилу Dust 0,5л - це побутова хімія." +
                                    "Відповідь має бути лаконічною і має містити ТІЛЬКИ назву товарної группи і нічого іншого." +
                                    "Якщо товарну группу тяжко визначити то поверни 'інше'.");

                    final List<String> answer = new ArrayList<>();
                    messages.add(systemMessage);
                    final ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), item.name());
                    messages.add(userMessage);

                    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder().model("gpt-3.5-turbo").messages(messages).topP(1d).temperature(0d).logitBias(new HashMap<>()).build();

                    service.streamChatCompletion(chatCompletionRequest).doOnError(exception -> {
                        internalLogger.add(new Date() + ": помилка звязку на етапі оброблення позиції: " + item.name());
                        internalLogger.add(exception.getMessage());
                        service.shutdownExecutor();
                    }).blockingForEach(chatCompletionChunk -> answer.add(chatCompletionChunk.getChoices().get(0).getMessage().getContent()));

                    String groupName = String.join("", answer).replaceAll("null", "");

                    internalLogger.add(new Date() + ": " + item.name() + "->" + groupName);
                    item.setGroup(groupName);
                    service.shutdownExecutor();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

        this.catalog
                .setGroupList(this.excelFetcher.getCatalogItems()
                        .stream()
                        .map(Item::group)
                        .collect(Collectors.toList()));
    }


    @Override
    public void afterPropertiesSet() {
        logger.info("CatalogEnhancer started with openAI token: {}", this.openAiAuthToken);
    }
}
