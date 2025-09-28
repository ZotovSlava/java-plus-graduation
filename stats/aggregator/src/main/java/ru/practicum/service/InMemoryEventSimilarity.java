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

    private final Map<Long, Map<Long, Double>> weightMatrix = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();
    private final Map<Long, Double> weightSumForEvent = new HashMap<>();

    public List<EventSimilarityAvro> calculate(UserActionAvro userAction) {
        List<EventSimilarityAvro> result = new ArrayList<>();

        Long eventId = userAction.getEventId();
        Long userId = userAction.getUserId();
        Double rating = switch (userAction.getType()) {
            case LIKE -> 1.0;
            case REGISTER -> 0.8;
            case VIEW -> 0.4;
        };

        Map<Long, Double> userWeights = weightMatrix.computeIfAbsent(eventId, k -> new HashMap<>());
        Double oldWeight = userWeights.getOrDefault(userId, 0.0);

        if (rating > oldWeight) {
            userWeights.put(userId, rating);
            weightMatrix.put(eventId, userWeights);
            weightSumForEvent.put(eventId, calculateWeightSum(userWeights));

            result = recalcSimilarity(eventId, userId);
        }

        return result;
    }

    private Double calculateWeightSum(Map<Long, Double> userWeights) {
        double sum = 0.0;
        for (Double w : userWeights.values()) sum += w;
        return Math.sqrt(sum);
    }

    private List<EventSimilarityAvro> recalcSimilarity(Long eventId, Long baseUserId) {
        List<EventSimilarityAvro> result = new ArrayList<>();
        Map<Long, Double> baseWeights = weightMatrix.get(eventId);
        double baseSum = weightSumForEvent.getOrDefault(eventId, 0.0);

        for (Long curEventId : weightMatrix.keySet()) {
            if (curEventId.equals(eventId)) continue;

            Map<Long, Double> curWeights = weightMatrix.get(curEventId);
            double curSum = weightSumForEvent.getOrDefault(curEventId, 0.0);

            if (!curWeights.containsKey(baseUserId)) continue;

            double sumMin = 0.0;
            Set<Long> allUsers = new HashSet<>();
            allUsers.addAll(baseWeights.keySet());
            allUsers.addAll(curWeights.keySet());

            for (Long uid : allUsers) {
                sumMin += Math.min(baseWeights.getOrDefault(uid, 0.0),
                        curWeights.getOrDefault(uid, 0.0));
            }

            if (sumMin > 0) {
                double similarity = sumMin / (baseSum * curSum);
                minWeightsSums
                        .computeIfAbsent(Math.min(eventId, curEventId), k -> new HashMap<>())
                        .put(Math.max(eventId, curEventId), sumMin);

                EventSimilarityAvro msg = EventSimilarityAvro.newBuilder()
                        .setEventA(Math.min(eventId, curEventId))
                        .setEventB(Math.max(eventId, curEventId))
                        .setSimilarity(BigDecimal.valueOf(similarity).setScale(2, RoundingMode.HALF_UP).doubleValue())
                        .setTimestamp(Instant.now())
                        .build();

                result.add(msg);
            }
        }

        return result;
    }

    public void clearState() {
        weightMatrix.clear();
        minWeightsSums.clear();
        weightSumForEvent.clear();
    }
}