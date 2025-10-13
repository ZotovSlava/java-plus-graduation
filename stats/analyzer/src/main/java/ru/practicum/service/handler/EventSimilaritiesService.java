package ru.practicum.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.EventSimilarities;
import ru.practicum.repository.EventSimilaritiesRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilaritiesService {
    private final EventSimilaritiesRepository eventSimilaritiesRepository;

    public void handleRecord(EventSimilarityAvro record) {
        EventSimilarities eventSimilarities = eventSimilaritiesRepository
                .findByEventAAndEventB(record.getEventA(), record.getEventB());

        if (eventSimilarities == null) {
            eventSimilarities = new EventSimilarities();
            eventSimilarities.setEventA(record.getEventA());
            eventSimilarities.setEventB(record.getEventB());
        }

        eventSimilarities.setSimilarity(record.getSimilarity());
        eventSimilarities.setTime(record.getTimestamp());

        eventSimilaritiesRepository.save(eventSimilarities);
    }
}
