package pl.nordbud.nordbud_api.security;

import io.github.bucket4j.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k ->
                Bucket.builder()
                        // limit "burst": 5 żądań na minutę
                        .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                        // dodatkowy "długoterminowy": 50 / godzinę
                        .addLimit(Bandwidth.classic(50, Refill.intervally(50, Duration.ofHours(1))))
                        .build()
        );
    }
}