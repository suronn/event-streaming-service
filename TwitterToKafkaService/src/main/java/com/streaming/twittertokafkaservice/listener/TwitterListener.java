package com.streaming.twittertokafkaservice.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import twitter4j.v1.Status;
import twitter4j.v1.StatusAdapter;

@Component
@Slf4j
public class TwitterListener extends StatusAdapter {

    @Override
    public void onStatus(Status status) {
        log.info("Received: {}-{}", status.getUser().getStatus(), status.getText());
    }
}
