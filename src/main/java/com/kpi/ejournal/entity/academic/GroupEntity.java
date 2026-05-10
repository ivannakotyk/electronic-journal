package com.kpi.ejournal.entity.academic;

import com.kpi.ejournal.entity.user.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "groups")
public class GroupEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Integer course;

    @Column(nullable = false)
    private String specialty;

    @OneToMany(mappedBy = "group")
    private List<Student> students = new ArrayList<>();
}
