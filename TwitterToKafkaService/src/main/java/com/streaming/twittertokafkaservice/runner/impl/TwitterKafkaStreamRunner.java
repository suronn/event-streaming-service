package com.streaming.twittertokafkaservice.runner.impl;

import com.streaming.appconfigdata.TwitterToKafkaConfig;
import com.streaming.twittertokafkaservice.listener.TwitterListener;
import com.streaming.twittertokafkaservice.runner.StreamRunner;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import twitter4j.Twitter;
import twitter4j.v1.FilterQuery;
import twitter4j.v1.TwitterStream;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name="twitter-to-kafka-service.enable-mock-tweets", havingValue = "false", matchIfMissing = true)
public class TwitterKafkaStreamRunner implements StreamRunner {

    private final TwitterToKafkaConfig twitterToKafkaConfig;
    private final TwitterListener twitterListener;
    private TwitterStream twitterStream;

    @Override
    public void start() {
        twitterStream = Twitter.newBuilder()
                .listener(twitterListener)
                .build().v1().stream();
        addFilter();
    }

    @PreDestroy
    public void stop() {
        if (twitterStream != null) {
            log.info("Closing Twitter Stream");
            twitterStream.shutdown();
        }
    }

    private void addFilter() {
        String[] keywords = twitterToKafkaConfig.getTwitterKeywords().toArray(new String[0]);
        FilterQuery filterQuery = FilterQuery.ofTrack(keywords);
        twitterStream.filter(filterQuery);
        log.info("Started Twitter Stream for the keywords: {}", Arrays.toString(keywords));
    }
}
