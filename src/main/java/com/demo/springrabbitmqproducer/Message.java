package com.demo.springrabbitmqproducer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class Message {

    private String messageId;
    private String customMessage;
    private String messageTimestamp;
}
