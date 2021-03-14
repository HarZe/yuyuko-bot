package org.harze.yuyukobot.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import org.harze.yuyukobot.processor.CombinedMessageCreatedProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MessageCreatedHandler {

    @Autowired
    private CombinedMessageCreatedProcessor combinedProcessor;

    public void handle(MessageCreateEvent event) {
        Message message = event.getMessage();
        for (String response : combinedProcessor.generateResponses(event))
            message.getChannel()
                    .flatMap(msgChannel -> referencedResponse(message.getId(), msgChannel, response))
                    .subscribe();
    }

    private Mono<Message> referencedResponse(Snowflake msgId, MessageChannel msgChannel, String content) {
        return msgChannel.createMessage(spec -> {
            spec.setContent(content);
            spec.setMessageReference(msgId);
        });
    }
}
