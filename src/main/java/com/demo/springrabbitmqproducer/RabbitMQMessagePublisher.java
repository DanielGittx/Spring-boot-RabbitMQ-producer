package com.demo.springrabbitmqproducer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@RestController
public class RabbitMQMessagePublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/publish")
    public String publishMessage (@RequestBody Message message)
    {
        message.setMessageId(UUID.randomUUID().toString());
        message.setMessageTimestamp(new Date().toString());
        rabbitTemplate.convertAndSend(RabbitMQConfiguration.EXCHANGE, RabbitMQConfiguration.ROUTING_KEY, message);

        return "We have published message on the broker";
    }
}
