package org.harze.yuyukobot.database.service;

import org.harze.yuyukobot.database.entities.DiscordUser;
import org.harze.yuyukobot.database.entities.GeneralContent;

import java.util.List;

public interface GeneralContentService {

    GeneralContent create(GeneralContent generalContent);

    GeneralContent edit(GeneralContent generalContent);

    void delete(GeneralContent generalContent);

    List<GeneralContent> findByDiscordUser(DiscordUser discordUser);

    List<String> queryRankedContent(String tsQuery, Integer limit);

    List<GeneralContent> queryRanked(String tsQuery, Integer limit);

    List<GeneralContent> findAll();

}
