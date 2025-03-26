package com.streaming.twittertokafkaservice;

import com.streaming.twittertokafkaservice.config.TwitterToKafkaConfig;
import com.streaming.twittertokafkaservice.runner.StreamRunner;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
@AllArgsConstructor
@Slf4j
public class TwitterToKafkaServiceApplication implements CommandLineRunner {

    private final TwitterToKafkaConfig twitterToKafkaConfig;
    private StreamRunner streamRunner;

    public static void main(String[] args) {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Posts to Kafka Service Started");
        log.info(Arrays.toString(twitterToKafkaConfig.getTwitterKeywords().toArray(new String[]{})));
        log.info(twitterToKafkaConfig.getWelcomeMessage());
        streamRunner.start();
    }
}
