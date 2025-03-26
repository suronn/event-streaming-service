package com.streaming.twittertokafkaservice.runner;

import com.streaming.appconfigdata.TwitterToKafkaConfig;
import com.streaming.twittertokafkaservice.exception.TwitterToKafkaServiceException;
import com.streaming.twittertokafkaservice.listener.TwitterListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.v1.Status;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Slf4j
@AllArgsConstructor
@ConditionalOnProperty(name="twitter-to-kafka-service.enable-mock-tweets", havingValue = "true")
public class MockKafkaStreamRunner implements StreamRunner {

    private final TwitterToKafkaConfig twitterToKafkaConfig;
    private final TwitterListener twitterListener;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String[] WORDS = {
            "Lorem", "ipsum", "dolor", "sit", "amet,", "consectetur", "adipiscing", "elit.",
            "Sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore", "et", "dolore", "magna", "aliqua.",
            "Ut", "enim", "ad", "minim", "veniam,", "quis", "nostrud", "exercitation", "ullamco", "laboris",
            "nisi", "ut", "aliquip", "ex", "ea", "commodo", "consequat.",
            "Duis", "aute", "irure", "dolor", "in", "reprehenderit", "in", "voluptate", "velit", "esse", "cillum",
            "dolore", "eu", "fugiat", "nulla", "pariatur.",
            "Excepteur", "sint", "occaecat", "cupidatat", "non", "proident,", "sunt", "in", "culpa", "qui", "officia",
            "deserunt", "mollit", "anim", "id", "est", "laborum."
    };

    private static final String TWEET_AS_RAW_JSON = "{" +
            "\"createdAt\": \"{0}\"," +
            "\"id\": \"{1}\"," +
            "\"text\": \"{2}\"," +
            "\"user\": {\"id\":\"{3}\"}" +
            "}";

    private static final String TWITTER_STATUS_DATE_FORMAT = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";

    @Override
    public void start(){
        String[] keywords = twitterToKafkaConfig.getTwitterKeywords().toArray(new String[0]);
        int minTweetLength = twitterToKafkaConfig.getMockMinTweetLength();
        int maxTweetLength = twitterToKafkaConfig.getMockMaxTweetLength();
        long sleepMs = twitterToKafkaConfig.getMockSleepMs();

        log.info("Started Mock Twitter Stream for the keywords: {}", String.join(",", keywords));

        simulateTwitterStream(keywords, minTweetLength, maxTweetLength, sleepMs);
    }

    private void simulateTwitterStream(String[] keywords, int minTweetLength, int maxTweetLength, long sleepMs) {
        Executors.newSingleThreadExecutor().submit(() -> {
           try {
               while (true) {
                String formatterTweetAsRawJson = getFormatterTweet(keywords, minTweetLength, maxTweetLength);
                Status status = TwitterObjectFactory.createStatus(formatterTweetAsRawJson);
                twitterListener.onStatus(status);
                sleep(sleepMs);
            }
           } catch (TwitterException e) {
               log.error("Error during simulating twitter stream", e);
            }
        });
    }

    private void sleep(long sleepInMs) {
        try {
            Thread.sleep(sleepInMs);
        } catch (InterruptedException e) {
            log.error("Error during sleeping for {} ms while waiting for a new status to be created", sleepInMs, e);
            Thread.currentThread().interrupt();
            throw new TwitterToKafkaServiceException("Error during sleeping while waiting for a new status to be created!", e);
        }
    }

    private String getFormatterTweet(String[] keywords, int minTweetLength, int maxTweetLength) {
            String[] params = new String[] {
            ZonedDateTime.now().format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, Locale.ENGLISH)),
            String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)),
            getRandomTweetContent(keywords, minTweetLength, maxTweetLength),
            String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE))
        };
        return formatTweetAsJson(params);
    }

    private static String formatTweetAsJson(String[] params) {
        String tweet = TWEET_AS_RAW_JSON;
        for (int i = 0; i < params.length; i++) {
            tweet = tweet.replace("{" + i + "}", params[i]);
        }
        return tweet;
    }

    private String getRandomTweetContent(String[] keywords, int minTweetLength, int maxTweetLength) {

        StringBuilder tweetContent = new StringBuilder();
        int tweetlength  = RANDOM.nextInt(maxTweetLength - minTweetLength + 1) + minTweetLength;
        return constructRandomTweet(keywords, tweetlength, tweetContent);
    }

    private static String constructRandomTweet(String[] keywords, int tweetlength, StringBuilder tweetContent) {
        for (int i = 0; i < tweetlength; i++) {
            tweetContent.append(WORDS[RANDOM.nextInt(WORDS.length)]).append(" ");
            if(i== tweetlength /2){
                tweetContent.append(keywords[RANDOM.nextInt(keywords.length)]).append(" ");
            }
        }
        return tweetContent.toString().trim();
    }
}
