package org.harze.yuyukobot.processor;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.harze.yuyukobot.database.entities.DiscordUser;

import java.util.List;

public interface MessageCreatedProcessor {

    List<String> generateResponse(MessageCreateEvent event, DiscordUser author);

}
