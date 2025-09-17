package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.model.Request;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByRequesterId(Long userId);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByEventIdAndIdIn(Long eventId, Set<Long> requestIds);

    int countByEventIdAndStatus(Long eventId, RequestStatus status);

    Optional<Request> findByEventIdAndRequesterId(Long eventId, Long requesterId);
}
