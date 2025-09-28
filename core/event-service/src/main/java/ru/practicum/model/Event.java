package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import ru.practicum.dto.event.EventState;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "annotation", nullable = false, length = 2000)
    private String annotation;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "confirmed_requests")
    private Integer confirmedRequests;

    @Column(name = "created")
    private LocalDateTime createdOn;

    @Column(name = "description", length = 7000)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "initiator_id")
    private Long initiatorId;

    @Column(name = "location_lat")
    public Float lat;

    @Column(name = "location_lon")
    public Float lon;

    @Column(name = "paid", nullable = false)
    @ColumnDefault("false")
    private Boolean paid;

    @Column(name = "participant_limit")
    @ColumnDefault("0")
    private Integer participantLimit;

    @Column(name = "published")
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    @ColumnDefault("true")
    private Boolean requestModeration = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private EventState state;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "rating")
    private Double rating;

    @PrePersist
    public void prePersist() {
        if (paid == null) {
            paid = false;
        }
        if (requestModeration == null) {
            requestModeration = true;
        }
        if (participantLimit == null) {
            participantLimit = 0;
        }
    }
}
