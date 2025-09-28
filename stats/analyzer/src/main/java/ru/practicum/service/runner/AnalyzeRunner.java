package ru.practicum.service.runner;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.service.processor.EventSimilaritiesProcessor;
import ru.practicum.service.processor.UserActionProcessor;

@Component
@RequiredArgsConstructor
public class AnalyzeRunner implements CommandLineRunner {

    private final UserActionProcessor userActionProcessor;
    private final EventSimilaritiesProcessor eventSimilarityProcessor;

    @Override
    public void run(String... args) {
        Thread userActionThread = new Thread(userActionProcessor, "UserActionProcessorThread");
        userActionThread.start();

        Thread eventSimilaritiesThread = new Thread(eventSimilarityProcessor, "EventSimilaritiesProcessorThread");
        eventSimilaritiesThread.start();
    }
}
