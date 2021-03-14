package org.harze.yuyukobot.database.service.impl;

import org.harze.yuyukobot.database.entities.DiscordUser;
import org.harze.yuyukobot.database.entities.StoreRequest;
import org.harze.yuyukobot.database.repositories.StoreRequestRepository;
import org.harze.yuyukobot.database.service.StoreRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreRequestServiceImpl implements StoreRequestService {

    @Autowired
    private StoreRequestRepository repository;

    @Override
    public StoreRequest create(StoreRequest storeRequest) {
        return repository.save(storeRequest);
    }

    @Override
    public StoreRequest edit(StoreRequest storeRequest) {
        return repository.save(storeRequest);
    }

    @Override
    public void delete(StoreRequest storeRequest) {
        repository.delete(storeRequest);
    }

    @Override
    public List<StoreRequest> find(DiscordUser discordUser) {
        return repository.findByDiscordUser(discordUser);
    }

    @Override
    public StoreRequest find(DiscordUser discordUser, String requestMessageId) {
        return repository.findByDiscordUserAndRequestMessageId(discordUser, requestMessageId);
    }

    @Override
    public List<StoreRequest> findAll() {
        return repository.findAll();
    }
}
