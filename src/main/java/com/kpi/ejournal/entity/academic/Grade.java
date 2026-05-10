package com.kpi.ejournal.entity.academic;

import com.kpi.ejournal.entity.user.Student;
import com.kpi.ejournal.entity.user.Teacher;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "grades")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grade_value", nullable = false)
    private Double value;

    @Column(name = "grade_date", nullable = false)
    private LocalDate gradeDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "control_type", nullable = false)
    private ControlType controlType;

    private String comment;

    @ManyToOne(optional = false)
    private Student student;

    @ManyToOne(optional = false)
    private Teacher teacher;

    @ManyToOne(optional = false)
    private Discipline discipline;
}