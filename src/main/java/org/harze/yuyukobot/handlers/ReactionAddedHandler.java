package org.harze.yuyukobot.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import org.harze.yuyukobot.processor.CombinedReactionAddedProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReactionAddedHandler {

    private static final Logger log = LoggerFactory.getLogger(ReactionAddedHandler.class);

    private static final String thumbsUp = "\uD83D\uDC4D";

    @Autowired
    private CombinedReactionAddedProcessor combinedProcessor;

    public void handle(ReactionAddEvent event) {
        for (String response : combinedProcessor.generateResponses(event))
            event.getChannel()
                    .flatMap(msgChannel -> referencedResponse(event.getMessageId(), msgChannel, response))
                    .subscribe();
    }

    private Mono<Message> referencedResponse(Snowflake msgId, MessageChannel msgChannel, String content) {
        return msgChannel.createMessage(spec -> {
            spec.setContent(content);
            spec.setMessageReference(msgId);
        });
    }
}
