package org.harze.yuyukobot.database.repositories;

import org.harze.yuyukobot.database.entities.DiscordUser;
import org.harze.yuyukobot.database.entities.StoreRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StoreRequestRepository extends JpaRepository<StoreRequest, UUID> {

    List<StoreRequest> findByDiscordUser(DiscordUser discordUser);

    StoreRequest findByDiscordUserAndRequestMessageId(DiscordUser discordUser, String requestMessageId);

}
