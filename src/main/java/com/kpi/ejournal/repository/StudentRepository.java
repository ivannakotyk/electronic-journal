package com.kpi.ejournal.repository;
import com.kpi.ejournal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface StudentRepository extends JpaRepository<Student, Long>
{
    List<Student> findByGroupId(Long groupId);
}
