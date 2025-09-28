package ru.practicum.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.grpc.recommendations.RecommendedEventProto;
import ru.practicum.model.EventSimilarities;
import ru.practicum.model.UserAction;
import ru.practicum.repository.EventSimilaritiesRepository;
import ru.practicum.repository.UserActionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserActionRepository userActionRepository;
    private final EventSimilaritiesRepository eventSimilaritiesRepository;

    private static final int RECENT_K = 10;

    public List<RecommendedEventProto> recommendEvents(Long userId, int maxResults) {
        List<UserAction> userActions = userActionRepository.findByUserId(userId);
        if (userActions.isEmpty()) return List.of();

        Set<Long> allUserEvents = userActions.stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        Set<Long> recentEvents = userActions.stream()
                .sorted(Comparator.comparing(UserAction::getActionTime).reversed())
                .limit(RECENT_K)
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        if (recentEvents.isEmpty()) return List.of();

        List<EventSimilarities> sims = eventSimilaritiesRepository
                .findByEventAInOrEventBIn(recentEvents);

        Map<Long, Double> candidateScores = new LinkedHashMap<>();
        for (EventSimilarities sim : sims) {
            Long candidate = recentEvents.contains(sim.getEventA()) ? sim.getEventB() : sim.getEventA();
            if (!allUserEvents.contains(candidate)) {
                candidateScores.putIfAbsent(candidate, sim.getSimilarity());
            }
            if (candidateScores.size() >= maxResults) break;
        }

        return candidateScores.entrySet().stream()
                .map(e -> buildProto(e.getKey(), e.getValue()))
                .toList();
    }

    public List<RecommendedEventProto> similarEvents(Long userId, Long eventId, int maxResults) {
        Set<Long> userEvents = userActionRepository.findByUserId(userId).stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        return eventSimilaritiesRepository.findByEventAOrEventB(eventId).stream()
                .filter(s -> !userEvents.contains(s.getEventA()) || !userEvents.contains(s.getEventB()))
                .sorted(Comparator.comparing(EventSimilarities::getSimilarity).reversed())
                .limit(maxResults)
                .map(s -> buildProto(s.getEventA().equals(eventId) ? s.getEventB() : s.getEventA(),
                        s.getSimilarity()))
                .toList();
    }

    public List<RecommendedEventProto> interactionCounts(Set<Long> eventIds) {
        Map<Long, Double> scores = new HashMap<>();
        userActionRepository.findByEventIdIn(eventIds).forEach(ua ->
                scores.merge(ua.getEventId(), ua.getWeight(), Double::sum)
        );

        return eventIds.stream()
                .map(id -> buildProto(id, scores.getOrDefault(id, 0.0)))
                .sorted(Comparator.comparingDouble(RecommendedEventProto::getScore).reversed())
                .toList();
    }

    private RecommendedEventProto buildProto(Long eventId, Double score) {
        double rounded = BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return RecommendedEventProto.newBuilder()
                .setEventId(eventId)
                .setScore(rounded)
                .build();
    }
}
