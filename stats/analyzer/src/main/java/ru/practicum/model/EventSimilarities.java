package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "event_similarities")
public class EventSimilarities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_A", nullable = false)
    private Long eventA;

    @Column(name = "event_B", nullable = false)
    private Long eventB;

    @Column(nullable = false)
    private Double similarity;

    @Column(nullable = false)
    private Instant time;

}
