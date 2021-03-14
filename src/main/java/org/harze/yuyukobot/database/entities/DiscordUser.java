package org.harze.yuyukobot.database.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "discord_user")
public class DiscordUser {

    @Id
    @Column(name = "discord_id", nullable = false)
    private String discordId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Boolean admin;

    @Column(name = "karma_value", nullable = false)
    private Long karmaValue;

    @Column(name = "karma_date", nullable = false)
    private Timestamp karmaDate;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

}
