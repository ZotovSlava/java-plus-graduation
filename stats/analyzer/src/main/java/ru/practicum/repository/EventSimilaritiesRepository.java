package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.EventSimilarities;

import java.util.List;
import java.util.Set;

public interface EventSimilaritiesRepository extends JpaRepository<EventSimilarities, Long> {

    EventSimilarities findByEventAAndEventB(Long eventA, Long eventB);

    @Query("select e from EventSimilarities e where e.eventA in :eventIds or e.eventB in :eventIds")
    List<EventSimilarities> findByEventAInOrEventBIn(@Param("eventIds") Set<Long> eventIds);

    @Query("select e from EventSimilarities e where e.eventA = :eventId or e.eventB = :eventId")
    List<EventSimilarities> findByEventAOrEventB(@Param("eventId") Long eventId);
}
