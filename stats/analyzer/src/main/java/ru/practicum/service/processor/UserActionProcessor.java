package ru.practicum.service.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.handler.UserActionService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class UserActionProcessor implements Runnable {
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);
    private final UserActionService userActionService;
    private final KafkaConsumer<Long, SpecificRecordBase> consumer;

    public UserActionProcessor(UserActionService userActionService,
                               @Qualifier("UserActionConsumer") KafkaConsumer<Long, SpecificRecordBase> consumer) {
        this.userActionService = userActionService;
        this.consumer = consumer;
    }

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    @Override
    public void run() {
        consumer.subscribe(List.of("stats.user-actions.v1"));

        try {
            int count = 0;
            while (true) {
                ConsumerRecords<Long, SpecificRecordBase> records = consumer.poll(POLL_TIMEOUT);
                for (ConsumerRecord<Long, SpecificRecordBase> record : records) {
                    userActionService.handleRecord((UserActionAvro) record.value());
                    manageOffsets(record, count);
                    count++;
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Ошибка во время обработки HubEvent", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем HubEvent consumer");
                consumer.close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<Long, SpecificRecordBase> record, int count) {
        currentOffsets.put(new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1));

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, ex) -> {
                if (ex != null) {
                    log.warn("Ошибка фиксации оффсетов: {}", offsets, ex);
                }
            });
        }
    }
}
