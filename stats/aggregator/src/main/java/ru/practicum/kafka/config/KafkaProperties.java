package ru.practicum.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.serializer.EventSimilaritySerializer;

import java.util.Properties;

@Component
public class KafkaProperties {
    public Properties producerProperties() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.LongSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EventSimilaritySerializer.class);
        return config;
    }

    public Properties consumerProperties() {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.LongDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ru.practicum.kafka.deserializer.UserActionDeserializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-client-sensor");
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 500);
        return config;
    }
}
