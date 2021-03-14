package org.harze.yuyukobot.database.service;

import org.harze.yuyukobot.database.entities.DiscordUser;

import java.util.List;

public interface DiscordUserService {

    DiscordUser create(DiscordUser discordUser);

    DiscordUser edit(DiscordUser discordUser);

    void delete(DiscordUser discordUser);

    DiscordUser find(String discordId);

    List<DiscordUser> findAll();

}
