package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class InMemoryEventSimilarity {

    private final Map<Long, Map<Long, Double>> userActionWeightInEvent = new HashMap<>(); // event -> (user -> weight)
    private final Map<Long, Map<Long, Double>> minWeightsSum = new HashMap<>(); // eventA -> (eventB -> minWeightsSum)
    private final Map<Long, Double> sumUsersActionsWeights = new HashMap<>(); // event -> sum(weights)

    private final List<EventSimilarityAvro> eventSimilarityAvroList = new ArrayList<>();

    public List<EventSimilarityAvro> updateState(UserActionAvro userActionAvro) {
        eventSimilarityAvroList.clear();

        Long eventId = userActionAvro.getEventId();
        Long userId = userActionAvro.getUserId();
        Instant time = userActionAvro.getTimestamp();
        Double newWeight = switch (userActionAvro.getType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };

        Map<Long, Double> userWeights = userActionWeightInEvent.computeIfAbsent(eventId, k -> new HashMap<>());
        Double oldWeight = userWeights.getOrDefault(userId, 0.0);

        if (newWeight > oldWeight) {
            userWeights.put(userId, newWeight);
            sumUsersActionsWeights.put(eventId, sumUsersActionsWeights.getOrDefault(eventId, 0.0) + (newWeight - oldWeight));

            recalcSimilarity(eventId, userId, newWeight, oldWeight, time);
        }

        return eventSimilarityAvroList;
    }

    private void recalcSimilarity(Long eventA, Long userId, Double newWeight, Double oldWeight, Instant time) {
        for (Long eventB : userActionWeightInEvent.keySet()) {
            if (eventA.equals(eventB)) continue;

            long first = Math.min(eventA, eventB);
            long second = Math.max(eventA, eventB);

            Map<Long, Double> minWeightsMap = minWeightsSum.computeIfAbsent(first, k -> new HashMap<>());
            Map<Long, Double> weightsA = userActionWeightInEvent.get(eventA);
            Map<Long, Double> weightsB = userActionWeightInEvent.get(eventB);

            Double prevMinSum = minWeightsMap.getOrDefault(second, -1.0);

            if (prevMinSum < 0) {
                Set<Long> allUsers = new HashSet<>();
                allUsers.addAll(weightsA.keySet());
                allUsers.addAll(weightsB.keySet());

                double sumMin = allUsers.stream()
                        .mapToDouble(uid -> Math.min(weightsA.getOrDefault(uid, 0.0), weightsB.getOrDefault(uid, 0.0)))
                        .sum();

                if (sumMin > 0) {
                    minWeightsMap.put(second, sumMin);
                    addEventSimilarity(first, second, sumMin, time);
                }
            } else {
                double diff = Math.min(newWeight, weightsB.getOrDefault(userId, 0.0))
                        - Math.min(oldWeight, weightsB.getOrDefault(userId, 0.0));

                if (diff != 0) {
                    double updatedMinSum = prevMinSum + diff;
                    if (updatedMinSum > 0) {
                        minWeightsMap.put(second, updatedMinSum);
                        addEventSimilarity(first, second, updatedMinSum, time);
                    } else {
                        minWeightsMap.remove(second);
                    }
                }
            }
        }
    }

    private void addEventSimilarity(Long firstEvent, Long secondEvent, Double sumMinWeights, Instant time) {
        double sum1 = Math.sqrt(sumUsersActionsWeights.getOrDefault(firstEvent, 0.0));
        double sum2 = Math.sqrt(sumUsersActionsWeights.getOrDefault(secondEvent, 0.0));

        if (sum1 == 0.0 || sum2 == 0.0) return;

        double similarity = sumMinWeights / (sum1 * sum2);

        if (similarity > 0) {
            EventSimilarityAvro msg = EventSimilarityAvro.newBuilder()
                    .setEventA(firstEvent.intValue())
                    .setEventB(secondEvent.intValue())
                    .setSimilarity(BigDecimal.valueOf(similarity).setScale(2, RoundingMode.HALF_UP).doubleValue())
                    .setTimestamp(time)
                    .build();
            eventSimilarityAvroList.add(msg);
        }
    }

    public void clearState() {
        userActionWeightInEvent.clear();
        minWeightsSum.clear();
        sumUsersActionsWeights.clear();
    }
}