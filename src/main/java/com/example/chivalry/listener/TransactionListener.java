package com.example.chivalry.listener;

import com.example.chivalry.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class TransactionListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionListener.class);
    private static final String TRANSACTION_TOPIC = "transactions";
    private static final String DESERIALIZATION_EXCEPTION_HEADER_NAME = "springDeserializerException";


    @KafkaListener(topics = TRANSACTION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void listen(
            @Payload(required = false) Transaction transaction,

            @Header(value = DESERIALIZATION_EXCEPTION_HEADER_NAME, required = false) byte[] deserializationExceptionHeader,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.OFFSET) long offset) {

        if (transaction != null) {
            log.info("✅ SUCCESSFUL TRANSACTION [Key: {}, Offset: {}]: {}",
                    key, offset, transaction);
        } else if (deserializationExceptionHeader != null) {

            String exceptionMessage = new String(deserializationExceptionHeader, StandardCharsets.UTF_8);

            log.error("DESERIALIZATION ERROR [Key: {}, Offset: {}]. Moving to next record. Exception Details: {}",
                    key, offset, exceptionMessage.substring(0, Math.min(exceptionMessage.length(), 200)) + "...");
        } else {
            log.warn("UNEXPECTED NULL PAYLOAD [Key: {}, Offset: {}]. Skipping record.", key, offset);
        }
    }
}