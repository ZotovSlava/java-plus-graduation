package ru.practicum.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.dto.UserActionDto;
import ru.practicum.mapper.AvroMapper;

@Service
@AllArgsConstructor
@Slf4j
public class CollectorServicelmpl implements CollectorService {
    private final KafkaTemplate<Long, SpecificRecordBase> kafkaTemplate;

    @Override
    public void createUserAction(UserActionDto userActionDto) {
        SpecificRecordBase avroRecord = AvroMapper.toAvro(userActionDto);
        log.info("Sending sensor event to Kafka: key={}, value={}", userActionDto.getUserId(), avroRecord);
        kafkaTemplate.send("stats.user-actions.v1", userActionDto.getUserId(), avroRecord);
    }
}
