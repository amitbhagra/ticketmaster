package org.example.eventslist.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.eventslist.model.kafka.OutboxEvent;
import org.example.eventslist.service.EventIngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

    @Service
    @Slf4j
    public class EventMessageListener {

        @Autowired
        EventIngestionService eventIngestionService;

        @KafkaListener(topics = "jdbc-outbox_event", containerFactory = "kafkaListenerContainerFactory")
        public void listenWithHeaders(@Payload OutboxEvent message,
                                      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, @Header(KafkaHeaders.OFFSET) int offset) throws Exception {
            log.info("Received Message: {}, from partition: {}, from offset: {}", message, partition, offset);
            eventIngestionService.processEvent(message);
        }



}
