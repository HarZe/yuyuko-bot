package org.harze.yuyukobot.database.service;

import org.harze.yuyukobot.database.entities.DiscordUser;
import org.harze.yuyukobot.database.entities.StoreRequest;

import java.util.List;

public interface StoreRequestService {

    StoreRequest create(StoreRequest storeRequest);

    StoreRequest edit(StoreRequest storeRequest);

    void delete(StoreRequest storeRequest);

    List<StoreRequest> find(DiscordUser discordUser);

    StoreRequest find(DiscordUser discordUser, String requestMessageId);

    List<StoreRequest> findAll();

}
