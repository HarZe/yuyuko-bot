package org.harze.yuyukobot.processor;

import discord4j.core.event.domain.message.ReactionAddEvent;
import org.harze.yuyukobot.database.entities.DiscordUser;

public interface ReactionAddedProcessor {

    String generateResponse(ReactionAddEvent event, DiscordUser author);

}
