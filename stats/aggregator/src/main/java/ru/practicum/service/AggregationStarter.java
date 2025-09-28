package ru.practicum.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.config.KafkaConfiguration;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaConfiguration configuration;
    private final InMemoryEventSimilarity memorySensorEvents;

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private Consumer<String, SpecificRecordBase> consumer;
    private Producer<String, SpecificRecordBase> producer;

    @PostConstruct
    public void init() {
        this.consumer = configuration.kafkaConsumer();
        this.producer = configuration.kafkaProducer();
    }

    public void start() {

        try {
            consumer.subscribe(List.of("stats.user-actions.v1"));

            int count = 0;
            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    log.info("Обновление снапшота");
                    List<EventSimilarityAvro> snapshots = memorySensorEvents.updateState((UserActionAvro) record.value());

                    if (!snapshots.isEmpty()) {
                        snapshots.forEach(snapshot -> {
                            log.info("Новый снапшот: {}", snapshot);
                            producer.send(new ProducerRecord<>("stats.events-similarity.v1", snapshot));
                            log.info("Снапшот отправлен в топик {}", "stats.events-similarity.v1");
                        });
                    } else {
                        log.info("Обновление снапшота не произошло.");
                    }

                    manageOffsets(record, count);
                    count++;
                }

                consumer.commitAsync();
            }

        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync(currentOffsets);
            } finally {
                close();
            }
        }
    }


    private void manageOffsets(ConsumerRecord<String, SpecificRecordBase> record, int count) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

    @PreDestroy
    public void close() {
        try {
            consumer.close();
        } catch (Exception e) {
            log.warn("Ошибка при закрытии consumer", e);
        }
        try {
            producer.close();
        } catch (Exception e) {
            log.warn("Ошибка при закрытии producer", e);
        }
    }
}


