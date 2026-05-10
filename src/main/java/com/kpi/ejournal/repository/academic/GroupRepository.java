package com.kpi.ejournal.repository.academic;

import com.kpi.ejournal.entity.academic.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<GroupEntity, Long>
{
    boolean existsByCode(String code);
}