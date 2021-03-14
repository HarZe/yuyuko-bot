package org.harze.yuyukobot.database.service.impl;

import org.harze.yuyukobot.database.entities.DiscordUser;
import org.harze.yuyukobot.database.repositories.DiscordUserRepository;
import org.harze.yuyukobot.database.service.DiscordUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscordUserServiceImpl implements DiscordUserService {

    @Autowired
    private DiscordUserRepository repository;

    @Override
    public DiscordUser create(DiscordUser discordUser) {
        return repository.save(discordUser);
    }

    @Override
    public DiscordUser edit(DiscordUser discordUser) {
        return repository.save(discordUser);
    }

    @Override
    public void delete(DiscordUser discordUser) {
        repository.delete(discordUser);
    }

    @Override
    public DiscordUser find(String discordId) {
        return repository.findByDiscordId(discordId);
    }

    @Override
    public List<DiscordUser> findAll() {
        return repository.findAll();
    }
}
