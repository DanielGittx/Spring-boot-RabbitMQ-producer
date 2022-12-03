package com.demo.springrabbitmqproducer;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    final static String QUEUE_NAME = "statements_message_queue";
    final static String EXCHANGE   = "statements_message_exchange";
    final static String ROUTING_KEY  = "statements_message_routing_key";

    @Bean
    public Queue queue ()
    {   //create queue and set Queue Name
        return new Queue(QUEUE_NAME);
    }
    @Bean
    public TopicExchange exchange()
    {
        return new TopicExchange(EXCHANGE);
    }
    @Bean
    public Binding binding (Queue queue, TopicExchange exchange)
    {
        return BindingBuilder
                .bind(queue).to(exchange)   //Topic binding
                .with(ROUTING_KEY);
    }
    @Bean
    public MessageConverter messageConverter ()
    {
        // convert to json
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public AmqpTemplate template (ConnectionFactory connectionFactory)
    {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

}
