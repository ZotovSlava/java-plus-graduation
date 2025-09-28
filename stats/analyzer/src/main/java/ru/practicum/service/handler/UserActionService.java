package ru.practicum.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserActionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionService {

    private final UserActionRepository userActionRepository;

    public void handleRecord(UserActionAvro record) {
        double newWeight = switch (record.getType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };

        UserAction userAction = userActionRepository.findByUserIdAndEventId(
                record.getUserId(), record.getEventId()
        );

        if (userAction == null) {
            userAction = new UserAction();
            userAction.setUserId(record.getUserId());
            userAction.setEventId(record.getEventId());
            userAction.setActionTime(record.getTimestamp());
            userAction.setWeight(newWeight);

            userActionRepository.save(userAction);
            log.info("Создана новая запись: userId={} eventId={} weight={}",
                    record.getUserId(), record.getEventId(), newWeight);

        } else if (userAction.getWeight() < newWeight) {
            userAction.setWeight(newWeight);
            userAction.setActionTime(record.getTimestamp());

            userActionRepository.save(userAction);
            log.info("Обновлён maxWeight: userId={} eventId={} weight={}",
                    record.getUserId(), record.getEventId(), newWeight);

        } else {
            log.info("Обновление не требуется: userId={} eventId={} текущий вес={}, новый вес={}",
                    record.getUserId(), record.getEventId(), userAction.getWeight(), newWeight);
        }
    }
}
