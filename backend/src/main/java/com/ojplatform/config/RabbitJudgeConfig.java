package com.ojplatform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 判题消息队列配置类。
 */
@Configuration
@EnableRabbit
public class RabbitJudgeConfig {

    @Bean
    public DirectExchange judgeExchange(JudgeQueueProperties properties) {
        return new DirectExchange(properties.getExchange(), true, false);
    }

    @Bean
    public Queue contestSubmitQueue(JudgeQueueProperties properties) {
        return QueueBuilder.durable(properties.getContestSubmitQueue())
                .deadLetterExchange(properties.getExchange())
                .deadLetterRoutingKey(properties.getContestSubmitDlRoutingKey())
                .build();
    }

    @Bean
    public Binding contestSubmitBinding(@Qualifier("contestSubmitQueue") Queue contestSubmitQueue,
                                        DirectExchange judgeExchange,
                                        JudgeQueueProperties properties) {
        return BindingBuilder.bind(contestSubmitQueue)
                .to(judgeExchange)
                .with(properties.getContestSubmitRoutingKey());
    }

    @Bean
    public Queue contestSubmitDlq(JudgeQueueProperties properties) {
        return QueueBuilder.durable(properties.getContestSubmitDlq()).build();
    }

    @Bean
    public Binding contestSubmitDlBinding(@Qualifier("contestSubmitDlq") Queue contestSubmitDlq,
                                          DirectExchange judgeExchange,
                                          JudgeQueueProperties properties) {
        return BindingBuilder.bind(contestSubmitDlq)
                .to(judgeExchange)
                .with(properties.getContestSubmitDlRoutingKey());
    }

    @Bean
    public Queue contestPollQueue(JudgeQueueProperties properties) {
        return QueueBuilder.durable(properties.getContestPollQueue())
                .deadLetterExchange(properties.getExchange())
                .deadLetterRoutingKey(properties.getContestPollDlRoutingKey())
                .build();
    }

    @Bean
    public Binding contestPollBinding(@Qualifier("contestPollQueue") Queue contestPollQueue,
                                      DirectExchange judgeExchange,
                                      JudgeQueueProperties properties) {
        return BindingBuilder.bind(contestPollQueue)
                .to(judgeExchange)
                .with(properties.getContestPollRoutingKey());
    }

    @Bean
    public Queue contestPollDlq(JudgeQueueProperties properties) {
        return QueueBuilder.durable(properties.getContestPollDlq()).build();
    }

    @Bean
    public Binding contestPollDlBinding(@Qualifier("contestPollDlq") Queue contestPollDlq,
                                        DirectExchange judgeExchange,
                                        JudgeQueueProperties properties) {
        return BindingBuilder.bind(contestPollDlq)
                .to(judgeExchange)
                .with(properties.getContestPollDlRoutingKey());
    }

    @Bean
    public Queue contestPollDelayQueue(JudgeQueueProperties properties) {
        return QueueBuilder.durable(properties.getContestPollDelayQueue())
                .deadLetterExchange(properties.getExchange())
                .deadLetterRoutingKey(properties.getContestPollRoutingKey())
                .build();
    }

    @Bean
    public Binding contestPollDelayBinding(@Qualifier("contestPollDelayQueue") Queue contestPollDelayQueue,
                                           DirectExchange judgeExchange,
                                           JudgeQueueProperties properties) {
        return BindingBuilder.bind(contestPollDelayQueue)
                .to(judgeExchange)
                .with(properties.getContestPollDelayRoutingKey());
    }

    @Bean
    public MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
