package ru.practicum.client;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.recommendations.InteractionsCountRequestProto;
import ru.practicum.grpc.recommendations.RecommendedEventProto;
import ru.practicum.grpc.recommendations.SimilarEventsRequestProto;
import ru.practicum.grpc.recommendations.UserPredictionsRequestProto;


import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub clientStub;

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        return callGrpc(() -> clientStub.getRecommendationsForUser(request), "getRecommendationsForUser");
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        return callGrpc(() -> clientStub.getSimilarEvents(request), "getSimilarEvents");
    }

    public Stream<RecommendedEventProto> getInteractionsCount(Collection<Long> eventIds) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();
        return callGrpc(() -> clientStub.getInteractionsCount(request), "getInteractionsCount");
    }

    private Stream<RecommendedEventProto> callGrpc(Supplier<Iterator<RecommendedEventProto>> grpcCall, String methodName) {
        try {
            Iterator<RecommendedEventProto> iterator = grpcCall.get();
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
        } catch (StatusRuntimeException e) {
            log.error("Ошибка при вызове gRPC метода {}: {}", methodName, e.getStatus(), e);
            return Stream.empty();
        }
    }
}
