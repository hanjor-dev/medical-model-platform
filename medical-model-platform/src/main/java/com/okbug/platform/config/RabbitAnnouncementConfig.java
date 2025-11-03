package com.okbug.platform.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置：公告到点推送（TTL + DLX）
 */
@Configuration
public class RabbitAnnouncementConfig {

    public static final String EXCHANGE_DELAY = "announcement.delay.exchange";
    public static final String EXCHANGE_READY = "announcement.ready.exchange";
    public static final String QUEUE_DELAY = "announcement.delay.queue";
    public static final String QUEUE_READY = "announcement.ready.queue";
    public static final String RK_DELAY = "announcement.delay";
    public static final String RK_READY = "announcement.publish";

    @Bean
    public DirectExchange announcementDelayExchange() {
        return new DirectExchange(EXCHANGE_DELAY, true, false);
    }

    @Bean
    public DirectExchange announcementReadyExchange() {
        return new DirectExchange(EXCHANGE_READY, true, false);
    }

    @Bean
    public Queue announcementDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EXCHANGE_READY);
        args.put("x-dead-letter-routing-key", RK_READY);
        return new Queue(QUEUE_DELAY, true, false, false, args);
    }

    @Bean
    public Queue announcementReadyQueue() {
        return new Queue(QUEUE_READY, true);
    }

    @Bean
    public Binding bindAnnouncementDelay() {
        return BindingBuilder.bind(announcementDelayQueue()).to(announcementDelayExchange()).with(RK_DELAY);
    }

    @Bean
    public Binding bindAnnouncementReady() {
        return BindingBuilder.bind(announcementReadyQueue()).to(announcementReadyExchange()).with(RK_READY);
    }
}


