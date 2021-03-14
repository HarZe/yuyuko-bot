package org.harze.yuyukobot.processor.message;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import org.harze.yuyukobot.Utils;
import org.harze.yuyukobot.analyzer.AnalysisResult;
import org.harze.yuyukobot.analyzer.MessageAnalyzer;
import org.harze.yuyukobot.configuration.BotInfo;
import org.harze.yuyukobot.database.entities.DiscordUser;
import org.harze.yuyukobot.database.service.DiscordUserService;
import org.harze.yuyukobot.processor.MessageCreatedProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
public class WelcomeProcessor implements MessageCreatedProcessor {

    @Autowired
    private BotInfo botInfo;
    @Autowired
    private MessageAnalyzer messageAnalyzer;
    @Autowired
    private DiscordUserService discordUserService;

    private String welcomeResponse() {
        return "Ara Ara~ :face_with_hand_over_mouth: Another human that wants to entertain me :purple_heart: \n" +
                "I'm just bored in the Netherworld, so I'm just lurking the Overwold to amuse myself :eyes: \n" +
                "If you need to **store** or **find** something, let me know about it! :grin: ";
    }

    private synchronized void createDiscordUser(String discordId, String username) {
        Timestamp now = Utils.now();
        DiscordUser discordUser = new DiscordUser();
        discordUser.setDiscordId(discordId);
        discordUser.setUsername(username);
        discordUser.setAdmin(false);
        discordUser.setKarmaValue(1L);
        discordUser.setKarmaDate(now);
        discordUser.setCreatedAt(now);
        discordUser.setUpdatedAt(now);
        discordUserService.create(discordUser);
    }

    @Override
    public List<String> generateResponse(MessageCreateEvent event, DiscordUser author) {

        Message message = event.getMessage();
        AnalysisResult analysisResult = messageAnalyzer.analyze(message.getContent());

        // Not enough words, drop attempt
        if (analysisResult.getMeaning().isEmpty() || analysisResult.getMeaning().size() < 2)
            return List.of();

        // Not calling the bot, drop
        if (!botInfo.getBotName().equals(analysisResult.getMeaning().get(0)))
            return List.of();

        // If there is no author, drop
        User messageAuthor = event.getMessage().getAuthor().orElse(null);
        if (messageAuthor == null)
            return List.of();

        // If author is a registered user already, drop
        String discordId = messageAuthor.getId().asString();
        if (discordUserService.find(discordId) != null)
            return List.of();

        // Create user and return welcome message
        createDiscordUser(discordId, messageAuthor.getUsername());
        return List.of(welcomeResponse());
    }
}
