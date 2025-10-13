package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {
    @Bean
    public KafkaConsumer<Long, SpecificRecordBase> UserActionConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "UserActionConsumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "user-action-hub");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "ru.practicum.kafka.deserializer.UserActionDeserializer");
        return new KafkaConsumer<>(props);
    }

    @Bean
    public KafkaConsumer<Long, SpecificRecordBase> EventSimilaritiesConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "EventSimilaritiesConsumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "event-similarities-hub");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "ru.practicum.kafka.deserializer.EventSimilaritiesDeserializer");
        return new KafkaConsumer<>(props);
    }
}
