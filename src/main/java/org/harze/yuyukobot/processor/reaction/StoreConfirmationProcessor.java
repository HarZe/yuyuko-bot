package org.harze.yuyukobot.processor.reaction;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import org.harze.yuyukobot.Utils;
import org.harze.yuyukobot.database.entities.DiscordUser;
import org.harze.yuyukobot.database.entities.GeneralContent;
import org.harze.yuyukobot.database.entities.StoreRequest;
import org.harze.yuyukobot.database.service.GeneralContentService;
import org.harze.yuyukobot.database.service.StoreRequestService;
import org.harze.yuyukobot.processor.ReactionAddedProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

@Component
public class StoreConfirmationProcessor implements ReactionAddedProcessor {

    private static final String thumbsUp = "\uD83D\uDC4D";

    @Autowired
    private StoreRequestService storeRequestService;
    @Autowired
    private GeneralContentService generalContentService;

    private synchronized void createGeneralContent(StoreRequest storeRequest, DiscordUser author) {
        Timestamp now = Utils.now();
        GeneralContent generalContent = new GeneralContent();
        generalContent.setId(UUID.randomUUID());
        generalContent.setDiscordUser(author);
        generalContent.setContent(storeRequest.getContent());
        generalContent.setTags(storeRequest.getTags());
        generalContent.setReadableTabs(storeRequest.getReadableTabs());
        generalContent.setCreatedAt(now);
        generalContent.setUpdatedAt(now);
        generalContentService.create(generalContent);
    }

    @Override
    public String generateResponse(ReactionAddEvent event, DiscordUser author) {

        ReactionEmoji.Unicode emoji = event.getEmoji().asUnicodeEmoji().orElse(null);

        if (emoji == null || !emoji.getRaw().equals(thumbsUp))
            return null;

        String refMessageId = event.getMessage()
                .map(Message::getReferencedMessage)
                .map(refMsg -> refMsg.isPresent() ? refMsg.get().getId().asString() : "")
                .block();

        if (refMessageId == null || refMessageId.isBlank())
            return null;

        StoreRequest storeRequest = storeRequestService.find(author, refMessageId);
        if (storeRequest == null)
            return null;

        createGeneralContent(storeRequest, author);
        storeRequestService.delete(storeRequest);

        return ":white_check_mark: Stored!";
    }
}
