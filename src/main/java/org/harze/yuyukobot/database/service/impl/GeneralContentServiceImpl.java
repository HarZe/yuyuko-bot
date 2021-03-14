package org.harze.yuyukobot.database.service.impl;

import org.harze.yuyukobot.database.entities.DiscordUser;
import org.harze.yuyukobot.database.entities.GeneralContent;
import org.harze.yuyukobot.database.repositories.GeneralContentRepository;
import org.harze.yuyukobot.database.service.GeneralContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GeneralContentServiceImpl implements GeneralContentService {

    @Autowired
    private GeneralContentRepository repository;

    @Override
    public GeneralContent create(GeneralContent generalContent) {
        return repository.save(generalContent);
    }

    @Override
    public GeneralContent edit(GeneralContent generalContent) {
        return repository.save(generalContent);
    }

    @Override
    public void delete(GeneralContent generalContent) {
        repository.delete(generalContent);
    }

    @Override
    public List<GeneralContent> findByDiscordUser(DiscordUser discordUser) {
        return repository.findByDiscordUser(discordUser);
    }

    @Override
    public List<String> queryRankedContent(String tsQuery, Integer limit) {
        return repository.queryRankedContent(tsQuery, limit);
    }

    @Override
    public List<GeneralContent> queryRanked(String tsQuery, Integer limit) {
        return repository.queryRanked(tsQuery, limit).stream()
                .map(UUID::fromString)
                .map(id -> repository.findById(id).orElse(null))
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneralContent> findAll() {
        return repository.findAll();
    }
}
