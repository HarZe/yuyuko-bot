package org.harze.yuyukobot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BotInfo {

    @Value("${botInfo.name:yuyuko}")
    private String botName;

    public String getBotName() {
        return botName.trim().toLowerCase();
    }

}
