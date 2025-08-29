package org.example.booking.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.booking.sm.events.PaymentStatusResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

	@Value(value = "${kafka.bootstrap.servers}")
	private String bootstrapAddress;

	@Bean
	public ConsumerFactory<String, PaymentStatusResponse> consumerFactory() {
		Map<String, Object> props = new HashMap<>();
		
		JsonDeserializer<PaymentStatusResponse> deserializer = new JsonDeserializer<>(PaymentStatusResponse.class);
	    deserializer.setRemoveTypeHeaders(false);
	    deserializer.addTrustedPackages("*");
	    deserializer.setUseTypeMapperForKey(true);
		
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "booking-service-group");
		return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
				deserializer);
	}

	@Bean
	public <T> ConcurrentKafkaListenerContainerFactory<String, PaymentStatusResponse> kafkaListenerContainerFactory() {

		ConcurrentKafkaListenerContainerFactory<String, PaymentStatusResponse> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setAutoStartup(true);
		factory.setConsumerFactory(consumerFactory());
		return factory;
	}
}
