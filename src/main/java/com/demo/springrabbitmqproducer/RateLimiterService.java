package com.demo.springrabbitmqproducer;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    @Bean
    public Bucket createNewBucket() {
        //Bucket capacity of 5 tokens and refill of a similar number
        long capacity = 5;  //TODO:- Read this from config file
        Refill refill = Refill.greedy(5, Duration.ofMinutes(1));    //TODO:- READ THIS FROM CONFIG FILE
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket4j.builder().addLimit(limit).build();
    }

    @Autowired
    RedisTemplate<String, Object> template;
    /**
     * This method is to return the current number
     * of api calls made by this msisdn from cache
     * @param msisdn - msisdn
     * @return String - number of calls made by this user
     */
    @Cacheable(cacheNames = "apiHitCount", key ="{#msisdn}")
    public String getApiHitCount(Integer msisdn) {
        return "0";
    }
    /**
     * This method is to increment the number of api
     * calls made by this msisdn in cache
     * @param msisdn - msisdn
     * @return void
     */
    public void incrementApiHitCount(Integer msisdn) {
        template.
                opsForValue().
                increment("apiHitCount" + "::" + msisdn);
    }


}
