package org.harze.yuyukobot.database.repositories;

import org.harze.yuyukobot.database.entities.DiscordUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscordUserRepository extends JpaRepository<DiscordUser, String> {

    DiscordUser findByDiscordId(String discordId);

}
