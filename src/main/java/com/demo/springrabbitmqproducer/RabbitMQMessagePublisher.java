package com.demo.springrabbitmqproducer;

import io.github.bucket4j.Bucket;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.UUID;

@RestController
public class RabbitMQMessagePublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    RateLimiterService rateLimitedService;

    private int apiCountLimit = 0;    //Process 1 request per msisdn within 1 minute

    //BUCKETING ALGORITHM
    /*
    BUCKETING ALGORITHM
    You have a bucket that holds a maximum number of tokens (capacity).
    Whenever a consumer wants to call a service or consume a resource, he takes out one or multiple tokens.
    The consumer can only consume a service if he can take out the required number of tokens.
    If the bucket does not contain the required number of tokens, he needs to wait until there are enough tokens in the bucket.

     */

    @PostMapping("/publish")         ///BUCKET 4J UNDER UNDER APACHE 2.0 LICENSE
    public ResponseEntity publishMessage(@RequestBody Message message, HttpServletRequest httpRequest) throws Exception {
        HttpSession session = httpRequest.getSession(true);
        String appKey = String.valueOf(message.getMsisdn());
        Bucket bucket = (Bucket) session.getAttribute("apiHitCount::" + appKey);
        if (bucket == null) {
            bucket = rateLimitedService.createNewBucket();
            session.setAttribute("apiHitCount::" + appKey, bucket);
        }
        boolean still_have_tokens_in_the_bucket = bucket.tryConsume(1);  //TODO:- Read this from config

        if (still_have_tokens_in_the_bucket) {
            message.setMessageId(UUID.randomUUID().toString());
            message.setMessageTimestamp(new Date().toString());
            rabbitTemplate.convertAndSend(RabbitMQConfiguration.EXCHANGE, RabbitMQConfiguration.ROUTING_KEY, message);

            return new ResponseEntity("We have published message on the broker", HttpStatus.OK);

        } else

            return new ResponseEntity("You have exceeded the "+apiCountLimit+ " requests in 1 minute limit!", HttpStatus.TOO_MANY_REQUESTS);
    }

    // FIXED WINDOW COUNTER ALGORITHM
    /*

    Let’s understand how the Fixed window counter works.

        For each timeline we create counter and initialize it with zero.
        After each request we increment the counter by 1.
        Once the counter reaches the pre-defined threshold, we can start throwing exception and send 429 Http Status code to the client.
        Pros and Cons of using Fixed window counter:

        Pros
        Memory efficient. As we are just storing the count as value and user id as key
        Easy to understand.
        Resetting available quota at the end of an interval fits certain use case.
        Cons
        Spike in traffic at the edge of a window could cause more requests than the allowed limit to go through.

     */


    @PostMapping("/publish2")
    public ResponseEntity publishMessage2(@RequestBody Message message) throws Exception {
        if (null != message && null != message.getMsisdn()){
            // if the number of calls exceed the configured value,
            // then we throw Too many calls exception.
            //System.out.println("COUNT - " +Integer.parseInt(rateLimitedService.getApiHitCount(message.getMsisdn())));
            if (Integer.parseInt(rateLimitedService.getApiHitCount(message.getMsisdn()))> apiCountLimit)
            {
                // Log msisdn that has violated throttling rules
               System.out.println("CALLS EXCEEDED THRESHOLD FOR THE WINDOW");

                // send 429 http status code for too many requests
                return new ResponseEntity("You have exceeded the "+apiCountLimit+ " requests in 1 minute limit!", HttpStatus.TOO_MANY_REQUESTS);
            }

            //PUBLISH TO QUEUE
            message.setMessageId(UUID.randomUUID().toString());
            message.setMessageTimestamp(new Date().toString());
            rabbitTemplate.convertAndSend(RabbitMQConfiguration.EXCHANGE, RabbitMQConfiguration.ROUTING_KEY, message);

            // incrementing the count (Number of requests within set window)
            rateLimitedService.incrementApiHitCount(message.getMsisdn());

            return new ResponseEntity("We have published message on the broker", HttpStatus.OK);
        } else{
            return new ResponseEntity("Request came empty", HttpStatus.NOT_FOUND);
        }
    }


}