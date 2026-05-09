package com.kpi.ejournal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.*;

@Getter
@Setter
@Entity
@Table(name = "schedule_items")
public class ScheduleItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ScheduleType scheduleType;

    @Column(nullable = true)
    private Integer dayOfWeek;

    @Column(nullable = true)
    private Integer weekNumber;

    @Column(nullable = true)
    private LocalTime time;

    @Column(nullable = false)
    private String room;

    @ManyToOne(optional = false)
    private GroupEntity group;

    @ManyToOne(optional = false)
    private Discipline discipline;

    @ManyToOne
    private Teacher teacher;
}