package ru.practicum;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.recommendations.InteractionsCountRequestProto;
import ru.practicum.grpc.recommendations.RecommendedEventProto;
import ru.practicum.grpc.recommendations.SimilarEventsRequestProto;
import ru.practicum.grpc.recommendations.UserPredictionsRequestProto;
import ru.practicum.service.handler.RecommendationService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@GrpcService
public class RecommendationController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("GRPC message InteractionsCountRequestProto received");
            Set<Long> eventIds = new HashSet<>(request.getEventIdList());
            List<RecommendedEventProto> results = recommendationService.interactionCounts(eventIds);

            for (RecommendedEventProto result : results) {
                responseObserver.onNext(result);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getInteractionsCount", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("GRPC message UserPredictionsRequestProto received");
            List<RecommendedEventProto> results =
                    recommendationService.recommendEvents(request.getUserId(), request.getMaxResults());

            for (RecommendedEventProto result : results) {
                responseObserver.onNext(result);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getRecommendationsForUser", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("GRPC message SimilarEventsRequestProto received");
            List<RecommendedEventProto> results =
                    recommendationService.similarEvents(request.getUserId(), request.getEventId(), request.getMaxResults());

            for (RecommendedEventProto result : results) {
                responseObserver.onNext(result);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getSimilarEvents", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}