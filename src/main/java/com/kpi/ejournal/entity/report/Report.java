package com.kpi.ejournal.entity.report;

import com.kpi.ejournal.entity.academic.Discipline;
import com.kpi.ejournal.entity.academic.GroupEntity;
import com.kpi.ejournal.entity.user.Methodologist;
import com.kpi.ejournal.entity.user.Teacher;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reports")
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String period;

    @Column(nullable = false)
    private Double averageScore;

    @ManyToOne
    private GroupEntity group;

    @ManyToOne
    private Discipline discipline;

    @ManyToOne
    private Teacher teacher;

    @ManyToOne
    private Methodologist methodologist;
}
