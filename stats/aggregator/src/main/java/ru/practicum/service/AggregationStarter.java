package ru.practicum.service;

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

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationStarter {

    private final Consumer<String, SpecificRecordBase> consumer;
    private final Producer<String, SpecificRecordBase> producer;
    private final InMemoryEventSimilarity similarityService;

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public void start() {
        try {
            consumer.subscribe(List.of("stats.user-actions.v1"));
            int count = 0;

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    List<EventSimilarityAvro> snapshots =
                            similarityService.calculate((UserActionAvro) record.value());

                    for (EventSimilarityAvro snapshot : snapshots) {
                        producer.send(new ProducerRecord<>("stats.events-similarity.v1", snapshot));
                    }

                    manageOffsets(record, count);
                    count++;
                }

                consumer.commitAsync();
            }

        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Ошибка обработки сообщений", e);
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
                if (exception != null) log.warn("Ошибка фиксации оффсетов: {}", offsets, exception);
            });
        }
    }

    @PreDestroy
    private void close() {
        try {
            consumer.close();
        } catch (Exception ignored) {
        }
        try {
            producer.close();
        } catch (Exception ignored) {
        }
    }
}


