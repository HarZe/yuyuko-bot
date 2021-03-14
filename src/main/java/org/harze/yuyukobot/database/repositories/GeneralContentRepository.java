package org.harze.yuyukobot.database.repositories;

import org.harze.yuyukobot.database.entities.DiscordUser;
import org.harze.yuyukobot.database.entities.GeneralContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface GeneralContentRepository extends JpaRepository<GeneralContent, UUID> {

    List<GeneralContent> findByDiscordUser(DiscordUser discordUser);

    @Query(nativeQuery = true, value = "SELECT \"content\" FROM ( " +
            "SELECT gc.\"content\", gc.updated_at, length(gc.tags) as ranking " +
            "FROM general_content gc " +
            "WHERE ?1\\:\\:tsquery @@ gc.tags " +
            "ORDER BY ranking, gc.updated_at ASC " +
            "LIMIT ?2 " +
            ") query")
    List<String> queryRankedContent(String tsQuery, Integer limit);

    @Query(nativeQuery = true, value = "SELECT id\\:\\:text FROM ( " +
            "SELECT gc.id, gc.updated_at, length(gc.tags) as ranking " +
            "FROM general_content gc " +
            "WHERE ?1\\:\\:tsquery @@ gc.tags " +
            "ORDER BY ranking, gc.updated_at ASC " +
            "LIMIT ?2 " +
            ") query")
    List<String> queryRanked(String tsQuery, Integer limit);
}
