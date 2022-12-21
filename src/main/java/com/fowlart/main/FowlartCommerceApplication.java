package com.fowlart.main;


import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@SpringBootApplication
@Configuration
public class FowlartCommerceApplication {

    private static Logger log;

    private static String propertiesPath = "src/main/resources/application.properties";

    private static Properties getPropertiesFromFile(String filePath) {
        final Properties propsFromFile = new Properties();
        try (InputStream input = Files.newInputStream(Paths.get(filePath))) {
            propsFromFile.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return propsFromFile;
    }

    public static void main(String[] args) {
        System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
        var context = getLoggerContext();
        log = context.getLogger("MyLogger");
        SpringApplication.run(FowlartCommerceApplication.class, args);
        TelegramBotsApi telegramBotsApi;
        log.info("Registering bot...");
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(Bot.getInstance());
        } catch (TelegramApiException e) {
            log.error("Failed to register bot(check internet connection / bot token or make sure only one instance of bot is running).", e);
        }
        log.info("Telegram bot is ready to accept updates from user!");
    }

    public static LoggerContext getLoggerContext() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.ERROR);
        builder.setConfigurationName("BuilderTest");
        builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL).addAttribute("level", Level.INFO));

        // CONSOLE appender
        AppenderComponentBuilder consoleAppenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
        consoleAppenderBuilder.add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
        consoleAppenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL).addAttribute("marker", "FLOW"));

        // FILE appender
        AppenderComponentBuilder fileAppenderBuilder = builder.newAppender("FileAppender", "File");
        fileAppenderBuilder.addAttribute("fileName", "/Users/artur/Documents/TG_BOT/logs/log.txt");
        fileAppenderBuilder.add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
        fileAppenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL).addAttribute("marker", "FLOW"));
        builder.add(consoleAppenderBuilder);
        builder.add(fileAppenderBuilder);

        // KAFKA appender
        String kafkaServer = getPropertiesFromFile(propertiesPath).getProperty("logging.kafka.server").trim();
        String kafkaCredentials = getPropertiesFromFile(propertiesPath).getProperty("logging.kafka.sasl.jaas.config").trim();

        AppenderComponentBuilder kafkaAppenderBuilder = builder.newAppender("KafkaAppender", "Kafka");
        kafkaAppenderBuilder.addAttribute("topic", "Sephora.DataPlatform.GoldenBook.VendorMaster");
        kafkaAppenderBuilder.addComponent(builder.newProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer));
        kafkaAppenderBuilder.addComponent(builder.newProperty("sasl.jaas.config", kafkaCredentials));
        kafkaAppenderBuilder.addComponent(builder.newProperty("security.protocol", "SASL_SSL"));
        kafkaAppenderBuilder.addComponent(builder.newProperty("sasl.mechanism", "PLAIN"));
        kafkaAppenderBuilder.addComponent(builder.newProperty(ProducerConfig.ACKS_CONFIG, "all"));
        kafkaAppenderBuilder.addComponent(builder.newProperty(ProducerConfig.RETRIES_CONFIG, "1"));
        kafkaAppenderBuilder.addComponent(builder.newProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer"));

        // kafkaAppenderBuilder.addComponent(builder.newProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer"));
        // Todo: check if interactions with schema registry needed
        // kafkaAppenderBuilder.add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
        // kafkaAppenderBuilder.addAttribute(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, propsFromFile.getProperty("schema-registry.server"));
        // kafkaAppenderBuilder.addAttribute(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, false);
        // kafkaAppenderBuilder.addAttribute(AbstractKafkaSchemaSerDeConfig.USE_SCHEMA_ID, schemaId);
        // kafkaAppenderBuilder.addAttribute(AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
        // kafkaAppenderBuilder.addAttribute(AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG, propsFromFile.getProperty("basic.auth.user.info"));

        kafkaAppenderBuilder.add(builder.newLayout("JsonLayout"));
        builder.add(kafkaAppenderBuilder);

        //Console appender will be added automatically
        builder.add(builder.newLogger("FileLogger", Level.INFO).add(builder.newAppenderRef("FileAppender")));
        builder.add(builder.newLogger("KafkaLogger", Level.INFO).add(builder.newAppenderRef("KafkaAppender")));

        return Configurator.initialize(builder.build());
    }
}
