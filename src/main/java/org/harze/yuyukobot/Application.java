package org.harze.yuyukobot;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.discordjson.json.gateway.StatusUpdate;
import org.harze.yuyukobot.configuration.BotInfo;
import org.harze.yuyukobot.handlers.MessageCreatedHandler;
import org.harze.yuyukobot.handlers.ReactionAddedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private BotInfo botInfo;
    @Autowired
    private MessageCreatedHandler messageCreatedHandler;
    @Autowired
    private ReactionAddedHandler reactionAddedHandler;

    public static void main(String[] args) {
        log.info("Starting YuyukoBOT");
        SpringApplication.run(Application.class, args);
    }

    private String loadToken() {
        String token = null;
        try (BufferedReader reader = new BufferedReader(new FileReader("token"))) {
            token = reader.readLine();
        } catch (IOException e) {
            log.error("Token file not found");
            System.exit(1);
        }
        if (token == null || token.isBlank()) {
            log.error("Token is missing or blank");
            System.exit(1);
        }
        return token;
    }

    @Override
    public void run(String... args) {

        GatewayDiscordClient client = DiscordClientBuilder.create(loadToken()).build().login().block();
        if (client == null) {
            log.error("Client could not be started");
            System.exit(1);
        }

        //client.getEventDispatcher().on(ReadyEvent.class).subscribe(); // TODO readyEvent handling
        client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(messageCreatedHandler::handle);
        client.getEventDispatcher().on(ReactionAddEvent.class).subscribe(reactionAddedHandler::handle);

        client.onDisconnect().block();
    }
}
