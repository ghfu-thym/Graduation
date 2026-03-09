package com.spike.ticket.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Slf4j
@Configuration
public class RedisConfig {
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        configureRedisNotification(connectionFactory);
        return container;
    }


    public void configureRedisNotification(RedisConnectionFactory connectionFactory){
        try{
            var connection = connectionFactory.getConnection();

            String config = connection.serverCommands().getConfig("notify-keyspace-events").getProperty("notify-keyspace-events");
            if (config == null || !config.contains("E")) {
                connection.serverCommands().setConfig("notify-keyspace-events", "Ex");
                log.info("Redis keyspace events enabled");

            }
            connection.close();
        } catch (Exception e){
            log.info("Redis keyspace events failed to enable"+e.getMessage());
        }
    }
}
