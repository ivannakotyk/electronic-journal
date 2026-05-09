package com.kpi.ejournal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "students")
public class Student extends User {
    private String studentCardNumber;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private GroupEntity group;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Grade> grades;
}