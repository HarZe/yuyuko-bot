package org.harze.yuyukobot.processor;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import org.harze.yuyukobot.configuration.BotInfo;
import org.harze.yuyukobot.database.entities.DiscordUser;
import org.harze.yuyukobot.database.service.DiscordUserService;
import org.harze.yuyukobot.processor.message.SearchProcessor;
import org.harze.yuyukobot.processor.message.StoreProcessor;
import org.harze.yuyukobot.processor.message.WelcomeProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CombinedMessageCreatedProcessor {

    private static final Logger log = LoggerFactory.getLogger(CombinedMessageCreatedProcessor.class);

    @Autowired
    private BotInfo botInfo;
    @Autowired
    private DiscordUserService discordUserService;

    @Autowired
    private WelcomeProcessor welcomeProcessor;
    @Autowired
    private StoreProcessor storeProcessor;
    @Autowired
    private SearchProcessor searchProcessor;

    private DiscordUser findAuthor(MessageCreateEvent event) {
        Message message = event.getMessage();
        User author = message.getAuthor().orElse(null);
        if (message.getContent().isBlank() || author == null || author.isBot())
            return null;
        String discordId = author.getId().asString();
        return discordUserService.find(discordId);
    }

    private String errorMessage(MessageCreateEvent event) {
        try {
            String content = event.getMessage().getContent();
            if (content.trim().toLowerCase().startsWith(botInfo.getBotName()))
                return "Are~ :sweat_smile: \nMy mind went blank trying to understand that... \nごめんなさい :pray: ";
        } catch (Exception e) {
            log.error("Fatal error, event: {}", event, e);
        }
        return null;
    }

    public List<String> generateResponses(MessageCreateEvent event) {
        try {
            List<String> responses = new ArrayList<>();
            DiscordUser author = findAuthor(event);
            if (author == null) {
                responses.addAll(welcomeProcessor.generateResponse(event, null));
                author = findAuthor(event);
            }
            responses.addAll(storeProcessor.generateResponse(event, author));
            responses.addAll(searchProcessor.generateResponse(event, author));
            return responses.stream().filter(Objects::nonNull).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error, event: {}", event, e);
            String message = errorMessage(event);
            if (message == null)
                return List.of();
            return List.of(message);
        }
    }
}
