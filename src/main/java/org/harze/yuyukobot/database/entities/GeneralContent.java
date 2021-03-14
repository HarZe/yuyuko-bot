package org.harze.yuyukobot.database.entities;

import com.vladmihalcea.hibernate.type.search.PostgreSQLTSVectorType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "general_content")
@TypeDef(name = "tsvector", typeClass = PostgreSQLTSVectorType.class)
public class GeneralContent {

    @Id
    @Column(nullable = false)
    @Type(type = "pg-uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "discord_user_id", nullable = false)
    private DiscordUser discordUser;

    @Column(nullable = false)
    private String content;

    @Type(type = "tsvector")
    @Column(columnDefinition = "tsvector")
    private String tags;

    @Type(type = "tsvector")
    @Column(name = "readable_tags", columnDefinition = "tsvector")
    private String readableTags;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

}
