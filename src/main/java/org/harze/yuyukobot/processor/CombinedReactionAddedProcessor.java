package org.harze.yuyukobot.processor;

import discord4j.core.event.domain.message.ReactionAddEvent;
import org.harze.yuyukobot.configuration.BotInfo;
import org.harze.yuyukobot.database.entities.DiscordUser;
import org.harze.yuyukobot.database.service.DiscordUserService;
import org.harze.yuyukobot.processor.reaction.StoreConfirmationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CombinedReactionAddedProcessor {

    private static final Logger log = LoggerFactory.getLogger(CombinedReactionAddedProcessor.class);

    @Autowired
    private BotInfo botInfo;
    @Autowired
    private DiscordUserService discordUserService;

    @Autowired
    private StoreConfirmationProcessor storeConfirmationProcessor;

    private DiscordUser findAuthor(ReactionAddEvent event) {
        return discordUserService.find(event.getUserId().asString());
    }

    public List<String> generateResponses(ReactionAddEvent event) {
        try {
            List<String> responses = new ArrayList<>();
            DiscordUser author = findAuthor(event);
            if (author == null)
                return List.of();
            responses.add(storeConfirmationProcessor.generateResponse(event, author));
            return responses.stream().filter(Objects::nonNull).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error, event: {}", event, e);
            return List.of();
        }
    }
}
