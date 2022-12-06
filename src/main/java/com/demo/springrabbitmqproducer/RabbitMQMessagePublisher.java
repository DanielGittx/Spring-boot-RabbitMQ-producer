package com.demo.springrabbitmqproducer;

import io.github.bucket4j.Bucket;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@RestController
public class RabbitMQMessagePublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    RateLimiterService rateLimitedService;

    private int apiCountLimit = 4;    //Process 5 request per msisdn within 1 minute - 0-4

    //BUCKETING ALGORITHM

    @PostMapping("/publish")         ///BUCKET 4J UNDER UNDER APACHE 2.0 LICENSE
    public ResponseEntity publishMessage(@RequestBody Message message,
                                         HttpServletRequest httpRequest) throws Exception {

        HttpSession session = httpRequest.getSession(true);
        String appKey = String.valueOf(message.getMsisdn());      //MSISDN is used to identify incoming requests; OTHER parameter can be used - IPs etc
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

            return new ResponseEntity("We have published message on the broker - ", HttpStatus.OK);

        } else

            return new ResponseEntity("REQUESTS EXCEEDED TOKENS AVAILABLE IN THE BUCKET", HttpStatus.TOO_MANY_REQUESTS);
    }

    // FIXED WINDOW COUNTER ALGORITHM

    @PostMapping("/publish2")
    public ResponseEntity publishMessage2(@RequestBody Message message) throws Exception {
        if (null != message && null != message.getMsisdn()) {
            // if the number of calls exceed the configured value,
            // then we throw Too many calls exception.
            //System.out.println("COUNT - " +Integer.parseInt(rateLimitedService.getApiHitCount(message.getMsisdn())));
            if (Integer.parseInt(rateLimitedService.getApiHitCount(message.getMsisdn())) > apiCountLimit) {
                // Log msisdn that has violated throttling rules
                System.out.println("REQUESTS EXCEEDED THRESHOLD SET FOR THE WINDOW");

                // send 429 http status code for too many requests
                return new ResponseEntity("REQUESTS EXCEEDED THRESHOLD SET FOR THE WINDOW - "
                        +Integer.parseInt(rateLimitedService.getApiHitCount(message.getMsisdn())), HttpStatus.TOO_MANY_REQUESTS);
            }

            //PUBLISH TO QUEUE
            message.setMessageId(UUID.randomUUID().toString());
            message.setMessageTimestamp(new Date().toString());
            rabbitTemplate.convertAndSend(RabbitMQConfiguration.EXCHANGE, RabbitMQConfiguration.ROUTING_KEY, message);

            // incrementing the count (Number of requests within set window)
            rateLimitedService.incrementApiHitCount(message.getMsisdn());

            return new ResponseEntity("We have published message on the broker - "
                    +Integer.parseInt(rateLimitedService.getApiHitCount(message.getMsisdn())), HttpStatus.OK);
        } else {
            return new ResponseEntity("Request came empty", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping ("/upload")         ///BUCKET 4J UNDER UNDER APACHE 2.0 LICENSE
    public String publishMessage() throws Exception {
        // upload a file to SharePoint site
        UploadToSharePoint uploadToSharePoint = new UploadToSharePoint();
        //uploadToSharePoint.createFolder("dan new folder");
        uploadToSharePoint.uploadFile( "C:\\Users\\dmgitau\\Downloads\\details.zip");   //details.zip

        return "uploaded successfully";
    }


}