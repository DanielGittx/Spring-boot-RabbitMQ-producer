package com.demo.springrabbitmqproducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("application.properties")
public class CacheConfiguration {

     @Bean
    public RedisConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName("127.0.0.1"); //TODO:- Pick this from config file
        configuration.setPort(6379); //TODO:- Pick this from config file
            return new LettuceConnectionFactory(configuration);

    }
    @Bean
    public CacheManager cacheManager (RedisConnectionFactory connectionFactory)
    {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig();
        defaultCacheConfig.disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Set TTL for a particular cache key.
        // Set to 1 minute
        // After one minute cached key will be cleared
        cacheConfigurations.put("apiHitCount",
                defaultCacheConfig.entryTtl(Duration.ofMinutes(1)).   //TODO:- Pick this from config file
                        serializeValuesWith(RedisSerializationContext.
                                SerializationPair.fromSerializer(RedisSerializer.string()))
        );
        return RedisCacheManager.builder(connectionFactory).
                cacheDefaults(defaultCacheConfig).
                withInitialCacheConfigurations(cacheConfigurations).
                build();
    }
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory());
        template.setDefaultSerializer(new StringRedisSerializer());

        return template;
    }



}